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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The page server logs on a thread of its own, so the thread that starts the server and opens the
 * page does not pay for starting java.util.logging.
 */
@Tag("native-unit")
class WebFlutterServerLogNativeTest {

    private File appDir;
    private final List<WebFlutterServer> servers = new ArrayList<>();
    private final HttpClient client = HttpClient.newHttpClient();
    private final Logger log = Logger.getLogger(WebFlutterServer.class.getName());
    private final CompletableFuture<String> started = new CompletableFuture<>();
    private final CompletableFuture<LogRecord> opened = new CompletableFuture<>();
    private final Handler startedLine = new Handler() {
        @Override
        public void publish(LogRecord record) {
            if (record.getMessage().startsWith("WebFlutterServer started on")) started.complete(record.getMessage());
            if (record.getMessage().startsWith("The host opened")) opened.complete(record);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }
    };

    @BeforeEach
    void webDir() throws Exception {
        appDir = Files.createTempDirectory("equo-log-app").toFile();
        Files.writeString(appDir.toPath().resolve("index.html"),
                "<script>window.evolve = {port: {{EQUO_COMM_PORT}}, pending: {{EQUO_COMM_PORT_PENDING}},"
                        + " instance: \"{{EQUO_INSTANCE}}\"};</script>", StandardCharsets.UTF_8);
        log.addHandler(startedLine);
    }

    @AfterEach
    void teardown() throws Exception {
        log.removeHandler(startedLine);
        dev.equo.swt.spi.FlutterBridgeSpi.registerPageOpener(url -> false);
        servers.forEach(WebFlutterServer::stop);
        try (java.util.stream.Stream<Path> walk = Files.walk(appDir.toPath())) {
            walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
    }

    private WebFlutterServer start() throws Exception {
        WebFlutterServer server = new WebFlutterServer.Builder().webDirectory(appDir).httpPort(0).stablePorts()
                .commPortLater().build();
        servers.add(server);
        server.start();
        return server;
    }

    private HttpResponse<String> get(WebFlutterServer server, String path) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create(server.getApplicationUrl() + path)).build(),
                HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void theStartedLineNamesTheUrl() throws Exception {
        WebFlutterServer server = start();

        assertThat(started.get(10, TimeUnit.SECONDS))
                .isEqualTo("WebFlutterServer started on " + server.getApplicationUrl());
    }

    @Test
    void theServerListensOnTheLoopbackAddress() throws Exception {
        WebFlutterServer server = start();

        assertThat(server.boundAddress().getAddress()).isEqualTo(java.net.InetAddress.getLoopbackAddress());
        assertThat(get(server, "/").statusCode()).isEqualTo(200);
    }

    @Test
    void openingThePageDoesNotLogOnTheThreadThatOpensIt() throws Exception {
        dev.equo.swt.spi.FlutterBridgeSpi.registerPageOpener(url -> true);
        WebFlutterServer server = start();

        server.launchBrowser();

        assertThat(opened.get(10, TimeUnit.SECONDS).getLongThreadID()).isNotEqualTo(Thread.currentThread().getId());
    }

    @Test
    void everyServerHasItsOwnInstance() {
        assertThat(WebFlutterServer.newInstanceId()).isNotEqualTo(WebFlutterServer.newInstanceId())
                .matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }
}
