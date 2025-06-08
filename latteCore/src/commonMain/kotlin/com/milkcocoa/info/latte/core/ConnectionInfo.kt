package com.milkcocoa.info.latte.core

sealed interface ConnectionInfo{
    val host: String
    val searchCodePath: String
    val addressZipPath: String
    val tokenPath: String

    class Proxy(
        override val host: String,
        override val tokenPath: String = "/api/v1/j/token",
        override val searchCodePath: String = "/api/v1/searchcode",
        override val addressZipPath: String = "/api/v1/addresszip"
    ): ConnectionInfo{
        private var _credentialsProvider: CredentialsProvider? = null
        val credentialsProvider: CredentialsProvider? get() = _credentialsProvider

        fun with(credentialsProvider: CredentialsProvider): Proxy{
            _credentialsProvider = credentialsProvider
            return this
        }
    }

    class Direct(
        override val host: String,
        val clientId: String,
        val secretKey: String,
        val forwardedFor: String
    ): ConnectionInfo{
        override val tokenPath: String = "/api/v1/j/token"
        override val searchCodePath: String = "/api/v1/searchcode"
        override val addressZipPath: String = "/api/v1/addresszip"
    }
}