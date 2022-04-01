package cn.netdiscovery.adbd.utils

import io.netty.buffer.ByteBuf

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.utils.MessageUtils
 * @author: Tony Shen
 * @date: 2022/4/1 6:25 下午
 * @version: V1.0 <描述当前版本功能>
 */
fun checksum(payload: ByteBuf): Int {
    var sum = 0
    for (i in 0 until payload.readableBytes()) {
        sum += payload.getUnsignedByte(i).toInt()
    }
    return sum
}