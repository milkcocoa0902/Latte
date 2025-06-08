package com.milkcocoa.info.latte.cache

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class MemoryCacheBackend: CacheBackend {
    private val mutex = Mutex()

    @OptIn(ExperimentalTime::class)
    private data class CachedValue(
        val value: ByteArray,
        val expire: Instant? = null
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as CachedValue

            if (!value.contentEquals(other.value)) return false
            if (expire != other.expire) return false

            return true
        }

        override fun hashCode(): Int {
            var result = value.contentHashCode()
            result = 31 * result + (expire?.hashCode() ?: 0)
            return result
        }
    }

    private val map = mutableMapOf<String, CachedValue>()

    @OptIn(ExperimentalTime::class)
    override suspend fun write(key: String, value: ByteArray) {
        mutex.withLock {
            map.put(
                key,
                CachedValue(value, null)
            )
        }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun write(key: String, value: ByteArray, expire: Duration): Instant {
        val ex = Clock.System.now() + expire
        mutex.withLock {
            map[key] = CachedValue(value, ex)
        }
        return ex
    }

    override suspend fun read(key: String): ByteArray? {
        return mutex.withLock {
            map[key]?.takeIf { (it.expire?.compareTo(Clock.System.now()) ?: 0) >= 0 }?.value
        }
    }
}