package cn.netdiscovery.adbd.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.lightColors
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.runBlocking
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

fun main() = application {

    val privateKey: RSAPrivateCrtKey
    val publicKey: ByteArray
    var device: SocketAdbDevice? = null

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

                Row {
                    Column(Modifier.background(MaterialTheme.colors.surface)) {
                        connectMessage { ip, port ->
                            try {
                                device = SocketAdbDevice(ip, port.toInt(), privateKey, publicKey)
                                device?.addListener(object : DeviceListener {
                                    override fun onConnected(device: AdbDevice) {
//                                        Store.setDeviceInfo("${device.device()} ${device.product()}")
                                        Store.changeConnectStatus(1)

                                        Store.addLog {
                                            LogItem("[${device.serial()}] device connected")
                                        }

                                        GetPhoneInfoTask.execute(device)
                                    }

                                    override fun onDisconnected(device: AdbDevice) {
                                        Store.changeConnectStatus(2)
                                        Store.addLog {
                                            LogItem("[${device.serial()}] device disconnected")
                                        }
                                    }
                                })
                            } catch (e: Exception) {
                                Store.changeConnectStatus(2)
                                Store.addLog {
                                    LogItem("[${ip}:${port}] device disconnected")
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 10.dp)) {
                            Text("shell 相关", modifier = Modifier.padding(end = 5.dp), fontSize = titleFrontSize)
                        }

                        shellCommandMessage { shellCommand ->

                            device.wrapLet {
                                val commands = shellCommand.trim().split("\\s+".toRegex())
                                val shell = commands[0]
                                val args = commands.drop(1).toTypedArray()

                                it.shell(shell, *args).addListener { f ->
                                    if (f.cause() != null) {
                                        f.cause().printStackTrace()
                                    } else {
                                        Store.addLog {
                                            LogItem(f.now as String)
                                        }
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

                        }

                        uninstallMessage { uninstallCommand ->

                        }

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 10.dp)) {
                            Text("网络相关", modifier = Modifier.padding(end = 5.dp), fontSize = titleFrontSize)
                        }

                        forwardMessage { local, remote ->

                        }

                        reverseMessage { remote, local ->

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
                            }
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

fun ApplicationScope.closeRequest() = runBlocking {
    exitApplication()
}