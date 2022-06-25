package cn.netdiscovery.adbd.ui

import cn.netdiscovery.adbd.device.AdbDevice
import cn.netdiscovery.rxjava.refresh
import io.netty.util.concurrent.Future
import io.reactivex.rxjava3.disposables.Disposable
import java.io.File
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
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

    private val dirPath: String by lazy {
        File("").absolutePath + File.separator + "screenshot"
    }

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

        val src = "/sdcard/screenshot.png"
        val dir = File("$dirPath")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val dest = File("$dirPath/screenshot.png")

        return refresh(0, 1, TimeUnit.SECONDS, func = {
            val shellCommand = "/system/bin/screencap -p $src"
            executeADBShell(device,shellCommand) { f->
                device.pull(src, dest).addListener {
                    if (f.cause() != null) {
                        f.cause().printStackTrace()
                    } else {
                        dest.inputStream()?.let {
                            Store.setBufferedImage(ImageIO.read(it))
                        }
                    }
                }
            }
        })
    }

    private fun getDeviceName(device: AdbDevice) {
        val shellCommand = "getprop ro.product.device"
        executeADBShell(device,shellCommand) { f->
            Store.setDeviceName(f.now.toString().trim())
        }
    }

    private fun getDeviceType(device: AdbDevice) {
        val shellCommand = "getprop ro.product.model"
        executeADBShell(device,shellCommand) { f->
            Store.setDeviceType(f.now.toString().trim())
        }
    }

    private fun getBrand(device: AdbDevice) {
        val shellCommand = "getprop ro.product.brand"
        executeADBShell(device,shellCommand) { f->
            Store.setBrand(f.now.toString().trim())
        }
    }

    private fun getManufacturer(device: AdbDevice) {
        val shellCommand = "getprop ro.product.manufacturer"
        executeADBShell(device,shellCommand) { f->
            Store.setManufacturer(f.now.toString().trim())
        }
    }

    private fun getOSVersion(device: AdbDevice) {
        val shellCommand = "getprop ro.build.version.release"
        executeADBShell(device,shellCommand) { f->
            Store.setOSVersion(f.now.toString().trim())
        }
    }

    private fun getCPUArchVersion(device: AdbDevice) {
        val shellCommand = "getprop ro.product.cpu.abi"
        executeADBShell(device,shellCommand) { f->
            Store.setCpuArch(f.now.toString().trim())
        }
    }

    private fun getCPUNum(device: AdbDevice) {
        val shellCommand = "cat /proc/cpuinfo | grep processor"
        executeADBShell(device,shellCommand) { f ->
            val args = f.now.toString().trim().split("\n")
            Store.setCpuNum("${args.size}")
        }
    }

    private fun getPhysicalSize(device: AdbDevice) {
        val shellCommand = "wm size"
        executeADBShell(device,shellCommand) { f ->
            Store.setPhysicalSize(f.now.toString().trim().replace("Physical size:",""))
        }
    }

    private fun getMemTotal(device: AdbDevice) {
        val shellCommand = "cat /proc/meminfo | grep MemTotal"
        executeADBShell(device,shellCommand) { f ->
            val total = f.now.toString().trim().replace("MemTotal:","").trim().replace("kB","").toDouble()
            val result = ceil(total/1024/1024)
            Store.setMemTotal("$result GB")
        }
    }

    private inline fun executeADBShell(device: AdbDevice, shellCommand:String, noinline block:(f: Future<String>)->Unit) {
        val commands = shellCommand.trim().split("\\s+".toRegex())
        val shell = commands[0]
        val args = commands.drop(1).toTypedArray()
        device.shell(shell, *args).addListener { f ->
            if (f.cause() != null) {
                f.cause().printStackTrace()
            } else {
                block.invoke(f as Future<String>)
            }
        }
    }
}