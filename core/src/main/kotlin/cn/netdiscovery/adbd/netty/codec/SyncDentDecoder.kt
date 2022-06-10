package cn.netdiscovery.adbd.netty.codec

import cn.netdiscovery.adbd.domain.SyncID
import cn.netdiscovery.adbd.domain.sync.*
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import java.net.ProtocolException
import java.nio.charset.StandardCharsets

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.netty.codec.SyncDentDecoder
 * @author: Tony Shen
 * @date: 2022/4/11 2:58 下午
 * @version: V1.0 <描述当前版本功能>
 */
class SyncDentDecoder : SyncStatDecoder() {

    @Throws(Exception::class)
    private fun decodeDent(ctx: ChannelHandlerContext, `in`: ByteBuf, out: MutableList<Any>) {
        val readerIndex = `in`.readerIndex()
        val len = `in`.getIntLE(readerIndex + 16)
        if (`in`.readableBytes() >= 20 + len) {
            `in`.skipBytes(4)
            val stat: SyncStat = decodeStat(`in`)
            `in`.skipBytes(4)
            val b = ByteArray(len)
            `in`.readBytes(b)
            out.add(SyncDent(stat.mode, stat.size, stat.mtime, String(b, StandardCharsets.UTF_8)))
        }
    }

    @Throws(Exception::class)
    private fun decodeDentV2(ctx: ChannelHandlerContext, `in`: ByteBuf, out: MutableList<Any>) {
        val readerIndex = `in`.readerIndex()
        val len = `in`.getIntLE(readerIndex + 72)
        if (`in`.readableBytes() >= 76 + len) {
            `in`.skipBytes(4)
            val stat: SyncStatV2 = decodeStatV2(`in`)
            `in`.skipBytes(4)
            val b = ByteArray(len)
            `in`.readBytes(b)
            out.add(SyncDentV2(stat.mode, stat.size, stat.mtime, String(b, StandardCharsets.UTF_8), stat.error, stat.dev, stat.ino, stat.nlink, stat.uid, stat.gid, stat.atime, stat.ctime))
        }
    }

    @Throws(Exception::class)
    override fun decode(sid: SyncID, ctx: ChannelHandlerContext, `in`: ByteBuf, out: MutableList<Any>) {
        when (sid) {
            SyncID.DENT_V1 -> decodeDent(ctx, `in`, out)
            SyncID.DENT_V2 -> decodeDentV2(ctx, `in`, out)
            SyncID.DONE -> {
                `in`.skipBytes(20)
                out.add(SyncDentDone())
            }
            else -> ctx.fireExceptionCaught(ProtocolException("unsupported sid:$sid"))
        }
    }
}