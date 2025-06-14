package com.milkcocoa.info.latte.model.token

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * トークン取得リクエストを表すデータクラス。
 * 
 * このクラスは日本郵便APIのトークンエンドポイントに送信されるリクエストの
 * 形式を定義します。
 *
 * @property clientId クライアントID
 * @property secretKey シークレットキー
 * @property grantType 認証タイプ（常に "client_credentials"）
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
@SerialName("TokenRequest")
data class TokenRequest(
    @SerialName("client_id")
    val clientId: String,
    @SerialName("secret_key")
    val secretKey: String
){
    /** 認証タイプ（常に "client_credentials"） */
    @SerialName("grant_type")
    @EncodeDefault(mode = EncodeDefault.Mode.ALWAYS)
    val grantType: String = "client_credentials"
}
