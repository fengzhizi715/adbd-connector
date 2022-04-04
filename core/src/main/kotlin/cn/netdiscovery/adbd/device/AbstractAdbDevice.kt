package cn.netdiscovery.adbd.device

import cn.netdiscovery.adbd.AdbChannelInitializer
import cn.netdiscovery.adbd.domain.DeviceInfo
import cn.netdiscovery.adbd.netty.codec.AdbPacketCodec
import io.netty.channel.*
import java.security.interfaces.RSAPrivateCrtKey
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.device.AbstractAdbDevice
 * @author: Tony Shen
 * @date: 2022/4/2 2:58 下午
 * @version: V1.0 <描述当前版本功能>
 */
abstract class AbstractAdbDevice protected constructor(
    private val serial: String,
    private val privateKey: RSAPrivateCrtKey,
    private val publicKey: ByteArray,
    private val factory: cn.netdiscovery.adbd.ChannelFactory
) : AdbDevice {

    private val channelIdGen: AtomicInteger
    private val reverseMap: MutableMap<CharSequence, AdbChannelInitializer>
    private val forwards: MutableSet<Channel>

    @Volatile
    private var listeners: MutableSet<DeviceListener>

    @Volatile
    private lateinit var channel: Channel

    @Volatile
    private var deviceInfo: DeviceInfo? = null

    init {
        channelIdGen = AtomicInteger(1)
        reverseMap = ConcurrentHashMap<CharSequence, AdbChannelInitializer>()
        forwards = ConcurrentHashMap.newKeySet()
        listeners = ConcurrentHashMap.newKeySet()
        newConnection()[30, TimeUnit.SECONDS]
    }


    private fun newConnection(): ChannelFuture {

        val future:ChannelFuture = factory.invoke(object : ChannelInitializer<Channel>() {
            override fun initChannel(ch: Channel) {
                val pipeline = ch.pipeline()
                pipeline.addLast(object : ChannelInboundHandlerAdapter() {
                    @Throws(Exception::class)
                    override fun channelInactive(ctx: ChannelHandlerContext) {

                        listeners.forEach(Consumer { listener: DeviceListener ->
                            try {
                                listener.onDisconnected(this@AbstractAdbDevice)
                            } catch (e: Exception) {
                            }
                        })
                        super.channelInactive(ctx)
                    }
                })
                .addLast("codec", AdbPacketCodec())
            }

        })
        channel = future.channel()
        return future
    }

    protected fun factory(): cn.netdiscovery.adbd.ChannelFactory = factory

    protected fun privateKey(): RSAPrivateCrtKey {
        return privateKey
    }

    protected fun publicKey(): ByteArray {
        return publicKey
    }

    fun eventLoop(): EventLoop {
        return channel!!.eventLoop()
    }

    override fun serial(): String {
        return serial
    }
}