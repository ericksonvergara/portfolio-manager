package com.portafolio_manager.backend.infrastructure.visit;

import com.portafolio_manager.backend.domain.visit.VisitRequest;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryVisitDeduplicatorTest {

    private static final VisitRequest VISIT = new VisitRequest("190.25.14.77", "Firefox/132.0", "sistema-erp");

    private final MutableClock clock = new MutableClock(Instant.parse("2026-11-05T10:00:00Z"));
    private final InMemoryVisitDeduplicator deduplicator = new InMemoryVisitDeduplicator(
            new VisitProperties(Path.of("none.mmdb"), Duration.ofMinutes(30)), clock);

    @Test
    void shouldCountOnlyTheFirstVisitOfTheWindow() {
        assertThat(deduplicator.isFirstInWindow(VISIT)).isTrue();
        assertThat(deduplicator.isFirstInWindow(VISIT)).isFalse();

        clock.advance(Duration.ofMinutes(29));
        assertThat(deduplicator.isFirstInWindow(VISIT)).isFalse();
    }

    @Test
    void shouldCountAgainWhenTheWindowEndsEvenIfTheVisitorKeptReloading() {
        deduplicator.isFirstInWindow(VISIT);
        clock.advance(Duration.ofMinutes(20));
        deduplicator.isFirstInWindow(VISIT); // a reload does not extend the window

        clock.advance(Duration.ofMinutes(10));
        assertThat(deduplicator.isFirstInWindow(VISIT)).isTrue();
    }

    @Test
    void shouldTreatEachVisitorAndPageApart() {
        deduplicator.isFirstInWindow(VISIT);

        assertThat(deduplicator.isFirstInWindow(new VisitRequest("190.25.14.78", "Firefox/132.0", "sistema-erp"))).isTrue();
        assertThat(deduplicator.isFirstInWindow(new VisitRequest("190.25.14.77", "Chrome/130.0", "sistema-erp"))).isTrue();
        assertThat(deduplicator.isFirstInWindow(new VisitRequest("190.25.14.77", "Firefox/132.0", "tienda"))).isTrue();
        assertThat(deduplicator.isFirstInWindow(new VisitRequest("190.25.14.77", "Firefox/132.0", null))).isTrue();
    }

    @Test
    void shouldAcceptAVisitWithoutUserAgent() {
        VisitRequest noAgent = new VisitRequest("190.25.14.77", null, null);

        assertThat(deduplicator.isFirstInWindow(noAgent)).isTrue();
        assertThat(deduplicator.isFirstInWindow(noAgent)).isFalse();
    }

    @Test
    void shouldForgetTheOldestVisitorsWhenFlooded() {
        deduplicator.isFirstInWindow(VISIT);
        IntStream.range(0, InMemoryVisitDeduplicator.MAX_VISITORS)
                .forEach(i -> deduplicator.isFirstInWindow(new VisitRequest("ip-" + i, null, null)));

        assertThat(deduplicator.isFirstInWindow(VISIT)).isTrue();
    }

    @Test
    void shouldRejectAnInvalidWindow() {
        assertThatThrownBy(() -> new VisitProperties(Path.of("none.mmdb"), Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("VISIT_DEDUP_WINDOW");
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
