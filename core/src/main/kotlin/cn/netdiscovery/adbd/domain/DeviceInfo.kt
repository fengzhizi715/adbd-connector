package cn.netdiscovery.adbd.domain

import cn.netdiscovery.adbd.domain.enum.DeviceType
import cn.netdiscovery.adbd.domain.enum.Feature

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.domain.DeviceInfo
 * @author: Tony Shen
 * @date: 2022/4/3 4:37 下午
 * @version: V1.0 <描述当前版本功能>
 */
data class DeviceInfo(
    val type: DeviceType,
    val model: String,
    val product: String,
    val device: String,
    val features: Set<Feature>
)
