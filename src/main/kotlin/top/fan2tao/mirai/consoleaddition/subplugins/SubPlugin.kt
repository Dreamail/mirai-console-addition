package top.fan2tao.mirai.consoleaddition.subplugins

interface SubPlugin {
    val name: String
    var on: Boolean
    var enabled: Boolean

    fun onLoad()

    fun onEnable()

    fun onDisable()
}