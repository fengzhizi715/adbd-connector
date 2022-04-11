package cn.netdiscovery.adbd.domain.sync

import cn.netdiscovery.adbd.domain.StatMode
import java.util.*

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.domain.sync.SyncDentV2
 * @author: Tony Shen
 * @date: 2022/4/11 3:14 下午
 * @version: V1.0 <描述当前版本功能>
 */
data class SyncDentV2(
    override val mode: StatMode?,
    override val size: Long,
    override val mtime: Date?,
    override val name: String,
    val error: Long,
    val dev: Long,
    val ino: Long,
    val nlink: Long,
    val uid: Long,
    val gid: Long,
    val atime: Date?,
    val ctime: Date?
) : SyncDent(mode, size, mtime, name) {

    override fun toString(): String {
        return name
    }
}
