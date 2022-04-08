package cn.netdiscovery.adbd.domain.sync

import cn.netdiscovery.adbd.domain.StatMode
import java.util.*

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.domain.sync.SyncStatV2
 * @author: Tony Shen
 * @date: 2022/4/8 5:45 下午
 * @version: V1.0 <描述当前版本功能>
 */
data class SyncStatV2(
    val mode: StatMode?,
    val size: Long,
    val mtime: Date?,
    val error: Long,
    val dev: Long,
    val ino: Long,
    val nlink: Long,
    val uid: Long,
    val gid: Long,
    val atime: Date?,
    val ctime: Date?
) : SyncStat(mode, size, mtime)