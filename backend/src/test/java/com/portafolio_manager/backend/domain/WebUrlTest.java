package com.portafolio_manager.backend.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class WebUrlTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "https://example.com",
            "http://example.com",
            "HTTPS://Example.com/Path",
            "https://github.com/ericksonvergara/portfolio-manager?tab=readme#top",
            "https://www.youtube.com/watch?v=abc123",
            "http://192.168.0.1:8080/demo",
            "https://example.com:8443",
            "https://xn--ejemplo-and-9db.com"
    })
    void shouldAcceptLinksAVisitorCanOpen(String url) {
        assertThat(WebUrl.isValid(url)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "javascript:alert(1)",          // never a link: it would run code
            "ftp://example.com",
            "www.example.com",              // no scheme
            "https://",
            "https://localhost",            // no domain a visitor can reach
            "https://foo",
            "https://user:pass@example.com", // credentials in the link
            "https://github.com@evil.example",
            "https://example.com:99999",    // no such port
            "https://example.com/a b",
            "https://exa mple.com",
            "https://example..com",
            "https://example.com/%zz"
    })
    void shouldRejectAnythingElse(String url) {
        assertThat(WebUrl.isValid(url)).isFalse();
    }
}
