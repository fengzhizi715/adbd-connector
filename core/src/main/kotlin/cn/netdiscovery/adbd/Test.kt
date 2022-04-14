package cn.netdiscovery.adbd

import cn.netdiscovery.adbd.device.SocketAdbDevice
import cn.netdiscovery.adbd.utils.AuthUtil
import java.nio.charset.StandardCharsets
import java.security.interfaces.RSAPrivateCrtKey

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.Test
 * @author: Tony Shen
 * @date: 2022/4/14 5:16 下午
 * @version: V1.0 <描述当前版本功能>
 */
fun main() {

    val privateKey: RSAPrivateCrtKey
    val publicKey: ByteArray

    try {
        privateKey = AuthUtil.loadPrivateKey("adbkey")
        publicKey = AuthUtil.generatePublicKey(privateKey).toByteArray(StandardCharsets.UTF_8)
    } catch (cause: Throwable) {
        throw RuntimeException("load private key failed:" + cause.message, cause)
    }

    val device = SocketAdbDevice("192.168.0.100", 5555, privateKey, publicKey)
    device.shell("ls", "-l", "/sdcard").addListener { f ->
        if (f.cause() != null) {
            f.cause().printStackTrace()
        } else {
            println(f.now)

            println(device.deviceInfo)
        }
    }
}