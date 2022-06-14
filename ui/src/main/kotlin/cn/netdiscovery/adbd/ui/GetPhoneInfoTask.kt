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

        getDeviceName(device)
        getDeviceType(device)
        getOSVersion(device)
        getCPUArchVersion(device)
        getPhysicalSize(device)
    }

    private fun getDeviceName(device: AdbDevice) {
        val shellCommand = "getprop ro.product.system.model"
        val commands = shellCommand.trim().split("\\s+".toRegex())
        val shell = commands[0]
        val args = commands.drop(1).toTypedArray()
        device.shell(shell, *args).addListener { f ->
            if (f.cause() != null) {
                f.cause().printStackTrace()
            } else {
                Store.setDeviceName(f.now.toString().trim())
            }
        }
    }

    private fun getDeviceType(device: AdbDevice) {
        val shellCommand = "getprop ro.product.model"
        val commands = shellCommand.trim().split("\\s+".toRegex())
        val shell = commands[0]
        val args = commands.drop(1).toTypedArray()
        device.shell(shell, *args).addListener { f ->
            if (f.cause() != null) {
                f.cause().printStackTrace()
            } else {
                Store.setDeviceType(f.now.toString().trim())
            }
        }
    }

    private fun getOSVersion(device: AdbDevice) {
        val shellCommand = "getprop ro.build.version.release"
        val commands = shellCommand.trim().split("\\s+".toRegex())
        val shell = commands[0]
        val args = commands.drop(1).toTypedArray()
        device.shell(shell, *args).addListener { f ->
            if (f.cause() != null) {
                f.cause().printStackTrace()
            } else {
                Store.setOSVersion(f.now.toString().trim())
            }
        }
    }

    private fun getCPUArchVersion(device: AdbDevice) {
        val shellCommand = "getprop ro.product.cpu.abi"
        val commands = shellCommand.trim().split("\\s+".toRegex())
        val shell = commands[0]
        val args = commands.drop(1).toTypedArray()
        device.shell(shell, *args).addListener { f ->
            if (f.cause() != null) {
                f.cause().printStackTrace()
            } else {
                Store.setCpuArch(f.now.toString().trim())
            }
        }
    }

    private fun getPhysicalSize(device: AdbDevice) {
        val shellCommand = "wm size"
        val commands = shellCommand.trim().split("\\s+".toRegex())
        val shell = commands[0]
        val args = commands.drop(1).toTypedArray()
        device.shell(shell, *args).addListener { f ->
            if (f.cause() != null) {
                f.cause().printStackTrace()
            } else {
                Store.setPhysicalSize(f.now.toString().trim().replace("Physical size:",""))
            }
        }
    }
}