package com.vodeno.datalake.dictionary.api.http.controllers

import com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath
import com.jayway.jsonpath.matchers.JsonPathMatchers.isJson
import com.vodeno.datalake.dictionary.TestApplication
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.withCharset
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.contentType
import io.ktor.server.testing.handleRequest
import java.util.concurrent.TimeUnit
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance

internal class VersionControllerTest : KodeinAware {

    override val kodein = TestApplication()

    private val server: TestApplicationEngine by instance()

    @BeforeAll
    fun start() {
        server.start()
    }

    @AfterAll
    fun stop() {
        server.stop(0L, 0L, TimeUnit.MILLISECONDS)
    }

    @Test
    fun `should return current version`() = with(server) {
        with(handleRequest(HttpMethod.Get, "/version")) {
            assertThat(response.status(), IsEqual(HttpStatusCode.OK))
            assertThat(response.contentType(), IsEqual(ContentType.Application.Json.withCharset(Charsets.UTF_8)))
            assertThat(response.content, isJson())
            assertThat(response.content, hasJsonPath("$.version"))
        }
    }
}
