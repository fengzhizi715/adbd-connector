package cn.netdiscovery.adbd.netty.handler

import cn.netdiscovery.adbd.constant.SYNC_DATA_MAX
import cn.netdiscovery.adbd.device.AdbDevice
import cn.netdiscovery.adbd.domain.enum.SyncID
import cn.netdiscovery.adbd.domain.sync.*
import cn.netdiscovery.adbd.exception.AdbException
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.util.ReferenceCountUtil
import io.netty.util.concurrent.Future
import io.netty.util.concurrent.Promise
import java.io.InputStream
import java.net.ProtocolException
import java.nio.channels.ClosedChannelException

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.netty.handler.SyncPushHandler
 * @author: Tony Shen
 * @date: 2022/4/12 5:18 下午
 * @version: V1.0 <描述当前版本功能>
 */
class SyncPushHandler(private val device: AdbDevice,private val src: InputStream,private val dest: String,private val mode: Int,private val mtime: Int,private val promise: Promise<*>) :
    ChannelInboundHandlerAdapter() {

    @Throws(Exception::class)
    override fun channelActive(ctx: ChannelHandlerContext) {
        //发送SEND指令
        val destAndMode = "$dest,$mode"
        ctx.writeAndFlush(SyncPath(SyncID.SEND_V1, destAndMode))
            .addListener { f1: Future<in Void> ->
                if (f1.cause() != null) {
                    promise.tryFailure(f1.cause())
                }
            }
        //发送数据
        //启动一个新的线程读取流并发送数据, 不能阻塞当前线程，否则收不到响应
        object : Thread() {
            override fun run() {
                try {
                    while (true) {
                        val buffer = ByteArray(SYNC_DATA_MAX)
                        val size = src.read(buffer)
                        if (size == -1) {
                            break
                        }
                        if (size == 0) {
                            continue
                        }
                        val payload = Unpooled.wrappedBuffer(buffer, 0, size)
                        try {
                            ctx.writeAndFlush(SyncData(payload))
                                .addListener { f2: Future<in Void> ->
                                    if (f2.cause() != null) {
                                        promise.tryFailure(f2.cause())
                                    }
                                }
                        } catch (cause: Throwable) {
                            ReferenceCountUtil.safeRelease(payload)
                            throw cause
                        }
                    }
                    //发送done
                    ctx.writeAndFlush(SyncDataDone(mtime))
                        .addListener { f3: Future<in Void> ->
                            if (f3.cause() != null) {
                                promise.tryFailure(f3.cause())
                            }
                        }
                } catch (cause: Throwable) {
                    promise.tryFailure(cause)
                }
            }
        }.start()
    }

    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext?, msg: Any) {
        when (msg) {
            is SyncFail -> {
                promise.tryFailure(AdbException(msg.error))
            }
            is SyncOkay -> {
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