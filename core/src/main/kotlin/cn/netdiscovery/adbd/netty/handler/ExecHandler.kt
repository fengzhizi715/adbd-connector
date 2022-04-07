package cn.netdiscovery.adbd.netty.handler

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.util.concurrent.Promise

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.netty.handler.ExecHandler
 * @author: Tony Shen
 * @date: 2022/4/7 4:20 下午
 * @version: V1.0 <描述当前版本功能>
 */
class ExecHandler(private val promise: Promise<String>) : ChannelInboundHandlerAdapter() {
    private val sb: StringBuilder = StringBuilder()

    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg is CharSequence) {
            sb.append(msg)
        } else {
            ctx.fireChannelRead(msg)
        }
    }

    @Throws(Exception::class)
    override fun channelInactive(ctx: ChannelHandlerContext) {
        try {
            promise.trySuccess(sb.toString())
        } catch (cause: Throwable) {
            promise.tryFailure(cause)
        }
    }

    @Throws(Exception::class)
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        promise.tryFailure(cause)
    }
}