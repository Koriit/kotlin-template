package koriit.kotlin.myapp.api.http.controllers

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.util.pipeline.PipelineContext
import koriit.kotlin.myapp.api.http.responses.VersionDetails
import koriit.kotlin.myapp.helpers.BuildInfo
import korrit.kotlin.ktor.controllers.EmptyBodyInput
import korrit.kotlin.ktor.controllers.GET
import korrit.kotlin.ktor.controllers.responds

fun Route.appVersionController(
    buildInfo: BuildInfo
) {

    class Version : EmptyBodyInput() {
        override suspend fun PipelineContext<Unit, ApplicationCall>.respond() {
            call.respond(VersionDetails(buildInfo.projectVersion))
        }
    }

    GET("/version") { Version() }
        .responds<VersionDetails>(OK)
}
