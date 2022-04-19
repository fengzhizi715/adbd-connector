package cn.netdiscovery.adbd.ui

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
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
    val logs = mutableStateListOf(LogItem("the adbd-connector is starting"))

    override fun addLog(msg: () -> LogItem) {
        val logItem = msg.invoke()

        logs.add(0, logItem)

        //只保留1000条日志,防止list储存过大
        if (logs.size > 2000) {
            logs.removeRange(1000, logs.size)
        }
    }

    override fun clearLog() {
        logs.clear()
    }

    override fun changeConnectStatus(value: Int) {
        device.deviceStatus.value = value
    }

    override fun setDeviceInfo(value: String) {
        device.deviceInfo.value = value
    }
}

data class Device(
    val deviceStatus: MutableState<Int> = mutableStateOf(0), //设备状态
    val ipAddress: MutableState<String> = mutableStateOf(""),  //ip 地址
    val port: MutableState<String> = mutableStateOf(""),
    val shellCommand: MutableState<String> = mutableStateOf(""),
    val deviceInfo: MutableState<String> = mutableStateOf(""), //手机信息
    val pushSrc: MutableState<String> = mutableStateOf(""),
    val pushDest: MutableState<String> = mutableStateOf(""),
    val pullSrc: MutableState<String> = mutableStateOf(""),
    val pullDest: MutableState<String> = mutableStateOf(""),
) {

    fun deviceStatus(): String = when (deviceStatus.value) {
        0 -> "初始化"
        1 -> "已连接"
        2 -> "已断开"
        else -> "连接中"
    }
}
