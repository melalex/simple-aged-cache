package io.collective;

import java.time.Clock;
import java.time.Instant;
import java.util.*;

public class SimpleAgedCache {

    private final Clock clock;

    private final Map<Object, CacheEntry> entries = new HashMap<>();
    private final Queue<Object> expirationQueue = new PriorityQueue<>(Comparator.comparing(it -> entries.get(it).validTo));

    public SimpleAgedCache(Clock clock) {
        this.clock = clock;
    }

    public SimpleAgedCache() {
        this(Clock.systemDefaultZone());
    }

    public void put(Object key, Object value, int retentionInMillis) {
        var expiryTime = clock.instant().plusMillis(retentionInMillis);
        var entry = new CacheEntry(value, expiryTime);

        var old = entries.put(key, entry);

        if (old == null) {
            expirationQueue.add(key);
        } else {
            var newValues = expirationQueue.stream()
                    .toList();

            expirationQueue.clear();
            expirationQueue.addAll(newValues);
        }
    }

    public boolean isEmpty() {
        removeExpiredEntries();

        return entries.isEmpty();
    }

    public int size() {
        removeExpiredEntries();

        return entries.size();
    }

    public Object get(Object key) {
        removeExpiredEntries();

        return entries.getOrDefault(key, CacheEntry.NULL).value;
    }

    private void removeExpiredEntries() {
        var now = clock.instant();
        var head = expirationQueue.peek();

        while (head != null && entries.get(head).validTo.isBefore(now)) {
            entries.remove(expirationQueue.poll());
            head = expirationQueue.peek();
        }
    }

    private record CacheEntry(
            Object value,
            Instant validTo
    ) {
        public static final CacheEntry NULL = new CacheEntry(null, Instant.MIN);
    }
}