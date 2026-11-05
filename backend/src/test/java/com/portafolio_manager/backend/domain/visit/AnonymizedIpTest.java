package com.portafolio_manager.backend.domain.visit;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class AnonymizedIpTest {

    @ParameterizedTest
    @CsvSource({
            "190.25.14.77, 190.25.14.0",
            "' 8.8.8.8 ', 8.8.8.0",
            "2001:0db8:85a3:0000:0000:8a2e:0370:7334, 2001:0db8:85a3::",
            "2001:DB8:1:2::1, 2001:db8:1::",
            "2001:db8::1, 2001:db8::",
            "::1, ::",
            "::ffff:190.25.14.77, 190.25.14.0"
    })
    void shouldKeepOnlyTheNetworkPart(String ip, String expected) {
        assertThat(AnonymizedIp.of(ip)).isEqualTo(expected);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "localhost", "not an ip", "1.2.3", "<script>"})
    void shouldNotStoreAnythingElse(String ip) {
        assertThat(AnonymizedIp.of(ip)).isEqualTo(AnonymizedIp.UNKNOWN);
    }
}
