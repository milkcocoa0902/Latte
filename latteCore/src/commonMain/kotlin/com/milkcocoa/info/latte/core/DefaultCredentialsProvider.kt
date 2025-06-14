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

/**
 * Latteプロキシサーバー用のデフォルト認証情報プロバイダー。
 * 
 * このプロバイダーは、プロキシサーバーの/proxytokenエンドポイントを使用して
 * APIキーを取得し、キャッシュします。キャッシュされたトークンが期限切れに
 * 近づくと、自動的に新しいトークンを取得します。
 */
object DefaultCredentialsProvider: CredentialsProvider {
    /** スレッドセーフな操作のためのミューテックス */
    private val mutex = Mutex()

    /**
     * 認証情報を保持するための内部クラス。
     *
     * @property token APIキー
     * @property expiresAt トークンの有効期限
     */
    @OptIn(ExperimentalTime::class)
    private data class Credentials(
        val token: String,
        val expiresAt: Instant,
    )

    /** 現在の認証情報 */
    private var currentCredentials: Credentials? = null

    /**
     * 指定されたホストに対する認証情報を提供します。
     * 
     * このメソッドは以下の処理を行います：
     * 1. 有効な認証情報がキャッシュにある場合はそれを返す
     * 2. 認証情報がない、または期限切れが近い場合は/proxytokenエンドポイントから新しい認証情報を取得
     * 3. 取得した認証情報をキャッシュして返す
     *
     * @param host 接続先のホストURL
     * @return ヘッダー名と値のペア（"X-API-KEY" to "api-key-value"）
     * @throws LatteException トークン取得中にエラーが発生した場合
     */
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
