package koriit.kotlin.myapp.api.http.controllers

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize
import com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath
import com.jayway.jsonpath.matchers.JsonPathMatchers.isJson
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpHeaders.Location
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.withCharset
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.contentType
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import java.util.UUID
import koriit.kotlin.myapp.TestApplication
import koriit.kotlin.myapp.test.helpers.asInt
import koriit.kotlin.myapp.test.helpers.asString
import koriit.kotlin.myapp.test.helpers.assertApiError
import koriit.kotlin.myapp.test.helpers.testCases
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.emptyOrNullString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.nullValue
import org.hamcrest.core.IsNot
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance

internal class UsersControllerTest : KodeinAware {

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

    @Nested
    @DisplayName("GET /users")
    inner class GetUsers {

        @Test
        fun `Should return list of users`() = with(server) {
            with(handleRequest(HttpMethod.Get, "/api/users")) {
                assertThat(response.status(), equalTo(OK))
                assertThat(response.contentType(), equalTo(ContentType.Application.Json.withCharset(Charsets.UTF_8)))
                assertThat(response.content, isJson())
                assertThat(response.content, hasJsonPath("$.users", hasSize(5)))
                assertThat(response.content, hasJsonPath("$.users[?(@.login=='admin')]"))
                assertThat(response.content, hasJsonPath("$.users[?(@.login=='john')]"))
                assertThat(response.content, hasJsonPath("$.users[?(@.login=='kowal123')]"))
                assertThat(response.content, hasJsonPath("$.users[?(@.login=='mieszko1')]"))
                assertThat(response.content, hasJsonPath("$.users[?(@.login=='krex')]"))
            }
        }

        @Test
        fun `Should allow to paginate items`() = with(server) {
            data class Case(
                val limit: Int,
                val page: Int,
                val results: Int
            )

            listOf(
                Case(0, 0, 5),
                Case(1, 0, 1),
                Case(2, 0, 2),
                Case(5, 0, 5),
                Case(5, 1, 0),
                Case(6, 0, 5),
                Case(3, 0, 3),
                Case(3, 1, 2),
                Case(3, 2, 0),
                Case(3, 3, 0)
            ).testCases {
                with(handleRequest(HttpMethod.Get, "/api/users?limit=$limit&page=$page")) {
                    assertThat(response.status(), equalTo(OK))
                    assertThat(response.contentType(), equalTo(ContentType.Application.Json.withCharset(Charsets.UTF_8)))
                    assertThat(response.content, isJson())
                    assertThat(response.content, hasJsonPath("$.users", hasSize(results)))
                }
            }
        }

        @Test
        fun `Should return 400 for negative page`() = with(server) {
            with(handleRequest(HttpMethod.Get, "/api/users?limit=0&page=-1")) {
                assertApiError(BadRequest)
                assertThat(response.content, hasJsonPath("$[?(@.detail =~ /.*page.*/)]"))
                assertThat(response.content, hasJsonPath("$[?(@.detail =~ /.*negative.*/)]"))
            }
        }
    }

    @Nested
    @DisplayName("GET /user/{userId}")
    inner class GetUserById {

        @Test
        fun `Should return user body`() = with(server) {
            with(handleRequest(HttpMethod.Get, "/api/users/50")) {
                assertThat(response.status(), equalTo(OK))
                assertThat(response.contentType(), equalTo(ContentType.Application.Json.withCharset(Charsets.UTF_8)))
                assertThat(response.content, isJson())
                assertThat(response.content, hasJsonPath("$.code", asString))
                assertThat(response.content, hasJsonPath("$.id", asInt))
            }
        }

        @Test
        fun `Should return 404 when user not found`() = with(server) {
            with(handleRequest(HttpMethod.Get, "/api/users/1337")) {
                assertApiError(NotFound)
                assertThat(response.content, hasJsonPath("$[?(@.detail =~ /.*1337.*/)]"))
            }
        }
    }

    @Nested
    @DisplayName("GET /user/code={userCode}")
    inner class GetUserByCode {

        @Test
        fun `Should return user body`() = with(server) {
            with(handleRequest(HttpMethod.Get, "/api/users/code=user_50")) {
                assertThat(response.status(), equalTo(OK))
                assertThat(response.contentType(), equalTo(ContentType.Application.Json.withCharset(Charsets.UTF_8)))
                assertThat(response.content, isJson())
                assertThat(response.content, hasJsonPath("$.code", asString))
                assertThat(response.content, hasJsonPath("$.id", asInt))
            }
        }

        @Test
        fun `Should return 404 when user not found`() = with(server) {
            with(handleRequest(HttpMethod.Get, "/api/users/code=NOT_EXISTING_user")) {
                assertApiError(NotFound)
                assertThat(response.content, hasJsonPath("$[?(@.detail =~ /.*NOT_EXISTING_user.*/)]"))
            }
        }
    }

    @Nested
    @DisplayName("POST /users")
    inner class PostSearch {

        @Test
        fun `Should add users`() {
            registerUser("user_TEST_POST")
        }

        @Test
        fun `Should return 415 when no content type`() = with(server) {
            with(handleRequest(HttpMethod.Post, "/api/users")) {
                assertApiError(HttpStatusCode.UnsupportedMediaType)
            }
        }

        @Test
        fun `Should return 400 when invalid request body`() = with(server) {
            listOf(
                """{}""",
                "asdf",
                "1337",
                ""
            ).testCases {
                with(handleRequest(HttpMethod.Post, "/api/users") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(this@testCases)
                }) {
                    assertApiError(BadRequest)
                }
            }
        }
    }

    @Nested
    @DisplayName("PUT /user/{id}")
    inner class PutUser {

        @Test
        fun `Should update user`() = with(server) {
            val id = registerUser("user_TEST_PUT")

            val location = with(handleRequest(HttpMethod.Put, "/api/users/$id") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody("""{"code": "user_TEST_PUT_2", "id": 1337}""")
            }) {
                assertThat(response.status(), equalTo(OK))
                assertThat(response.content, emptyOrNullString())

                response.headers[Location].also { assertThat(it, not(nullValue())) }!!
            }

            with(handleRequest(HttpMethod.Get, "/$location")) {
                assertThat(response.status(), equalTo(OK))
                assertThat(response.contentType(), equalTo(ContentType.Application.Json.withCharset(Charsets.UTF_8)))
                assertThat(response.content, isJson())
                assertThat(response.content, hasJsonPath("$.code", equalTo("user_TEST_PUT_2")))
                assertThat(response.content, hasJsonPath("$.id", equalTo(id)))
            }
        }

        @Test
        fun `Should return 415 when no content type`() = with(server) {
            with(handleRequest(HttpMethod.Put, "/api/users/1337")) {
                assertApiError(HttpStatusCode.UnsupportedMediaType)
            }
        }

        @Test
        fun `Should return 400 when invalid request body`() = with(server) {
            listOf(
                """{}""",
                """{"id": 1337}""",
                "asdf",
                "1337",
                ""
            ).testCases {
                with(handleRequest(HttpMethod.Put, "/api/users/1337") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(this@testCases)
                }) {
                    assertApiError(BadRequest)
                }
            }
        }

        @Test
        fun `Should return 404 when user not found`() = with(server) {
            with(handleRequest(HttpMethod.Put, "/api/users/1337") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody("""{"code": "user_1337"}""")
            }) {
                assertApiError(NotFound)
            }
        }
    }

    @Test
    fun `Should respond with X-Request-Id header`() = with(server) {
        with(handleRequest(HttpMethod.Get, "/api/users")) {
            assertThat(response.status(), equalTo(OK))
            assertThat(response.headers[HttpHeaders.XRequestId], IsNot(emptyOrNullString()))
        }
    }

    @Test
    fun `Should pass X-Request-Id header`() = with(server) {
        val requestId = UUID.randomUUID().toString()

        with(handleRequest(HttpMethod.Get, "/api/users") {
            addHeader(HttpHeaders.XRequestId, requestId)
        }) {
            assertThat(response.status(), equalTo(OK))
            assertThat(response.headers[HttpHeaders.XRequestId], equalTo(requestId))
        }
    }

    private fun registerUser(code: String): Int = with(server) {
        val location = with(handleRequest(HttpMethod.Post, "/api/users") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody("""{"code": "$code"}""")
        }) {
            assertThat(response.status(), equalTo(Created))
            assertThat(response.content, emptyOrNullString())

            response.headers[Location].also { assertThat(it, not(nullValue())) }!!
        }

        with(handleRequest(HttpMethod.Get, "/$location")) {
            assertThat(response.status(), equalTo(OK))
            assertThat(response.contentType(), equalTo(ContentType.Application.Json.withCharset(Charsets.UTF_8)))
            assertThat(response.content, isJson())
            assertThat(response.content, hasJsonPath("$.code", equalTo(code)))
            assertThat(response.content, hasJsonPath("$.id", asInt))
        }

        location.split("/").last().toInt()
    }
}
