package cn.netdiscovery.adbd.netty.codec

import cn.netdiscovery.adbd.domain.sync.SyncDent
import cn.netdiscovery.adbd.domain.sync.SyncDentDone
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.netty.codec.SyncDentAggregator
 * @author: Tony Shen
 * @date: 2022/4/11 3:36 下午
 * @version: V1.0 <描述当前版本功能>
 */
class SyncDentAggregator : ChannelInboundHandlerAdapter() {

    private val dents: MutableList<SyncDent> = ArrayList()

    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        when (msg) {
            is SyncDent -> {
                dents.add(msg)
            }
            is SyncDentDone -> {
                val dentArray = dents.toTypedArray()
                dents.clear()
                ctx.fireChannelRead(dentArray)
            }
            else -> {
                ctx.fireChannelRead(msg)
            }
        }
    }
}