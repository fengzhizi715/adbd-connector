package cn.netdiscovery.adbd.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.lightColors
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import cn.netdiscovery.adbd.device.AdbDevice
import cn.netdiscovery.adbd.device.DeviceListener
import cn.netdiscovery.adbd.device.SocketAdbDevice
import cn.netdiscovery.adbd.utils.AuthUtil
import cn.netdiscovery.adbd.utils.extension.executeADBShell
import cn.netdiscovery.rxjava.extension.safeDispose
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.runBlocking
import java.awt.image.BufferedImage
import java.io.File
import java.nio.charset.StandardCharsets
import java.security.interfaces.RSAPrivateCrtKey

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.ui.Launch
 * @author: Tony Shen
 * @date: 2022/4/16 3:11 下午
 * @version: V1.0 <描述当前版本功能>
 */
val padding = 13.dp
val fontSize = 13.sp
val titleFrontSize = 20.sp
const val previewWidth = 600

var device: SocketAdbDevice? = null
var disposable: Disposable? = null

fun main() = application {

    val privateKey: RSAPrivateCrtKey
    val publicKey: ByteArray

    try {
        privateKey = AuthUtil.loadPrivateKey("adbkey")
        publicKey = AuthUtil.generatePublicKey(privateKey).toByteArray(StandardCharsets.UTF_8)
    } catch (cause: Throwable) {
        throw RuntimeException("load private key failed:" + cause.message, cause)
    }

    Window(
        icon = painterResource("image/ic_logo.ico"),
        onCloseRequest = { closeRequest() },
        title = "adbd-connector v1.0",
        resizable = false,
        state = rememberWindowState(width = Dp(previewWidth * 2.toFloat()), height = 900.dp)
    ) {
        MaterialTheme(colors = lightColors(primary = Color(0xFF999999))) {
            Column(Modifier.background(MaterialTheme.colors.surface).padding(padding)) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("adbd-connector", modifier = Modifier.padding(end = 5.dp), fontSize = titleFrontSize)
                }

                Row (modifier = Modifier.padding(top = 5.dp)){
                    Column(Modifier.background(MaterialTheme.colors.surface)) {
                        connectMessage { ip, port ->
                            try {
                                device = SocketAdbDevice(ip, port.toInt(), privateKey, publicKey).apply {
                                    addListener(object : DeviceListener {
                                        override fun onConnected(device: AdbDevice) {
                                            Store.changeConnectStatus(1)

                                            Store.addLog {
                                                LogItem("[${device.serial()}] device connected")
                                            }

                                            GetPhoneInfoTask.execute(device)
                                            disposable = GetPhoneInfoTask.displayScreenShot(device)
                                        }

                                        override fun onDisconnected(device: AdbDevice) {
                                            Store.changeConnectStatus(2)
                                            Store.addLog {
                                                LogItem("[${device.serial()}] device disconnected")
                                            }
                                            dispose()
                                        }
                                    })
                                }
                            } catch (e: Exception) {
                                Store.changeConnectStatus(2)
                                Store.addLog {
                                    LogItem("[${ip}:${port}] device disconnected")
                                }
                                dispose()
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 10.dp)) {
                            Text("shell 相关", modifier = Modifier.padding(end = 5.dp), fontSize = titleFrontSize)
                        }

                        shellCommandMessage { shellCommand ->

                            device.wrapLet {
                                it.executeADBShell(shellCommand) { f->
                                    Store.addLog {
                                        LogItem(f.now as String)
                                    }
                                }
                            }

                            return@shellCommandMessage
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 10.dp)) {
                            Text("文件传送", modifier = Modifier.padding(end = 5.dp), fontSize = titleFrontSize)
                        }

                        pullMessage { src, dest ->

                            device.wrapLet {
                                it.pull(src, File(dest)).addListener { f ->
                                    if (f.cause() != null) {
                                        f.cause().printStackTrace()
                                        Store.addLog {
                                            LogItem("adb pull $src $dest error")
                                        }
                                    } else {
                                        Store.addLog {
                                            LogItem("adb pull $src $dest done")
                                        }
                                    }
                                }
                            }
                        }

                        pushMessage { src, dest ->

                            device.wrapLet {

                                it.push(File(src), dest).addListener { f ->
                                    if (f.cause() != null) {
                                        f.cause().printStackTrace()
                                        Store.addLog {
                                            LogItem("adb push $src $dest error")
                                        }
                                    } else {
                                        Store.addLog {
                                            LogItem("adb push $src $dest done")
                                        }
                                    }
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 10.dp)) {
                            Text("App 安装", modifier = Modifier.padding(end = 5.dp), fontSize = titleFrontSize)
                        }

                        installMessage { installCommand ->
                            device.wrapLet {
                                it.executeADBShell(installCommand) { f->
                                    Store.addLog {
                                        LogItem(f.now as String)
                                    }
                                }
                            }
                        }

                        uninstallMessage { uninstallCommand ->
                            device.wrapLet {
                                it.executeADBShell(uninstallCommand) { f->
                                    Store.addLog {
                                        LogItem(f.now as String)
                                    }
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 10.dp)) {
                            Text("网络相关", modifier = Modifier.padding(end = 5.dp), fontSize = titleFrontSize)
                        }

                        forwardMessage { remote, localPort ->
                            device.wrapLet {

                                it.forward(remote, localPort.toInt()).addListener { f ->
                                    if (f.cause() != null) {
                                        f.cause().printStackTrace()
                                        Store.addLog {
                                            LogItem("adb forward tcp:$localPort $remote error")
                                        }
                                    } else {
                                        Store.addLog {
                                            LogItem("adb forward tcp:$localPort $remote done")
                                        }
                                    }
                                }
                            }
                        }

                        reverseMessage { remote, local ->
                            device.wrapLet {

                                it.reverse(remote, local).addListener { f ->
                                    if (f.cause() != null) {
                                        f.cause().printStackTrace()
                                        Store.addLog {
                                            LogItem("adb reverse $remote $local error")
                                        }
                                    } else {
                                        Store.addLog {
                                            LogItem("adb reverse $remote $local done")
                                        }
                                    }
                                }
                            }
                        }

                    }

                    Column(modifier = Modifier.absolutePadding(left = 10.dp), verticalArrangement = Arrangement.Top) {
                        Text("手机连接状态: ${Store.device.deviceStatus()}", Modifier.padding(10.dp), fontSize = fontSize, fontWeight = FontWeight.Bold)

                        SelectionContainer {

                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(start = 10.dp, top = 5.dp)) {

                                Text("设备名称: ${Store.device.deviceName.value}", modifier = Modifier.width(250.dp), fontSize = fontSize)

                                Text("设备型号: ${Store.device.deviceType.value}", modifier = Modifier.width(250.dp), fontSize = fontSize)

                                Text("品牌: ${Store.device.brand.value}", modifier = Modifier.width(250.dp), fontSize = fontSize)

                                Text("制造商: ${Store.device.manufacturer.value}", modifier = Modifier.width(250.dp), fontSize = fontSize)

                                Text("Android OS 版本: ${Store.device.os.value}", modifier = Modifier.width(250.dp), fontSize = fontSize)

                                Text("CPU 架构: ${Store.device.cpuArch.value}", modifier = Modifier.width(250.dp), fontSize = fontSize)

                                Text("CPU 数目: ${Store.device.cpuNum.value}", modifier = Modifier.width(250.dp), fontSize = fontSize)

                                Text("分辨率: ${Store.device.physicalSize.value}", modifier = Modifier.width(250.dp), fontSize = fontSize)

                                Text("内存大小: ${Store.device.memTotal.value}", modifier = Modifier.width(250.dp), fontSize = fontSize)
                            }
                        }
                    }

                    Column(modifier = Modifier.absolutePadding(right = 10.dp), verticalArrangement = Arrangement.Top) {

                        if (Store.device.bufferedImage.value!=null) {
                            Image(
                                bitmap = loadLocalImage(Store.device.bufferedImage.value!!),
                                contentDescription = "",
                                modifier = Modifier.height(400.dp).width(300.dp)
                            )
                        }
                    }
                }

                Row {
                    messageList()
                }
            }
        }
    }
}

private fun loadLocalImage(value: BufferedImage): ImageBitmap {
    return value.toComposeImageBitmap()
}

fun dispose() {
    Store.device.clear()
    disposable.safeDispose()
    device?.close()
    device = null
}

fun ApplicationScope.closeRequest() = runBlocking {
    exitApplication()
}