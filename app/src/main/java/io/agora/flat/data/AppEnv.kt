package io.agora.flat.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import io.agora.flat.BuildConfig
import io.agora.flat.di.interfaces.LogConfig
import javax.inject.Inject

/**
 * 应用内切换配置
 */
class AppEnv @Inject constructor(@ApplicationContext context: Context) {
    private val store: SharedPreferences = context.getSharedPreferences("flat_env", Context.MODE_PRIVATE)

    companion object {
        const val ENV_PROD = "prod"
        const val ENV_DEV = "dev"

        const val ENV_SG_PROD = "sg_prod"
        const val ENV_SG_DEV = "sg_dev"

        const val STORE_KEY_ENV = "key_env"
    }

    val envMap = mutableMapOf<String, EnvItem>()

    init {
        envMap[ENV_DEV] = EnvItem(
            agoraAppId = "a185de0a777f4c159e302abcc0f03b64",
            serviceUrl = "https://flat-api-dev.whiteboard.agora.io",
            githubClientId = "9821657775fbc74773f1",
            baseInviteUrl = "https://flat-web-dev.whiteboard.agora.io",
            versionCheckUrl = "https://flat-storage.oss-cn-hangzhou.aliyuncs.com/versions/latest/beta/android/checkVersion.json",
            logConfig = LogConfig(
                ak = BuildConfig.ALIYUN_LOG_DEV_AK,
                sk = BuildConfig.ALIYUN_LOG_DEV_SK,
                project = "hz-flat-dev",
                logstore = "android",
                endpoint = "cn-hangzhou.log.aliyuncs.com",
            ),
            ossKey = "LTAI5tD4WSVBxAyfwVoaKTWr",
            wechatId = "wx09437693798bc108",
            whiteAppId = "cFjxAJjiEeuUQ0211QCRBw/mO9uJB_DiCIqug",
            googleClientId = "273996094508-p97og69ojac5ja0khn1rvmi3tb7vgfgm.apps.googleusercontent.com",
        )

        envMap[ENV_PROD] = EnvItem(
            agoraAppId = "931b86d6781e49a2a255db4ce6e8e804",
            serviceUrl = "https://flat-api.whiteboard.agora.io",
            githubClientId = "71a29285a437998bdfe0",
            baseInviteUrl = "https://flat-web.whiteboard.agora.io",
            versionCheckUrl = "https://flat-storage.oss-cn-hangzhou.aliyuncs.com/versions/latest/stable/android/checkVersion.json",
            logConfig = LogConfig(
                ak = BuildConfig.ALIYUN_LOG_PROD_AK,
                sk = BuildConfig.ALIYUN_LOG_PROD_SK,
                project = "flat-prod",
                logstore = "android",
                endpoint = "cn-hangzhou.log.aliyuncs.com",
            ),
            ossKey = "LTAI5tMwHQ1xyroeneA9XLh4",
            wechatId = "wx09437693798bc108",
            whiteAppId = "cFjxAJjiEeuUQ0211QCRBw/mO9uJB_DiCIqug",
            googleClientId = "273996094508-p97og69ojac5ja0khn1rvmi3tb7vgfgm.apps.googleusercontent.com",
        )

        envMap[ENV_SG_PROD] = EnvItem(
            agoraAppId = "549c8a24c1e64852bd163f853f5fe14f",
            serviceUrl = "https://api.flat.agora.io",
            githubClientId = "da83d7e14217594fba46",
            baseInviteUrl = "https://flat-web.whiteboard.agora.io",
            versionCheckUrl = "",
            ossKey = "LTAI5tMwHQ1xyroeneA9XLh4",
            wechatId = "wx09437693798bc108",
            whiteAppId = "cFjxAJjiEeuUQ0211QCRBw/kndLTOWdG2qYcQ",
            googleClientId = "273996094508-2rpraucen77a1o5dul5ftrua5k3og157.apps.googleusercontent.com",
            loginConfig = LoginConfig(
                smsForce = false,
            )
        )

        envMap[ENV_SG_DEV] = EnvItem(
            agoraAppId = "931b86d6781e49a2a255db4ce6e8e804",
            serviceUrl = "https://flat-api-dev-sg.whiteboard.agora.io",
            githubClientId = "0ac608815326aead5db7",
            baseInviteUrl = "https://flat-web-dev-sg.whiteboard.agora.io",
            versionCheckUrl = "",
            ossKey = "LTAI5tMwHQ1xyroeneA9XLh4",
            wechatId = "wx09437693798bc108",
            whiteAppId = "n9q1oBxDEeyuBMn1qc0iFw/fLgNSEvdwKjlig",
            googleClientId = "273996094508-p97og69ojac5ja0khn1rvmi3tb7vgfgm.apps.googleusercontent.com",
            loginConfig = LoginConfig(
                smsForce = false,
            )
        )
    }

    fun setEnv(env: String) {
        store.edit(commit = true) {
            putString(STORE_KEY_ENV, env)
        }
    }

    fun getEnv(): String {
        return store.getString(STORE_KEY_ENV, ENV_PROD)!!
    }

    private val currentEnvItem = envMap[getEnv()]!!

    val flatServiceUrl = run {
        currentEnvItem.serviceUrl
    }

    val githubClientID = run {
        currentEnvItem.githubClientId
    }

    val githubCallback get() = "${flatServiceUrl}/v1/login/github/callback"

    val githubBindingCallback get() = "${flatServiceUrl}/v1/login/github/callback/binding"

    val googleCallback get() = "${flatServiceUrl}/v1/login/google/callback"

    val googleBindingCallback get() = "${flatServiceUrl}/v1/user/binding/platform/google"

    val baseInviteUrl = run {
        currentEnvItem.baseInviteUrl
    }

    val agoraAppId get() = currentEnvItem.agoraAppId

    val versionCheckUrl get() = currentEnvItem.versionCheckUrl

    val logConfig get() = currentEnvItem.logConfig

    val ossKey get() = currentEnvItem.ossKey

    val whiteAppId get() = currentEnvItem.whiteAppId

    val wechatId get() = currentEnvItem.wechatId

    val googleClientId get() = currentEnvItem.googleClientId

    val loginConfig get() = currentEnvItem.loginConfig

    data class EnvItem(
        val agoraAppId: String,
        val serviceUrl: String,
        val githubClientId: String,
        val baseInviteUrl: String,
        val versionCheckUrl: String,
        val logConfig: LogConfig? = null,
        val ossKey: String,

        val googleClientId: String? = null,
        val wechatId: String? = null,
        val whiteAppId: String? = null,

        val loginConfig: LoginConfig = LoginConfig(),
    )

    data class LoginConfig(
        val wechat: Boolean = true,
        val github: Boolean = true,
        val google: Boolean = true,
        val apple: Boolean = true,
        val agora: Boolean = false,
        val sms: Boolean = true,
        val smsForce: Boolean = true,
    ) {
        fun forceBindPhone(): Boolean {
            return sms && smsForce
        }
    }
}