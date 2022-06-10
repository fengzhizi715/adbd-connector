package cn.netdiscovery.adbd.domain.sync

import cn.netdiscovery.adbd.domain.SyncID

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.domain.sync.SyncPath
 * @author: Tony Shen
 * @date: 2022/4/8 8:30 下午
 * @version: V1.0 <描述当前版本功能>
 */
data class SyncPath(val sid: SyncID, val path: String)
