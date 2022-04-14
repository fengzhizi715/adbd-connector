package cn.netdiscovery.adbd.netty.codec

import cn.netdiscovery.adbd.domain.AdbPacket
import cn.netdiscovery.adbd.domain.enum.Command
import cn.netdiscovery.adbd.utils.logger
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageCodec
import java.net.ProtocolException

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.netty.codec.AdbPacketCodec
 * @author: Tony Shen
 * @date: 2022/4/4 11:35 下午
 * @version: V1.0 <描述当前版本功能>
 */
class AdbPacketCodec : ByteToMessageCodec<AdbPacket>() {

    private val logger = logger<AdbPacketCodec>()

    @Throws(Exception::class)
    override fun decode(ctx: ChannelHandlerContext, `in`: ByteBuf, out: MutableList<Any>) {
        //读取长度
        `in`.markReaderIndex()
        if (`in`.readableBytes() < 16) {
            `in`.resetReaderIndex()
            return
        }
        val cmd = `in`.readIntLE()
        val arg0 = `in`.readIntLE()
        val arg1 = `in`.readIntLE()
        val len = `in`.readIntLE()
        if (`in`.readableBytes() < 8 /*剩余头的长度*/ + len) {
            `in`.resetReaderIndex()
            return
        }
        val checksum = `in`.readIntLE()
        val magic = `in`.readIntLE()
        val command: Command? = Command.findByValue(cmd)
        if (command == null) {
            ctx.fireExceptionCaught(ProtocolException("Unknown command: $cmd"))
            return
        }
        if (command.magic() !== magic) {
            ctx.fireExceptionCaught(ProtocolException("Unmatched magic field expect=" + command.magic().toString() + ", actual=" + magic))
            return
        }
        var payload: ByteBuf? = null
        if (len > 0) {
            payload = `in`.readRetainedSlice(len)
        }
        val message = AdbPacket(command, arg0, arg1, len, checksum, magic, payload)

        logger.info("<== recv command={}, arg0={}, arg1={}, size={}", message.command, message.arg0, message.arg1, message.size)
        out.add(message)
    }

    @Throws(Exception::class)
    override fun encode(ctx: ChannelHandlerContext, msg: AdbPacket, out: ByteBuf) {

        logger.info("==> send command={}, arg0={}, arg1={}, size={}", msg.command, msg.arg0, msg.arg1, msg.size)

        out.writeIntLE(msg.command.value())
        out.writeIntLE(msg.arg0)
        out.writeIntLE(msg.arg1)
        out.writeIntLE(msg.size)
        out.writeIntLE(msg.checksum)
        out.writeIntLE(msg.magic)
        if (msg.payload != null) {
            out.writeBytes(msg.payload)
        }
    }
}