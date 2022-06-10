package cn.netdiscovery.adbd.netty.handler

import cn.netdiscovery.adbd.device.AdbDevice
import cn.netdiscovery.adbd.domain.SyncID
import cn.netdiscovery.adbd.domain.enum.Feature
import cn.netdiscovery.adbd.domain.sync.SyncFail
import cn.netdiscovery.adbd.domain.sync.SyncPath
import cn.netdiscovery.adbd.domain.sync.SyncStat
import cn.netdiscovery.adbd.exception.AdbException
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.util.concurrent.Future
import io.netty.util.concurrent.Promise
import java.net.ProtocolException
import java.nio.channels.ClosedChannelException

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.netty.handler.SyncStatHandler
 * @author: Tony Shen
 * @date: 2022/4/8 8:59 下午
 * @version: V1.0 <描述当前版本功能>
 */
class SyncStatHandler(private val device: AdbDevice, private val path: String, private val promise: Promise<SyncStat>) :
    ChannelInboundHandlerAdapter() {

    @Throws(Exception::class)
    override fun channelActive(ctx: ChannelHandlerContext) {
        val hasStatV2: Boolean = device.features()?.contains(Feature.STAT_V2)?:false
        val sid: SyncID = if (hasStatV2) SyncID.STAT_V2 else SyncID.LSTAT_V1
        val syncPath = SyncPath(sid, path)
        ctx.writeAndFlush(syncPath)
            .addListener { f: Future<in Void> ->
                if (f.cause() != null) {
                    promise.tryFailure(f.cause())
                }
            }
    }

    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg is SyncFail) {
            promise.tryFailure(AdbException(msg.error))
        } else if (msg is SyncStat) {
            promise.trySuccess(msg)
        } else {
            promise.tryFailure(ProtocolException("Error reply:$msg"))
        }
    }

    @Throws(Exception::class)
    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable) {
        promise.tryFailure(cause)
    }

    @Throws(Exception::class)
    override fun channelInactive(ctx: ChannelHandlerContext) {
        promise.tryFailure(ClosedChannelException())
    }
}