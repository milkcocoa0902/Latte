package com.milkcocoa.info.latte.cache

import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

interface CacheBackend {
    suspend fun write(key: String, value: ByteArray)

    @OptIn(ExperimentalTime::class)
    suspend fun write(key: String, value: ByteArray, expire: Duration): Instant

    suspend fun read(key: String): ByteArray?
}