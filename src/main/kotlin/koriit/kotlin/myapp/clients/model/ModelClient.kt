package koriit.kotlin.myapp.clients.model

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import koriit.kotlin.myapp.clients.model.exceptions.ModelClientException
import koriit.kotlin.slf4j.mdc.correlation.continueCorrelation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

// Example of some service client and use of ktor-client
// Note that use of Dispatchers.IO is required to properly work with logging features
class ModelClient(
    private val httpClient: HttpClient,
    val serviceUrl: String
) {

    /**
     * Get all models.
     */
    fun getModels(): String {
        return runBlocking(Dispatchers.IO + continueCorrelation()) {
            @Suppress("TooGenericExceptionCaught") // intended
            try {
                httpClient.get<String>("$serviceUrl/models")
            } catch (e: Exception) {
                throw ModelClientException("Could not fetch data model", e)
            }
        }
    }

    /**
     * Get single model. Throws if model not found.
     */
    fun getModel(name: String): String {
        return runBlocking(Dispatchers.IO + continueCorrelation()) {
            @Suppress("TooGenericExceptionCaught") // intended
            try {
                httpClient.get<String>("$serviceUrl/models/$name")
            } catch (e: Exception) {
                throw ModelClientException("Could not fetch model of '$name'", e)
            }
        }
    }
}
