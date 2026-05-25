package cz.cvut.fit.phamgiab.filmdevassistant.core.data.api

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import cz.cvut.fit.phamgiab.filmdevassistant.core.domain.HttpException

class ApiClient {
    private val httpClient = HttpClient {
        defaultRequest {
            contentType(ContentType.Application.Json)
            url("http://localhost:7493/api/v1/times/")
        }
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                }
            )
        }
        install(Logging) {
            level = LogLevel.ALL
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d("ktor", message)
                }
            }
        }
    }

    internal suspend inline fun <reified T: Any> request(
        endpoint: String,
        method: HttpMethod,
        requestBuilder: HttpRequestBuilder.() -> Unit = {}
    ) : T {
        val response = httpClient.request(endpoint) {
            this.method = method
            requestBuilder()
        }
        return if (response.status.isSuccess()) {
            response.body()
        } else {
            throw HttpException(response.status.value, response.bodyAsText())
        }
    }
}