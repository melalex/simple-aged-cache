package io.collective

import java.time.Clock
import java.time.Instant

class SimpleAgedKache {

    private val clock: Clock
    private val cacheEntryMap: MutableMap<Any?, CacheEntry> = HashMap()

    constructor(clock: Clock?) {
        this.clock = clock ?: Clock.systemUTC()
    }

    constructor() : this(null) {

    }

    fun put(key: Any?, value: Any?, retentionInMillis: Int) {
        val now = clock.instant()

        cacheEntryMap[key] = CacheEntry(value, now.plusMillis(retentionInMillis.toLong()))
    }

    fun isEmpty(): Boolean {
        return size() == 0
    }

    fun size(): Int {
        val now = clock.instant()

        return cacheEntryMap.values.stream()
            .filter { it.validTo.isAfter(now) }
            .count()
            .toInt()
    }

    fun get(key: Any?): Any? {
        val now = clock.instant()
        val value = cacheEntryMap[key]

        return if (value == null) null
        else if (value.validTo.isAfter(now)) value.value
        else null
    }

    private data class CacheEntry(
        val value: Any?,
        val validTo: Instant
    )
}