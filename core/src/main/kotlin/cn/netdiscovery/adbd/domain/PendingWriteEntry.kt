package cn.netdiscovery.adbd.domain

import io.netty.channel.ChannelPromise

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.domain.PendingWriteEntry
 * @author: Tony Shen
 * @date: 2022/4/4 2:32 下午
 * @version: V1.0 <描述当前版本功能>
 */
data class PendingWriteEntry(
    val msg: Any,
    val promise: ChannelPromise
)
