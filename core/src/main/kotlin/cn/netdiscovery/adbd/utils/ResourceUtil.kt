package cn.netdiscovery.adbd.utils

import java.io.ByteArrayOutputStream
import java.io.IOException

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.utils.ResourceUtil
 * @author: Tony Shen
 * @date: 2022/4/6 11:03 下午
 * @version: V1.0 <描述当前版本功能>
 */
@Throws(IOException::class)
fun readAll(name: String?): ByteArray {
    val classLoader = Thread.currentThread().contextClassLoader
    val `is` = classLoader.getResourceAsStream(name)
    val os = ByteArrayOutputStream()
    val bytes = ByteArray(8192)
    var size: Int
    while (`is`.read(bytes).also { size = it } != -1) {
        os.write(bytes, 0, size)
    }
    os.close()
    return os.toByteArray()
}