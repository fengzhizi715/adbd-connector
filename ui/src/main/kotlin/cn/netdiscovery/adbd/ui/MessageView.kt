package cn.netdiscovery.adbd.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.netdiscovery.adbd.utils.extension.isNumeric

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.ui.MessageView
 * @author: Tony Shen
 * @date: 2022/4/16 3:28 下午
 * @version: V1.0 <描述当前版本功能>
 */
@Composable
fun connectMessage(onClick: (ip:String, port:String) -> Unit) {

    Text("手机连接状态: ${Store.device.deviceStatus()}", Modifier.padding(top = 3.dp), fontSize = fontSize, fontWeight = FontWeight.Bold)

    SelectionContainer {

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 5.dp)) {

            Text("设备信息: ${Store.device.deviceInfo.value}", modifier = Modifier.width(250.dp), fontSize = fontSize)
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 5.dp)) {
        Text("手机 ip 地址:", modifier = Modifier.padding(end = 5.dp), fontSize = fontSize)
        customTextField(
            hint = "请输入手机 ip 地址",
            hintTextStyle = TextStyle(Color.Gray, fontSize = 12.sp),
            textFieldStyle = TextStyle(Color.Black, fontSize = 12.sp),
            text = Store.device.ipAddress,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            onTextChange = { this },
            modifier = Modifier
                .padding(end = 7.dp) //设置背景,对应背景来说，在它之前设置的padding 就相当于外边距
                .background(Color.LightGray.copy(alpha = 0.5f), shape = RoundedCornerShape(3.dp))
                .padding(end = 10.dp) //在设置size之前设置padding相当于外边距
                .size(200.dp, 25.dp),
        )

        Text("端口号:", modifier = Modifier.padding(end = 5.dp), fontSize = fontSize)
        customTextField(
            hint = "请输入端口号",
            hintTextStyle = TextStyle(Color.Gray, fontSize = 12.sp),
            textFieldStyle = TextStyle(Color.Black, fontSize = 12.sp),
            text = Store.device.port,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            onTextChange = { this.filter { it.toString().isNumeric() }  },
            modifier = Modifier
                .padding(end = 7.dp) //设置背景,对应背景来说，在它之前设置的padding 就相当于外边距
                .background(Color.LightGray.copy(alpha = 0.5f), shape = RoundedCornerShape(3.dp))
                .padding(end = 10.dp) //在设置size之前设置padding相当于外边距
                .size(100.dp, 25.dp),
        )

        button("连接", 100.dp, enableClick(ExecuteType.CONNECT)) {
            onClick.invoke(Store.device.ipAddress.value, Store.device.port.value)
        }
    }
}

@Composable
fun shellCommandMessage(onClick: (shellCommand:String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 5.dp)) {
        Text("adb shell 命令:", modifier = Modifier.padding(end = 5.dp), fontSize = fontSize)
        customTextField(
            hint = "请输入 adb shell 命令",
            hintTextStyle = TextStyle(Color.Gray, fontSize = 12.sp),
            textFieldStyle = TextStyle(Color.Black, fontSize = 12.sp),
            text = Store.device.shellCommand,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            onTextChange = { this },
            modifier = Modifier
                .padding(end = 7.dp) //设置背景,对应背景来说，在它之前设置的padding 就相当于外边距
                .background(Color.LightGray.copy(alpha = 0.5f), shape = RoundedCornerShape(3.dp))
                .padding(end = 10.dp) //在设置size之前设置padding相当于外边距
                .size(350.dp, 25.dp),
        )

        button("执行", 100.dp, enableClick(ExecuteType.SHELL)) {
            Store.addLog {
                LogItem(msg = "adb shell ${Store.device.shellCommand.value}")
            }
            onClick.invoke(Store.device.shellCommand.value)
        }
    }
}

@Composable
fun pullMessage(onClick: (src: String, dest: String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 5.dp)) {
        Text("adb pull 命令:", modifier = Modifier.padding(end = 5.dp), fontSize = fontSize)
        customTextField(
            hint = "手机路径",
            hintTextStyle = TextStyle(Color.Gray, fontSize = 12.sp),
            textFieldStyle = TextStyle(Color.Black, fontSize = 12.sp),
            text = Store.device.pullSrc,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            onTextChange = { this },
            modifier = Modifier
                .padding(end = 7.dp) //设置背景,对应背景来说，在它之前设置的padding 就相当于外边距
                .background(Color.LightGray.copy(alpha = 0.5f), shape = RoundedCornerShape(3.dp))
                .padding(end = 10.dp) //在设置size之前设置padding相当于外边距
                .size(200.dp, 25.dp),
        )

        customTextField(
            hint = "本机路径",
            hintTextStyle = TextStyle(Color.Gray, fontSize = 12.sp),
            textFieldStyle = TextStyle(Color.Black, fontSize = 12.sp),
            text = Store.device.pullDest,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            onTextChange = { this },
            modifier = Modifier
                .padding(end = 7.dp) //设置背景,对应背景来说，在它之前设置的padding 就相当于外边距
                .background(Color.LightGray.copy(alpha = 0.5f), shape = RoundedCornerShape(3.dp))
                .padding(end = 10.dp) //在设置size之前设置padding相当于外边距
                .size(200.dp, 25.dp),
        )

        button("执行", 100.dp, enableClick(ExecuteType.PULL)) {
            Store.addLog {
                LogItem(msg = "adb pull ${Store.device.pullSrc.value} ${Store.device.pullDest.value}")
            }
            onClick.invoke(Store.device.pullSrc.value, Store.device.pullDest.value)
        }
    }
}

@Composable
fun pushMessage(onClick: (src: String, dest: String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 5.dp)) {
        Text("adb push 命令:", modifier = Modifier.padding(end = 5.dp), fontSize = fontSize)
        customTextField(
            hint = "本机路径",
            hintTextStyle = TextStyle(Color.Gray, fontSize = 12.sp),
            textFieldStyle = TextStyle(Color.Black, fontSize = 12.sp),
            text = Store.device.pushSrc,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            onTextChange = { this },
            modifier = Modifier
                .padding(end = 7.dp) //设置背景,对应背景来说，在它之前设置的padding 就相当于外边距
                .background(Color.LightGray.copy(alpha = 0.5f), shape = RoundedCornerShape(3.dp))
                .padding(end = 10.dp) //在设置size之前设置padding相当于外边距
                .size(200.dp, 25.dp),
        )

        customTextField(
            hint = "手机路径",
            hintTextStyle = TextStyle(Color.Gray, fontSize = 12.sp),
            textFieldStyle = TextStyle(Color.Black, fontSize = 12.sp),
            text = Store.device.pushDest,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            onTextChange = { this },
            modifier = Modifier
                .padding(end = 7.dp) //设置背景,对应背景来说，在它之前设置的padding 就相当于外边距
                .background(Color.LightGray.copy(alpha = 0.5f), shape = RoundedCornerShape(3.dp))
                .padding(end = 10.dp) //在设置size之前设置padding相当于外边距
                .size(200.dp, 25.dp),
        )

        button("执行", 100.dp, enableClick(ExecuteType.PUSH)) {
            Store.addLog {
                LogItem(msg = "adb push ${Store.device.pushSrc.value} ${Store.device.pushDest.value}")
            }
            onClick.invoke(Store.device.pushSrc.value, Store.device.pushDest.value)
        }
    }
}

@Composable
fun installMessage(onClick: (installCommand:String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 5.dp)) {
        Text("adb install 命令:", modifier = Modifier.padding(end = 5.dp), fontSize = fontSize)
        customTextField(
            hint = "请输入 adb install 命令",
            hintTextStyle = TextStyle(Color.Gray, fontSize = 12.sp),
            textFieldStyle = TextStyle(Color.Black, fontSize = 12.sp),
            text = Store.device.installCommand,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            onTextChange = { this },
            modifier = Modifier
                .padding(end = 7.dp) //设置背景,对应背景来说，在它之前设置的padding 就相当于外边距
                .background(Color.LightGray.copy(alpha = 0.5f), shape = RoundedCornerShape(3.dp))
                .padding(end = 10.dp) //在设置size之前设置padding相当于外边距
                .size(350.dp, 25.dp),
        )

        button("执行", 100.dp, enableClick(ExecuteType.SHELL)) {
            Store.addLog {
                LogItem(msg = "adb install ${Store.device.installCommand.value}")
            }
            onClick.invoke(Store.device.installCommand.value)
        }
    }
}

@Composable
fun uninstallMessage(onClick: (uninstallCommand:String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 5.dp)) {
        Text("adb uninstall 命令:", modifier = Modifier.padding(end = 5.dp), fontSize = fontSize)
        customTextField(
            hint = "请输入 adb uninstall 命令",
            hintTextStyle = TextStyle(Color.Gray, fontSize = 12.sp),
            textFieldStyle = TextStyle(Color.Black, fontSize = 12.sp),
            text = Store.device.uninstallCommand,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            onTextChange = { this },
            modifier = Modifier
                .padding(end = 7.dp) //设置背景,对应背景来说，在它之前设置的padding 就相当于外边距
                .background(Color.LightGray.copy(alpha = 0.5f), shape = RoundedCornerShape(3.dp))
                .padding(end = 10.dp) //在设置size之前设置padding相当于外边距
                .size(350.dp, 25.dp),
        )

        button("执行", 100.dp, enableClick(ExecuteType.SHELL)) {
            Store.addLog {
                LogItem(msg = "adb uninstall ${Store.device.uninstallCommand.value}")
            }
            onClick.invoke(Store.device.uninstallCommand.value)
        }
    }
}

@Composable
fun forwardMessage(onClick: (local:String,remote:String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 5.dp)) {
        Text("adb forward 命令:", modifier = Modifier.padding(end = 5.dp), fontSize = fontSize)
        customTextField(
            hint = "local",
            hintTextStyle = TextStyle(Color.Gray, fontSize = 12.sp),
            textFieldStyle = TextStyle(Color.Black, fontSize = 12.sp),
            text = Store.device.forwardLocal,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            onTextChange = { this },
            modifier = Modifier
                .padding(end = 7.dp) //设置背景,对应背景来说，在它之前设置的padding 就相当于外边距
                .background(Color.LightGray.copy(alpha = 0.5f), shape = RoundedCornerShape(3.dp))
                .padding(end = 10.dp) //在设置size之前设置padding相当于外边距
                .size(200.dp, 25.dp),
        )

        customTextField(
            hint = "remote",
            hintTextStyle = TextStyle(Color.Gray, fontSize = 12.sp),
            textFieldStyle = TextStyle(Color.Black, fontSize = 12.sp),
            text = Store.device.forwardRemote,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            onTextChange = { this },
            modifier = Modifier
                .padding(end = 7.dp) //设置背景,对应背景来说，在它之前设置的padding 就相当于外边距
                .background(Color.LightGray.copy(alpha = 0.5f), shape = RoundedCornerShape(3.dp))
                .padding(end = 10.dp) //在设置size之前设置padding相当于外边距
                .size(200.dp, 25.dp),
        )

        button("执行", 100.dp, enableClick(ExecuteType.FORWARD)) {
//            Store.addLog {
//                LogItem(msg = "adb push ${Store.device.pushSrc.value} ${Store.device.pushDest.value}")
//            }
            onClick.invoke(Store.device.forwardLocal.value, Store.device.forwardRemote.value)
        }
    }
}

@Composable
fun reverseMessage(onClick: (remote:String,local:String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 5.dp)) {
        Text("adb reverse 命令:", modifier = Modifier.padding(end = 5.dp), fontSize = fontSize)
        customTextField(
            hint = "remote",
            hintTextStyle = TextStyle(Color.Gray, fontSize = 12.sp),
            textFieldStyle = TextStyle(Color.Black, fontSize = 12.sp),
            text = Store.device.reverseRemote,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            onTextChange = { this },
            modifier = Modifier
                .padding(end = 7.dp) //设置背景,对应背景来说，在它之前设置的padding 就相当于外边距
                .background(Color.LightGray.copy(alpha = 0.5f), shape = RoundedCornerShape(3.dp))
                .padding(end = 10.dp) //在设置size之前设置padding相当于外边距
                .size(200.dp, 25.dp),
        )

        customTextField(
            hint = "local",
            hintTextStyle = TextStyle(Color.Gray, fontSize = 12.sp),
            textFieldStyle = TextStyle(Color.Black, fontSize = 12.sp),
            text = Store.device.reverseLocal,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            onTextChange = { this },
            modifier = Modifier
                .padding(end = 7.dp) //设置背景,对应背景来说，在它之前设置的padding 就相当于外边距
                .background(Color.LightGray.copy(alpha = 0.5f), shape = RoundedCornerShape(3.dp))
                .padding(end = 10.dp) //在设置size之前设置padding相当于外边距
                .size(200.dp, 25.dp),
        )

        button("执行", 100.dp, enableClick(ExecuteType.REVERSE)) {
//            Store.addLog {
//                LogItem(msg = "adb push ${Store.device.pushSrc.value} ${Store.device.pushDest.value}")
//            }
            onClick.invoke(Store.device.reverseRemote.value, Store.device.reverseLocal.value)
        }
    }
}

/**
 * 自定义输入框,TextField 不可修改高度
 */
@Composable
fun customTextField(
    modifier: Modifier = Modifier,
    text : MutableState<String>,
    hint: String? = null,
    showCleanIcon: Boolean = true,
    onTextChange: String.() -> String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: String.() -> Unit = {},
    textFieldStyle: TextStyle = TextStyle.Default,
    hintTextStyle: TextStyle = TextStyle.Default
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        leadingIcon?.invoke()
        BasicTextField(
            value = onTextChange.invoke(text.value),
            onValueChange = { text.value = onTextChange.invoke(it)},
            cursorBrush = SolidColor(Color.Gray),
            singleLine = true,
            modifier = Modifier.weight(1f).padding(start = 10.dp),
            textStyle = textFieldStyle,
            decorationBox = { innerTextField ->
                if (text.value.isBlank() && !hint.isNullOrBlank()) {
                    Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.CenterStart) {
                        innerTextField()
                        Text(hint, modifier = Modifier.fillMaxWidth(), style = hintTextStyle)
                    }
                } else innerTextField()
            },
            keyboardActions = KeyboardActions { keyboardActions(text.value) },
            keyboardOptions = keyboardOptions
        )
        trailingIcon?.invoke()
        if (showCleanIcon) {
            Image(
                painter = painterResource("image/ic_close.png"),
                contentDescription = "",
                modifier = Modifier.size(10.dp).clickable(onClick = { text.value = "" }),
            )
        }
    }
}

@Composable
fun button(text: String, width: Dp = 40.dp, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        modifier = Modifier.size(width, 25.dp),
        shape = RoundedCornerShape(10),
        contentPadding = PaddingValues(vertical = 1.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color(0xFFDDDDDD),
            disabledBackgroundColor = Color(0xFFf2f2f2).copy(alpha = 0.7f)
        ),
        onClick = onClick,
    ) {
        Text(text, fontSize = 12.sp, color = Color.Black)
    }
}

private fun enableClick(type:ExecuteType):Boolean {

    return when(type) {
        ExecuteType.CONNECT -> Store.device.ipAddress.value.isNotEmpty() && Store.device.port.value.isNotEmpty()

        ExecuteType.SHELL -> Store.device.shellCommand.value.isNotEmpty()

        ExecuteType.PULL -> Store.device.pullSrc.value.isNotEmpty() && Store.device.pullDest.value.isNotEmpty()

        ExecuteType.PUSH -> Store.device.pushSrc.value.isNotEmpty() && Store.device.pushDest.value.isNotEmpty()

        ExecuteType.FORWARD -> Store.device.forwardLocal.value.isNotEmpty() && Store.device.forwardRemote.value.isNotEmpty()

        ExecuteType.REVERSE -> Store.device.reverseLocal.value.isNotEmpty() && Store.device.reverseRemote.value.isNotEmpty()
    }
}

@Composable
fun textButton(text: String, width: Dp = 40.dp, onClick: () -> Unit) {
    TextButton(
        modifier = Modifier.size(width, 30.dp),
        shape = RoundedCornerShape(50),
        contentPadding = PaddingValues(vertical = 3.dp),
        onClick = onClick,
    ) {
        Text(text, fontSize = 12.sp, color = Color.Black)
    }
}

@Composable
fun messageList() {
    Column(Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween,) {
            Text("运行日志:", fontSize = fontSize)
            textButton("清空") {
                Store.clearLog()
            }
        }

        val messages = remember {
            Store.logs
        }

        val state = rememberLazyListState()

        Box(Modifier.fillMaxSize().border(1.dp, color = Color.Gray)) {
            SelectionContainer {
                LazyColumn(Modifier.padding(10.dp), state, verticalArrangement = Arrangement.Center) {
                    itemsIndexed(messages) { index, item ->
                        Text(item.getMessage(), fontSize = 12.sp, color = Color.Black)
                        Spacer(modifier = Modifier.height(5.dp))
                    }
                }
            }
            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(scrollState = state)
            )
        }
    }
}