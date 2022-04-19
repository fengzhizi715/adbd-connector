package cn.netdiscovery.adbd.ui

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.ui.ExecuteType
 * @author: Tony Shen
 * @date: 2022/4/19 7:49 下午
 * @version: V1.0 <描述当前版本功能>
 */
enum class ExecuteType(private val type:String) {

    CONNECT("connect"),
    SHELL("shell"),
    PULL("pull"),
    PUSH("push"),
    FORWARD("forward"),
    REVERSE("reverse");
}