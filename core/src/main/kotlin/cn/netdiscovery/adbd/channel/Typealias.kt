package cn.netdiscovery.adbd.channel

import io.netty.channel.Channel

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.channel.Typealias
 * @author: Tony Shen
 * @date: 2021-06-02 15:46
 * @version: V1.0 <描述当前版本功能>
 */
typealias AdbChannelInitializer = (Channel) -> Unit