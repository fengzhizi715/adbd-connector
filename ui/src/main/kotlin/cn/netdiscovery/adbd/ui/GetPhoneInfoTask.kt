package cn.netdiscovery.adbd.ui

import cn.netdiscovery.adbd.device.AdbDevice
import cn.netdiscovery.rxjava.refresh
import io.reactivex.rxjava3.disposables.Disposable
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.math.ceil

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
        getBrand(device)
        getManufacturer(device)
        getOSVersion(device)
        getCPUArchVersion(device)
        getCPUNum(device)
        getPhysicalSize(device)
        getMemTotal(device)
    }

    fun displayScreenShot(device: AdbDevice): Disposable {
        return refresh(0, 1, TimeUnit.SECONDS, func = {

            val src = "/sdcard/screenshot.png"
            val dest = File("/Users/tony/screenshot.png")
            val shellCommand = "/system/bin/screencap -p $src"
            val commands = shellCommand.trim().split("\\s+".toRegex())
            val shell = commands[0]
            val args = commands.drop(1).toTypedArray()
            device.shell(shell, *args).addListener { f ->
                if (f.cause() != null) {
                    f.cause().printStackTrace()
                } else {
                    device.pull(src, dest).addListener {
                        if (f.cause() != null) {
                            f.cause().printStackTrace()
                        } else {
                            Store.setScreenShot(dest.absolutePath)
                        }
                    }
                }
            }
        })
    }

    private fun getDeviceName(device: AdbDevice) {
        val shellCommand = "getprop ro.product.device"
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

    private fun getBrand(device: AdbDevice) {
        val shellCommand = "getprop ro.product.brand"
        val commands = shellCommand.trim().split("\\s+".toRegex())
        val shell = commands[0]
        val args = commands.drop(1).toTypedArray()
        device.shell(shell, *args).addListener { f ->
            if (f.cause() != null) {
                f.cause().printStackTrace()
            } else {
                Store.setBrand(f.now.toString().trim())
            }
        }
    }

    private fun getManufacturer(device: AdbDevice) {
        val shellCommand = "getprop ro.product.manufacturer"
        val commands = shellCommand.trim().split("\\s+".toRegex())
        val shell = commands[0]
        val args = commands.drop(1).toTypedArray()
        device.shell(shell, *args).addListener { f ->
            if (f.cause() != null) {
                f.cause().printStackTrace()
            } else {
                Store.setManufacturer(f.now.toString().trim())
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

    private fun getCPUNum(device: AdbDevice) {
        val shellCommand = "cat /proc/cpuinfo | grep processor"
        val commands = shellCommand.trim().split("\\s+".toRegex())
        val shell = commands[0]
        val args = commands.drop(1).toTypedArray()
        device.shell(shell, *args).addListener { f ->
            if (f.cause() != null) {
                f.cause().printStackTrace()
            } else {
                val args = f.now.toString().trim().split("\n")
                Store.setCpuNum("${args.size}")
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

    private fun getMemTotal(device: AdbDevice) {
        val shellCommand = "cat /proc/meminfo | grep MemTotal"
        val commands = shellCommand.trim().split("\\s+".toRegex())
        val shell = commands[0]
        val args = commands.drop(1).toTypedArray()
        device.shell(shell, *args).addListener { f ->
            if (f.cause() != null) {
                f.cause().printStackTrace()
            } else {
                val total = f.now.toString().trim().replace("MemTotal:","").trim().replace("kB","").toDouble()
                val result = ceil(total/1024/1024)
                Store.setMemTotal("$result GB")
            }
        }
    }
}