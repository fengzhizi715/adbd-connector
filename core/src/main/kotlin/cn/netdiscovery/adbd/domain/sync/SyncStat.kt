package cn.netdiscovery.adbd.domain.sync

import cn.netdiscovery.adbd.domain.StatMode
import java.util.*

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.domain.sync.SyncStat
 * @author: Tony Shen
 * @date: 2021-06-02 14:22
 * @version: V1.0 <描述当前版本功能>
 */
open class SyncStat (
    open val mode: StatMode?,
    open val size: Long,
    open val mtime: Date?)
{
    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(mode)
        sb.append("  ")
        sb.append(size)
        sb.append("  ")
        sb.append(mtime)
        return sb.toString()
    }
}