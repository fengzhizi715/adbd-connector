package cn.netdiscovery.adbd.netty.handler

import cn.netdiscovery.adbd.device.AdbDevice
import cn.netdiscovery.adbd.domain.SyncID
import cn.netdiscovery.adbd.domain.sync.SyncData
import cn.netdiscovery.adbd.domain.sync.SyncDataDone
import cn.netdiscovery.adbd.domain.sync.SyncFail
import cn.netdiscovery.adbd.domain.sync.SyncPath
import cn.netdiscovery.adbd.exception.AdbException
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.util.ReferenceCountUtil
import io.netty.util.concurrent.Future
import io.netty.util.concurrent.Promise
import java.io.OutputStream
import java.net.ProtocolException
import java.nio.channels.ClosedChannelException

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.netty.handler.SyncPullHandler
 * @author: Tony Shen
 * @date: 2022/4/11 9:18 下午
 * @version: V1.0 <描述当前版本功能>
 */
class SyncPullHandler(private val device: AdbDevice,private val src: String,private val dest: OutputStream,private val promise: Promise<*>) :
    ChannelInboundHandlerAdapter() {

    @Throws(Exception::class)
    override fun channelActive(ctx: ChannelHandlerContext) {
        ctx.writeAndFlush(SyncPath(SyncID.RECV_V1, src))
            .addListener { f: Future<in Void?> ->
                if (f.cause() != null) {
                    promise.tryFailure(f.cause())
                }
            }
    }

    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext?, msg: Any) {
        when (msg) {
            is SyncFail -> {
                promise.tryFailure(AdbException(msg.error))
            }
            is SyncData -> {
                val buf: ByteBuf = msg.data
                try {
                    val size = buf.readableBytes()
                    if (size > 0) {
                        buf.readBytes(dest, size)
                    }
                } catch (cause: Throwable) {
                    promise.tryFailure(cause)
                } finally {
                    ReferenceCountUtil.safeRelease(msg)
                }
            }
            is SyncDataDone -> {
                promise.trySuccess(null)
            }
            else -> {
                promise.tryFailure(ProtocolException("Error reply:$msg"))
            }
        }
    }

    @Throws(Exception::class)
    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
        promise.tryFailure(cause)
    }

    @Throws(Exception::class)
    override fun channelInactive(ctx: ChannelHandlerContext?) {
        promise.tryFailure(ClosedChannelException())
    }
}