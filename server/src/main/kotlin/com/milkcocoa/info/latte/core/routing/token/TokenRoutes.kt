package com.milkcocoa.info.latte.core.routing.token

import com.milkcocoa.info.latte.cache.CacheBackend
import com.milkcocoa.info.latte.core.token.TokenGenerator
import com.milkcocoa.info.latte.model.proxytoken.ProxyTokenRequest
import com.milkcocoa.info.latte.model.proxytoken.ProxyTokenResponse
import com.milkcocoa.info.latte.model.token.TokenResponse
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import java.security.SecureRandom
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

@Resource("/proxytoken")
class TokenRoutes()


@OptIn(ExperimentalEncodingApi::class, ExperimentalTime::class)
fun Route.tokenRoutes(
    tokenStorage: CacheBackend,
    tokenGenerator: TokenGenerator
){
    post<TokenRoutes>{
        val request: ProxyTokenRequest = call.receive()

        val token = tokenGenerator.generate()
        val value = ByteArray(16)
        SecureRandom().nextBytes(value)

        val expiresAt = tokenStorage.write(
            key = token,
            value = value,
            expire = 300.seconds
        )

        call.respond(
            status = HttpStatusCode.OK,
            message = ProxyTokenResponse(
                proxyToken = token,
                expiresAt = expiresAt
            )
        )
    }
}