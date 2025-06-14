package com.milkcocoa.info.latte.model.token

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * トークン取得レスポンスを表すデータクラス。
 * 
 * このクラスは日本郵便APIのトークンエンドポイントからの応答の
 * 形式を定義します。
 *
 * @property token アクセストークン
 * @property tokenType トークンタイプ（通常は "Bearer"）
 * @property expiresIn トークンの有効期限（秒）
 * @property scope トークンのスコープ
 */
@Serializable
@SerialName("TokenResponse")
data class TokenResponse(
    @SerialName("token")
    val token: String,
    @SerialName("token_type")
    val tokenType: String,
    @SerialName("expires_in")
    val expiresIn: Int,
    @SerialName("scope")
    val scope: String
)
