package com.milkcocoa.info.latte.core

interface CredentialsProvider{
    suspend fun provide(host: String): Pair<String, String>
}