package cn.netdiscovery.adbd.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.utils.LogUtils
 * @author: Tony Shen
 * @date: 2022/4/13 5:09 下午
 * @version: V1.0 <描述当前版本功能>
 */
inline fun <reified T> logger(): Logger = LoggerFactory.getLogger(T::class.java)

fun logInfo(logger: Logger,tag:String = "<adbd-connector> -->", msg:()->String) {
    logInfo(logger,tag,msg.invoke())
}

fun logInfo(logger: Logger,tag:String = "<adbd-connector> -->", msg: String) {
    logger.info("$tag $msg")
}