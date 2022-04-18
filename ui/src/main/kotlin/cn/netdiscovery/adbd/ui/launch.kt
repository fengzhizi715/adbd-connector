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
import kotlinx.coroutines.runBlocking

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
                    
                }

                commandMessage {

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