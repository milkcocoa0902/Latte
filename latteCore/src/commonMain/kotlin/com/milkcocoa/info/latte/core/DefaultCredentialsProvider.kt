package com.milkcocoa.info.latte.core

import com.milkcocoa.info.latte.model.proxytoken.ProxyTokenRequest
import com.milkcocoa.info.latte.model.proxytoken.ProxyTokenResponse
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.headers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

object DefaultCredentialsProvider: CredentialsProvider {
    private val mutex = Mutex()
    @OptIn(ExperimentalTime::class)
    private data class Credentials(
        val token: String,
        val expiresAt: Instant,
    )

    private var currentCredentials: Credentials? = null

    @OptIn(ExperimentalTime::class)
    override suspend fun provide(host: String): Pair<String, String> {
        return mutex.withLock {
            currentCredentials?.let {
                // 手元にトークンを持っている
                if(Clock.System.now() + 60.seconds < it.expiresAt) {
                    // 「いま」から60秒以上猶予がある
                    return@withLock "X-API-KEY" to it.token
                }
            }

            // 手元にトークンを持っていない
            // あるいは、「いま」から60秒後には切れている

            val proxyTokenResponse: ProxyTokenResponse = executeCatching {
                ktorHttpClient().post(urlString = host + "/proxytoken"){
                    headers{
                        contentType(ContentType.Application.Json)
                    }
                    setBody(ProxyTokenRequest())
                }
            }

            currentCredentials = Credentials(
                token = proxyTokenResponse.proxyToken,
                expiresAt = proxyTokenResponse.expiresAt
            )

            return@withLock "X-API-KEY" to proxyTokenResponse.proxyToken
        }
    }
}