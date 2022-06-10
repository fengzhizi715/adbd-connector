package cn.netdiscovery.adbd.netty.codec

import cn.netdiscovery.adbd.constant.*
import cn.netdiscovery.adbd.domain.FilePerm
import cn.netdiscovery.adbd.domain.StatMode
import cn.netdiscovery.adbd.domain.SyncID
import cn.netdiscovery.adbd.domain.enum.FileType
import cn.netdiscovery.adbd.domain.sync.SyncStat
import cn.netdiscovery.adbd.domain.sync.SyncStatV2
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import java.net.ProtocolException
import java.util.*

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.netty.codec.SyncStatDecoder
 * @author: Tony Shen
 * @date: 2022/4/8 4:44 下午
 * @version: V1.0 <描述当前版本功能>
 */
open class SyncStatDecoder : SyncDecoder() {

    @Throws(Exception::class)
    private fun decodeStat(ctx: ChannelHandlerContext, `in`: ByteBuf, out: MutableList<Any>) {
        if (`in`.readableBytes() >= 16) {
            `in`.skipBytes(4)
            out.add(decodeStat(`in`))
        }
    }

    @Throws(Exception::class)
    private fun decodeStatV2(ctx: ChannelHandlerContext, `in`: ByteBuf, out: MutableList<Any>) {
        if (`in`.readableBytes() >= 72) {
            `in`.skipBytes(4)
            out.add(decodeStatV2(`in`))
        }
    }

    @Throws(Exception::class)
    override fun decode(sid: SyncID, ctx: ChannelHandlerContext, `in`: ByteBuf, out: MutableList<Any>) {
        when (sid) {
            SyncID.LSTAT_V1 -> decodeStat(ctx, `in`, out)
            SyncID.STAT_V2 -> decodeStatV2(ctx, `in`, out)
            else -> ctx.fireExceptionCaught(ProtocolException("unsupported sid:$sid"))
        }
    }

    companion object {
        private fun toMode(mode: Int): StatMode {
            val type: FileType? = FileType.findByValue(mode)
            val usrPerm = FilePerm(
                mode and S_IRUSR === S_IRUSR,
                mode and S_IWUSR === S_IWUSR,
                mode and S_IXUSR === S_IXUSR
            )
            val grpPerm = FilePerm(
                mode and S_IRGRP === S_IRGRP,
                mode and S_IWGRP === S_IWGRP,
                mode and S_IXGRP === S_IXGRP
            )
            val othPerm = FilePerm(
                mode and S_IROTH === S_IROTH,
                mode and S_IWOTH === S_IWOTH,
                mode and S_IXOTH === S_IXOTH
            )
            return StatMode(type, usrPerm, grpPerm, othPerm)
        }

        fun decodeStat(`in`: ByteBuf): SyncStat {
            val mode = `in`.readIntLE()
            val size = `in`.readUnsignedIntLE()
            val mtime = `in`.readUnsignedIntLE()
            return SyncStat(
                (if (mode != 0) toMode(mode) else null),
                size,
                (if (mtime != 0L) Date(mtime * 1000) else null)
            )
        }

        fun decodeStatV2(`in`: ByteBuf): SyncStatV2 {
            val error = `in`.readIntLE()
            val dev = `in`.readLongLE()
            val ino = `in`.readLongLE()
            val mode = `in`.readIntLE()
            val nlink = `in`.readUnsignedIntLE()
            val uid = `in`.readUnsignedIntLE()
            val gid = `in`.readUnsignedIntLE()
            val size = `in`.readLongLE()
            val atime = `in`.readLongLE()
            val mtime = `in`.readLongLE()
            val ctime = `in`.readLongLE()
            return SyncStatV2(
                if (error == 0) toMode(mode) else null,
                size,
                if (error == 0) Date(mtime * 1000) else null,
                error.toLong(),
                dev,
                ino,
                nlink,
                uid,
                gid,
                if (error == 0) Date(atime * 1000) else null,
                if (error == 0) Date(ctime * 1000) else null
            )
        }
    }
}