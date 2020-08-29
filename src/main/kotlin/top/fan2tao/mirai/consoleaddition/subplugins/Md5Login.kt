package top.fan2tao.mirai.consoleaddition.subplugins

import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI
import net.mamoe.mirai.event.selectMessagesUnit
import net.mamoe.mirai.utils.DirectoryLogger
import net.mamoe.mirai.utils.weeksToMillis
import top.fan2tao.mirai.consoleaddition.AdditionBase
import java.io.File

object Md5Login : SubPlugin {
    override var on: Boolean = false
    override var enabled: Boolean = false

    override fun onLoad() {

    }

    override fun onEnable() {
        md5LoginCommand.register()
    }

    override fun onDisable() {
        md5LoginCommand.unregister()
        enabled = false
    }

    object md5LoginCommand : SimpleCommand(
            AdditionBase, "md5login",
            description = "使用MD5登录 /md5login QQ Passwd"
    ) {
        @Handler
        suspend fun CommandSender.handle(id: Long, md5: String) {
            md5Login(id, md5)
        }
    }

    @ConsoleExperimentalAPI
    fun md5Login(id: Long, md5str: String) {
        AdditionBase.launch {
            kotlin.runCatching {
                Bot(id, md5str.chunkedHexToBytes()) {
                    fileBasedDeviceInfo()
                    redirectNetworkLogToDirectory()
                    //parentCoroutineContext = MiraiConsole.childScopeContext()

                    //this.loginSolver = MiraiConsoleImplementationBridge.createLoginSolver(id, this)
                }.alsoLogin()
            }.fold(
                    onSuccess = { AdditionBase.logger.info("${it.nick} ($id) Login succeed") },
                    onFailure = { throwable ->
                        AdditionBase.logger.info(
                                "Login failed: ${throwable.localizedMessage ?: throwable.message ?: throwable.toString()}" +
                                        if (this is MessageEventContextAware<*>) {
                                            this.fromEvent.selectMessagesUnit {
                                                "stacktrace" reply {
                                                    throwable.stackTraceToString()
                                                }
                                            }
                                            "test"
                                        } else "")

                        throw throwable
                    }
            )
        }
    }
    @ExperimentalUnsignedTypes
    internal fun String.chunkedHexToBytes(): ByteArray =
            this.asSequence().chunked(2).map { (it[0].toString() + it[1]).toUByte(16).toByte() }.toList().toByteArray()
}