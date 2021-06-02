package cn.netdiscovery.adbd.domain

import cn.netdiscovery.adbd.domain.enum.FileType

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.domain.StatMode
 * @author: Tony Shen
 * @date: 2021-06-02 14:56
 * @version: V1.0 <描述当前版本功能>
 */
data class StatMode(val type: FileType, val owner: FilePerm, val group: FilePerm, val other: FilePerm) {

    override fun toString(): String {
        val sb = StringBuilder().apply{
            append(type.code())
            append(owner.toString())
            append(group.toString())
            append(other.toString())
        }
        return sb.toString()
    }
}
