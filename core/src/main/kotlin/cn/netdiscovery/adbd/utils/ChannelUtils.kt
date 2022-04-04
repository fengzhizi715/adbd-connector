package cn.netdiscovery.adbd.utils

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.utils.ChannelUtils
 * @author: Tony Shen
 * @date: 2022/4/5 12:16 上午
 * @version: V1.0 <描述当前版本功能>
 */
fun getChannelName(localId: Int): String {
    return "ADB-CHANNEL-HANDLER@$localId"
}