package top.fan2tao.mirai.consoleaddition.subplugins

import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import top.fan2tao.mirai.consoleaddition.AdditionBase
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object AutoLogin : SubPlugin {
    override val name: String = "autologin"
    override var on: Boolean = false
    override var enabled: Boolean = false

    override fun onLoad() {

    }

    override fun onEnable() {
        if (AdditionBase.config.autologin.qq != 0L && AdditionBase.config.autologin.passwd !="") {
            AdditionBase.logger.info("正在自动登录${AdditionBase.config.autologin.qq}")
            Md5Login.md5Login(AdditionBase.config.autologin.qq, AdditionBase.config.autologin.passwd)
        } else {
            AdditionBase.logger.info("未设置自动登录的账号，跳过自动登录...")
        }
        autoLoginCommand.register()
    }

    override fun onDisable() {
        enabled = false
        autoLoginCommand.unregister()
    }

    object autoLoginCommand : SimpleCommand(
            AdditionBase, "autologin",
            description = "设置自动登录：/auto-login QQ Passwd"
    ) {
        @Handler
        suspend fun CommandSender.handle(qq: Long, passwd: String) {
            AdditionBase.config.autologin.qq = qq
            AdditionBase.config.autologin.passwd = encodeMD5(passwd)
            AdditionBase.config.autologin.enabled = true
            AdditionBase.config.save("config.yml")
            AdditionBase.logger.info("添加自动登录成功")
        }
    }

    fun encodeMD5(text: String): String {
        try {
            //获取md5加密对象
            val instance: MessageDigest = MessageDigest.getInstance("MD5")
            //对字符串加密，返回字节数组
            val digest:ByteArray = instance.digest(text.toByteArray())
            var sb = StringBuffer()
            for (b in digest) {
                //获取低八位有效值
                val i :Int = b.toInt() and 0xff
                //将整数转化为16进制
                var hexString = Integer.toHexString(i)
                if (hexString.length < 2) {
                    //如果是一位的话，补0
                    hexString = "0" + hexString
                }
                sb.append(hexString)
            }
            return sb.toString().toUpperCase()

        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

        return ""
    }
}