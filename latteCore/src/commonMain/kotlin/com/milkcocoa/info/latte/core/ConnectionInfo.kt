package com.milkcocoa.info.latte.core

/**
 * APIへの接続情報を表すインターフェース。
 * 
 * このインターフェースは直接APIに接続する方法とプロキシサーバーを経由する方法の
 * 両方をサポートするための共通の抽象化を提供します。
 */
sealed interface ConnectionInfo{
    /** APIのホストURL */
    val host: String

    /** 郵便番号検索APIのパス */
    val searchCodePath: String

    /** 住所から郵便番号を検索するAPIのパス */
    val addressZipPath: String

    /** トークン取得APIのパス */
    val tokenPath: String

    /**
     * プロキシサーバーを経由してAPIに接続するための設定。
     *
     * @property host プロキシサーバーのホストURL
     * @property tokenPath トークン取得APIのパス（デフォルト: "/api/v1/j/token"）
     * @property searchCodePath 郵便番号検索APIのパス（デフォルト: "/api/v1/searchcode"）
     * @property addressZipPath 住所から郵便番号を検索するAPIのパス（デフォルト: "/api/v1/addresszip"）
     */
    class Proxy(
        override val host: String,
        override val tokenPath: String = "/api/v1/j/token",
        override val searchCodePath: String = "/api/v1/searchcode",
        override val addressZipPath: String = "/api/v1/addresszip"
    ): ConnectionInfo{
        /** 認証情報プロバイダー */
        private var _credentialsProvider: CredentialsProvider? = null

        /** 現在設定されている認証情報プロバイダー */
        val credentialsProvider: CredentialsProvider? get() = _credentialsProvider

        /**
         * 認証情報プロバイダーを設定します。
         *
         * @param credentialsProvider 使用する認証情報プロバイダー
         * @return 自身のインスタンス（メソッドチェーン用）
         */
        fun with(credentialsProvider: CredentialsProvider): Proxy{
            _credentialsProvider = credentialsProvider
            return this
        }
    }

    /**
     * 直接APIに接続するための設定。
     *
     * @property host APIのホストURL
     * @property clientId クライアントID
     * @property secretKey シークレットキー
     * @property forwardedFor X-Forwarded-Forヘッダーの値
     */
    class Direct(
        override val host: String,
        val clientId: String,
        val secretKey: String,
        val forwardedFor: String
    ): ConnectionInfo{
        /** トークン取得APIのパス */
        override val tokenPath: String = "/api/v1/j/token"

        /** 郵便番号検索APIのパス */
        override val searchCodePath: String = "/api/v1/searchcode"

        /** 住所から郵便番号を検索するAPIのパス */
        override val addressZipPath: String = "/api/v1/addresszip"
    }
}
