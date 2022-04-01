package cn.netdiscovery.adbd.domain.enum

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.domain.enum.Command
 * @author: Tony Shen
 * @date: 2022/4/1 5:37 下午
 * @version: V1.0 <描述当前版本功能>
 */
enum class Command(value: Int) {

    A_SYNC(0x434e5953),
    A_CNXN(0x4e584e43),
    A_AUTH(0x48545541),
    A_OPEN(0x4e45504f),
    A_OKAY(0x59414b4f),
    A_CLSE(0x45534c43),
    A_WRTE(0x45545257),
    A_STLS(0x534C5453);

    private val value: Int
    private val magic: Int

    init {
        this.value = value
        magic = value xor -0x1
    }

    fun value(): Int {
        return value
    }

    fun magic(): Int {
        return magic
    }

    companion object {
        fun findByValue(value: Int): Command? {
            for (command in values()) {
                if (command.value == value) {
                    return command
                }
            }
            return null
        }
    }
}