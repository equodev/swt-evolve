package dev.equo.swt;

import com.sun.net.httpserver.HttpServer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The app is served cross-origin isolated, and a browser refuses a nested document that does not
 * itself declare a {@code Cross-Origin-Embedder-Policy} — even a same-origin one. A proxied page
 * served without those headers therefore never loads at all: the Browser shows the browser's own
 * "refused to connect" frame instead of the page, which is the same blank area the proxy was added
 * to fix. Every {@code /proxy} response must carry the app's own isolation headers.
 */
@Tag("native-unit")
class WebFlutterServerProxyEmbeddingNativeTest {

    private static final String COEP = "Cross-Origin-Embedder-Policy";

    private WebFlutterServer app;
    private File appDir;
    private HttpServer origin;
    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    void boot() throws Exception {
        appDir = Files.createTempDirectory("equo-proxy-embed-app").toFile();
        Files.writeString(appDir.toPath().resolve("index.html"),
                "<!doctype html><title>fake-flutter-app</title>", StandardCharsets.UTF_8);

        app = new WebFlutterServer.Builder()
                .webDirectory(appDir)
                .commPort(0)
                .widgetId(1)
                .widgetName("Display")
                .serveServiceWorker(false)
                .build();
        app.start();

        // Stands in for an embedder's own loopback UI server: plain HTML, no isolation headers.
        origin = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        origin.createContext("/", exchange -> {
            byte[] body = "<html><head></head><body>hosted page</body></html>"
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
            exchange.close();
        });
        origin.start();
    }

    @AfterEach
    void teardown() {
        if (app != null) app.stop();
        if (origin != null) origin.stop(0);
        deleteRecursively(appDir);
    }

    @Test
    void proxiedPageIsEmbeddableInTheIsolatedApp() throws Exception {
        HttpResponse<String> page = get(proxyUrl("http://127.0.0.1:" + origin.getAddress().getPort()
                + "/EmailTemplatePage.html?type=TEST_SUITE"));

        assertThat(page.statusCode()).isEqualTo(200);
        assertThat(page.body())
                .as("the proxy re-serves the hosted page and anchors its relative sub-resources")
                .contains("hosted page")
                .contains("<base href=");
        assertThat(page.headers().firstValue(COEP))
                .as("without this the browser refuses the frame and the page never loads")
                .hasValue(appIsolationPolicy());
    }

    @Test
    void aRefusalIsEmbeddableToo() throws Exception {
        // A frame that is blocked outright shows a browser error page, hiding the reason it failed.
        HttpResponse<String> refused = get(app.getApplicationUrl() + "/proxy");

        assertThat(refused.statusCode()).isEqualTo(403);
        assertThat(refused.headers().firstValue(COEP)).hasValue(appIsolationPolicy());
    }

    /** The policy the app's own document is served with; a nested document must match it. */
    private String appIsolationPolicy() throws Exception {
        return get(app.getApplicationUrl() + "/index.html").headers().firstValue(COEP).orElseThrow();
    }

    private String proxyUrl(String target) {
        return app.getApplicationUrl() + "/proxy?url="
                + URLEncoder.encode(target, StandardCharsets.UTF_8);
    }

    private HttpResponse<String> get(String url) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create(url)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private static void deleteRecursively(File dir) {
        if (dir == null || !dir.exists()) return;
        try (var paths = Files.walk(dir.toPath())) {
            paths.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        } catch (Exception ignored) {
        }
    }
}
