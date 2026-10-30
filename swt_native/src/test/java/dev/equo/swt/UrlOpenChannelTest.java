package dev.equo.swt;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * The {@code swt.evolve.url.open} channel hands a Flutter-supplied string to {@code Program.launch},
 * which asks the OS default handler to open it — ShellExecute on Windows, which would just as
 * happily run an executable path or a custom scheme. The scheme check is therefore a trust boundary,
 * not a formality. Everything rejected here stops before {@code Program.launch}, so nothing is
 * spawned; the accepted cases only assert the URL is extracted, they never reach the OS.
 */
public class UrlOpenChannelTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "http://example.com",
            "https://example.com",
            "https://example.com/docs?a=b#frag",
            "HTTPS://EXAMPLE.COM",
    })
    void accepts_http_and_https(String url) {
        assertThat(FlutterBridge.urlOf(url)).isEqualTo(url);
    }

    @Test
    void trims_surrounding_whitespace() {
        assertThat(FlutterBridge.urlOf("  https://example.com  ")).isEqualTo("https://example.com");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "file:///C:/Windows/System32/calc.exe",
            "javascript:alert(1)",
            "data:text/html,<script>alert(1)</script>",
            "cmd.exe",
            "C:\\Windows\\System32\\calc.exe",
            "\\\\server\\share\\payload.bat",
            "ftp://example.com",
            "mailto:someone@example.com",
            "vbscript:msgbox",
            "example.com",
            "httpx://example.com",
            "",
            "   ",
    })
    void rejects_everything_else(String url) {
        assertThat(FlutterBridge.urlOf(url)).isNull();
    }

    /** A malformed frame must not throw out of the comm thread. */
    @Test
    void handler_swallows_unusable_payloads() {
        assertDoesNotThrow(() -> {
            FlutterBridge.handleUrlOpenFromFlutter(null);
            FlutterBridge.handleUrlOpenFromFlutter("");
            FlutterBridge.handleUrlOpenFromFlutter("javascript:alert(1)");
        });
    }
}
