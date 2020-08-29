package top.fan2tao.mirai.consoleaddition.subplugins

import top.fan2tao.mirai.consoleaddition.AdditionBase

interface SubPlugin {
    var on: Boolean
    var enabled: Boolean

    fun onLoad()

    fun onEnable()

    fun onDisable()
}