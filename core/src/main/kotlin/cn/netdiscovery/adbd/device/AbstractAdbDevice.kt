package cn.netdiscovery.adbd.device

import cn.netdiscovery.adbd.AdbChannelInitializer
import cn.netdiscovery.adbd.domain.AdbChannelAddress
import cn.netdiscovery.adbd.domain.DeviceInfo
import cn.netdiscovery.adbd.domain.PendingWriteEntry
import cn.netdiscovery.adbd.domain.enum.DeviceType
import cn.netdiscovery.adbd.domain.enum.Feature
import cn.netdiscovery.adbd.netty.channel.AdbChannel
import cn.netdiscovery.adbd.netty.codec.AdbPacketCodec
import cn.netdiscovery.adbd.netty.connection.AdbChannelProcessor
import cn.netdiscovery.adbd.netty.handler.AdbAuthHandler
import cn.netdiscovery.adbd.utils.getChannelName
import io.netty.channel.*
import io.netty.util.concurrent.Future
import java.security.interfaces.RSAPrivateCrtKey
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.device.AbstractAdbDevice
 * @author: Tony Shen
 * @date: 2022/4/2 2:58 下午
 * @version: V1.0 <描述当前版本功能>
 */
abstract class AbstractAdbDevice protected constructor(
    private val serial: String,
    private val privateKey: RSAPrivateCrtKey,
    private val publicKey: ByteArray,
    private val factory: cn.netdiscovery.adbd.ChannelFactory
) : AdbDevice {

    private val channelIdGen: AtomicInteger = AtomicInteger(1)
    private val reverseMap: MutableMap<CharSequence, AdbChannelInitializer> = ConcurrentHashMap<CharSequence, AdbChannelInitializer>()
    private val forwards: MutableSet<Channel> = ConcurrentHashMap.newKeySet()

    @Volatile
    private var listeners: MutableSet<DeviceListener> = ConcurrentHashMap.newKeySet()

    @Volatile
    private lateinit var channel: Channel

    @Volatile
    var deviceInfo: DeviceInfo?=null

    init {
        newConnection()[30, TimeUnit.SECONDS]
    }

    private fun newConnection(): ChannelFuture {

        val future:ChannelFuture = factory.invoke(object : ChannelInitializer<Channel>() {
            override fun initChannel(ch: Channel) {
                val pipeline = ch.pipeline()
                pipeline.addLast(object : ChannelInboundHandlerAdapter() {
                    @Throws(Exception::class)
                    override fun channelInactive(ctx: ChannelHandlerContext) {

                        listeners.forEach{ listener ->
                            try {
                                listener.onDisconnected(this@AbstractAdbDevice)
                            } catch (e: Exception) {
                            }
                        }
                        super.channelInactive(ctx)
                    }
                })
                .addLast("codec", AdbPacketCodec())
                .addLast("auth", AdbAuthHandler(privateKey, publicKey))
                .addLast("connect", ConnectHandler(this@AbstractAdbDevice))
            }

        })
        channel = future.channel()
        return future
    }

    protected fun factory(): cn.netdiscovery.adbd.ChannelFactory = factory

    protected fun privateKey(): RSAPrivateCrtKey {
        return privateKey
    }

    protected fun publicKey(): ByteArray {
        return publicKey
    }

    fun eventLoop(): EventLoop {
        return channel.eventLoop()
    }

    override fun serial(): String {
        return serial
    }

    override fun type(): DeviceType?  = deviceInfo?.type?:null

    override fun model(): String? = deviceInfo?.model?:null

    override fun product(): String? = deviceInfo?.product?:null

    override fun device(): String? = deviceInfo?.device?:null

    override fun features(): Set<Feature>? = deviceInfo?.features?:null

    override fun open(destination: String, timeoutMs: Int, initializer: AdbChannelInitializer): ChannelFuture {
        val localId = channelIdGen.getAndIncrement()
        val channelName: String = getChannelName(localId)
        val adbChannel = AdbChannel(channel, localId, 0)
        adbChannel.config().connectTimeoutMillis = timeoutMs
        initializer.invoke(adbChannel)
        channel.pipeline().addLast(channelName, adbChannel)
        return adbChannel.connect(AdbChannelAddress(destination, localId))
    }

    override fun addListener(listener: DeviceListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: DeviceListener) {
        listeners.remove(listener)
    }

    private class ConnectHandler(val device: AbstractAdbDevice) : ChannelDuplexHandler() {

        private val pendingWriteEntries: Queue<PendingWriteEntry> = LinkedList()

        @Throws(Exception::class)
        override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
            if (msg is DeviceInfo) {
                ctx.pipeline()
                    .remove(this)
                    .addAfter("codec", "processor", AdbChannelProcessor(device.channelIdGen, device.reverseMap))
                device.deviceInfo = msg
                while (true) {
                    val entry: PendingWriteEntry = pendingWriteEntries.poll() ?: break
                    ctx.channel().write(entry.msg).addListener { f: Future<in Void> ->
                        if (f.cause() != null) {
                            entry.promise.tryFailure(f.cause())
                        } else {
                            entry.promise.trySuccess()
                        }
                    }
                }
                ctx.channel().flush()

                device.listeners.forEach{ listener ->
                    try {
                        listener.onConnected(device)
                    } catch (e: Exception) {
                    }
                }
            } else {
                super.channelRead(ctx, msg)
            }
        }

        @Throws(Exception::class)
        override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable?) {
            ctx.close()
        }

        @Throws(Exception::class)
        override fun write(ctx: ChannelHandlerContext?, msg: Any, promise: ChannelPromise) {
            if (!pendingWriteEntries.offer(PendingWriteEntry(msg, promise))) {
                promise.tryFailure(RejectedExecutionException("queue is full"))
            }
        }
    }
}