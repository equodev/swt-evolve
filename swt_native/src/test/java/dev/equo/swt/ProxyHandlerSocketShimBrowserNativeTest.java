package dev.equo.swt;

import com.sun.net.httpserver.HttpServer;
import dev.equo.swt.harness.DevTools;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * The socket shim, run in a browser: a WebSocket a proxied page opens on the app's host goes to the
 * page's own server, with {@code /proxy} standing for the page's directory. The page here is
 * served by a local server that plays the app; the page's own server is never contacted, the
 * routed URL is read from the socket.
 */
@Tag("native-unit")
class ProxyHandlerSocketShimBrowserNativeTest {

    private static final String PAGE = "http://localhost:8283/editor/form/index.html";
    private static final String SECURE_PAGE = "https://example.test:8443/editor/index.html";

    private static HttpServer app;
    private static HeadlessChrome chrome;
    private static DevTools devTools;
    private static String host;

    @BeforeAll
    static void openPages() throws Exception {
        String binary = HeadlessChrome.resolveBinary(System.getProperty("equo.swt.browser"));
        assumeTrue(binary != null, "no Chrome/Chromium; set -Dequo.swt.browser=<path>");
        app = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        app.createContext("/plain", ex -> serve(ex, PAGE));
        app.createContext("/secure", ex -> serve(ex, SECURE_PAGE));
        app.start();
        host = "127.0.0.1:" + app.getAddress().getPort();
        chrome = HeadlessChrome.launch(binary, "http://" + host + "/plain", true, false,
                HeadlessChrome.Io.DISCARD, "--remote-debugging-port=0");
        devTools = DevTools.connect(chrome.profileDir(), "http://" + host + "/plain");
        awaitLoaded("/plain");
    }

    @AfterAll
    static void close() {
        if (devTools != null) devTools.close();
        if (chrome != null) chrome.close();
        if (app != null) app.stop(0);
    }

    @Test
    void aSocketUnderProxyGoesToThePageDirectoryOnThePageServer() {
        assertThat(routed("ws://" + host + "/proxy/rfb/websocket/1?id=7"))
                .isEqualTo("ws://localhost:8283/editor/form/rfb/websocket/1?id=7");
        assertThat(routed("ws://" + host + "/proxy")).isEqualTo("ws://localhost:8283/editor/form");
    }

    @Test
    void aSocketOutsideProxyKeepsItsPathOnThePageServer() {
        assertThat(routed("ws://" + host + "/live")).isEqualTo("ws://localhost:8283/live");
        assertThat(routed("ws://" + host + "/proxyless")).isEqualTo("ws://localhost:8283/proxyless");
    }

    @Test
    void aSocketToAnotherHostIsLeftAlone() {
        assertThat(routed("ws://other.test:9000/proxy/x")).isEqualTo("ws://other.test:9000/proxy/x");
    }

    @Test
    void aPageServedOverHttpsGetsASecureSocket() {
        devTools.evaluate("location.href = 'http://" + host + "/secure'");
        try {
            awaitLoaded("/secure");
            assertThat(routed("ws://" + host + "/proxy/push")).isEqualTo("wss://example.test:8443/editor/push");
        } finally {
            devTools.evaluate("location.href = 'http://" + host + "/plain'");
            awaitLoaded("/plain");
        }
    }

    /**
     * Waits until the app's page at {@code path} has loaded, shim included. The DevTools target can
     * already carry the page's URL while the initial blank document, itself complete, is still the
     * one evaluated; a navigation also discards the document a pending evaluation runs in. So each
     * check is a single synchronous evaluation, retried.
     */
    private static void awaitLoaded(String path) {
        String loaded = "http://" + host + path + " complete";
        long deadline = System.currentTimeMillis() + 10_000;
        String seen = null;
        while (System.currentTimeMillis() < deadline) {
            try {
                seen = devTools.evaluate("location.href + ' ' + document.readyState").getAsString();
                if (loaded.equals(seen)) return;
            } catch (IllegalStateException navigating) {
                seen = navigating.getMessage();
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
        }
        throw new IllegalStateException("Page " + path + " did not load; last seen: " + seen);
    }

    /** The URL the socket a page opens on {@code url} actually connects to. */
    private static String routed(String url) {
        return devTools.evaluate("(() => { const s = new WebSocket('" + url + "'); s.onerror = () => {};"
                + " const u = s.url; s.close(); return u; })()").getAsString();
    }

    private static void serve(com.sun.net.httpserver.HttpExchange ex, String pageUrl) throws java.io.IOException {
        byte[] body = ProxyHandler.injectSocketShim("<html><head></head><body></body></html>", pageUrl)
                .getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        ex.sendResponseHeaders(200, body.length);
        try (OutputStream out = ex.getResponseBody()) {
            out.write(body);
        }
    }
}
