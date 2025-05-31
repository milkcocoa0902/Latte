package com.milkcocoa.info.latte.model.token

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@SerialName("TokenRequest")
data class TokenRequest(
    @SerialName("client_id")
    val clientId: String,
    @SerialName("secret_key")
    val secretKey: String
){
    @SerialName("grant_type")
    @EncodeDefault(mode = EncodeDefault.Mode.ALWAYS)
    val grantType: String = "client_credentials"
}