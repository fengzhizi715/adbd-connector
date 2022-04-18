package cn.netdiscovery.adbd.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
import java.nio.charset.StandardCharsets
import java.security.interfaces.RSAPrivateCrtKey

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.ui.launch
 * @author: Tony Shen
 * @date: 2022/4/16 3:11 下午
 * @version: V1.0 <描述当前版本功能>
 */
val padding = 13.dp
val fontSize = 13.sp
const val previewWidth = 500

fun main() = application {

    val privateKey: RSAPrivateCrtKey
    val publicKey: ByteArray
    lateinit var device:SocketAdbDevice

    try {
        privateKey = AuthUtil.loadPrivateKey("adbkey")
        publicKey = AuthUtil.generatePublicKey(privateKey).toByteArray(StandardCharsets.UTF_8)
    } catch (cause: Throwable) {
        throw RuntimeException("load private key failed:" + cause.message, cause)
    }

    Window(
        icon = painterResource("image/ic_logo.ico"),
        onCloseRequest = { closeRequest() },
        title = "adbd-connector ui",
        resizable = false,
        state = rememberWindowState(width = Dp(previewWidth * 2.toFloat()), height = 600.dp)
    ) {
        MaterialTheme(colors = lightColors(primary = Color(0xFF999999))) {
            Column(Modifier.background(MaterialTheme.colors.surface).padding(padding)) {

                connectMessage { ip, port ->
                    try {
                        device = SocketAdbDevice(ip, port.toInt(), privateKey, publicKey)
                        device.addListener(object :DeviceListener{
                            override fun onConnected(device: AdbDevice) {
                                Store.setDeviceInfo("${device.model()} ${device.product()}")
                            }

                            override fun onDisconnected(device: AdbDevice) {
                            }
                        })
                    } catch (e:Exception) {
                        Store.device.deviceStatus.value = 2
                    }
                }

                commandMessage { shellCommand ->
                    val commands = shellCommand.trim().split("\\s+".toRegex())
                    val shell = commands[0]
                    val args = commands.drop(1).toTypedArray()

                    device.shell(shell,*args).addListener { f->
                        if (f.cause() != null) {
                            f.cause().printStackTrace()
                        } else {
                            println(f.now)
                        }
                    }

                    return@commandMessage
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