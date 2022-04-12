package cn.netdiscovery.adbd.device

import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.util.concurrent.Future
import java.security.interfaces.RSAPrivateCrtKey
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.device.SocketAdbDevice
 * @author: Tony Shen
 * @date: 2022/4/13 12:24 上午
 * @version: V1.0 <描述当前版本功能>
 */
class SocketAdbDevice(private val host: String, private val port: Int, privateKey: RSAPrivateCrtKey, publicKey: ByteArray) :
    AbstractAdbDevice("$host:$port", privateKey, publicKey, SocketChannelFactory(host, port)) {

    fun host(): String {
        return host
    }

    fun port(): Int {
        return port
    }

    @Throws(Exception::class)
    override fun close() {
        try {
            super.close()
        } finally {
            val factory = factory() as SocketChannelFactory
            try {
                factory.eventLoop.shutdownGracefully()[30, TimeUnit.SECONDS]
            } catch (e: Exception) {
            }
        }
    }

    private class SocketChannelFactory(private val host: String, private val port: Int) : cn.netdiscovery.adbd.ChannelFactory {

        val eventLoop: EventLoopGroup

        init {
            eventLoop = NioEventLoopGroup(1, ThreadFactory { r: Runnable ->
                Thread(r, "AdbThread-$host:$port")
            })
        }

        override fun invoke(initializer: ChannelInitializer<Channel>): ChannelFuture {
            val bootstrap = Bootstrap()
            return bootstrap.group(eventLoop)
                .channel(NioSocketChannel::class.java)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.AUTO_CLOSE, true)
                .handler(initializer)
                .connect(host, port)
        }
    }
}
