package cn.netdiscovery.adbd.utils.extension

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.utils.extension.`String+Extension`
 * @author: Tony Shen
 * @date: 2022/4/18 4:27 下午
 * @version: V1.0 <描述当前版本功能>
 */

/**
 * 校验是否为数字
 */
fun String.isNumeric(): Boolean {
    for (i in this.indices) {
        if (!Character.isDigit(this[i])) {
            return false
        }
    }
    return true
}