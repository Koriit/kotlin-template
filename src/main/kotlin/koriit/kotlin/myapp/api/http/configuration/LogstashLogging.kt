package koriit.kotlin.myapp.api.http.configuration

import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationFeature
import io.ktor.features.origin
import io.ktor.http.HttpHeaders
import io.ktor.request.contentType
import io.ktor.request.httpMethod
import io.ktor.util.AttributeKey
import io.ktor.util.KtorExperimentalAPI
import koriit.kotlin.slf4j.logger
import koriit.kotlin.slf4j.performance
import korrit.kotlin.ktor.features.logging.Logging
import net.logstash.logback.marker.Markers.appendEntries
import org.slf4j.Logger

@KtorExperimentalAPI
class LogstashLogging(config: Configuration) : Logging(config) {

    private val log: Logger = config.logger ?: logger {}

    override fun logPerformance(call: ApplicationCall) {
        val duration = System.currentTimeMillis() - call.attributes[startTimeKey]
        val route = call.attributes.getOrNull(routeKey)?.parent.toString()
        val method = call.request.httpMethod.value

        val requestInfo = mapOf(
            "method" to method,
            "protocol" to call.request.origin.version,
            "url" to call.request.origin.run { "$scheme://$host:$port$uri" },
            "api" to "$method $route",
            "route" to route,
            "remoteHost" to call.request.origin.remoteHost,
            "contentType" to call.request.contentType().toString(),
            "contentLength" to call.request.headers[HttpHeaders.ContentLength]?.toInt()
        )

        val responseInfo = mapOf(
            "status" to call.response.status()?.value,
            "contentType" to call.response.headers[HttpHeaders.ContentType],
            "contentLength" to call.response.headers[HttpHeaders.ContentLength]?.toInt()
        )

        val additionalInfo = mapOf(
            "request" to requestInfo,
            "response" to responseInfo
        )

        log.performance("{} ms - {} - {} {}", duration, responseInfo["status"], method, requestInfo["url"], appendEntries(additionalInfo))
    }

    companion object Feature : ApplicationFeature<Application, Configuration, LogstashLogging> {
        override val key = AttributeKey<LogstashLogging>("Logstash Logging Feature")

        override fun install(pipeline: Application, configure: Configuration.() -> Unit): LogstashLogging {
            val configuration = Configuration().apply(configure)

            return LogstashLogging(configuration).apply { install(pipeline) }
        }
    }
}
