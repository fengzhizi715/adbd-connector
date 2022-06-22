package cn.netdiscovery.adbd.ui

import cn.netdiscovery.adbd.device.SocketAdbDevice

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.ui.`SocketAdbDevice+Extension`
 * @author: Tony Shen
 * @date: 2022/6/14 11:57 上午
 * @version: V1.0 <描述当前版本功能>
 */
fun <T> SocketAdbDevice?.wrapLet(block:(SocketAdbDevice)-> T) {

    this?.let {
        block.invoke(it)
    }?: run{
        Store.addLog {
            LogItem("the phone is not connected，please connect the phone first")
        }
    }
}