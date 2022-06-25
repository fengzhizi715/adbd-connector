package cn.netdiscovery.adbd.ui

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import java.awt.image.BufferedImage
import java.lang.NumberFormatException

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

    override fun setDeviceName(value: String) {
        device.deviceName.value = value
    }

    override fun setDeviceType(value: String) {
        device.deviceType.value = value
    }

    override fun setBrand(value: String) {
        device.brand.value = value
    }

    override fun setManufacturer(value: String) {
        device.manufacturer.value = value
    }

    override fun setOSVersion(value: String) {
        device.os.value = value
    }

    override fun setCpuArch(value: String) {
        device.cpuArch.value = value
    }

    override fun setCpuNum(value: String) {
        device.cpuNum.value = value
    }

    override fun setPhysicalSize(value: String) {
        device.physicalSize.value = value
    }

    override fun setMemTotal(value: String) {
        device.memTotal.value = value
    }

    override fun setBufferedImage(value: BufferedImage) {
        device.bufferedImage.value = value
    }
}

data class Device(
    val deviceStatus: MutableState<Int> = mutableStateOf(0), //设备状态
    val ipAddress: MutableState<String> = mutableStateOf(""),  //ip 地址
    val port: MutableState<String> = mutableStateOf(""),
    val shellCommand: MutableState<String> = mutableStateOf(""),
    val deviceName: MutableState<String> = mutableStateOf(""),
    val deviceType: MutableState<String> = mutableStateOf(""),
    val brand: MutableState<String> = mutableStateOf(""),
    val manufacturer: MutableState<String> = mutableStateOf(""),
    val os: MutableState<String> = mutableStateOf(""),
    val cpuArch: MutableState<String> = mutableStateOf(""),
    val cpuNum: MutableState<String> = mutableStateOf(""),
    val physicalSize: MutableState<String> = mutableStateOf(""), // 分辨率
    val memTotal: MutableState<String> = mutableStateOf(""),
    val bufferedImage: MutableState<BufferedImage?> = mutableStateOf(null),
    val pushSrc: MutableState<String> = mutableStateOf(""),
    val pushDest: MutableState<String> = mutableStateOf(""),
    val pullSrc: MutableState<String> = mutableStateOf(""),
    val pullDest: MutableState<String> = mutableStateOf(""),
    val installCommand: MutableState<String> = mutableStateOf(""),
    val uninstallCommand: MutableState<String> = mutableStateOf(""),
    val forwardLocalPort: MutableState<String> = mutableStateOf(""),
    val forwardRemote: MutableState<String> = mutableStateOf(""),
    val reverseLocal: MutableState<String> = mutableStateOf(""),
    val reverseRemote: MutableState<String> = mutableStateOf(""),
) {

    fun deviceStatus(): String = when (deviceStatus.value) {
        0 -> "未连接"
        1 -> "已连接"
        2 -> "已断开"
        else -> "连接中"
    }

    fun clear() {
        deviceName.value = ""
        deviceType.value = ""
        brand.value = ""
        manufacturer.value = ""
        os.value = ""
        cpuArch.value = ""
        cpuNum.value = ""
        physicalSize.value = ""
        memTotal.value = ""
        bufferedImage.value = null
    }
}
