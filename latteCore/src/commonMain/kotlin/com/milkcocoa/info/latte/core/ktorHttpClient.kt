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

/**
 * プラットフォーム固有のHTTPクライアントを提供します。
 * 
 * この関数は各プラットフォーム（JVM、Android、iOS、JS）で異なる実装を持ちます。
 *
 * @return 設定済みのHTTPクライアント
 */
expect fun ktorHttpClient(): HttpClient


/**
 * HTTPレスポンスを処理し、適切な型に変換または例外をスローします。
 * 
 * ステータスコードに基づいて以下の処理を行います：
 * - 成功（2xx）: レスポンスボディを指定された型にデシリアライズ
 * - クライアントエラー（4xx）: 適切な例外をスロー
 * - サーバーエラー（5xx）: 適切な例外をスロー
 * - その他: 不明なエラー例外をスロー
 *
 * @param T 期待される戻り値の型
 * @return デシリアライズされたレスポンスボディ
 * @throws LatteException レスポンスがエラーを示す場合
 */
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

/**
 * 例外を適切なLatteExceptionに変換します。
 * 
 * ネットワーク関連の例外を適切なLatteExceptionに変換し、
 * すでにLatteExceptionの場合はそのまま再スローします。
 *
 * @throws LatteException 変換された例外
 */
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

/**
 * HTTPリクエストを実行し、エラー処理を行います。
 * 
 * 指定されたブロックを実行し、成功した場合はレスポンスを処理し、
 * 失敗した場合は適切な例外に変換します。
 *
 * @param R 期待される戻り値の型
 * @param block 実行するHTTPリクエスト
 * @return 処理されたレスポンス
 * @throws LatteException リクエスト中にエラーが発生した場合
 */
internal suspend inline fun<reified R> executeCatching(block: suspend () -> HttpResponse): R{
    return runCatching {
        block()
    }.fold(
        onSuccess = { it.handle<R>() },
        onFailure = { it.relocate() }
    )
}
