package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * A web Display opens its page as it starts being created, so the browser loads the page while the
 * rest of the Display is built; the page is only given its comm port once the Display's comm listens.
 */
@Tag("native-unit")
class WebPageLaunchNativeTest {

    private File appDir;
    private Display display;
    private final Map<String, String> overridden = new HashMap<>();
    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    void webDir() throws Exception {
        appDir = Files.createTempDirectory("equo-page-launch-app").toFile();
        Files.writeString(appDir.toPath().resolve("index.html"),
                "port={{EQUO_COMM_PORT}} pending={{EQUO_COMM_PORT_PENDING}}", StandardCharsets.UTF_8);
        override("dev.equo.swt.web.dir", appDir.getAbsolutePath());
        override("equo.swt.browser", "none");
        FlutterBridge.set(null);
    }

    @AfterEach
    void teardown() throws Exception {
        FlutterBridgeSpi.registerPageOpener(url -> false);
        FlutterBridgeSpi.registerDisplayCreatedListener(d -> { });
        if (display != null && !display.isDisposed()) display.dispose();
        overridden.forEach((key, previous) -> {
            if (previous == null) System.clearProperty(key);
            else System.setProperty(key, previous);
        });
        try (java.util.stream.Stream<Path> walk = Files.walk(appDir.toPath())) {
            walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
    }

    @Test
    void thePageIsOpenedBeforeTheDisplayHasACommAndGivenItsPortOnceItHas() throws Exception {
        String[] pageAtOpen = new String[1];
        CompletableFuture<String>[] askedPort = new CompletableFuture[1];
        FlutterBridgeSpi.registerPageOpener(url -> {
            // What a browser opened right now would be served, and the port it would be answered.
            pageAtOpen[0] = get(url + "/").join();
            askedPort[0] = get(url + "/equo-comm-port");
            return true;
        });

        display = new Display();

        assertThat(pageAtOpen[0]).as("opened while the Display was being created").isEqualTo("port=0 pending=true");
        int commPort = FlutterBridgeSpi.getCommPort(display);
        assertThat(commPort).isPositive();
        assertThat(askedPort[0].get(5, TimeUnit.SECONDS)).isEqualTo(String.valueOf(commPort));
        assertThat(get(FlutterBridgeSpi.getWebServerUrl(display) + "/").join())
                .isEqualTo("port=" + commPort + " pending=false");
    }

    @Test
    void aDisplayThatFailsToFinishItsCreationStopsThePageServerItStarted() {
        String[] opened = new String[1];
        FlutterBridgeSpi.registerPageOpener(url -> {
            opened[0] = url;
            return true;
        });
        // Called by the bridge once it has taken the page's server over, at the end of Display creation.
        FlutterBridgeSpi.registerDisplayCreatedListener(d -> {
            throw new IllegalStateException("the host refuses the Display");
        });

        assertThatThrownBy(Display::new).hasMessageContaining("the host refuses the Display");

        assertThat(opened[0]).as("the page was opened before the failure").isNotNull();
        assertThat(get(opened[0] + "/")).as("its server is gone").failsWithin(5, TimeUnit.SECONDS);
    }

    @Test
    void aHarnessThatInjectedItsOwnBridgeOpensNoPage() {
        boolean[] opened = new boolean[1];
        FlutterBridgeSpi.registerPageOpener(url -> opened[0] = true);
        FlutterBridge.set(new FlutterBridge() {
            @Override
            public void initFlutterView(Composite parent, DartControl control) {
            }
        });
        try {
            display = new Display();
        } finally {
            FlutterBridge.set(null);
        }

        assertThat(opened[0]).isFalse();
        assertThat(FlutterBridgeSpi.getWebServerUrl(display)).isNull();
    }

    private CompletableFuture<String> get(String url) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body);
    }

    private void override(String key, String value) {
        overridden.putIfAbsent(key, System.getProperty(key));
        System.setProperty(key, value);
    }
}
