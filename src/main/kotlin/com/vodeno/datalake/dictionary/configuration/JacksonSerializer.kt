package com.vodeno.datalake.dictionary.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.call.TypeInfo
import io.ktor.client.features.json.JsonSerializer
import io.ktor.http.ContentType
import io.ktor.http.content.OutgoingContent
import io.ktor.http.content.TextContent
import kotlinx.io.core.Input
import kotlinx.io.core.readText

// With https://github.com/ktorio/ktor/pull/1443 you can pass your own ObjectMapper instance to
// ktor's built-in JacksonSerializer
class JacksonSerializer(private val backend: ObjectMapper) : JsonSerializer {

    override fun write(data: Any, contentType: ContentType): OutgoingContent =
        TextContent(backend.writeValueAsString(data), contentType)

    override fun read(type: TypeInfo, body: Input): Any =
        backend.readValue(body.readText(), backend.typeFactory.constructType(type.reifiedType))
}
