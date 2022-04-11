package cn.netdiscovery.adbd.netty.handler

import cn.netdiscovery.adbd.device.AdbDevice
import cn.netdiscovery.adbd.domain.enum.Feature
import cn.netdiscovery.adbd.domain.enum.SyncID
import cn.netdiscovery.adbd.domain.sync.SyncDent
import cn.netdiscovery.adbd.domain.sync.SyncFail
import cn.netdiscovery.adbd.domain.sync.SyncPath
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.util.concurrent.Future
import io.netty.util.concurrent.Promise
import java.net.ProtocolException
import java.nio.channels.ClosedChannelException
import java.rmi.RemoteException

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.netty.handler.SyncListHandler
 * @author: Tony Shen
 * @date: 2022/4/11 4:37 下午
 * @version: V1.0 <描述当前版本功能>
 */
class SyncListHandler(private val device: AdbDevice, private val path: String, private val promise: Promise<Array<SyncDent>>) :
    ChannelInboundHandlerAdapter() {

    @Throws(Exception::class)
    override fun channelActive(ctx: ChannelHandlerContext) {
        val hasLsV2: Boolean = device.features()?.contains(Feature.LS_V2)?:false
        val sid: SyncID = if (hasLsV2) SyncID.LIST_V2 else SyncID.LIST_V1
        val syncPath = SyncPath(sid, path)
        ctx.writeAndFlush(syncPath)
            .addListener { f: Future<in Void?> ->
                if (f.cause() != null) {
                    promise.tryFailure(f.cause())
                }
            }
    }

    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext?, msg: Any) {
        if (msg is SyncFail) {
            promise.tryFailure(RemoteException(msg.error))
        } else if (msg is Array<*> && msg.isArrayOf<SyncDent>()) {
            promise.trySuccess(msg as Array<SyncDent>)
        } else {
            promise.tryFailure(ProtocolException("Error reply:$msg"))
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