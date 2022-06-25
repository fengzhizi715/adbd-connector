package cn.netdiscovery.adbd.utils.extension

import cn.netdiscovery.adbd.device.AdbDevice
import io.netty.util.concurrent.Future
import java.io.File

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.utils.extension.`AdbDevice+Extension`
 * @author: Tony Shen
 * @date: 2022/6/25 4:21 下午
 * @version: V1.0 <描述当前版本功能>
 */

/**
 * 执行 adb shell 相关的命令
 * @param shellCommand adb shell 的命令，可以省略输入"adb shell"，只需后面的命令
 * @param block        命令执行成功后的回调
 */
inline fun AdbDevice.executeADBShell(shellCommand:String, noinline block:(f: Future<String>)->Unit) {
    val commands = shellCommand.trim().split("\\s+".toRegex())
    val shell = commands[0]
    val args = commands.drop(1).toTypedArray()
    this.shell(shell, *args).addListener { f ->
        if (f.cause() != null) {
            f.cause().printStackTrace()
        } else {
            block.invoke(f as Future<String>)
        }
    }
}

/**
 * 执行 adb pull 相关的命令
 * @param src    手机路径的文件
 * @param dest   PC 本机路径的文件
 * @param block  命令执行成功后的回调
 */
inline fun AdbDevice.executeADBPull(src: String, dest: File, noinline block:(f: Future<String>)->Unit) {

    this.pull(src, dest).addListener { f ->
        if (f.cause() != null) {
            f.cause().printStackTrace()
        } else {
            block.invoke(f as Future<String>)
        }
    }
}