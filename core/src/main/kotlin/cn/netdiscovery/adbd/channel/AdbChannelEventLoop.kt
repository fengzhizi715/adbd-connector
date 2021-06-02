package cn.netdiscovery.adbd.channel

import io.netty.channel.*
import io.netty.util.concurrent.Future
import io.netty.util.concurrent.ScheduledFuture
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.channel.AdbChannelEventLoop
 * @author: Tony Shen
 * @date: 2021-06-02 11:31
 * @version: V1.0 <描述当前版本功能>
 */
class AdbChannelEventLoop(private val eventLoop: EventLoop) : AbstractEventLoop(eventLoop) {

    override fun register(channel: Channel): ChannelFuture {
        return register(channel, channel.newPromise())
    }

    override fun register(promise: ChannelPromise): ChannelFuture {
        return register(promise.channel(), promise)
    }

    override fun register(channel: Channel, promise: ChannelPromise): ChannelFuture {
        channel.unsafe().register(this, promise)
        return promise
    }

    override fun shutdown() {
        //不需要实现，这个线程是沿用的
    }

    override fun inEventLoop(thread: Thread): Boolean {
        return eventLoop.inEventLoop(thread)
    }

    override fun isShuttingDown(): Boolean {
        return eventLoop.isShuttingDown
    }

    override fun shutdownGracefully(quietPeriod: Long, timeout: Long, unit: TimeUnit): Future<*> {
        return eventLoop.newPromise<Any>().setSuccess(null)
    }

    override fun terminationFuture(): Future<*> {
        return eventLoop.newPromise<Any>().setSuccess(null)
    }

    override fun isShutdown(): Boolean {
        return false
    }

    override fun isTerminated(): Boolean {
        return false
    }

    override fun awaitTermination(timeout: Long, unit: TimeUnit): Boolean {
        return true
    }

    override fun execute(command: Runnable) {
        eventLoop.execute(command)
    }

    override fun schedule(command: Runnable, delay: Long, unit: TimeUnit): ScheduledFuture<*> {
        return eventLoop.schedule(command, delay, unit)
    }

    override fun <V> schedule(callable: Callable<V>, delay: Long, unit: TimeUnit): ScheduledFuture<V> {
        return eventLoop.schedule(callable, delay, unit)
    }

    override fun scheduleAtFixedRate(
        command: Runnable,
        initialDelay: Long,
        period: Long,
        unit: TimeUnit
    ): ScheduledFuture<*> {
        return eventLoop.scheduleAtFixedRate(command, initialDelay, period, unit)
    }

    override fun scheduleWithFixedDelay(
        command: Runnable,
        initialDelay: Long,
        delay: Long,
        unit: TimeUnit
    ): ScheduledFuture<*> {
        return eventLoop.scheduleWithFixedDelay(command, initialDelay, delay, unit)
    }
}