package dev.equo.swt;

import dev.equo.swt.spi.FlutterBridgeSpi;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * A web Display's server starts before its comm listens, so the browser loads the page while the
 * Display is still being created. The page is told its comm port is still to come, asks for it, and
 * is answered once the Display publishes it.
 */
@Tag("native-unit")
class WebFlutterServerCommPortLaterNativeTest {

    private static final String PAGE = "port={{EQUO_COMM_PORT}} pending={{EQUO_COMM_PORT_PENDING}}";

    private File appDir;
    private final List<WebFlutterServer> servers = new ArrayList<>();
    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    void webDir() throws Exception {
        appDir = Files.createTempDirectory("equo-comm-port-app").toFile();
        page(PAGE);
    }

    @AfterEach
    void teardown() throws Exception {
        servers.forEach(WebFlutterServer::stop);
        FlutterBridgeSpi.registerPageOpener(url -> false);
        try (java.util.stream.Stream<Path> walk = Files.walk(appDir.toPath())) {
            walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
    }

    @Test
    void aPageServedBeforeTheCommListensIsToldItsPortIsToCome() throws Exception {
        WebFlutterServer server = start(new WebFlutterServer.Builder().commPortLater());

        assertThat(get(server, "/").join()).isEqualTo("port=0 pending=true");
    }

    @Test
    void thePageIsAnsweredItsPortOnceTheDisplayPublishesIt() throws Exception {
        WebFlutterServer server = start(new WebFlutterServer.Builder().commPortLater());

        CompletableFuture<String> asked = get(server, WebFlutterServer.COMM_PORT_PATH);
        assertThatThrownBy(() -> asked.get(200, TimeUnit.MILLISECONDS))
                .as("no port before the Display publishes one").isInstanceOf(TimeoutException.class);

        server.publishCommPort(4242);

        assertThat(asked.get(5, TimeUnit.SECONDS)).isEqualTo("4242");
        assertThat(get(server, "/").join()).isEqualTo("port=4242 pending=false");
    }

    @Test
    void aPageThatCannotAskForItsPortIsHeldUntilThereIsOne() throws Exception {
        page("port={{EQUO_COMM_PORT}}");
        WebFlutterServer server = start(new WebFlutterServer.Builder().commPortLater());

        CompletableFuture<String> served = get(server, "/");
        assertThatThrownBy(() -> served.get(200, TimeUnit.MILLISECONDS))
                .as("served without a port, the page would connect to none").isInstanceOf(TimeoutException.class);

        server.publishCommPort(4243);

        assertThat(served.get(5, TimeUnit.SECONDS)).isEqualTo("port=4243");
    }

    @Test
    void aServerGivenItsPortServesItAtOnce() throws Exception {
        WebFlutterServer server = start(new WebFlutterServer.Builder().commPort(4244));

        assertThat(get(server, "/").join()).isEqualTo("port=4244 pending=false");
        assertThat(get(server, WebFlutterServer.COMM_PORT_PATH).join()).isEqualTo("4244");
    }

    @Test
    void aHostThatOpensThePageItselfIsHandedItInsteadOfTheSystemBrowser() throws Exception {
        List<String> opened = new ArrayList<>();
        FlutterBridgeSpi.registerPageOpener(opened::add);
        WebFlutterServer server = start(new WebFlutterServer.Builder().commPortLater());

        server.launchBrowser();

        assertThat(opened).containsExactly(server.getApplicationUrl());
    }

    private void page(String content) throws Exception {
        Files.writeString(appDir.toPath().resolve("index.html"), content, StandardCharsets.UTF_8);
    }

    private WebFlutterServer start(WebFlutterServer.Builder builder) throws Exception {
        WebFlutterServer server = builder.webDirectory(appDir).widgetId(1).widgetName("Display")
                .serveServiceWorker(false).build();
        server.start();
        servers.add(server);
        return server;
    }

    private CompletableFuture<String> get(WebFlutterServer server, String path) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(server.getApplicationUrl() + path)).build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body);
    }
}
