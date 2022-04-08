package cn.netdiscovery.adbd.domain.sync

import io.netty.buffer.ByteBuf
import io.netty.util.ReferenceCounted

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.domain.sync.SyncData
 * @author: Tony Shen
 * @date: 2022/4/8 8:27 下午
 * @version: V1.0 <描述当前版本功能>
 */
class SyncData(val data: ByteBuf) : ReferenceCounted {

    override fun refCnt(): Int {
        return data.refCnt()
    }

    override fun retain(): ReferenceCounted {
        data.retain()
        return this
    }

    override fun retain(increment: Int): ReferenceCounted {
        data.retain(increment)
        return this
    }

    override fun touch(): ReferenceCounted {
        data.touch()
        return this
    }

    override fun touch(hint: Any?): ReferenceCounted {
        data.touch(hint)
        return this
    }

    override fun release(): Boolean {
        return data.release()
    }

    override fun release(decrement: Int): Boolean {
        return data.release(decrement)
    }
}
