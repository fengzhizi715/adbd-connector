package cn.netdiscovery.adbd.device

import cn.netdiscovery.adbd.channel.AdbChannelInitializer
import cn.netdiscovery.adbd.constant.DEFAULT_MODE
import cn.netdiscovery.adbd.domain.enum.DeviceType
import cn.netdiscovery.adbd.domain.enum.Feature
import cn.netdiscovery.adbd.domain.sync.SyncDent
import cn.netdiscovery.adbd.domain.sync.SyncStat
import io.netty.channel.Channel
import io.netty.channel.ChannelInboundHandler
import io.netty.channel.EventLoop
import io.netty.util.AttributeMap
import io.netty.util.concurrent.Future
import java.io.*

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.device.AdbDevice
 * @author: Tony Shen
 * @date: 2021-06-02 12:09
 * @version: V1.0 <描述当前版本功能>
 */
interface AdbDevice : AttributeMap {

    fun isClosed(): Boolean

    /**
     * serial             print <serial-number>
     * @return
    </serial-number> */
    fun serial(): String?

    /**
     * type                print bootloader | device
     * @return
     */
    fun type(): DeviceType

    fun model(): String

    fun product(): String

    fun device(): String

    fun features(): Set<Feature>

    fun executor(): EventLoop

    fun open(destination: String?, initializer: AdbChannelInitializer): Future<Channel>

    fun exec(destination: String?): Future<String?>?

    fun shell(cmd: String?, vararg args: String?): Future<String?>?

    fun shell(lineFramed: Boolean, handler: ChannelInboundHandler?): Future<Channel>

    fun shell(
        cmd: String,
        args: Array<String>,
        lineFramed: Boolean,
        handler: ChannelInboundHandler
    ): Future<Channel>

    fun stat(path: String): Future<SyncStat>

    fun list(path: String): Future<Array<SyncDent>>

    fun pull(src: String, dest: OutputStream): Future<*>

    fun push(src: InputStream, dest: String, mode: Int, mtime: Int): Future<*>

    @Throws(IOException::class)
    fun pull(src: String, dest: File): Future<*> {
        val os = FileOutputStream(dest)
        return pull(src, os).addListener {
            os.flush()
            os.close()
        }
    }

    @Throws(IOException::class)
    fun push(src: File, dest: String): Future<*> {
        val `is` = FileInputStream(src)
        val mtime = src.lastModified() / 1000
        return push(`is`, dest, DEFAULT_MODE, mtime.toInt())
            .addListener { `is`.close() }
    }

    /**
     * root                     restart adbd with root permissions
     */
    fun root(): Future<*>?

    /**
     * unroot                   restart adbd without root permissions
     */
    fun unroot(): Future<*>?

    /**
     * remount partitions read-write.
     */
    fun remount(): Future<*>?

    /**
     * usb                      restart adbd listening on USB
     * tcpip PORT               restart adbd listening on TCP on PORT
     * @param port
     * @return
     */
    fun reload(port: Int): Future<*>

    fun reverse(destination: String, initializer: AdbChannelInitializer): Future<String>

    fun reverse(remote: String, local: String): Future<String>

    fun reverseList(): Future<Array<String>>

    fun reverseRemove(destination: String): Future<*>

    fun reverseRemoveAll(): Future<*>

    fun close(): Future<*>
}