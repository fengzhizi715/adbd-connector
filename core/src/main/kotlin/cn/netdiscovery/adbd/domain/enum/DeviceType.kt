package cn.netdiscovery.adbd.domain.enum

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.domain.enum.DeviceType
 * @author: Tony Shen
 * @date: 2021-06-02 13:58
 * @version: V1.0 <描述当前版本功能>
 */
enum class DeviceType(private val code: String) {

    //设备连接类型
    UNKNOWN("unknown"),
    DEVICE("device"),
    RECOVERY("recovery"),
    BOOTLOADER("bootloader"),
    SIDELOAD("sideload"),
    RESCUE("rescue");

    fun getCode() = code

    companion object {
        fun findByCode(code: String): DeviceType {
            for (state in values()) {
                if (state.getCode().equals(code, ignoreCase = true)) {
                    return state
                }
            }
            return UNKNOWN
        }
    }
}