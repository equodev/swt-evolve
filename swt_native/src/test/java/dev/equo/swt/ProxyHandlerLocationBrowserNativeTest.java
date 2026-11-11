package dev.equo.swt;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import dev.equo.swt.harness.DevTools;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * A page served through {@code /proxy}, run in a browser, sees its own path and query in its
 * {@code location}, as a client-side router needs to find its route, and moves through its own
 * history with the root-relative URLs it would use on its own server.
 */
@Tag("native-unit")
class ProxyHandlerLocationBrowserNativeTest {

    private static final String PAGE_PATH = "/designer/solution/s/form/f/index.html";
    private static final String PAGE_TEXT = "the form designer";

    private static HttpServer server;
    private static HttpServer proxy;
    private static HeadlessChrome chrome;
    private static DevTools devTools;
    private static String requested;

    @BeforeAll
    static void openPage() throws Exception {
        String binary = HeadlessChrome.resolveBinary(System.getProperty("equo.swt.browser"));
        assumeTrue(binary != null, "no Chrome/Chromium; set -Dequo.swt.browser=<path>");
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext(PAGE_PATH, ex -> respond(ex, "text/html",
                "<html><head><base href=\"/\"></head><body>" + PAGE_TEXT + "</body></html>"));
        server.createContext("/api/x", ex -> respond(ex, "text/plain", "from the page's server"));
        server.start();
        proxy = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        proxy.createContext("/proxy", new ProxyHandler(new ProxyCookieJar(WebFlutterServer::proxyAllowed)));
        proxy.createContext("/blank", ex -> respond(ex, "text/html", "<html><head></head><body></body></html>"));
        proxy.start();
        // The origin ProxyHandler sends a proxied page's requests to.
        String self = "http://localhost:" + proxy.getAddress().getPort();
        String target = "http://127.0.0.1:" + server.getAddress().getPort() + PAGE_PATH + "?nr=2";
        requested = self + "/proxy?url=" + URLEncoder.encode(target, StandardCharsets.UTF_8);
        chrome = HeadlessChrome.launch(binary, self + "/blank", true, false,
                HeadlessChrome.Io.DISCARD, "--remote-debugging-port=0");
        devTools = DevTools.connect(chrome.profileDir(), self + "/blank");
        // The target lists /blank's URL before /blank commits, and that commit cancels a navigation
        // started from the document before it.
        awaitDocument("document.readyState === 'complete' && location.pathname === '/blank'");
        devTools.evaluate("location.href = '" + requested + "'");
        awaitProxiedPage();
    }

    @AfterAll
    static void close() {
        if (devTools != null) devTools.close();
        if (chrome != null) chrome.close();
        if (proxy != null) proxy.stop(0);
        if (server != null) server.stop(0);
    }

    @Test
    void thePageSeesItsOwnPathAndQuery() {
        assertThat(eval("location.pathname")).isEqualTo(PAGE_PATH);
        assertThat(eval("location.search")).isEqualTo("?nr=2");
    }

    @Test
    void theDocumentKeepsTheAddressItWasRequestedAt() {
        assertThat(eval("document.__equoRequestedUrl")).isEqualTo(requested);
    }

    @Test
    void aRootRelativeHistoryEntryKeepsItsPath() {
        assertThat(eval("(() => { history.pushState({n: 1}, '', '/designer/solution/s/form/g/index.html');"
                + " const p = location.pathname; history.replaceState(null, '', '" + PAGE_PATH + "?nr=2');"
                + " return p; })()")).isEqualTo("/designer/solution/s/form/g/index.html");
        assertThat(eval("location.pathname + location.search")).isEqualTo(PAGE_PATH + "?nr=2");
    }

    @Test
    void aRequestBuiltFromItsLocationAfterARouteChangeReachesItsServer() {
        eval("history.pushState(null, '', '/designer/solution/s/form/g/index.html'); ''");
        try {
            assertThat(eval("fetch(location.origin + '/api/x').then(r => r.text())"))
                    .isEqualTo("from the page's server");
            assertThat(eval("(() => { const x = new XMLHttpRequest();"
                    + " x.open('GET', location.origin + '/api/x', false); x.send(); return x.responseText; })()"))
                    .isEqualTo("from the page's server");
        } finally {
            eval("history.replaceState(null, '', '" + PAGE_PATH + "?nr=2'); ''");
        }
    }

    @Test
    void aReloadServesThePageAgain() {
        eval("window.__beforeReload = true; setTimeout(() => location.reload()); ''");
        awaitDocument("window.__beforeReload === undefined && document.readyState === 'complete'");
        assertThat(eval("document.body.textContent")).isEqualTo(PAGE_TEXT);
        assertThat(eval("location.pathname + location.search")).isEqualTo(PAGE_PATH + "?nr=2");
        assertThat(eval("new URL(document.__equoRequestedUrl).pathname")).isEqualTo("/proxy");
    }

    private static String eval(String js) {
        return devTools.evaluate(js).getAsString();
    }

    private static void awaitDocument(String condition) {
        long deadline = System.currentTimeMillis() + 10_000;
        while (System.currentTimeMillis() < deadline) {
            try {
                if (devTools.evaluate(condition).getAsBoolean()) return;
            } catch (IllegalStateException navigating) {
                // The evaluation ran in the document the navigation discarded.
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
        }
        throw new IllegalStateException("The document did not reach: " + condition);
    }

    /**
     * Waits until the proxied document has loaded. A navigation discards the document a pending
     * evaluation runs in, so each check is a single synchronous evaluation, retried.
     */
    private static void awaitProxiedPage() {
        long deadline = System.currentTimeMillis() + 10_000;
        String seen = null;
        while (System.currentTimeMillis() < deadline) {
            try {
                seen = devTools.evaluate("document.readyState + ' ' + location.href").getAsString();
                if (seen.startsWith("complete ") && !seen.endsWith("/blank")) return;
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
        throw new IllegalStateException("The proxied page did not load; last seen: " + seen);
    }

    private static void respond(HttpExchange ex, String contentType, String text) throws IOException {
        byte[] body = text.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", contentType + "; charset=utf-8");
        ex.sendResponseHeaders(200, body.length);
        try (OutputStream out = ex.getResponseBody()) {
            out.write(body);
        }
    }
}
