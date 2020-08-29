package top.fan2tao.mirai.consoleaddition

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.data.*
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI
import net.mamoe.yamlkt.Yaml
import top.fan2tao.mirai.consoleaddition.subplugins.AutoLogin
import top.fan2tao.mirai.consoleaddition.subplugins.Md5Login

object AdditionBase : KotlinPlugin() {
    private val subPlugins = mapOf(
            Pair("autologin", Pair(AutoLogin, PluginConfig.autoLogin)),
            Pair("md5login", Pair(Md5Login, PluginConfig.md5Login))
    )

    interface IConfig {
        var enabled: Boolean
    }

    @Serializable
    data class Config(
            var autologin: AutoLogin,
            var md5login: Md5Login
    ) {
        @Serializable
        data class AutoLogin(
                override var enabled: Boolean = true,

                var qq: Long,
                var passwd: String
        ) : IConfig
        @Serializable
        data class Md5Login(
                override var enabled: Boolean = true,
        ) : IConfig
        fun get(str: String) : IConfig? {
            return when(str) {
                "autologin" -> autologin
                "md5login" -> md5login
                else -> null
            }
        }
    }

    object PluginConfig : AutoSavePluginConfig() {
        var autoLogin by value(AutoLogin(qq = 0L, passwd = ""))
        var md5Login by value(Md5Login())

        @Serializable
        data class AutoLogin(
                override var enabled: Boolean = true,

                var qq: Long,
                var passwd: String
        ) : IConfig
        @Serializable
        data class Md5Login(
                override var enabled: Boolean = true,
        ) : IConfig
    }

    /**
     * 在插件被加载时调用. 只会被调用一次.
     */
    override fun onLoad() {
        subPlugins.forEach {
            if (it.value.second.enabled && !it.value.first.on)  {
                logger.info("[Console-Addition] Loading ${it.key}")
                it.value.first.on = true
                it.value.first.onLoad()
            }
        }
    }

    /**
     * 在插件被启用时调用, 可能会被调用多次
     */
    override fun onEnable() {
        PluginConfig.reload()
        subPlugins.forEach {
            if (it.value.first.on && !it.value.first.enabled) {
                it.value.first.onEnable()
            }
        }
        additionCommand.register()
    }

    /**
     * 在插件被关闭时调用, 可能会被调用多次
     */
    override fun onDisable() {
        subPlugins.forEach {
            if (it.value.first.on && it.value.first.enabled) {
                logger.info("[Console-Addition] Disabling ${it.key}")
                it.value.first.onDisable()
            }
        }
        additionCommand.unregister()
    }

    @ConsoleExperimentalAPI
    object additionCommand : CompositeCommand(
            AdditionBase, "addition",
            description = "Console-Addition插件管理主命令"
    ) {
        @SubCommand
        suspend fun CommandSender.reload() {
            onDisable()
            onLoad()
            onEnable()
        }
        @SubCommand
        suspend fun CommandSender.enable(str: String) {
            if (if (subPlugins.containsKey(str)) {
                        logger.info("你输入了个错误的字插件名")
                        return
                    } else {
                        subPlugins[str]!!.second.enabled
                    }) {
                logger.info(str + "already enabled")
            } else {
                subPlugins[str]!!.second.enabled = true
                reload()
                logger.info("[Console-Addition] Enable $str Success")
            }
        }
        @SubCommand
        suspend fun CommandSender.disable(str: String) {
            if (if (subPlugins.containsKey(str)) {
                        logger.info("你输入了个错误的字插件名")
                        return
                    } else {
                        !subPlugins[str]!!.second.enabled
                    }) {
                logger.info(str + "already disabled")
            } else {
                subPlugins[str]!!.second.enabled = false
                reload()
                logger.info("[Console-Addition] Disable $str Success")
            }
        }
    }
}