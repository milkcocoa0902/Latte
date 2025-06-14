package com.milkcocoa.info.latte.cache

import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class RedisCacheBackend(
    private val host: String,
    private val port: Int,
    private val useSsl: Boolean = false
): CacheBackend {
    override suspend fun write(key: String, value: ByteArray) {
        TODO("Not yet implemented")
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun write(key: String, value: ByteArray, expire: Duration): Instant {
        TODO("Not yet implemented")
    }

    override suspend fun read(key: String): ByteArray? {
        TODO("Not yet implemented")
    }
}