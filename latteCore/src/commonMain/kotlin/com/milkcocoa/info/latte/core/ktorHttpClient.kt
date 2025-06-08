package com.milkcocoa.info.latte.core

import com.milkcocoa.info.latte.isClientError
import com.milkcocoa.info.latte.isServerError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

expect fun ktorHttpClient(): HttpClient




internal suspend inline fun<reified T> HttpResponse.handle(): T{
    return when{
        status.isSuccess() -> {
            body<T>()
        }
        status.isClientError() -> {
            throw runCatching {
                runCatching {
                    body<LatteException.ProxyError>().withCode(status)
                }.getOrElse { throwable ->
                    when(throwable){
                        is SerializationException -> body<LatteException.ApiCallFailed>().withCode(status)
                        else -> throwable
                    }
                }
            }.getOrElse {
                when(it){
                    is LatteException -> it
                    else -> LatteException.UnknownClientError(it).withCode(status)
                }
            }
        }
        status.isServerError() -> {
            throw runCatching {
                runCatching {
                    body<LatteException.ProxyError>().withCode(status)
                }.getOrElse { throwable ->
                    when(throwable){
                        is SerializationException -> body<LatteException.ApiCallFailed>().withCode(status)
                        else -> throwable
                    }
                }
            }.getOrElse {
                when(it){
                    is LatteException -> it
                    else -> LatteException.UnknownServerError(it).withCode(status)
                }
            }
        }
        else -> {
            throw LatteException.Unknown(null).withCode(status)
        }
    }
}

internal suspend fun Throwable.relocate(): Nothing{
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


internal suspend inline fun<reified R> executeCatching(block: suspend () -> HttpResponse): R{
    return runCatching {
        block()
    }.fold(
        onSuccess = { it.handle<R>() },
        onFailure = { it.relocate() }
    )
}