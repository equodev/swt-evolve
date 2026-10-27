package dev.equo.swt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * On web a {@code Browser} is an iframe, and a {@code BrowserFunction}'s shim can only be injected
 * into same-origin content. An app that serves its own UI from a local HTTP server is a different
 * origin, so without the same-origin proxy a page that waits on its browser function renders
 * nothing at all. Loopback is therefore proxied unconditionally; every other host still needs the
 * {@code dev.equo.swt.web.proxy} opt-in, because proxying arbitrary remote URLs is an SSRF surface.
 */
@Tag("native-unit")
public class WebFlutterServerProxyPolicyNativeTest {

    private static final String PROP = "dev.equo.swt.web.proxy";

    @AfterEach
    public void clearProperty() {
        System.clearProperty(PROP);
    }

    @Test
    public void loopback_is_proxied_without_the_opt_in() {
        System.clearProperty(PROP);
        assertThat(WebFlutterServer.proxyAllowed("http://127.0.0.1:62453/EmailTemplatePage.html?type=TEST_SUITE"))
                .isTrue();
        assertThat(WebFlutterServer.proxyAllowed("http://localhost:8080/index.html")).isTrue();
        assertThat(WebFlutterServer.proxyAllowed("http://127.7.7.7:1234/x")).isTrue();
        assertThat(WebFlutterServer.proxyAllowed("http://[::1]:8080/x")).isTrue();
    }

    @Test
    public void remote_hosts_still_need_the_opt_in() {
        System.clearProperty(PROP);
        assertThat(WebFlutterServer.proxyAllowed("https://example.com/index.html")).isFalse();

        System.setProperty(PROP, "example.com");
        assertThat(WebFlutterServer.proxyAllowed("https://example.com/index.html")).isTrue();
        assertThat(WebFlutterServer.proxyAllowed("https://other.test/index.html")).isFalse();

        System.setProperty(PROP, "all");
        assertThat(WebFlutterServer.proxyAllowed("https://other.test/index.html")).isTrue();
    }

    /**
     * Only a literal loopback address matches. A hostname that merely resolves to 127.0.0.1 must
     * not, or DNS rebinding would smuggle a remote address past the opt-in.
     */
    @Test
    public void loopback_test_does_not_resolve_names() {
        System.clearProperty(PROP);
        assertThat(WebFlutterServer.isLoopbackHost("localtest.me")).isFalse();
        assertThat(WebFlutterServer.isLoopbackHost("127.0.0.1.evil.test")).isFalse();
        assertThat(WebFlutterServer.isLoopbackHost("1270.0.0.1")).isFalse();
        assertThat(WebFlutterServer.isLoopbackHost("127.999.0.1")).isFalse();
        assertThat(WebFlutterServer.proxyAllowed("https://127.0.0.1.evil.test/x")).isFalse();
    }

    @Test
    public void unparseable_url_is_refused() {
        System.setProperty(PROP, "all");
        assertThat(WebFlutterServer.proxyAllowed("not a url")).isFalse();
        assertThat(WebFlutterServer.proxyAllowed("file:///etc/passwd")).isFalse();
    }
}
