package dev.equo.swt;

import com.sun.net.httpserver.HttpServer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
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
 * A proxied page runs on the app's origin, so a URL it builds from its own {@code location.host}
 * (a nested frame's {@code "http://" + location.host + "/path"}, say) reaches the app server, not
 * the page's own server. The app must hand such a request to the page's server: serving its own
 * index.html there boots a second copy of the application inside the page.
 */
@Tag("native-unit")
class WebFlutterServerProxiedPageOwnHostNativeTest {

    private static final String APP_MARKER = "fake-flutter-app";

    private WebFlutterServer app;
    private File appDir;
    private HttpServer origin;
    private HttpServer otherOrigin;
    private final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL).build();
    private final HttpClient noRedirects = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER).build();

    @BeforeEach
    void boot() throws Exception {
        appDir = Files.createTempDirectory("equo-proxy-own-host-app").toFile();
        Files.writeString(appDir.toPath().resolve("index.html"),
                "<!doctype html><title>" + APP_MARKER + "</title>", StandardCharsets.UTF_8);
        app = new WebFlutterServer.Builder()
                .webDirectory(appDir)
                .commPort(0)
                .widgetId(1)
                .widgetName("Display")
                .serveServiceWorker(false)
                .build();
        app.start();

        origin = startOrigin("first");
        otherOrigin = startOrigin("second");
    }

    private static HttpServer startOrigin(String name) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            String body = "<html><head></head><body>" + name + " served " + exchange.getRequestURI()
                    + "</body></html>";
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
            exchange.close();
        });
        server.start();
        return server;
    }

    @AfterEach
    void teardown() {
        if (app != null) app.stop();
        if (origin != null) origin.stop(0);
        if (otherOrigin != null) otherOrigin.stop(0);
        deleteRecursively(appDir);
    }

    @Test
    void aFrameBuiltFromTheProxiedPageHostLoadsFromThePageServer() throws Exception {
        String page = originUrl("/editor/index.html?s=a&f=b");

        HttpResponse<String> frame = get(app.getApplicationUrl() + "/content/form/b/index.html?n=2",
                proxyUrl(page));

        assertThat(frame.statusCode()).isEqualTo(200);
        assertThat(frame.body())
                .as("the page's own server answers the path and query the page asked for")
                .contains("served /content/form/b/index.html?n=2")
                .doesNotContain(APP_MARKER);
        assertThat(frame.uri().getPath())
                .as("served through the proxy, so the page can still script the frame")
                .isEqualTo("/proxy");
    }

    @Test
    void aPageInThePathFormIsRoutedToItsServerToo() throws Exception {
        String referer = app.getApplicationUrl() + "/proxy/" + originUrl("/editor/index.html");

        HttpResponse<String> frame = get(app.getApplicationUrl() + "/content/index.html", referer);

        assertThat(frame.body()).contains("served /content/index.html").doesNotContain(APP_MARKER);
    }

    @Test
    void theAppOwnRoutesStillServeTheApp() throws Exception {
        assertThat(get(app.getApplicationUrl() + "/some/route", null).body()).contains(APP_MARKER);
        assertThat(get(app.getApplicationUrl() + "/some/route", app.getApplicationUrl() + "/").body())
                .contains(APP_MARKER);
    }

    @Test
    void anElementBuiltFromAProxiedPageLocationLoadsFromThePageServer() throws Exception {
        get(proxyUrl(originUrl("/designer/form.html")), null);

        HttpResponse<String> image = send(noRedirects, app.getApplicationUrl() + "/img/logo.png",
                app.getApplicationUrl() + "/designer/form.html");

        assertThat(image.statusCode()).isEqualTo(307);
        assertThat(image.headers().firstValue("Location"))
                .hasValue("/proxy?url=" + URLEncoder.encode(originUrl("/img/logo.png"), StandardCharsets.UTF_8));
    }

    @Test
    void aPathShownByPagesOfTwoServersBelongsToTheLaterOne() throws Exception {
        get(proxyUrl(originUrl("/designer/form.html")), null);
        get(proxyUrl(otherOriginUrl("/designer/form.html")), null);

        HttpResponse<String> image = get(app.getApplicationUrl() + "/img/logo.png",
                app.getApplicationUrl() + "/designer/form.html");

        assertThat(image.body()).contains("second served /img/logo.png");
    }

    @Test
    void aMissingFileTheAppAsksForGetsTheAppWhileAProxiedPageShowsItsAddress() throws Exception {
        get(proxyUrl(originUrl("/")), null);
        get(proxyUrl(originUrl("/index.html")), null);

        for (String appDocument : new String[] {"/", "/index.html", "/?x", "/index.html?x"}) {
            HttpResponse<String> missing = send(noRedirects, app.getApplicationUrl() + "/some/route",
                    app.getApplicationUrl() + appDocument);

            assertThat(missing.statusCode()).as("asked from %s", appDocument).isEqualTo(200);
            assertThat(missing.body()).as("asked from %s", appDocument).contains(APP_MARKER);
        }
    }

    @Test
    void aProxiedPageShownAtTheAppDocumentLoadsItsLocationBuiltElementsFromTheApp() throws Exception {
        for (String appDocument : new String[] {"/", "/index.html"}) {
            get(proxyUrl(originUrl(appDocument)), null);

            HttpResponse<String> image = get(app.getApplicationUrl() + "/img/logo.png",
                    app.getApplicationUrl() + appDocument);

            assertThat(image.body())
                    .as("shown at %s, the app's own document, whose requests the app answers", appDocument)
                    .contains(APP_MARKER)
                    .doesNotContain("served /img/logo.png");
        }
    }

    @Test
    void aProxiedPageSocketToTheAppHostGoesToThePageServer() {
        String html = ProxyHandler.injectSocketShim(
                "<html><head><script src=\"app.js\"></script></head></html>",
                "http://localhost:8283/editor/index.html");

        assertThat(html.indexOf("window.WebSocket")).as("patched before the page's scripts run")
                .isPositive().isLessThan(html.indexOf("app.js"));
        assertThat(html).contains("\"http://localhost:8283/editor/index.html\"");
    }

    private String originUrl(String pathAndQuery) {
        return "http://127.0.0.1:" + origin.getAddress().getPort() + pathAndQuery;
    }

    private String otherOriginUrl(String pathAndQuery) {
        return "http://127.0.0.1:" + otherOrigin.getAddress().getPort() + pathAndQuery;
    }

    private String proxyUrl(String target) {
        return app.getApplicationUrl() + "/proxy?url=" + URLEncoder.encode(target, StandardCharsets.UTF_8);
    }

    private HttpResponse<String> get(String url, String referer) throws Exception {
        return send(client, url, referer);
    }

    private static HttpResponse<String> send(HttpClient via, String url, String referer) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url)).GET();
        if (referer != null) request.header("Referer", referer);
        return via.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static void deleteRecursively(File dir) {
        if (dir == null || !dir.exists()) return;
        try (var paths = Files.walk(dir.toPath())) {
            paths.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        } catch (Exception ignored) {
        }
    }
}
