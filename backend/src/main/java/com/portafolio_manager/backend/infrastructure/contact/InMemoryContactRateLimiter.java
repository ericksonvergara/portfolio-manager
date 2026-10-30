package com.portafolio_manager.backend.infrastructure.contact;

import com.portafolio_manager.backend.application.port.out.contact.ContactRateLimiterPort;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Sliding window per sender, kept in memory: a restart resets the counters, which is acceptable for a
 * single backend instance. Several instances would need a shared store (for example Redis).
 */
@Component
@EnableConfigurationProperties(ContactProperties.class)
class InMemoryContactRateLimiter implements ContactRateLimiterPort {

    /** Above this many senders, the expired ones are dropped so the map does not grow forever. */
    private static final int PRUNE_THRESHOLD = 10_000;

    private final ContactProperties properties;
    private final Clock clock;
    private final Map<String, Deque<Instant>> sent = new HashMap<>();

    InMemoryContactRateLimiter(ContactProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    public synchronized boolean tryAcquire(String senderKey) {
        Instant now = clock.instant();
        Instant windowStart = now.minus(properties.window());
        if (sent.size() > PRUNE_THRESHOLD) {
            sent.values().forEach(times -> dropBefore(times, windowStart));
            sent.values().removeIf(Deque::isEmpty);
        }

        Deque<Instant> times = sent.computeIfAbsent(senderKey, key -> new ArrayDeque<>());
        dropBefore(times, windowStart);
        if (times.size() >= properties.maxMessages()) {
            return false;
        }
        times.addLast(now);
        return true;
    }

    private static void dropBefore(Deque<Instant> times, Instant windowStart) {
        while (!times.isEmpty() && !times.peekFirst().isAfter(windowStart)) {
            times.removeFirst();
        }
    }
}
