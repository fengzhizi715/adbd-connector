package cn.netdiscovery.adbd.netty.codec

import cn.netdiscovery.adbd.domain.enum.SyncID
import cn.netdiscovery.adbd.domain.sync.SyncData
import cn.netdiscovery.adbd.domain.sync.SyncDataDone
import cn.netdiscovery.adbd.domain.sync.SyncPath
import cn.netdiscovery.adbd.domain.sync.SyncQuit
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import java.nio.charset.StandardCharsets

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.netty.codec.SyncEncoder
 * @author: Tony Shen
 * @date: 2022/4/8 8:26 下午
 * @version: V1.0 <描述当前版本功能>
 */
class SyncEncoder : MessageToByteEncoder<Any>() {

    private val PADDING = ByteArray(4)

    @Throws(Exception::class)
    override fun acceptOutboundMessage(msg: Any): Boolean {
        return msg is SyncPath ||
                msg is SyncData ||
                msg is SyncDataDone ||
                msg is SyncQuit
    }

    @Throws(Exception::class)
    override fun encode(ctx: ChannelHandlerContext?, msg: Any, out: ByteBuf) {
        when (msg) {
            is SyncPath -> {
                val syncPath: SyncPath = msg
                out.writeBytes(syncPath.sid.byteArray())
                val b: ByteArray = syncPath.path.toByteArray(StandardCharsets.UTF_8)
                out.writeIntLE(b.size)
                out.writeBytes(b)
            }
            is SyncData -> {
                val syncData: SyncData = msg
                out.writeBytes(SyncID.DATA.byteArray())
                out.writeIntLE(syncData.data.readableBytes())
                out.writeBytes(syncData.data)
            }
            is SyncDataDone -> {
                val done: SyncDataDone = msg
                out.writeBytes(SyncID.DONE.byteArray())
                out.writeIntLE(done.mtime)
            }
            is SyncQuit -> {
                out.writeBytes(SyncID.QUIT.byteArray())
                out.writeBytes(PADDING)
            }
        }
    }
}