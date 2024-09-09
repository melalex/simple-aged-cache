package io.collective;

import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class SimpleAgedCache {

    private final Clock clock;
    private final Map<Object, CacheEntry> cacheEntryMap = new HashMap<>();

    public SimpleAgedCache(Clock clock) {
        this.clock = clock;
    }

    public SimpleAgedCache() {
        this(Clock.systemUTC());
    }

    public void put(Object key, Object value, int retentionInMillis) {
        var now = clock.instant();

        cacheEntryMap.put(key, new CacheEntry(value, now.plusMillis(retentionInMillis)));
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public int size() {
        var now = clock.instant();

        return (int) cacheEntryMap.values().stream()
                .filter(it -> it.validTo.isAfter(now))
                .count();
    }

    public Object get(Object key) {
        var now = clock.instant();
        var value = cacheEntryMap.get(key);

        if (value == null) {
            return null;
        } else if (value.validTo.isAfter(now)) {
            return value.value;
        } else {
            return null;
        }
    }

    private record CacheEntry(
            Object value,
            Instant validTo
    ) {
    }
}