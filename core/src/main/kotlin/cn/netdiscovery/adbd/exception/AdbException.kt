package cn.netdiscovery.adbd.exception

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.exception.AdbException
 * @author: Tony Shen
 * @date: 2022/4/2 5:18 下午
 * @version: V1.0 <描述当前版本功能>
 */
open class AdbException : Exception {

    constructor()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}