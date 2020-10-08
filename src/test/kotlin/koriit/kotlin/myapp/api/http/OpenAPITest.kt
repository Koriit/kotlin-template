package koriit.kotlin.myapp.api.http

import io.ktor.http.HttpHeaders.XRequestId
import io.ktor.server.testing.TestApplicationEngine
import koriit.kotlin.myapp.TestApplication
import koriit.kotlin.slf4j.logger
import korrit.kotlin.ktor.controllers.HttpHeader
import korrit.kotlin.ktor.controllers.openapi.KtorOpenAPIAnalyzer
import korrit.kotlin.ktor.features.errorresponses.ApiError
import korrit.kotlin.openapi.OpenAPIMatcher
import korrit.kotlin.openapi.OpenAPIReader
import korrit.kotlin.openapi.model.OpenAPI
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance

class OpenAPITest : KodeinAware {

    private val log = logger {}

    override val kodein = TestApplication()

    private val server: TestApplicationEngine by instance()

    @BeforeAll
    fun start() {
        server.start()
    }

    @AfterAll
    fun stop() {
        server.stop(0L, 0L)
    }

    @Test
    fun testSpec() {
        log.info("Analyzing Ktor server...")
        val analyzer = KtorOpenAPIAnalyzer(
            ktor = server.application,
            basePaths = listOf("/version", "/api"),
            defaultResponseHeaders = listOf(HttpHeader(XRequestId)),
            defaultErrorType = ApiError::class
        )

        val source: OpenAPI = analyzer.analyze()

        log.info("Reading OpenApi spec...")
        val doc: OpenAPI = OpenAPIReader().load(javaClass.getResourceAsStream("/openapi.yaml"))

        log.info("Validating spec...")
        val errors = OpenAPIMatcher().match(doc, source)

        if (errors.isNotEmpty()) {
            log.info("Result of server analysis:\n{}", source)

            errors.forEach {
                log.error(it)
            }

            fail("There are ${errors.size} validation errors!")
        } else {
            log.info("OK!")
        }
    }
}
