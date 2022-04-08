package cn.netdiscovery.adbd.netty.codec

import cn.netdiscovery.adbd.domain.enum.SyncID
import cn.netdiscovery.adbd.domain.sync.SyncFail
import cn.netdiscovery.adbd.domain.sync.SyncOkay
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import java.net.ProtocolException
import java.nio.charset.StandardCharsets

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.netty.codec.SyncDecoder
 * @author: Tony Shen
 * @date: 2022/4/8 4:22 下午
 * @version: V1.0 <描述当前版本功能>
 */
class SyncDecoder : ByteToMessageDecoder() {

    @Throws(Exception::class)
    protected fun decode(sid: SyncID, ctx: ChannelHandlerContext, `in`: ByteBuf, out: List<Any>) {
        ctx.fireExceptionCaught(ProtocolException("unsupported sid:$sid"))
    }

    @Throws(Exception::class)
    override fun decode(ctx: ChannelHandlerContext, `in`: ByteBuf, out: MutableList<Any>) {
        val readerIndex = `in`.readerIndex()
        val id = `in`.getIntLE(readerIndex)
        val sid: SyncID? = SyncID.findByValue(id)
        if (sid == null) {
            `in`.skipBytes(4)
            ctx.fireExceptionCaught(Exception("Unknown sid: $id"))
            return
        }
        when (sid) {
            SyncID.OKAY -> {
                //fall through
                `in`.skipBytes(8)
                out.add(SyncOkay())
            }
            SyncID.FAIL -> {
                val len = `in`.getIntLE(readerIndex + 4)
                if (`in`.readableBytes() >= 8 + len) {
                    val b = ByteArray(len)
                    `in`.skipBytes(8)
                    `in`.readBytes(b)
                    out.add(SyncFail(String(b, StandardCharsets.UTF_8)))
                }
            }
            else -> decode(sid, ctx, `in`, out)
        }
    }
}