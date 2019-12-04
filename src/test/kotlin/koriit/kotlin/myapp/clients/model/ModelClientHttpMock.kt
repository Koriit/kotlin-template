package koriit.kotlin.myapp.clients.model

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.fullPath
import io.ktor.http.headersOf

fun newModelClientHttpMock() = HttpClient(MockEngine) {
    engine {
        val headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

        addHandler { request ->
            when (request.url.fullPath) {
                "/models" -> respond(
                    content = javaClass.getResource("/models.json").readBytes(),
                    headers = headers
                )
                "/models/model_1" -> respond(
                    content = javaClass.getResource("/model_1.json").readBytes(),
                    headers = headers
                )
                else -> error("Unhandled ${request.url}")
            }
        }
    }
}
