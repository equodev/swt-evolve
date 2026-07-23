package dev.equo.swt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Covers {@link WebFlutterServer}'s root handler resolving a local-file page's root-absolute
 * sub-resource requests via the {@code Referer} header (see
 * {@link LocalFileServing#tokenFromLocalFileReferer}), instead of falling back to this app's own
 * SPA {@code index.html}. Drives a real HTTP server; no Chrome needed.
 */
@Tag("flutter-it")
class WebFlutterServerLocalFileFlutterTest {

    private WebFlutterServer server;
    private File appDir;
    private File pageDir;
    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    void boot() throws Exception {
        appDir = Files.createTempDirectory("equo-fake-webapp").toFile();
        Files.writeString(appDir.toPath().resolve("index.html"),
                "<!doctype html><title>fake-flutter-app</title>", StandardCharsets.UTF_8);

        server = new WebFlutterServer.Builder()
                .webDirectory(appDir)
                .commPort(0)
                .widgetId(1)
                .widgetName("Display")
                .serveServiceWorker(false)
                .build();
        server.start();
    }

    @AfterEach
    void teardown() {
        if (server != null) server.stop();
        deleteRecursively(appDir);
        deleteRecursively(pageDir);
    }

    @Test
    void rootAbsolutePathSubResource_resolvesViaRefererFallback() throws Exception {
        LocalFileServing.Served served = registerMobileRecorderFixture();

        String base = server.getApplicationUrl();
        String pageUrl = base + LocalFileServing.URL_PREFIX + served.tokenPath();

        HttpRequest request = HttpRequest.newBuilder(URI.create(base + "/static/js/main.js"))
                .header("Referer", pageUrl)
                .GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode())
                .as("the sibling script must be served, not 404 or the SPA index.html fallback")
                .isEqualTo(200);
        assertThat(response.body())
                .as("must be the actual JS asset, not this app's own index.html")
                .contains("document.title='rendered-widgets'");
    }

    @Test
    void rootAbsolutePathSubResource_withUnrelatedReferer_fallsBackToOwnIndexHtml() throws Exception {
        // The registered directory exists, but the Referer doesn't name it -- the fallback must not
        // fire off some other heuristic (e.g. "any known token"); only the one the page itself sent.
        registerMobileRecorderFixture();

        String base = server.getApplicationUrl();
        HttpRequest request = HttpRequest.newBuilder(URI.create(base + "/static/js/main.js"))
                .header("Referer", base + "/some/other/page")
                .GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body())
                .as("falls back to this app's own SPA index.html, exactly as before this fix")
                .contains("fake-flutter-app");
    }

    @Test
    void rootAbsolutePathSubResource_withoutReferer_fallsBackToOwnIndexHtml() throws Exception {
        registerMobileRecorderFixture();

        String base = server.getApplicationUrl();
        HttpRequest request = HttpRequest.newBuilder(URI.create(base + "/static/js/main.js")).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("fake-flutter-app");
    }

    @Test
    void directLocalFileEndpoint_stillServesTheHtmlItself() throws Exception {
        LocalFileServing.Served served = registerMobileRecorderFixture();

        String base = server.getApplicationUrl();
        HttpRequest request = HttpRequest
                .newBuilder(URI.create(base + "/local-file/" + served.tokenPath()))
                .GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("static/js/main.js");
    }

    /** Mimics Katalon's MobileRecorder.html: a bundled page whose script is root-absolute. */
    private LocalFileServing.Served registerMobileRecorderFixture() throws Exception {
        pageDir = Files.createTempDirectory("equo-mobile-recorder").toFile();
        File html = new File(pageDir, "MobileRecorder.html");
        Files.writeString(html.toPath(),
                "<!doctype html><html><body><script src=\"/static/js/main.js\"></script></body></html>",
                StandardCharsets.UTF_8);
        File js = new File(pageDir, "static/js/main.js");
        js.getParentFile().mkdirs();
        Files.writeString(js.toPath(), "document.title='rendered-widgets';", StandardCharsets.UTF_8);

        LocalFileServing.Served served = LocalFileServing.registerIfLocalFile("file:" + html.getAbsolutePath());
        assertThat(served).as("fixture setup: the HTML file itself must register cleanly").isNotNull();
        return served;
    }

    private static void deleteRecursively(File dir) {
        if (dir == null || !dir.exists()) return;
        try (var paths = Files.walk(dir.toPath())) {
            paths.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        } catch (Exception ignored) {
        }
    }
}
