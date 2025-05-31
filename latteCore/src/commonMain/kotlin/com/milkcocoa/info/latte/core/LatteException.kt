package com.milkcocoa.info.latte.core

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

sealed class LatteException: Exception() {
    @OptIn(ExperimentalUuidApi::class)
    @Serializable
    @SerialName("ApiCallFailed")
    data class ApiCallFailed(
        @SerialName("request_id")
        val requestId: Uuid,
        @SerialName("error_code")
        val errorCode: String,
        @SerialName("message")
        override val message: String
    ): LatteException()

    class UnknownServerError(
        override val cause: Throwable?
    ): LatteException()

    class UnknownClientError(
        override val cause: Throwable?
    ): LatteException()

    data object NoInternetConnection: LatteException()
    data object NetworkTimeout: LatteException()
    data class Unknown(
        override val cause: Throwable?
    ): LatteException()
}