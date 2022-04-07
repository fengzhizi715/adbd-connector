package cn.netdiscovery.adbd.utils

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.utils.ShellUtils
 * @author: Tony Shen
 * @date: 2022/4/7 4:39 下午
 * @version: V1.0 <描述当前版本功能>
 */
private fun quote(s: String): String? {
    return if (s.matches("\\S+".toRegex())) {
        s
    } else "'" + s.replace("'", "'\\''") + "'"
}

private fun buildCmdLine(cmd: String, vararg args: String): StringBuilder? {
    val cmdLine = StringBuilder(cmd)
    for (arg in args) {
        cmdLine.append(" ")
        cmdLine.append(quote(arg))
    }
    return cmdLine
}

fun buildShellCmd(cmd: String?, vararg args: String): String {
    val sb = StringBuilder()
    sb.append("shell:")
    if (cmd != null) {
        sb.append(buildCmdLine(cmd, *args))
    }
    sb.append("\u0000")
    return sb.toString()
}