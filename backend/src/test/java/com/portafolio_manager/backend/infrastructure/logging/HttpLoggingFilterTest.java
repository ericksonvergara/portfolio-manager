package com.portafolio_manager.backend.infrastructure.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

class HttpLoggingFilterTest {

    private static final Logger LOGGER =
            (Logger) LoggerFactory.getLogger(HttpLoggingFilter.class);

    private final HttpLoggingFilter filter = new HttpLoggingFilter();
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void setUp() {
        appender = new ListAppender<>();
        appender.start();
        LOGGER.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        LOGGER.detachAppender(appender);
        appender.stop();
    }

    @Test
    void shouldLogSuccessfulRequestAtInfoLevel() throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/test");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain chain = (req, res) ->
                ((MockHttpServletResponse) res).setStatus(200);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(appender.list)
                .singleElement()
                .satisfies(event -> {
                    assertThat(event.getLevel()).isEqualTo(Level.INFO);
                    assertThat(event.getFormattedMessage())
                            .isEqualTo("HTTP GET /test -> 200");
                });
    }

    @Test
    void shouldLogWarningRequestAtWarnLevel() throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/test");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain chain = (req, res) ->
                ((MockHttpServletResponse) res).setStatus(404);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(appender.list)
                .singleElement()
                .satisfies(event -> {
                    assertThat(event.getLevel()).isEqualTo(Level.WARN);
                    assertThat(event.getFormattedMessage())
                            .isEqualTo("HTTP GET /test -> 404");
                });
    }

    @Test
    void shouldLogErrorRequestAtErrorLevel() throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/test");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain chain = (req, res) ->
                ((MockHttpServletResponse) res).setStatus(500);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(appender.list)
                .singleElement()
                .satisfies(event -> {
                    assertThat(event.getLevel()).isEqualTo(Level.ERROR);
                    assertThat(event.getFormattedMessage())
                            .isEqualTo("HTTP GET /test -> 500");
                });
    }
}