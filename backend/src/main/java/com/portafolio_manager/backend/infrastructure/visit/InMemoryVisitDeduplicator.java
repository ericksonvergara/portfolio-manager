package com.portafolio_manager.backend.infrastructure.visit;

import com.portafolio_manager.backend.application.port.out.visit.VisitDeduplicatorPort;
import com.portafolio_manager.backend.domain.visit.VisitRequest;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Remembers who opened which page during the window (VISIT_DEDUP_WINDOW), which starts at the first visit:
 * reloading does not extend it. Only a keyed hash is kept, in memory, and the key is random and lives as
 * long as the process, so the addresses cannot be recovered from it. A restart forgets everything, which
 * at most counts one extra visit; several backend instances would need a shared store.
 */
@Component
@EnableConfigurationProperties(VisitProperties.class)
class InMemoryVisitDeduplicator implements VisitDeduplicatorPort {

    private static final String ALGORITHM = "HmacSHA256";
    private static final int KEY_BYTES = 32;
    private static final char SEPARATOR = '\n';

    /** Bounds the memory if the public endpoint is flooded: the oldest visitors are forgotten first. */
    static final int MAX_VISITORS = 100_000;

    private final VisitProperties properties;
    private final Clock clock;
    private final SecretKeySpec key;

    /** Fingerprint and end of its window. Entries never change, so the insertion order is the expiry order. */
    private final Map<String, Instant> visitors = new LinkedHashMap<>();

    InMemoryVisitDeduplicator(VisitProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
        byte[] secret = new byte[KEY_BYTES];
        new SecureRandom().nextBytes(secret);
        this.key = new SecretKeySpec(secret, ALGORITHM);
    }

    @Override
    public synchronized boolean isFirstInWindow(VisitRequest request) {
        Instant now = clock.instant();
        forgetExpired(now);

        String fingerprint = fingerprint(request);
        if (visitors.containsKey(fingerprint)) {
            return false;
        }
        if (visitors.size() >= MAX_VISITORS) {
            Iterator<String> oldest = visitors.keySet().iterator();
            oldest.next();
            oldest.remove();
        }
        visitors.put(fingerprint, now.plus(properties.dedupWindow()));
        return true;
    }

    private void forgetExpired(Instant now) {
        Iterator<Instant> ends = visitors.values().iterator();
        while (ends.hasNext() && !ends.next().isAfter(now)) {
            ends.remove();
        }
    }

    private String fingerprint(VisitRequest request) {
        String visit = request.clientIp() + SEPARATOR + request.userAgent() + SEPARATOR + request.projectSlug();
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(key);
            return Base64.getEncoder().encodeToString(mac.doFinal(visit.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("HmacSHA256 is not available", exception);
        }
    }
}
