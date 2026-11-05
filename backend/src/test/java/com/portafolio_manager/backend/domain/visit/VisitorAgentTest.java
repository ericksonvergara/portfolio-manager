package com.portafolio_manager.backend.domain.visit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class VisitorAgentTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0 Safari/537.36 | Chrome | Windows | Desktop",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/130.0 Safari/537.36 Edg/130.0 | Edge | Windows | Desktop",
            "Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 Chrome/130.0 Safari/537.36 OPR/115.0 | Opera | Windows | Desktop",
            "Mozilla/5.0 (X11; Linux x86_64; rv:132.0) Gecko/20100101 Firefox/132.0 | Firefox | Linux | Desktop",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 Version/18.0 Safari/605.1.15 | Safari | macOS | Desktop",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 18_0 like Mac OS X) AppleWebKit/605.1.15 Version/18.0 Mobile/15E148 Safari/604.1 | Safari | iOS | Mobile",
            "Mozilla/5.0 (iPad; CPU OS 18_0 like Mac OS X) AppleWebKit/605.1.15 Version/18.0 Mobile/15E148 Safari/604.1 | Safari | iOS | Tablet",
            "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 Chrome/130.0 Mobile Safari/537.36 | Chrome | Android | Mobile",
            "Mozilla/5.0 (Linux; Android 14; SM-X710) AppleWebKit/537.36 Chrome/130.0 Safari/537.36 | Chrome | Android | Tablet",
            "Mozilla/5.0 (Linux; Android 14; SM-S918B) AppleWebKit/537.36 SamsungBrowser/26.0 Chrome/122.0 Mobile Safari/537.36 | Samsung Internet | Android | Mobile",
            "Mozilla/5.0 (X11; CrOS x86_64 14541.0.0) AppleWebKit/537.36 Chrome/130.0 Safari/537.36 | Chrome | ChromeOS | Desktop"
    })
    void shouldRecogniseCommonBrowsers(String userAgent, String browser, String system, String device) {
        assertThat(VisitorAgent.parse(userAgent)).isEqualTo(new VisitorAgent(browser, system, device));
    }

    @Test
    void shouldLabelBotsWithoutLeavingThemOut() {
        VisitorAgent agent = VisitorAgent.parse(
                "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)");

        assertThat(agent.browser()).isEqualTo("Bot");
        assertThat(agent.device()).isEqualTo("Bot");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void shouldReportAMissingHeaderAsUnknown(String userAgent) {
        assertThat(VisitorAgent.parse(userAgent))
                .isEqualTo(new VisitorAgent(VisitorAgent.UNKNOWN, VisitorAgent.UNKNOWN, VisitorAgent.UNKNOWN));
    }
}
