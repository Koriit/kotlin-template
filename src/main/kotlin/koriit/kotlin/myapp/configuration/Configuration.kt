package koriit.kotlin.myapp.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.yaml
import io.ktor.client.HttpClient
import io.ktor.client.features.DefaultRequest
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import koriit.kotlin.myapp.clients.model.ModelClient
import koriit.kotlin.myapp.configuration.spec.ApplicationConfig
import koriit.kotlin.myapp.configuration.spec.ApplicationConfig.Apis.Model
import koriit.kotlin.myapp.dao.UsersDAO
import koriit.kotlin.myapp.helpers.BuildInfo
import koriit.kotlin.myapp.services.UsersService
import koriit.kotlin.slf4j.logger
import koriit.kotlin.slf4j.mdc.correlation.correlationId
import korrit.kotlin.kodein.application.ApplicationEvents.Start
import korrit.kotlin.kodein.application.on
import korrit.kotlin.ktor.client.features.logging.ClientLogging
import org.kodein.di.Kodein
import org.kodein.di.direct
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton

const val MODULE_CONFIGURATION = "configuration"

private val log = logger {}

val configuration = Kodein.Module(MODULE_CONFIGURATION) {
    import(jackson)
    import(database)

    bind<Config>() with singleton {
        Config()
            .apply {
                addSpec(ApplicationConfig)
            }
            .from.yaml.resource("config.yaml")
            .from.env()
            .from.systemProperties()
    }

    on(Start) {
        val config: Config by instance()
        log.debug("{}", config)
    }

    daos()
    services()

    bind<ModelClient>() with singleton {
        val config: Config = instance()

        ModelClient(httpClient = instance(), serviceUrl = config[Model.service])
    }

    bind<BuildInfo>() with singleton {
        instance<ObjectMapper>().readValue(
            javaClass.getResourceAsStream("/build.json"),
            BuildInfo::class.java
        )
    }

    bind<HttpClient>() with provider {
        HttpClient {
            install(DefaultRequest) {
                header(HttpHeaders.XRequestId, correlationId)
            }
            install(ClientLogging) {
                logFullUrl = true
                logHeaders = false
                logBody = false
            }
            install(JsonFeature) {
                serializer = JacksonSerializer(instance())
            }
        }
    }
}

private fun Kodein.Builder.daos() {
    bind<UsersDAO>() with singleton {
        UsersDAO()
    }
}

private fun Kodein.Builder.services() {
    bind<UsersService>() with singleton {
        UsersService(instance(), instance())
    }

    // Eager load
    on(Start) {
        direct.apply {
            instance<UsersService>()
        }
    }
}
