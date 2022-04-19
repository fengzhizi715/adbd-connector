package cn.netdiscovery.adbd.utils

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.utils.TimeUtils
 * @author: Tony Shen
 * @date: 2021-04-01 21:16
 * @version: V1.0 <描述当前版本功能>
 */
val yyyyMMddHHmmssSSS = "yyyy/MM/dd HH:mm:ss.SSS"

val formatter: DateTimeFormatter by lazy {
    DateTimeFormatter.ofPattern(yyyyMMddHHmmssSSS).withZone(ZoneId.systemDefault())
}

/**
 * 生成当前日志的时间，推送给上位机在 UI 上展示
 */
fun currentLogTime() = ZonedDateTime.now().format(formatter)