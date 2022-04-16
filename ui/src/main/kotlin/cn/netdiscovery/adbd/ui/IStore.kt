package cn.netdiscovery.adbd.ui

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.ui.IStore
 * @author: Tony Shen
 * @date: 2022/4/16 3:56 下午
 * @version: V1.0 <描述当前版本功能>
 */
interface IStore {

    /**
     * 清空日志
     */
    fun clearLog()

    /**
     * 连接状态变更
     */
    fun changeConnectStatus(value: Int)
}