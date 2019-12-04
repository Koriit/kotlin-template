package koriit.kotlin.myapp.api.http.controllers

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.features.BadRequestException
import io.ktor.http.HttpHeaders.Location
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.route
import io.ktor.util.pipeline.PipelineContext
import koriit.kotlin.myapp.api.http.responses.EntitiesList
import koriit.kotlin.myapp.domain.Entity
import koriit.kotlin.myapp.services.EntityService
import korrit.kotlin.ktor.controllers.EmptyBodyInput
import korrit.kotlin.ktor.controllers.GET
import korrit.kotlin.ktor.controllers.HttpHeader
import korrit.kotlin.ktor.controllers.Input
import korrit.kotlin.ktor.controllers.POST
import korrit.kotlin.ktor.controllers.PUT
import korrit.kotlin.ktor.controllers.errors
import korrit.kotlin.ktor.controllers.path
import korrit.kotlin.ktor.controllers.query
import korrit.kotlin.ktor.controllers.responds
import kotlin.math.min

fun Route.entitiesController(
    entityService: EntityService
) {

    class Entities : EmptyBodyInput() {
        val limit: Int by query(default = 0)
        val page: Int by query(default = 0)

        override suspend fun PipelineContext<Unit, ApplicationCall>.respond() {
            val entities = entityService.getEntities().takePage(limit, page)
            val response = EntitiesList(entities)

            call.respond(response)
        }
    }

    class EntityById : EmptyBodyInput() {
        val id: Long by path()

        override suspend fun PipelineContext<Unit, ApplicationCall>.respond() {
            val entity = entityService.getEntity(id)

            call.respond(entity)
        }
    }

    class EntityByCode : EmptyBodyInput() {
        val code: String by path(name = "entityCode")

        override suspend fun PipelineContext<Unit, ApplicationCall>.respond() {
            val entity = entityService.getEntityByCode(code)

            call.respond(entity)
        }
    }

    class NewEntity : Input<Entity>() {

        override suspend fun PipelineContext<Unit, ApplicationCall>.respond() {
            val id = entityService.addEntity(entity = body())

            call.response.headers.append(Location, "api/entities/$id")
            call.respond(Created)
        }
    }

    class UpdateEntity : Input<Entity>() {
        val id: Long by path()

        override suspend fun PipelineContext<Unit, ApplicationCall>.respond() {
            entityService.updateEntity(id, entity = body())

            call.response.headers.append(Location, "api/entities/$id")
            call.respond(OK)
        }
    }

    route("/entities") {
        GET("/") { Entities() }
            .responds<EntitiesList>(OK)
            .errors(BadRequest)

        // Order is important for Ktor, this needs to be before "/{id}"
        GET("/code={entityCode}") { EntityByCode() }
            .responds<Entity>(OK)
            .errors(NotFound)

        GET("/{id}") { EntityById() }
            .responds<Entity>(OK)
            .errors(NotFound)

        POST("/") { NewEntity() }
            .responds<Unit>(Created, headers = listOf(HttpHeader(Location)))
            .errors(BadRequest)

        PUT("/{id}") { UpdateEntity() }
            .responds<Unit>(OK, headers = listOf(HttpHeader(Location)))
            .errors(BadRequest)
    }
}

private fun <T> List<T>.takePage(limit: Int, page: Int): List<T> {
    if (page < 0) {
        throw BadRequestException("Pagination page cannot be negative")
    }

    if (limit <= 0) {
        return this
    }

    val fromIndex = limit * page
    if (fromIndex >= size) {
        return emptyList()
    }

    val toIndex = min(limit * (page + 1), size)

    return subList(fromIndex, toIndex)
}
