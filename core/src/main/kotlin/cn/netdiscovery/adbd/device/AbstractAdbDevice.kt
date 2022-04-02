package cn.netdiscovery.adbd.device

import cn.netdiscovery.adbd.AdbChannelInitializer
import cn.netdiscovery.adbd.domain.enum.DeviceType
import cn.netdiscovery.adbd.domain.enum.Feature
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.util.DefaultAttributeMap
import io.netty.util.concurrent.Future
import io.netty.util.concurrent.Promise
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.device.AbstractAdbDevice
 * @author: Tony Shen
 * @date: 2022/4/2 2:58 下午
 * @version: V1.0 <描述当前版本功能>
 */
abstract class AbstractAdbDevice(private val serial:String): DefaultAttributeMap(),AdbDevice {

    private val CONNECT_PROMISE_UPDATER = AtomicReferenceFieldUpdater.newUpdater(
        AbstractAdbDevice::class.java,
        Promise::class.java, "connectPromise"
    )

    private val reverseMap: Map<CharSequence, AdbChannelInitializer> = ConcurrentHashMap()

    private val channelIdGen: AtomicInteger = AtomicInteger(1)

    @Volatile
    private lateinit var connectPromise: Promise<Channel>

    @Volatile
    private lateinit var closePromise: Promise<*>

    @Volatile
    private lateinit var type: DeviceType

    @Volatile
    private lateinit var model: String

    @Volatile
    private lateinit var product: String

    @Volatile
    private lateinit var device: String

    @Volatile
    private lateinit var features: Set<Feature>

    override fun isClosed(): Boolean = closePromise != null && (closePromise!!.isDone || closePromise!!.isCancelled)

    override fun serial(): String = serial

    override fun type(): DeviceType = type

    override fun model(): String = model

    override fun product(): String = product

    override fun device(): String = device

    override fun features(): Set<Feature> = features

    protected open fun closeFuture(): Future<*> = closePromise

    protected abstract fun newChannel(initializer: AdbChannelInitializer): ChannelFuture
}