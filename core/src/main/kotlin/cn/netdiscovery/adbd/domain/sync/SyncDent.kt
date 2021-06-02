package cn.netdiscovery.adbd.domain.sync

import cn.netdiscovery.adbd.domain.StatMode
import java.util.*

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.domain.sync.SyncDent
 * @author: Tony Shen
 * @date: 2021-06-02 15:25
 * @version: V1.0 <描述当前版本功能>
 */
class SyncDent(mode: StatMode,size: Long,mtime: Date, val name: String) : SyncStat(mode, size, mtime) {

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(name)
        sb.append("  ")
        sb.append(super.toString())
        return sb.toString()
    }
}
