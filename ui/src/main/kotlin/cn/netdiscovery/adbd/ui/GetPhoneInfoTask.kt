package cn.netdiscovery.adbd.ui

import cn.netdiscovery.adbd.device.AdbDevice

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.ui.GetPhoneInfoTask
 * @author: Tony Shen
 * @date: 2022/6/14 1:29 下午
 * @version: V1.0 <描述当前版本功能>
 */
object GetPhoneInfoTask {

    fun execute(device: AdbDevice) {

        val shellCommand = "getprop ro.build.version.release"
        val commands = shellCommand.trim().split("\\s+".toRegex())
        val shell = commands[0]
        val args = commands.drop(1).toTypedArray()
        device.shell(shell, *args).addListener { f ->
            if (f.cause() != null) {
                f.cause().printStackTrace()
            } else {
                Store.setOSVersion(f.now as String)
            }
        }
    }
}