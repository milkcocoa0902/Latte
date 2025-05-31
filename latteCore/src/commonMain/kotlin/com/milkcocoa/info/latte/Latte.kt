package com.milkcocoa.info.latte

import com.milkcocoa.info.latte.core.ConnectionInfo
import com.milkcocoa.info.latte.core.LatteException
import com.milkcocoa.info.latte.core.ktorHttpClient
import com.milkcocoa.info.latte.model.addresszip.AddressZipRequest
import com.milkcocoa.info.latte.model.addresszip.AddressZipResponse
import com.milkcocoa.info.latte.model.searchcode.SearchCodeRequest
import com.milkcocoa.info.latte.model.searchcode.SearchCodeResponse
import com.milkcocoa.info.latte.model.token.TokenRequest
import com.milkcocoa.info.latte.model.token.TokenResponse
import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.http.parameters
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.io.IOException
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.properties.Properties
import kotlinx.serialization.properties.encodeToStringMap
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


internal fun HttpStatusCode.isClientError() = this.value in 400..499
internal fun HttpStatusCode.isServerError() = this.value in 500..599


class Latte private constructor(
    val connectionInfo: ConnectionInfo
) {
    @OptIn(ExperimentalTime::class)
    private data class Token(
        val token: String,
        val issuedAt: Instant
    )

    private var _currentToken: Token? = null
    private val currentToken: String? = _currentToken?.token


    companion object{
        fun of(url: String): Latte{
            return Latte(ConnectionInfo.Proxy(url))
        }

        fun of(url: String, clientId: String, secretKey: String, forwardedFor: String): Latte{
            return Latte(ConnectionInfo.Direct(url, clientId, secretKey, forwardedFor))
        }

        fun of(connectionInfo: ConnectionInfo): Latte{
            return Latte(connectionInfo)
        }
    }



    @OptIn(ExperimentalTime::class)
    suspend fun token(): String{
        val credentials = when(connectionInfo){
            is ConnectionInfo.Direct -> null
            is ConnectionInfo.Proxy -> connectionInfo.credentialsProvider?.provide(connectionInfo.host)
        }

        val tokenResponse: TokenResponse = executeCatching {
            ktorHttpClient().post(connectionInfo.host.trimEnd('/') + connectionInfo.tokenPath){
                headers {
                    contentType(ContentType.Application.Json)
                    if(credentials != null) append(
                        credentials.first,
                        credentials.second
                    )
                }

                when(connectionInfo){
                    is ConnectionInfo.Direct -> {
                        setBody(
                            TokenRequest(
                                clientId = connectionInfo.clientId,
                                secretKey = connectionInfo.secretKey
                            )
                        )
                    }
                    is ConnectionInfo.Proxy -> {
                    }
                }
            }
        }


        _currentToken = Token(
            token = tokenResponse.token,
            issuedAt = Clock.System.now()
        )

        return tokenResponse.token
    }

    @OptIn(ExperimentalTime::class)
    suspend fun<R> withToken(block: suspend (String) -> R): R{
        val token = _currentToken?.takeIf {
            Clock.System.now() > it.issuedAt.plus(420.seconds)
        }?.token ?: token()

        return block(token)
    }

    suspend fun addressZip(token: String, request: AddressZipRequest): AddressZipResponse {
        val credentials = when(connectionInfo){
            is ConnectionInfo.Direct -> null
            is ConnectionInfo.Proxy -> connectionInfo.credentialsProvider?.provide(connectionInfo.host)
        }
        return executeCatching {
            ktorHttpClient().post(connectionInfo.host.trimEnd('/') + connectionInfo.addressZipPath){
                headers{
                    contentType(ContentType.Application.Json)
                    append(HttpHeaders.Authorization, "Bearer $token")
                    if(credentials != null) append(
                        credentials.first,
                        credentials.second
                    )
                }
                setBody(request)
            }
        }
    }

    suspend fun addressZip(token: String, request: AddressZipRequest.() -> Unit) = addressZip(
        token = token,
        request = AddressZipRequest().apply(request)
    )

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun search(token: String, searchCode: String, params: SearchCodeRequest): SearchCodeResponse {
        val credentials = when(connectionInfo){
            is ConnectionInfo.Direct -> null
            is ConnectionInfo.Proxy -> connectionInfo.credentialsProvider?.provide(connectionInfo.host)
        }
        return executeCatching {
            ktorHttpClient().get(connectionInfo.host.trim('/') + connectionInfo.searchCodePath.trimEnd('/') + "/${searchCode}"){
                headers{
                    contentType(ContentType.Application.Json)
                    append(HttpHeaders.Authorization, "Bearer $token")
                    if(credentials != null) append(
                        credentials.first,
                        credentials.second
                    )
                }

                parameters {
                    Properties.Default.encodeToStringMap(params).forEach { (key, value) ->
                        append(key, value)
                    }
                }

                when(connectionInfo){
                    is ConnectionInfo.Direct -> {
                    }
                    is ConnectionInfo.Proxy -> {
                    }
                }
            }
        }
    }

    suspend fun search(token: String, searchCode: String, params: SearchCodeRequest.() -> Unit) = search(
        token = token,
        searchCode = searchCode,
        params = SearchCodeRequest().apply(params)
    )



    private suspend inline fun<reified T> HttpResponse.handle(): T{
        return when{
            status.isSuccess() -> {
                body<T>()
            }
            status.isClientError() -> {
                throw runCatching {
                    body<LatteException.ApiCallFailed>()
                }.getOrElse {
                    LatteException.UnknownClientError(it)
                }
            }
            status.isServerError() -> {
                throw runCatching {
                    body<LatteException.ApiCallFailed>()
                }.getOrElse {
                    LatteException.UnknownServerError(it)
                }
            }
            else -> {
                throw LatteException.Unknown(null)
            }
        }
    }

    private suspend fun Throwable.relocate(): Nothing{
        when(this){
            is ConnectTimeoutException,
            is SocketTimeoutException ->{
                throw LatteException.NetworkTimeout
            }
            is UnresolvedAddressException,
            is IOException ->{
                throw LatteException.NoInternetConnection
            }
            is LatteException -> throw this
            else -> throw LatteException.Unknown(this)
        }
    }


    private suspend inline fun<reified R> executeCatching(block: suspend () -> HttpResponse): R{
        return runCatching {
            block()
        }.fold(
            onSuccess = { it.handle<R>() },
            onFailure = { it.relocate() }
        )
    }
}