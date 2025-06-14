package com.milkcocoa.info.latte

import com.milkcocoa.info.latte.core.ConnectionInfo
import com.milkcocoa.info.latte.core.LatteException
import com.milkcocoa.info.latte.core.executeCatching
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
import kotlinx.serialization.SerializationException
import kotlinx.serialization.properties.Properties
import kotlinx.serialization.properties.encodeToStringMap
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


/**
 * HTTPステータスコードがクライアントエラー（400-499）かどうかを判定します。
 *
 * @return クライアントエラーの場合はtrue、それ以外の場合はfalse
 */
internal fun HttpStatusCode.isClientError() = this.value in 400..499

/**
 * HTTPステータスコードがサーバーエラー（500-599）かどうかを判定します。
 *
 * @return サーバーエラーの場合はtrue、それ以外の場合はfalse
 */
internal fun HttpStatusCode.isServerError() = this.value in 500..599


/**
 * 日本郵便のデジタルアドレス・郵便番号APIにアクセスするためのクライアントクラス。
 * 
 * このクラスは直接APIに接続する方法とプロキシサーバーを経由する方法の両方をサポートしています。
 * インスタンスの作成には、companion objectの`of`メソッドを使用してください。
 *
 * @property connectionInfo 接続情報を含むオブジェクト
 */
class Latte private constructor(
    val connectionInfo: ConnectionInfo
) {
    /**
     * トークン情報を保持するための内部クラス。
     *
     * @property tokenRaw APIから取得したトークンレスポンス
     * @property issuedAt トークンが発行された時刻
     */
    @OptIn(ExperimentalTime::class)
    private data class Token(
        val tokenRaw: TokenResponse,
        val issuedAt: Instant
    )

    /** 現在のトークン情報 */
    private var _currentToken: Token? = null

    /** 現在のトークン文字列 */
    private val currentToken: String? = _currentToken?.tokenRaw?.token


    /**
     * Latteインスタンスを作成するためのファクトリメソッドを提供するコンパニオンオブジェクト。
     */
    companion object{
        /**
         * プロキシサーバーを使用してLatteインスタンスを作成します。
         *
         * @param url プロキシサーバーのURL
         * @return 新しいLatteインスタンス
         */
        fun of(url: String): Latte{
            return Latte(ConnectionInfo.Proxy(url))
        }

        /**
         * 直接APIに接続するLatteインスタンスを作成します。
         *
         * @param url 日本郵便APIのエンドポイントURL
         * @param clientId クライアントID
         * @param secretKey シークレットキー
         * @param forwardedFor X-Forwarded-Forヘッダーの値（
         * @return 新しいLatteインスタンス
         */
        fun of(url: String, clientId: String, secretKey: String, forwardedFor: String): Latte{
            return Latte(ConnectionInfo.Direct(url, clientId, secretKey, forwardedFor))
        }

        /**
         * 指定された接続情報を使用してLatteインスタンスを作成します。
         *
         * @param connectionInfo 接続情報
         * @return 新しいLatteインスタンス
         */
        fun of(connectionInfo: ConnectionInfo): Latte{
            return Latte(connectionInfo)
        }
    }



    /**
     * APIトークンを取得します。
     * 
     * このメソッドは新しいトークンをAPIから取得し、内部のトークンストアを更新します。
     * 接続タイプ（直接またはプロキシ）に応じて適切なリクエストを行います。
     *
     * @return 取得したトークンレスポンス
     * @throws LatteException APIリクエスト中にエラーが発生した場合
     */
    @OptIn(ExperimentalTime::class)
    suspend fun token(): TokenResponse{
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
            tokenRaw = tokenResponse,
            issuedAt = Clock.System.now()
        )

        return tokenResponse
    }

    /**
     * 有効なトークンを使用して指定された処理を実行します。
     * 
     * このメソッドは内部のトークンストアから有効なトークンを取得し、
     * トークンが期限切れの場合は新しいトークンを取得します。
     * トークンの有効期限は発行から420秒（7分）と想定しています。
     *
     * @param R 処理の戻り値の型
     * @param block トークンを使用して実行する処理
     * @return 処理の結果
     * @throws LatteException トークン取得中にエラーが発生した場合
     */
    @OptIn(ExperimentalTime::class)
    suspend fun<R> withToken(block: suspend (String) -> R): R{
        val token = _currentToken?.takeIf {
            Clock.System.now() > it.issuedAt.plus(420.seconds)
        }?.tokenRaw ?: token()

        return block(token.token)
    }

    /**
     * 住所情報から郵便番号を検索します。
     *
     * @param token 認証トークン
     * @param request 検索リクエスト
     * @return 検索結果のレスポンス
     * @throws LatteException APIリクエスト中にエラーが発生した場合
     */
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

    /**
     * 住所情報から郵便番号を検索します。
     * DSL形式でリクエストを構築できるオーバーロードメソッドです。
     *
     * @param token 認証トークン
     * @param request リクエストを構築するラムダ式
     * @return 検索結果のレスポンス
     * @throws LatteException APIリクエスト中にエラーが発生した場合
     */
    suspend fun addressZip(token: String, request: AddressZipRequest.() -> Unit) = addressZip(
        token = token,
        request = AddressZipRequest().apply(request)
    )

    /**
     * 郵便番号やデジタルアドレスから住所情報を検索します。
     *
     * @param token 認証トークン
     * @param searchCode 検索コード（郵便番号やデジタルアドレス）
     * @param params 検索パラメータ
     * @return 検索結果のレスポンス
     * @throws LatteException APIリクエスト中にエラーが発生した場合
     */
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

    /**
     * 郵便番号やデジタルアドレスから住所情報を検索します。
     * DSL形式でリクエストパラメータを構築できるオーバーロードメソッドです。
     *
     * @param token 認証トークン
     * @param searchCode 検索コード（郵便番号やデジタルアドレス）
     * @param params 検索パラメータを構築するラムダ式
     * @return 検索結果のレスポンス
     * @throws LatteException APIリクエスト中にエラーが発生した場合
     */
    suspend fun search(token: String, searchCode: String, params: SearchCodeRequest.() -> Unit) = search(
        token = token,
        searchCode = searchCode,
        params = SearchCodeRequest().apply(params)
    )


}
