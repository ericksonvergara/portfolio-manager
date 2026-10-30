package com.portafolio_manager.backend.infrastructure.contact;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryContactRateLimiterTest {

    private final MutableClock clock = new MutableClock(Instant.parse("2026-10-29T10:00:00Z"));
    private final InMemoryContactRateLimiter limiter = new InMemoryContactRateLimiter(
            new ContactProperties(2, Duration.ofHours(1), "Portfolio <no-reply@portfolio.test>"), clock);

    @Test
    void shouldLetEachSenderSendUpToTheLimitPerWindow() {
        assertThat(limiter.tryAcquire("203.0.113.7")).isTrue();
        assertThat(limiter.tryAcquire("203.0.113.7")).isTrue();
        assertThat(limiter.tryAcquire("203.0.113.7")).isFalse();

        // Another sender has its own counter.
        assertThat(limiter.tryAcquire("198.51.100.4")).isTrue();
    }

    @Test
    void shouldFreeTheOldestMessageWhenItLeavesTheWindow() {
        limiter.tryAcquire("203.0.113.7");
        clock.advance(Duration.ofMinutes(30));
        limiter.tryAcquire("203.0.113.7");
        assertThat(limiter.tryAcquire("203.0.113.7")).isFalse();

        clock.advance(Duration.ofMinutes(31)); // the first one is now over an hour old
        assertThat(limiter.tryAcquire("203.0.113.7")).isTrue();
        assertThat(limiter.tryAcquire("203.0.113.7")).isFalse();
    }

    private static final class MutableClock extends Clock {

        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
