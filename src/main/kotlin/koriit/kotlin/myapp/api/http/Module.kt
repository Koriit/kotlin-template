package koriit.kotlin.myapp.api.http

import com.uchuhimo.konf.Config
import io.ktor.server.cio.CIO
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import java.util.concurrent.TimeUnit
import koriit.kotlin.myapp.api.http.configuration.HttpApiConfig
import koriit.kotlin.myapp.api.http.configuration.MODULE_HTTP_API
import koriit.kotlin.slf4j.logger
import korrit.kotlin.kodein.application.ApplicationEvents.Start
import korrit.kotlin.kodein.application.ApplicationEvents.Stop
import korrit.kotlin.kodein.application.on
import korrit.kotlin.ktor.controllers.fullStart
import org.kodein.di.Kodein
import org.kodein.di.direct
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

private val log = logger {}

val httpApi = Kodein.Module(MODULE_HTTP_API) {
    bind<Config>(overrides = true) with singleton {
        (overriddenInstance() as Config).apply {
            addSpec(HttpApiConfig)
        }
    }

    bind<ApplicationEngine>(tag = MODULE_HTTP_API) with singleton {
        embeddedServer(CIO, serverConfig())
    }

    on(Start) {
        val server: ApplicationEngine = direct.instance(tag = MODULE_HTTP_API)

        server.fullStart()

        log.info("Base path: ${server.environment.rootPath}")
    }

    on(Stop) {
        val server: ApplicationEngine = direct.instance(tag = MODULE_HTTP_API)
        val config: Config = direct.instance()

        server.stop(
            config[HttpApiConfig.stopGracePeriod],
            config[HttpApiConfig.stopTimeout],
            TimeUnit.SECONDS
        )
    }
}
