package com.vodeno.datalake.dictionary

import com.vodeno.datalake.dictionary.api.http.httpApi
import com.vodeno.datalake.dictionary.configuration.configuration
import koriit.kotlin.slf4j.logback.closeLoggers
import koriit.kotlin.slf4j.mdc.correlation.correlateThread
import korrit.kotlin.kodein.application.kodeinApplication
import korrit.kotlin.kodein.application.run

val DictionaryApplication = kodeinApplication {
    import(configuration)
    import(httpApi, allowOverride = true)
}

fun main() {
    // Add correlation Id to MDC
    correlateThread()

    DictionaryApplication.run()

    // Finish logging threads and flush buffers
    closeLoggers()
}
