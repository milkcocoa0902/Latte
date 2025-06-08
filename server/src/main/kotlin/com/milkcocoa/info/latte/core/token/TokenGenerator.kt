package com.milkcocoa.info.latte.core.token

import java.security.MessageDigest
import java.security.SecureRandom
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class TokenGenerator {
    companion object{
        @OptIn(ExperimentalEncodingApi::class)
        val TOKEN_HEADER = Base64.encode("LatteToken".toByteArray())
    }

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun generate(): String{
        val byteArray = ByteArray(32)
        SecureRandom().nextBytes(byteArray)
        val payload = MessageDigest.getInstance("SHA-256")
            .digest(byteArray)
            .let {
                Base64.encode(it)
            }

        return "${TOKEN_HEADER}.${payload}"
    }

    suspend fun verify(tokenString: String): Boolean {
        return tokenString.startsWith("${TOKEN_HEADER}.")
    }
}