package cn.netdiscovery.adbd.domain

import cn.netdiscovery.adbd.domain.enum.DeviceType
import cn.netdiscovery.adbd.domain.enum.Feature

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.domain.ConnectResult
 * @author: Tony Shen
 * @date: 2022/4/1 8:39 下午
 * @version: V1.0 <描述当前版本功能>
 */
data class ConnectResult(
    private var type: DeviceType? = null,
    private var model: String? = null,
    private var product: String? = null,
    private var device: String? = null,
    private var features: Set<Feature>? = null
)
