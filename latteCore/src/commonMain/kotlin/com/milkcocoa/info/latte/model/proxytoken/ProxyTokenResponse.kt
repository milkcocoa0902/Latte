package com.milkcocoa.info.latte.model.proxytoken

import com.milkcocoa.info.latte.common.InstantSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * プロキシトークン取得レスポンスを表すデータクラス。
 * 
 * このクラスはプロキシサーバーのトークンエンドポイントからの応答の
 * 形式を定義します。
 *
 * @property proxyToken プロキシサーバーへのアクセスに使用するAPIキー
 * @property expiresAt トークンの有効期限（時刻）
 */
@Serializable
@SerialName("ProxyTokenResponse")
@OptIn(ExperimentalTime::class)
data class ProxyTokenResponse(
    @SerialName("ProxyToken")
    val proxyToken: String,
    @SerialName("expiresAt")
    @Serializable(with =  InstantSerializer::class)
    val expiresAt: Instant,
)
