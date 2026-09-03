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
import java.util.Base64;
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

    /** 1x1 transparent PNG. */
    private static final byte[] PIXEL_PNG = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk"
                    + "YPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==");

    private WebFlutterServer server;
    private File appDir;
    private File pageDir;
    private File imageDir;
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
        deleteRecursively(imageDir);
    }

    @Test
    void rootAbsolutePathSubResource_resolvesViaRefererFallback() throws Exception {
        LocalFileServing.Served served = registerLocalPageFixture();

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
        registerLocalPageFixture();

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
        registerLocalPageFixture();

        String base = server.getApplicationUrl();
        HttpRequest request = HttpRequest.newBuilder(URI.create(base + "/static/js/main.js")).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("fake-flutter-app");
    }

    @Test
    void directLocalFileEndpoint_stillServesTheHtmlItself() throws Exception {
        LocalFileServing.Served served = registerLocalPageFixture();

        String base = server.getApplicationUrl();
        HttpRequest request = HttpRequest
                .newBuilder(URI.create(base + "/local-file/" + served.tokenPath()))
                .GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("static/js/main.js");
    }

    @Test
    void servedHtmlPage_hasItsFileSchemeResourcesRewrittenAndServed() throws Exception {
        File image = writeImageFixture();
        LocalFileServing.Served served = registerLocalPageFixture(
                "<img src=\"" + image.toURI() + "\">");

        String base = server.getApplicationUrl();
        HttpResponse<String> page = client.send(
                HttpRequest.newBuilder(URI.create(base + "/local-file/" + served.tokenPath())).GET().build(),
                HttpResponse.BodyHandlers.ofString());

        assertThat(page.statusCode()).isEqualTo(200);
        assertThat(page.body())
                .as("a file: URL left in the served page is requested as file: from an http: "
                        + "document, which the browser refuses outright")
                .doesNotContain("file:")
                .contains(LocalFileServing.URL_PREFIX);

        String imagePath = page.body().replaceAll("(?s).*src=\"([^\"]+)\".*", "$1");
        HttpResponse<byte[]> servedImage = client.send(
                HttpRequest.newBuilder(URI.create(base + imagePath)).GET().build(),
                HttpResponse.BodyHandlers.ofByteArray());

        assertThat(servedImage.statusCode()).as("the rewritten image URL actually serves").isEqualTo(200);
        assertThat(servedImage.body()).isEqualTo(PIXEL_PNG);
    }

    @Test
    void servedBinaryResource_isLeftByteIdentical() throws Exception {
        File image = writeImageFixture();
        LocalFileServing.Served served = LocalFileServing.registerIfLocalFile("file:" + image.getAbsolutePath());

        HttpResponse<byte[]> response = client.send(
                HttpRequest.newBuilder(URI.create(server.getApplicationUrl()
                        + "/local-file/" + served.tokenPath())).GET().build(),
                HttpResponse.BodyHandlers.ofByteArray());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo(PIXEL_PNG);
    }

    private File writeImageFixture() throws Exception {
        imageDir = Files.createTempDirectory("equo-local-image").toFile();
        File image = new File(imageDir, "icon.png");
        Files.write(image.toPath(), PIXEL_PNG);
        return image;
    }

    private LocalFileServing.Served registerLocalPageFixture(String extraBody) throws Exception {
        pageDir = Files.createTempDirectory("equo-local-page").toFile();
        File html = new File(pageDir, "LocalPage.html");
        Files.writeString(html.toPath(),
                "<!doctype html><html><body>" + extraBody + "</body></html>", StandardCharsets.UTF_8);
        LocalFileServing.Served served = LocalFileServing.registerIfLocalFile("file:" + html.getAbsolutePath());
        assertThat(served).as("fixture setup: the HTML file itself must register cleanly").isNotNull();
        return served;
    }

    /** A local HTML fixture: a bundled page whose script is root-absolute. */
    private LocalFileServing.Served registerLocalPageFixture() throws Exception {
        LocalFileServing.Served served =
                registerLocalPageFixture("<script src=\"/static/js/main.js\"></script>");
        File js = new File(pageDir, "static/js/main.js");
        js.getParentFile().mkdirs();
        Files.writeString(js.toPath(), "document.title='rendered-widgets';", StandardCharsets.UTF_8);
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
