package com.milkcocoa.info.latte.core

/**
 * API呼び出しに必要な認証情報を提供するインターフェース。
 * 
 * このインターフェースは、プロキシサーバーを使用する際に、
 * APIリクエストに必要な認証ヘッダーとその値を提供するために使用されます。
 */
interface CredentialsProvider{
    /**
     * 指定されたホストに対する認証情報を提供します。
     *
     * @param host 接続先のホストURL
     * @return ヘッダー名と値のペア（例: "X-API-KEY" to "api-key-value"）
     */
    suspend fun provide(host: String): Pair<String, String>
}
