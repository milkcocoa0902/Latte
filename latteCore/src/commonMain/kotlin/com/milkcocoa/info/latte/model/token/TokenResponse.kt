package com.milkcocoa.info.latte.model.token

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
