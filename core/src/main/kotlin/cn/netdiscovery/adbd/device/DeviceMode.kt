package cn.netdiscovery.adbd.device

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.device.DeviceMode
 * @author: Tony Shen
 * @date: 2022/4/2 10:29 下午
 * @version: V1.0 <描述当前版本功能>
 */
enum class DeviceMode(val deviceName: String) {

    SYSTEM(""),
    BOOTLOADER("bootloader"),
    RECOVERY("recovery"),
    SIDELOAD("sideload"),
    SIDELOAD_AUTO_REBOOT("sideload-auto-reboot");
}