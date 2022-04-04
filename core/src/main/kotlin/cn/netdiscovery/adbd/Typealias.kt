package cn.netdiscovery.adbd

import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelInitializer

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.Typealias
 * @author: Tony Shen
 * @date: 2021-06-02 15:46
 * @version: V1.0 <描述当前版本功能>
 */
typealias AdbChannelInitializer = (Channel) -> Unit

typealias ChannelFactory = (ChannelInitializer<Channel>) -> ChannelFuture