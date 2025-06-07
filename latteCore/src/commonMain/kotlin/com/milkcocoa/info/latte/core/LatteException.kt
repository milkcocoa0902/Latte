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

object StatusCodeSerializer: KSerializer<HttpStatusCode> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor(
            serialName = "HttpStatusCode",
            kind = PrimitiveKind.INT
        )

    override fun deserialize(decoder: Decoder): HttpStatusCode {
        return HttpStatusCode.fromValue(decoder.decodeInt())
    }

    override fun serialize(encoder: Encoder, value: HttpStatusCode) {
        encoder.encodeInt(value.value)
    }
}

@Serializable
sealed class LatteException: Throwable() {
    @Serializable(with = StatusCodeSerializer::class)
    @SerialName("status_code")
    private var _statusCode: HttpStatusCode? = null
    val statusCode: HttpStatusCode? get() = _statusCode

    fun withCode(httpStatusCode: HttpStatusCode) = apply {
        this._statusCode = httpStatusCode
    }

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

    @Serializable
    data class ProxyError(
        @SerialName("error_code")
        val errorCode: String,
        @SerialName("message")
        override val message: String?
    ): LatteException()

    class UnknownServerError(
        override val cause: Throwable?,
    ): LatteException()

    class UnknownClientError(
        override val cause: Throwable?,
    ): LatteException()

    data object NoInternetConnection: LatteException()
    data object NetworkTimeout: LatteException()
    data class Unknown(
        override val cause: Throwable?
    ): LatteException()
}