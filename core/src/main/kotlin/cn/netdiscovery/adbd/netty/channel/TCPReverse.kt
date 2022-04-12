package cn.netdiscovery.adbd.netty.channel

import cn.netdiscovery.adbd.AdbChannelInitializer
import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.util.concurrent.Future

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.netty.channel.TCPReverse
 * @author: Tony Shen
 * @date: 2022/4/12 11:09 下午
 * @version: V1.0 <描述当前版本功能>
 */
class TCPReverse(private val host: String, private val port: Int, private val eventLoop: EventLoopGroup) : AdbChannelInitializer {

    override fun invoke(channel: Channel) {
        val bootstrap = Bootstrap()
        val chl = bootstrap.group(eventLoop)
            .channel(NioSocketChannel::class.java)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .option(ChannelOption.TCP_NODELAY, true)
            .option(ChannelOption.SO_LINGER, 3)
            .option(ChannelOption.SO_REUSEADDR, true)
            .option(ChannelOption.AUTO_CLOSE, false)
            .handler(object : ChannelInitializer<SocketChannel>() {

                @Throws(Exception::class)
                override fun initChannel(ch: SocketChannel) {
                    ch.pipeline().addLast(object : ChannelInboundHandlerAdapter() {
                        @Throws(Exception::class)
                        override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
                            channel.writeAndFlush(msg)
                        }

                        @Throws(Exception::class)
                        override fun channelInactive(ctx: ChannelHandlerContext) {
                            channel.close()
                        }
                    })
                }
            })
            .connect(host, port)
            .addListener { f: Future<in Void?> ->
                if (f.cause() != null) {
                    channel.close()
                }
            }
            .channel()

        channel.pipeline().addLast(object : ChannelInboundHandlerAdapter() {
            @Throws(Exception::class)
            override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
                chl.writeAndFlush(msg)
            }

            @Throws(Exception::class)
            override fun channelInactive(ctx: ChannelHandlerContext) {
                chl.close()
            }
        })
    }
}