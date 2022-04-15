package cn.netdiscovery.adbd.netty.handler

import cn.netdiscovery.adbd.constant.*
import cn.netdiscovery.adbd.domain.AdbPacket
import cn.netdiscovery.adbd.domain.DeviceInfo
import cn.netdiscovery.adbd.domain.enum.Command
import cn.netdiscovery.adbd.domain.enum.DeviceType
import cn.netdiscovery.adbd.domain.enum.Feature
import cn.netdiscovery.adbd.utils.AuthUtil
import cn.netdiscovery.adbd.utils.logger
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.util.ReferenceCountUtil
import io.netty.util.concurrent.Future
import java.net.ProtocolException
import java.nio.charset.StandardCharsets
import java.security.interfaces.RSAPrivateCrtKey
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.netty.handler.AdbAuthHandler
 * @author: Tony Shen
 * @date: 2022/4/5 12:26 上午
 * @version: V1.0 <描述当前版本功能>
 */
class AdbAuthHandler(private val privateKey: RSAPrivateCrtKey, private val publicKey: ByteArray) :
    ChannelInboundHandlerAdapter() {

    private val logger = logger<AdbAuthHandler>()

    private val state: AtomicInteger = AtomicInteger(STATE_CONNECTING)

    private fun write(ctx: ChannelHandlerContext, message: AdbPacket) {
        ctx.writeAndFlush(message)
            .addListener { f: Future<in Void> ->
                if (f.cause() != null) {
                    ctx.fireExceptionCaught(f.cause())
                }
            }
    }

    @Throws(Exception::class)
    override fun channelActive(ctx: ChannelHandlerContext) {
        val payload = ctx.alloc().buffer(2048)
        try {
            payload.writeCharSequence("host::features=", StandardCharsets.UTF_8)
            val features: Array<Feature> = Feature.values()
            for (i in features.indices) {
                if (i > 0) {
                    payload.writeChar(','.code)
                }
                payload.writeCharSequence(features[i].getCode(), StandardCharsets.UTF_8)
            }
            write(ctx, AdbPacket(Command.A_CNXN, A_VERSION, MAX_PAYLOAD, payload))
        } catch (e: Exception) {
            ReferenceCountUtil.safeRelease(payload)
            throw e
        }
    }

    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg !is AdbPacket) {
            ReferenceCountUtil.release(msg)
            return
        }
        val message: AdbPacket = msg
        var payload: ByteArray? = null
        if (message.payload != null) {
            payload = ByteArray(message.payload.readableBytes())
            message.payload.readBytes(payload)
            ReferenceCountUtil.release(message)
        }
        when (message.command) {
            Command.A_AUTH -> {
                if (message.arg0 !== ADB_AUTH_TOKEN) {
                    ctx.fireExceptionCaught(ProtocolException("Invalid auth type: " + message.arg0))
                    return
                }
                if (state.compareAndSet(STATE_CONNECTING, STATE_AUTHORIZING)) {
                    if (payload!!.size != TOKEN_SIZE) {
                        ctx.fireExceptionCaught(ProtocolException("Invalid token size, expect=" + TOKEN_SIZE.toString() + ", actual=" + payload.size))
                        return
                    }
                    val sign: ByteArray = AuthUtil.sign(privateKey, payload).toByteArray()
                    val signBuf = Unpooled.wrappedBuffer(sign)
                    write(ctx, AdbPacket(Command.A_AUTH, ADB_AUTH_SIGNATURE, 0, signBuf))
                } else if (state.compareAndSet(STATE_AUTHORIZING, STATE_AUTHORIZED)) {
                    val bytes = Arrays.copyOf(publicKey, publicKey.size + 1)
                    val keyBuf = Unpooled.wrappedBuffer(bytes)
                    write(ctx, AdbPacket(Command.A_AUTH, ADB_AUTH_RSAPUBLICKEY, 0, keyBuf))
                } else {
                    ctx.fireExceptionCaught(Exception("State error:$state"))
                }
            }
            Command.A_CNXN -> {
                //当连接上以后，把认证的handler从pipeline中移除掉
                ctx.pipeline().remove(this)
                var product = ""
                var model = ""
                var device = ""
                var features: Set<Feature>? = null
                val p = String(payload!!, StandardCharsets.UTF_8)
                val pieces = p.split(":").toTypedArray()
                if (pieces.size > 2) {
                    val props = pieces[2].split(";").toTypedArray()
                    for (prop in props) {
                        val kv = prop.split("=").toTypedArray()
                        if (kv.size != 2) {
                            continue
                        }
                        val key = kv[0]
                        val value = kv[1]
                        if ("ro.product.name" == key) {
                            product = value
                        } else if ("ro.product.model" == key) {
                            model = value
                        } else if ("ro.product.device" == key) {
                            device = value
                        } else if ("features" == key) {
                            val fts: MutableSet<Feature> = HashSet<Feature>()
                            for (f in value.split(",").toTypedArray()) {
                                val fe = Feature.findByCode(f)
                                if (fe == null) {
                                    logger.warn("Unknown feature: $f");
                                    continue
                                }
                                fts.add(fe)
                            }
                            features = Collections.unmodifiableSet(fts)
                        }
                    }
                }
                val deviceInfo = DeviceInfo(DeviceType.findByCode(pieces[0]), model, product, device, features?: mutableSetOf())
                ctx.fireChannelRead(deviceInfo)
            }
            else -> ctx.fireExceptionCaught(ProtocolException("Unexpected command, expect=A_AUTH|A_CNXN, actual=" + message.command))
        }
    }

    companion object {
        private const val STATE_CONNECTING = 0
        private const val STATE_AUTHORIZING = 1
        private const val STATE_AUTHORIZED = 2
    }
}