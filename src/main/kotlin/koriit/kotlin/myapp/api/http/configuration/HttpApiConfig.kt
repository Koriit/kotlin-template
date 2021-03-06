package koriit.kotlin.myapp.api.http.configuration

import com.uchuhimo.konf.ConfigSpec

const val MODULE_HTTP_API = "httpApi"

object HttpApiConfig : ConfigSpec(MODULE_HTTP_API) {
    val scheme by optional("http")
    val host by optional("localhost")
    val port by optional(8080)
    val rootPath by optional("/")
    val stopGracePeriod by optional(3000L)
    val stopTimeout by optional(5000L)
    val logPayloads by optional(false)
}
