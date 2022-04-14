package cn.netdiscovery.adbd.domain

import java.net.SocketAddress

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.domain.AdbChannelAddress
 * @author: Tony Shen
 * @date: 2021-06-02 11:14
 * @version: V1.0 <描述当前版本功能>
 */
data class AdbChannelAddress(val destination: String, val id: Int) : SocketAddress() {

    override fun toString(): String {
        return "$id:$destination"
    }
}
