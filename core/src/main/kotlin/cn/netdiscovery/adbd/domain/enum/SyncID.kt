package cn.netdiscovery.adbd.domain.enum

import kotlin.experimental.and
import kotlin.experimental.or

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.domain.enum.SyncID
 * @author: Tony Shen
 * @date: 2022/4/8 4:23 下午
 * @version: V1.0 <描述当前版本功能>
 */
enum class SyncID(code: String) {

    LSTAT_V1("STAT"),
    STAT_V2("STA2"),
    LSTAT_V2("LST2"),

    LIST_V1("LIST"),
    LIST_V2("LIS2"),
    DENT_V1("DENT"),
    DENT_V2("DNT2"),

    SEND_V1("SEND"),
    SEND_V2("SND2"),
    RECV_V1("RECV"),
    RECV_V2("RCV2"),

    DONE("DONE"),
    DATA("DATA"),
    OKAY("OKAY"),
    FAIL("FAIL"),
    QUIT("QUIT");

    private var value: Long
    private var array: ByteArray = ByteArray(4)

    init {
        for (i in code.indices) {
            array[i] = code[i].code.toByte()
        }

        value = (array[0] or ((array[1].toInt() shl 8).toByte()) or ((array[2].toInt() shl 16).toByte()) or ((array[3].toInt() shl 24).toByte()) and -0x1).toLong()
    }


    fun byteArray(): ByteArray {
        return array
    }

    companion object {
        fun findByValue(value: Int): SyncID? {
            val longValue = (value and -0x1).toLong()
            return findByValue(longValue)
        }

        fun findByValue(value: Long): SyncID? {
            for (id in values()) {
                if (id.value == value) {
                    return id
                }
            }
            return null
        }
    }
}
