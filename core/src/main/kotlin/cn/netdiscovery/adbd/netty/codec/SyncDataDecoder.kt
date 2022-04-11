package cn.netdiscovery.adbd.netty.codec

import cn.netdiscovery.adbd.domain.enum.SyncID
import cn.netdiscovery.adbd.domain.sync.SyncData
import cn.netdiscovery.adbd.domain.sync.SyncDataDone
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import java.net.ProtocolException

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.netty.codec.SyncDataDecoder
 * @author: Tony Shen
 * @date: 2022/4/11 9:28 下午
 * @version: V1.0 <描述当前版本功能>
 */
class SyncDataDecoder : SyncDecoder() {

    @Throws(Exception::class)
    override fun decode(sid: SyncID, ctx: ChannelHandlerContext, `in`: ByteBuf, out: MutableList<Any>) {
        when (sid) {
            SyncID.DATA -> {
                val readerIndex = `in`.readerIndex()
                val len = `in`.getIntLE(readerIndex + 4)
                if (`in`.readableBytes() >= 8 + len) {
                    `in`.skipBytes(8)
                    val payload = `in`.readRetainedSlice(len)
                    out.add(SyncData(payload))
                }
            }
            SyncID.DONE -> {
                `in`.skipBytes(4)
                out.add(SyncDataDone(`in`.readIntLE()))
            }
            else -> ctx.fireExceptionCaught(ProtocolException("unsupported sid:$sid"))
        }
    }
}