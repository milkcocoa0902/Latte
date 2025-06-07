package com.milkcocoa.info.latte

import com.milkcocoa.info.latte.core.LatteException
import com.milkcocoa.info.latte.core.routing.jpp.v1.digitalAddressRoutes
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.ratelimit.RateLimiter
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.plugins.statuspages.exception
import io.ktor.server.resources.Resources
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

fun Application.module() {
    val limit = environment.config.property("ktor.limit.token").getString().toIntOrNull() ?: 60
    val refill = environment.config.property("ktor.limit.period").getString().toIntOrNull() ?: 300

    install(RateLimit){
        global {
            requestKey { it.request.local.remoteAddress }
            rateLimiter(
                limit = limit,
                refillPeriod = refill.seconds,
            )
        }
    }
    install(Resources)
    install(ContentNegotiation){
        json(Json {
            ignoreUnknownKeys = true
            encodeDefaults = false
        })
    }
    install(CORS){
        allowOrigins { true }

        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)

        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Accept)
        allowHeader(HttpHeaders.UserAgent)
        allowHeader(HttpHeaders.AcceptEncoding)
        allowHeader(HttpHeaders.ContentEncoding)
        allowHeader(HttpHeaders.ContentLength)
    }

    install(StatusPages){
        status(HttpStatusCode.TooManyRequests){
            call.respond(
                status = HttpStatusCode.TooManyRequests,
                message = LatteException.ProxyError(
                    errorCode = "429-0001",
                    message = "APIの実行回数制限を超過しました。"
                )
            )
        }

        exception<Exception>{ call, cause ->
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = LatteException.ProxyError(
                    errorCode = "500-0001",
                    message = "不明なエラーが発生しました。"
                )
            )
        }
    }

    routing {
        digitalAddressRoutes(
            latte = Latte.of(
                url = environment.config.property("latte.endpoint").getString(),
                clientId = environment.config.property("latte.clientId").getString(),
                secretKey = environment.config.property("latte.secretKey").getString(),
                forwardedFor = ""
            )
        )
    }
}