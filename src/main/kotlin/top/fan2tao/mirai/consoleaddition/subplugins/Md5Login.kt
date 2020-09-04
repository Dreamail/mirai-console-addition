package top.fan2tao.mirai.consoleaddition.subplugins

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.command.ConsoleCommandSender.INSTANCE.sendMessage
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI
import net.mamoe.mirai.message.nextMessageOrNull
import net.mamoe.mirai.utils.secondsToMillis
import top.fan2tao.mirai.consoleaddition.AdditionBase

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
                    onSuccess = { sendMessage("${it.nick} ($id) Login successful") },
                    onFailure = { throwable ->
                        sendMessage(
                                "Login failed: ${throwable.localizedMessage ?: throwable.message ?: throwable.toString()}" +
                                        if (this is CommandSenderOnMessage<*>) {
                                            AdditionBase.launch(CoroutineName("stacktrace delayer from Login")) {
                                                fromEvent.nextMessageOrNull(60.secondsToMillis) { it.message.contentEquals("stacktrace") }
                                            }
                                            "\n 1 分钟内发送 stacktrace 以获取堆栈信息"
                                        } else ""
                        )

                        throw throwable
                    }
            )
        }
    }
    @ExperimentalUnsignedTypes
    internal fun String.chunkedHexToBytes(): ByteArray =
            this.asSequence().chunked(2).map { (it[0].toString() + it[1]).toUByte(16).toByte() }.toList().toByteArray()
}