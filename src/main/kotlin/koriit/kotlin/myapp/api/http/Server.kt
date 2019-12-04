package koriit.kotlin.myapp.api.http

import com.fasterxml.jackson.core.JsonProcessingException
import com.uchuhimo.konf.Config
import io.ktor.application.install
import io.ktor.features.AutoHeadResponse
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.features.DataConversion
import io.ktor.features.DefaultHeaders
import io.ktor.features.DoubleReceive
import io.ktor.features.ForwardedHeaderSupport
import io.ktor.features.XForwardedHeaderSupport
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.jackson.JacksonConverter
import io.ktor.routing.Routing
import io.ktor.routing.route
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import koriit.kotlin.myapp.api.http.configuration.HttpApiConfig
import koriit.kotlin.myapp.api.http.configuration.HttpApiConfig.host
import koriit.kotlin.myapp.api.http.configuration.HttpApiConfig.logPayloads
import koriit.kotlin.myapp.api.http.configuration.HttpApiConfig.port
import koriit.kotlin.myapp.api.http.configuration.HttpApiConfig.scheme
import koriit.kotlin.myapp.api.http.configuration.LogstashLogging
import koriit.kotlin.myapp.api.http.controllers.appVersionController
import koriit.kotlin.myapp.api.http.controllers.entitiesController
import koriit.kotlin.myapp.exceptions.ItemNotFoundException
import koriit.kotlin.myapp.helpers.BuildInfo
import koriit.kotlin.slf4j.logger
import koriit.kotlin.slf4j.mdc.correlation.continueCorrelation
import koriit.kotlin.slf4j.watched
import korrit.kotlin.ktor.controllers.openapi.openAPIController
import korrit.kotlin.ktor.controllers.openapi.swaggerUIController
import korrit.kotlin.ktor.convertTime
import korrit.kotlin.ktor.features.UUIDCallId
import korrit.kotlin.ktor.features.errorresponses.DefaultExceptionHandler
import korrit.kotlin.ktor.features.errorresponses.ErrorResponses
import org.kodein.di.DKodein
import org.kodein.di.generic.instance

private val log = logger {}

fun DKodein.serverConfig() = applicationEngineEnvironment {
    val config: Config = instance()
    val buildInfo: BuildInfo = instance()

    val logPayloads = config[logPayloads]

    log = koriit.kotlin.myapp.api.http.log
    rootPath = config[HttpApiConfig.rootPath]
    parentCoroutineContext += continueCorrelation() + log.watched(shutdown = true)

    connector {
        port = config[HttpApiConfig.port]
    }

    module {
        install(UUIDCallId)

        if (logPayloads) {
            install(DoubleReceive) {
                receiveEntireContent = true
            }
        }

        install(LogstashLogging) {
            this.logPayloads = logPayloads
            filterPath("/api", "/version", "/openapi")
        }

        install(XForwardedHeaderSupport)
        install(ForwardedHeaderSupport)
        install(AutoHeadResponse)

        install(CORS) {
            anyHost()
            allowNonSimpleContentTypes = true
        }

        install(ErrorResponses) {
            handler<DefaultExceptionHandler> {
                // Domain
                register<ItemNotFoundException>(NotFound)
                // Jackson
                registerReceive<JsonProcessingException>(BadRequest)
            }
        }

        install(DefaultHeaders) {
            header(HttpHeaders.Server, "${buildInfo.projectName}/${buildInfo.projectVersion}")
        }

        install(DataConversion) {
            convertTime(instance())
        }

        install(ContentNegotiation) {
            register(ContentType.Application.Json, JacksonConverter(instance()))
        }

        install(Routing) {
            appVersionController(buildInfo)

            route("/api") {
                entitiesController(instance())
            }

            val apiDoc = javaClass.getResourceAsStream("/openapi.yaml").reader().readText()

            openAPIController(apiDoc, buildInfo.projectVersion, config[scheme], "${config[host]}:${config[port]}", rootPath)
            swaggerUIController()
        }
    }
}
