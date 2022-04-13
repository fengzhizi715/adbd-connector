package cn.netdiscovery.adbd.netty.connection

import cn.netdiscovery.adbd.AdbChannelInitializer
import cn.netdiscovery.adbd.domain.AdbChannelAddress
import cn.netdiscovery.adbd.domain.AdbPacket
import cn.netdiscovery.adbd.domain.enum.Command
import cn.netdiscovery.adbd.netty.channel.AdbChannel
import cn.netdiscovery.adbd.utils.getChannelName
import cn.netdiscovery.adbd.utils.logger
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandler
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.util.ReferenceCountUtil
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicInteger

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.netty.connection.AdbChannelProcessor
 * @author: Tony Shen
 * @date: 2022/4/4 11:53 下午
 * @version: V1.0 <描述当前版本功能>
 */
class AdbChannelProcessor(
    private val channelIdGen: AtomicInteger,
    private val reverseMap: Map<CharSequence, AdbChannelInitializer>
) : ChannelInboundHandlerAdapter() {

    private val logger = logger<AdbChannelProcessor>()

    @Throws(Exception::class)
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        logger.error("Uncaught exception: {}", cause.message, cause)
    }

    @Throws(Exception::class)
    private fun fireChannelMessage(ctx: ChannelHandlerContext, message: AdbPacket): Boolean {
        val handlerName: String = getChannelName(message.arg1)
        val channelContext = ctx.pipeline().context(handlerName)
        return if (channelContext != null) {
            val handler = channelContext.handler() as ChannelInboundHandler
            handler.channelRead(channelContext, message)
            true
        } else {
            //这里将引用释放掉，避免泄漏
            ReferenceCountUtil.safeRelease(message)
            false
        }
    }

    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg !is AdbPacket) {
            ReferenceCountUtil.release(msg)
            return
        }
        val message: AdbPacket = msg
        when (message.command) {
            Command.A_OPEN -> {
                val remoteId: Int = message.arg0
                val localId = channelIdGen.getAndIncrement()
                try {
                    //-1是因为最后有一个\0
                    val payload = ByteArray(message.size - 1)
                    message.payload?.readBytes(payload)
                    val destination = String(payload, StandardCharsets.UTF_8)
                    val initializer: AdbChannelInitializer? = reverseMap[destination]
                    val channelName: String = getChannelName(localId)
                    val channel = AdbChannel(ctx.channel(), 0, remoteId)
                    channel.bind(AdbChannelAddress(destination, localId)).addListener { f ->
                        if (f.cause() == null) {
                            try {
                                initializer?.invoke(channel)
                                ctx.pipeline().addLast(channelName, channel)
                                ctx.writeAndFlush(AdbPacket(Command.A_OKAY, localId, remoteId))
                                channel.eventLoop().register(channel)
                            } catch (cause: Throwable) {
                                ctx.writeAndFlush(AdbPacket(Command.A_CLSE, 0, remoteId))
                            }
                        } else {
                            ctx.writeAndFlush(AdbPacket(Command.A_CLSE, 0, remoteId))
                        }
                    }
                } catch (cause: Throwable) {
                    ctx.writeAndFlush(AdbPacket(Command.A_CLSE, 0, remoteId))
                } finally {
                    ReferenceCountUtil.safeRelease(message)
                }
            }
            Command.A_OKAY -> fireChannelMessage(ctx, message)
            Command.A_WRTE -> {
                ctx.writeAndFlush(AdbPacket(Command.A_OKAY, message.arg1, message.arg0))
                fireChannelMessage(ctx, message)
            }
            Command.A_CLSE ->
                /**
                 * 如果成功转发了事件，那么AdbChannel#doClose的时候已经发送了CLSE命令了，不需要重复发送
                 * @see AdbChannel.doClose
                 */
                if (!fireChannelMessage(ctx, message)) {
                    ctx.writeAndFlush(AdbPacket(Command.A_CLSE, message.arg1, message.arg0))
                }
            else -> ctx.fireExceptionCaught(Exception("Unexpected channel command:" + message.command))
        }
    }
}