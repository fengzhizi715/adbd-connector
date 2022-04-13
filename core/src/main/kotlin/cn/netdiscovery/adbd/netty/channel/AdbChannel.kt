package cn.netdiscovery.adbd.netty.channel

import cn.netdiscovery.adbd.constant.WRITE_DATA_MAX
import cn.netdiscovery.adbd.domain.AdbChannelAddress
import cn.netdiscovery.adbd.domain.AdbPacket
import cn.netdiscovery.adbd.domain.PendingWriteEntry
import cn.netdiscovery.adbd.domain.enum.Command
import cn.netdiscovery.adbd.utils.logger
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.*
import io.netty.util.ReferenceCountUtil
import io.netty.util.ReferenceCounted
import io.netty.util.concurrent.Future
import io.netty.util.internal.StringUtil
import java.net.SocketAddress
import java.nio.channels.ClosedChannelException
import java.nio.channels.ConnectionPendingException
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.netty.channel.AdbChannel
 * @author: Tony Shen
 * @date: 2022/4/4 1:01 上午
 * @version: V1.0 <描述当前版本功能>
 */
class AdbChannel(parent: Channel, localId: Int, remoteId: Int) : AbstractChannel(parent), ChannelInboundHandler {

    private val logger = logger<AdbChannel>()

    private val eventLoop: EventLoop
    private val config: ChannelConfig
    private val metadata: ChannelMetadata
    private val pendingWriteEntries: Queue<PendingWriteEntry>

    @Volatile
    private var connectPromise: ChannelPromise? = null

    @Volatile
    private var connectTimeoutFuture: ScheduledFuture<*>? = null

    @Volatile
    private var localAddress: AdbChannelAddress? = null

    @Volatile
    private var remoteAddress: AdbChannelAddress? = null

    @Volatile
    private var localId: Int

    @Volatile
    private var remoteId: Int

    init {
        this.localId = localId
        this.remoteId = remoteId
        this.eventLoop = AdbChannelEventLoop(parent.eventLoop())
        this.metadata = ChannelMetadata(false)
        this.pendingWriteEntries = LinkedList()
        this.config = DefaultChannelConfig(this)
        config.allocator = parent.config().allocator
        config.connectTimeoutMillis = parent.config().connectTimeoutMillis
        config.isAutoClose = parent.config().isAutoClose
        config.isAutoRead = parent.config().isAutoRead
    }

    override fun newUnsafe(): AbstractUnsafe {
        return AdbUnsafe()
    }

    override fun isCompatible(loop: EventLoop): Boolean = loop is AdbChannelEventLoop

    override fun localAddress0(): SocketAddress? = localAddress

    override fun remoteAddress0(): SocketAddress? = remoteAddress

    @Throws(Exception::class)
    override fun doBind(localAddress: SocketAddress) {
        val address: AdbChannelAddress = localAddress as AdbChannelAddress
        this.localAddress = address
        localId = address.id
    }

    override fun connect(remoteAddress: SocketAddress?, promise: ChannelPromise): ChannelFuture {
        return super.connect(remoteAddress, promise)
    }

    @Throws(Exception::class)
    override fun doDisconnect() {
        doClose()
    }

    @Throws(Exception::class)
    override fun doClose() {
        val promise = connectPromise
        if (promise != null) {
            promise.tryFailure(ClosedChannelException())
            connectPromise = null
        }
        val future = connectTimeoutFuture
        if (future != null) {
            future.cancel(false)
            connectTimeoutFuture = null
        }
        if (isActive) {
            parent().writeAndFlush(AdbPacket(Command.A_CLSE, localId, remoteId))
        }
        if (parent().isOpen) {
            parent().pipeline().remove(this)
        }
        remoteId = 0
        localId = remoteId
        //将pending的写入全部失败
        while (true) {
            val entry: PendingWriteEntry = pendingWriteEntries.poll() ?: break
            entry.promise.tryFailure(ClosedChannelException())
            if (entry.msg is ReferenceCounted && entry.msg.refCnt() > 0) {
                ReferenceCountUtil.safeRelease(entry.msg)
            }
        }
    }

    @Throws(Exception::class)
    override fun doBeginRead() {
    }

    override fun write(msg: Any, promise: ChannelPromise): ChannelFuture {
        return if (!isActive) {
            //如果不是活跃的，则放到队列去
            if (!pendingWriteEntries.offer(PendingWriteEntry(msg, promise))) {
                promise.tryFailure(RejectedExecutionException("queue is full"))
            }
            promise
        } else {
            super.write(msg, promise)
        }
    }

    override fun write(msg: Any): ChannelFuture {
        return if (!isActive) {
            val promise = newPromise()
            //如果不是活跃的，则放到队列去
            if (!pendingWriteEntries.offer(PendingWriteEntry(msg, promise))) {
                promise.tryFailure(RejectedExecutionException("queue is full"))
            }
            promise
        } else {
            super.write(msg)
        }
    }

    override fun writeAndFlush(msg: Any, promise: ChannelPromise): ChannelFuture {
        return if (!isActive) {
            //如果不是活跃的，则放到队列去
            if (!pendingWriteEntries.offer(PendingWriteEntry(msg, promise))) {
                promise.tryFailure(RejectedExecutionException("queue is full"))
            }
            promise
        } else {
            super.writeAndFlush(msg, promise)
        }
    }

    override fun writeAndFlush(msg: Any): ChannelFuture {
        return if (!isActive) {
            //如果不是活跃的，则放到队列去
            val promise = newPromise()
            if (!pendingWriteEntries.offer(PendingWriteEntry(msg, promise))) {
                promise.tryFailure(RejectedExecutionException("queue is full"))
            }
            promise
        } else {
            super.writeAndFlush(msg)
        }
    }

    @Throws(Exception::class)
    override fun doWrite(`in`: ChannelOutboundBuffer) {
        while (true) {
            val msg = `in`.current() ?: break
            if (msg is ByteBuf) {
                val buf = msg
                if (!buf.isReadable) {
                    `in`.remove()
                    continue
                }
                val localFlushedAmount = buf.readableBytes()
                try {
                    /**
                     * @see cn.netdiscovery.adbd.constant.Constants.WRITE_DATA_MAX;
                     * 此处不能直接一次write, 超过大小的得分段write
                     */
                    while (true) {
                        val size = Math.min(buf.readableBytes(), WRITE_DATA_MAX)
                        if (size == 0) {
                            break
                        }
                        val tmp = buf.readRetainedSlice(size)
                        parent().writeAndFlush(AdbPacket(Command.A_WRTE, localId, remoteId, tmp))
                    }
                } catch (e: Exception) {
                    ReferenceCountUtil.safeRelease(buf)
                    throw e
                }
                `in`.progress(localFlushedAmount.toLong())
                if (!buf.isReadable) {
                    `in`.remove()
                }
            } else {
                `in`.remove(UnsupportedOperationException("unsupported message type: " + StringUtil.simpleClassName(msg)))
            }
        }
    }

    override fun eventLoop(): EventLoop {
        return eventLoop
    }

    override fun config(): ChannelConfig {
        return config
    }

    override fun isOpen(): Boolean {
        return remoteId > 0 || localId > 0
    }

    override fun isActive(): Boolean {
        return localId > 0 && remoteId > 0
    }

    override fun metadata(): ChannelMetadata {
        return metadata
    }

    //ChannelInboundHandler
    @Throws(Exception::class)
    override fun channelRegistered(ctx: ChannelHandlerContext) {
        ctx.fireChannelRegistered()
    }

    @Throws(Exception::class)
    override fun channelUnregistered(ctx: ChannelHandlerContext) {
        ctx.fireChannelUnregistered()
    }

    @Throws(Exception::class)
    override fun channelActive(ctx: ChannelHandlerContext) {
        ctx.fireChannelActive()
    }

    @Throws(Exception::class)
    override fun channelInactive(ctx: ChannelHandlerContext) {
        pipeline().fireChannelInactive()
        ctx.fireChannelInactive()
    }

    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg is AdbPacket) {
            val packet: AdbPacket = msg
            when (packet.command) {
                Command.A_OKAY -> {

                    if (!isActive) {
                        val promise = connectPromise
                        if (promise == null) {
                            //记录日志
                            logger.warn("connectPromise is null")
                            return
                        }
                        remoteId = packet.arg0
                        this.eventLoop.register(this)
                        val promiseSet = promise.trySuccess()
                        if (!promiseSet) {
                            close()
                        }
                        //开始写入pending write entries
                        while (true) {
                            val entry: PendingWriteEntry = pendingWriteEntries.poll() ?: break
                            pipeline().write(entry.msg).addListener { f: Future<in Void?> ->
                                if (f.cause() != null) {
                                    entry.promise.tryFailure(f.cause())
                                } else {
                                    entry.promise.trySuccess()
                                }
                            }
                        }
                        flush()
                    } else {
                        pipeline().fireUserEventTriggered("ACK")
                    }
                }

                Command.A_WRTE -> pipeline().fireChannelRead(packet.payload)

                Command.A_CLSE -> close()
            }
        } else {
            ctx.fireChannelRead(msg)
        }
    }

    @Throws(Exception::class)
    override fun channelReadComplete(ctx: ChannelHandlerContext) {
        ctx.fireChannelReadComplete()
    }

    @Throws(Exception::class)
    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any?) {
        ctx.fireUserEventTriggered(evt)
    }

    @Throws(Exception::class)
    override fun channelWritabilityChanged(ctx: ChannelHandlerContext) {
        pipeline().fireChannelWritabilityChanged()
        ctx.fireChannelWritabilityChanged()
    }

    @Throws(Exception::class)
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable?) {
        pipeline().fireExceptionCaught(cause)
        ctx.fireExceptionCaught(cause)
    }

    //ChannelHandler
    @Throws(Exception::class)
    override fun handlerAdded(ctx: ChannelHandlerContext?) {
    }

    @Throws(Exception::class)
    override fun handlerRemoved(ctx: ChannelHandlerContext?) {
    }

    private inner class AdbUnsafe : AbstractUnsafe() {

        override fun connect(remoteAddress: SocketAddress, localAddress: SocketAddress, promise: ChannelPromise) {
            this@AdbChannel.remoteAddress = remoteAddress as AdbChannelAddress
            var buf: ByteBuf? = null
            try {
                if (!promise.setUncancellable() || !ensureOpen(promise)) {
                    return
                }
                if (connectPromise != null) {
                    throw ConnectionPendingException()
                }
                connectPromise = promise
                val b: ByteArray = remoteAddress.destination.toByteArray(StandardCharsets.UTF_8)
                buf = Unpooled.wrappedBuffer(b)
                parent().writeAndFlush(AdbPacket(Command.A_OPEN, localId, remoteId, buf))
                    .addListener { f: Future<in Void?> ->
                        if (f.cause() != null) {
                            promise.tryFailure(f.cause())
                        }
                    }
                val connectTimeoutMillis = config().connectTimeoutMillis
                if (connectTimeoutMillis > 0) {
                    connectTimeoutFuture = eventLoop().schedule({
                        val cause =
                            ConnectTimeoutException("connection timed out: $remoteAddress")
                        if (promise.tryFailure(cause)) {
                            close(voidPromise())
                        }
                    }, connectTimeoutMillis.toLong(), TimeUnit.MILLISECONDS)
                }
                promise.addListener(ChannelFutureListener { future: ChannelFuture ->
                    if (!future.isSuccess) {
                        if (connectTimeoutFuture != null) {
                            connectTimeoutFuture!!.cancel(false)
                        }
                        connectPromise = null
                        close(voidPromise())
                    }
                })
            } catch (t: Throwable) {
                if (buf != null) {
                    ReferenceCountUtil.safeRelease(buf)
                }
                promise.tryFailure(annotateConnectException(t, remoteAddress))
                closeIfClosed()
            }
        }
    }
}