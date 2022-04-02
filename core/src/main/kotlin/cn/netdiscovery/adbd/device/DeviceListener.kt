package cn.netdiscovery.adbd.device

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.device.DeviceListener
 * @author: Tony Shen
 * @date: 2022/4/2 10:24 下午
 * @version: V1.0 <描述当前版本功能>
 */
interface DeviceListener {

    fun onConnected(device: AdbDevice)

    fun onDisconnected(device: AdbDevice)
}