package koriit.kotlin.myapp.api.http.controllers

import io.ktor.application.call
import io.ktor.features.BadRequestException
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.route
import koriit.kotlin.myapp.api.http.requests.UserPatch
import koriit.kotlin.myapp.api.http.responses.UserDetails
import koriit.kotlin.myapp.api.http.responses.UsersList
import koriit.kotlin.myapp.domain.NewUser
import koriit.kotlin.myapp.services.UsersService
import korrit.kotlin.ktor.controllers.Ctx
import korrit.kotlin.ktor.controllers.EmptyBodyInput
import korrit.kotlin.ktor.controllers.GET
import korrit.kotlin.ktor.controllers.Input
import korrit.kotlin.ktor.controllers.PATCH
import korrit.kotlin.ktor.controllers.POST
import korrit.kotlin.ktor.controllers.PUT
import korrit.kotlin.ktor.controllers.errors
import korrit.kotlin.ktor.controllers.path
import korrit.kotlin.ktor.controllers.query
import korrit.kotlin.ktor.controllers.responds
import kotlin.math.min

fun Route.usersController(
    usersService: UsersService
) {

    class GetUsers : EmptyBodyInput() {
        val limit: Int by query(default = 20)
        val page: Int by query(default = 0)

        override suspend fun Ctx.respond() {
            if (limit <= 0) throw BadRequestException("Pagination requires 'limit' to be greater than 0")
            if (page < 0) throw BadRequestException("Pagination requires 'page' to be greater or equal 0")
            val details = usersService
                .getUsers(page, limit)
                .map { UserDetails(it) }

            val response = UsersList(details)

            call.respond(response)
        }
    }

    class GetUserById : EmptyBodyInput() {
        val id: Long by path()

        override suspend fun Ctx.respond() {
            val user = usersService.getUser(id)

            call.respond(UserDetails(user))
        }
    }

    class GetUserByLogin : EmptyBodyInput() {
        val login: String by path(name = "userLogin")

        override suspend fun Ctx.respond() {
            val user = usersService.getUserByLogin(login)

            call.respond(UserDetails(user))
        }
    }

    class RegisterUser : Input<NewUser>() {

        override suspend fun Ctx.respond() {
            val user = usersService.registerUser(user = body())

            call.respond(Created, UserDetails(user))
        }
    }

    class UpdateUser : Input<UserPatch>() {
        val id: Long by path()

        override suspend fun Ctx.respond() {
            val update: UserPatch = body()

            val user = usersService.getUser(id)
            val updated = update.updated(user)
            usersService.update(updated)

            call.respond(UserDetails(user))
        }
    }

    class PatchUser : Input<UserPatch>() {
        val id: Long by path()

        override suspend fun Ctx.respond() {
            val patch: UserPatch = body()

            val user = usersService.getUser(id)
            val patched = patch.patched(user)
            usersService.update(patched)

            call.respond(UserDetails(user))
        }
    }

    route("/users") {
        GET("/") { GetUsers() }
            .responds<UsersList>(OK)
            .errors(BadRequest)

        // Order is important for Ktor, this needs to be before "/{id}"
        GET("/login={userLogin}") { GetUserByLogin() }
            .responds<UserDetails>(OK)
            .errors(NotFound)

        GET("/{id}") { GetUserById() }
            .responds<UserDetails>(OK)
            .errors(NotFound)

        POST("/") { RegisterUser() }
            .responds<UserDetails>(Created)
            .errors(BadRequest)

        PUT("/{id}") { UpdateUser() }
            .responds<UserDetails>(OK)
            .errors(BadRequest)
            .errors(NotFound)

        PATCH("/{id}") { PatchUser() }
            .responds<UserDetails>(OK)
            .errors(BadRequest)
            .errors(NotFound)
    }
}

/**
 * Pagination for list collections.
 */
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
