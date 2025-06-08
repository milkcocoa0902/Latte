package com.milkcocoa.info.latte.core.plugin

import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.*

class HeaderAuthProvider(config: HeaderAuthProviderConfig) : AuthenticationProvider(config) {

    /**
     * utility data class for Header
     */
    data class Header(
        val name: String,
        val value: String
    )
    class HeaderAuthProviderConfig(name: String?) : AuthenticationProvider.Config(name) {
        /**
         * header name
         */
        var headerName: String? = null

        /**
         * handler which called on reject
         */
        var whenReject: (suspend (ApplicationCall, Throwable) -> Unit)? = null

        /**
         * validation func
         */
        var validate: (suspend (ApplicationCall, String) -> Boolean)? = null

        /**
         * skip authentication if someone's condition becomes true
         */
        var skipWhen: List<ApplicationCallPredicate>? = null
    }


    /**
     * specified header is not found on request.
     */
    class HeaderNotProvidedException : Exception()

    /**
     * detected header value is not correct
     */
    class ValidateFailedException : Exception()

    private val headerName: String = config.headerName ?: ""
    private val validator = config.validate

    /**
     * handler which called on reject
     */
    private val whenReject = config.whenReject

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        with(context.call) {
            kotlin.runCatching {
                val headerValue = request.headers.get(headerName) ?: throw HeaderNotProvidedException()
                val result = validator?.invoke(this, headerValue) ?: false

                if (result.not()) {
                    throw ValidateFailedException()
                }
            }.onFailure {
                whenReject?.invoke(this, it)
            }
        }
    }
}

fun AuthenticationConfig.header(
    name: String,
    configure: HeaderAuthProvider.HeaderAuthProviderConfig.() -> Unit,
) {
    val provider = HeaderAuthProvider(
        HeaderAuthProvider.HeaderAuthProviderConfig(name).apply(configure)
    )
    register(provider)
}
