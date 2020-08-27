package top.fan2tao.mirai.consoleaddition

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI
import net.mamoe.yamlkt.Yaml
import top.fan2tao.mirai.consoleaddition.subplugins.AutoLogin
import top.fan2tao.mirai.consoleaddition.subplugins.Md5Login
import java.io.File

object AdditionBase : KotlinPlugin() {
    private val subPlugins = listOf(
            AutoLogin,
            Md5Login
    )

    val configFile by lazy { resolveDataFile("config.yml") }
    lateinit var config: Config

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
        fun save() {
            configFile.writeText(Yaml.default.encodeToString(serializer(), this))
        }
    }

    /**
     * 在插件被加载时调用. 只会被调用一次.
     */
    override fun onLoad() {
        if (!configFile.exists()) {
            configFile.createNewFile()
        }
        if (configFile.readText() == "") {
            config = Config(Config.AutoLogin(qq = 0L, passwd = ""), Config.Md5Login())
            config.save()
        } else {
            config = Yaml.default.decodeFromString(Config.serializer(), configFile.readText())
        }
        subPlugins.forEach {
            if (if (config.get(it.name) == null) {
                        return@forEach
                    } else {
                        config.get(it.name)!!.enabled
                    } && !it.on)  {
                logger.info("[Console-Addition] Loading ${it.name}")
                it.on = true
                it.onLoad()
            }
        }
    }

    /**
     * 在插件被启用时调用, 可能会被调用多次
     */
    override fun onEnable() {
        subPlugins.forEach {
            if (it.on && !it.enabled) {
                it.onEnable()
            }
        }
        additionCommand.register()
    }

    /**
     * 在插件被关闭时调用, 可能会被调用多次
     */
    override fun onDisable() {
        subPlugins.forEach {
            if (it.on && it.enabled) {
                logger.info("[Console-Addition] Disabling ${it.name}")
                it.onDisable()
            }
        }
        config.save()
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
            if (if (config.get(str) == null) {
                        logger.info("你输入了个错误的字插件名")
                        return
                    } else {
                        config.get(str)!!.enabled
                    }) {
                logger.info(str + "already enabled")
            } else {
                config.get(str)!!.enabled = true
                reload()
                logger.info("[Console-Addition] Enable $str Success")
            }
        }
        @SubCommand
        suspend fun CommandSender.disable(str: String) {
            if (if (config.get(str) == null) {
                        logger.info("你输入了个错误的字插件名")
                        return
                    } else {
                        !config.get(str)!!.enabled
                    }) {
                logger.info(str + "already disabled")
            } else {
                config.get(str)!!.enabled = false
                reload()
                logger.info("[Console-Addition] Disable $str Success")
            }
        }
    }
}