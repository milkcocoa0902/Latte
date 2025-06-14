package com.milkcocoa.info.latte.core

import io.ktor.http.HttpStatusCode
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * HTTPステータスコードをシリアライズ/デシリアライズするためのシリアライザー。
 * 
 * このシリアライザーはHTTPステータスコードを整数値として扱います。
 */
object StatusCodeSerializer: KSerializer<HttpStatusCode> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor(
            serialName = "HttpStatusCode",
            kind = PrimitiveKind.INT
        )

    /**
     * 整数値からHTTPステータスコードにデシリアライズします。
     *
     * @param decoder デコーダー
     * @return HTTPステータスコード
     */
    override fun deserialize(decoder: Decoder): HttpStatusCode {
        return HttpStatusCode.fromValue(decoder.decodeInt())
    }

    /**
     * HTTPステータスコードを整数値にシリアライズします。
     *
     * @param encoder エンコーダー
     * @param value HTTPステータスコード
     */
    override fun serialize(encoder: Encoder, value: HttpStatusCode) {
        encoder.encodeInt(value.value)
    }
}

/**
 * Latteライブラリで発生する例外の基底クラス。
 * 
 * このクラスはAPIリクエスト中に発生する様々なエラーを表現するための
 * 共通の基底クラスです。具体的なエラータイプはサブクラスで定義されます。
 */
@Serializable
sealed class LatteException: Throwable() {
    /** HTTPステータスコード */
    @Serializable(with = StatusCodeSerializer::class)
    @SerialName("status_code")
    private var _statusCode: HttpStatusCode? = null

    /** 現在設定されているHTTPステータスコード */
    val statusCode: HttpStatusCode? get() = _statusCode

    /**
     * 例外にHTTPステータスコードを設定します。
     *
     * @param httpStatusCode 設定するHTTPステータスコード
     * @return 自身のインスタンス（メソッドチェーン用）
     */
    fun withCode(httpStatusCode: HttpStatusCode) = apply {
        this._statusCode = httpStatusCode
    }

    /**
     * API呼び出しが失敗した場合の例外。
     * 
     * 日本郵便APIからのエラーレスポンスを表します。
     *
     * @property requestId リクエストID
     * @property errorCode エラーコード
     * @property message エラーメッセージ
     */
    @OptIn(ExperimentalUuidApi::class)
    @Serializable
    @SerialName("ApiCallFailed")
    data class ApiCallFailed(
        @SerialName("request_id")
        val requestId: Uuid,
        @SerialName("error_code")
        val errorCode: String,
        @SerialName("message")
        override val message: String,
    ): LatteException()

    /**
     * プロキシサーバーからのエラーを表す例外。
     *
     * @property errorCode エラーコード
     * @property message エラーメッセージ
     */
    @Serializable
    data class ProxyError(
        @SerialName("error_code")
        val errorCode: String,
        @SerialName("message")
        override val message: String?
    ): LatteException()

    /**
     * 不明なサーバーエラーを表す例外。
     *
     * @property cause 原因となった例外
     */
    class UnknownServerError(
        override val cause: Throwable?,
    ): LatteException()

    /**
     * 不明なクライアントエラーを表す例外。
     *
     * @property cause 原因となった例外
     */
    class UnknownClientError(
        override val cause: Throwable?,
    ): LatteException()

    /** インターネット接続がない場合の例外 */
    data object NoInternetConnection: LatteException()

    /** ネットワークタイムアウトが発生した場合の例外 */
    data object NetworkTimeout: LatteException()

    /**
     * 不明なエラーを表す例外。
     *
     * @property cause 原因となった例外
     */
    data class Unknown(
        override val cause: Throwable?
    ): LatteException()
}
