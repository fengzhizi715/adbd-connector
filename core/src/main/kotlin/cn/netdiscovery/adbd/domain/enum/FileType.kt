package cn.netdiscovery.adbd.domain.enum

import cn.netdiscovery.adbd.constant.S_IFMT

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.domain.enum.FileType
 * @author: Tony Shen
 * @date: 2021-06-02 15:01
 * @version: V1.0 <描述当前版本功能>
 */
enum class FileType(private val value: Int, private val code: Char) {

    //套接口文件（socket）
    SOCK(49152, 's'),  //符号链接文件（symbolic link）
    LNK(40960, 'l'),  //普通文件（regular file）
    REG(32768, '-'),  //块设备（block device）
    BLK(24576, 'b'),  //目录（directory）
    DIR(16384, 'd'),  //字符设备（character device）
    CHR(8192, 'c'),  //管道（FIFO<pipe>）
    FIFO(4096, 'p');

    fun value():Int = value

    fun code(): Char = code

    companion object {
        fun findByValue(mode: Int): FileType? {
            val v = mode and S_IFMT
            for (type in values()) {
                if (type.value == v) {
                    return type
                }
            }
            return null
        }
    }
}