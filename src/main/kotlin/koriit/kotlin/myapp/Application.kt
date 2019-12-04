package koriit.kotlin.myapp

import koriit.kotlin.myapp.api.http.httpApi
import koriit.kotlin.myapp.configuration.configuration
import koriit.kotlin.slf4j.logback.closeLoggers
import koriit.kotlin.slf4j.mdc.correlation.correlateThread
import korrit.kotlin.kodein.application.kodeinApplication
import korrit.kotlin.kodein.application.run

val MyApplication = kodeinApplication {
    import(configuration)
    import(httpApi, allowOverride = true)
}

fun main() {
    // Add correlation Id to MDC
    correlateThread()

    MyApplication.run()

    // Finish logging threads and flush buffers
    closeLoggers()
}
