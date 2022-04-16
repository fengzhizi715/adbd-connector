package cn.netdiscovery.adbd.ui

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.ui.Store
 * @author: Tony Shen
 * @date: 2022/4/16 3:55 下午
 * @version: V1.0 <描述当前版本功能>
 */
object Store: IStore {

    val device = Device()

    override fun clearLog() {
        TODO("Not yet implemented")
    }

    override fun changeConnectStatus(value: Int) {
        TODO("Not yet implemented")
    }
}

data class Device(
    val deviceStatus: MutableState<Int> = mutableStateOf(-1), //设备状态
    val ipAddress: MutableState<String> = mutableStateOf(""),  //ip 地址
    val port: MutableState<String> = mutableStateOf(""),
    val shellCommand: MutableState<String> = mutableStateOf(""),
    val deviceVersion: MutableState<String> = mutableStateOf(""), //手机信息
)
