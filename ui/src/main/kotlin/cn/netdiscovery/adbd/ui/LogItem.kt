package cn.netdiscovery.adbd.ui

import cn.netdiscovery.adbd.utils.currentLogTime

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.ui.LogItem
 * @author: Tony Shen
 * @date: 2022/4/19 2:43 下午
 * @version: V1.0 <描述当前版本功能>
 */
data class LogItem(val msg: String,val create: String = currentLogTime()) {

    fun getMessage(): String = "$create $msg"
}
