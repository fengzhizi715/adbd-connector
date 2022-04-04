package cn.netdiscovery.adbd.domain

import cn.netdiscovery.adbd.domain.enum.Command
import cn.netdiscovery.adbd.utils.checksum
import io.netty.buffer.ByteBuf
import io.netty.util.ReferenceCounted

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.domain.AdbPacket
 * @author: Tony Shen
 * @date: 2022/4/1 5:34 下午
 * @version: V1.0 <描述当前版本功能>
 */
class AdbPacket : ReferenceCounted {

    val command: Command
    val arg0: Int
    val arg1: Int
    val size: Int
    val checksum: Int
    val magic: Int
    val payload: ByteBuf?

    constructor(command: Command, arg0: Int, arg1: Int, size: Int, checksum: Int, magic: Int, payload: ByteBuf?) {
        this.command = command
        this.arg0 = arg0
        this.arg1 = arg1
        this.size = size
        this.checksum = checksum
        this.magic = magic
        this.payload = payload
    }

    constructor(command: Command, arg0: Int, arg1: Int, payload: ByteBuf?) {
        this.command = command
        this.arg0 = arg0
        this.arg1 = arg1
        this.size = payload?.readableBytes() ?: 0
        this.checksum = if (payload != null) checksum(payload) else 0
        this.magic = command.magic()
        this.payload = payload
    }

    constructor(command: Command, arg0: Int, arg1: Int) : this(command, arg0, arg1, null)

    override fun refCnt(): Int {
        return payload?.refCnt() ?: 0
    }

    override fun retain(): ReferenceCounted {
        payload?.retain()
        return this
    }

    override fun retain(increment: Int): ReferenceCounted {
        payload?.retain(increment)
        return this
    }

    override fun touch(): ReferenceCounted {
        payload?.touch()
        return this
    }

    override fun touch(hint: Any?): ReferenceCounted {
        payload?.touch(hint)
        return this
    }

    override fun release(): Boolean = payload?.release() ?: true

    override fun release(decrement: Int): Boolean = payload?.release(decrement) ?: true

    override fun toString(): String = "AdbPacket{" +
            "command=" + command +
            ", arg0=" + arg0 +
            ", arg1=" + arg1 +
            ", size=" + size +
            ", checksum=" + checksum +
            ", magic=" + magic +
            ", payload=" + payload +
            '}'
}