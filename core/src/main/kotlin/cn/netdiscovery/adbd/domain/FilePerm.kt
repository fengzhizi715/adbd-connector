package cn.netdiscovery.adbd.domain

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.domain.FilePerm
 * @author: Tony Shen
 * @date: 2021-06-02 14:57
 * @version: V1.0 <描述当前版本功能>
 */
data class FilePerm(val readable:Boolean,val writable:Boolean,val executable:Boolean) {

    override fun toString(): String {
        val sb = StringBuilder().apply {
            append(if (readable) "r" else "-")
            append(if (writable) "w" else "-")
            append(if (executable) "x" else "-")
        }
        return sb.toString()
    }
}
