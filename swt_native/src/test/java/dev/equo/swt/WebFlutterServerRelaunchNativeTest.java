package dev.equo.swt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An RCP application that relaunches itself starts a new JVM, and on the web the user is left with
 * the tab the previous one served. The new JVM serves on the previous one's port, carried in the
 * {@code eclipse.vmargs} the application rebuilds its command line from, and tells the old page it
 * is another server so the page reloads into it.
 */
@Tag("native-unit")
class WebFlutterServerRelaunchNativeTest {

    private static final Pattern INSTANCE = Pattern.compile("window\\.evolve\\.instance = \"([^\"]*)\"");

    private File appDir;
    private final List<WebFlutterServer> servers = new ArrayList<>();
    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    void webDir() throws Exception {
        appDir = Files.createTempDirectory("equo-relaunch-app").toFile();
        Files.writeString(appDir.toPath().resolve("index.html"),
                "<script>window.evolve.instance = \"{{EQUO_INSTANCE}}\";</script>", StandardCharsets.UTF_8);
    }

    @AfterEach
    void teardown() throws Exception {
        servers.forEach(WebFlutterServer::stop);
        System.clearProperty(WebFlutterServer.RELAUNCH_PORT);
        System.clearProperty("eclipse.vmargs");
        org.eclipse.swt.internal.cloudready.DefaultDisplayResolver.set(null);
        try (java.util.stream.Stream<Path> walk = Files.walk(appDir.toPath())) {
            walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
    }

    @Test
    void theRelaunchPortIsPutAheadOfTheVmArgsAndReplacesAPreviousOne() {
        String vmargs = "-Xmx2g\n-Dsoftware.session=abc\n-jar\nplugins/launcher.jar\n";

        String once = WebFlutterServer.withRelaunchPort(vmargs, 61001);
        String twice = WebFlutterServer.withRelaunchPort(once, 61002);

        assertThat(once).isEqualTo("-D" + WebFlutterServer.RELAUNCH_PORT + "=61001\n" + vmargs);
        assertThat(twice).isEqualTo("-D" + WebFlutterServer.RELAUNCH_PORT + "=61002\n" + vmargs);
    }

    @Test
    void anApplicationStartedByTheLauncherCarriesThePortToItsRelaunch() {
        System.setProperty("eclipse.vmargs", "-Xmx2g\n-jar\nplugins/launcher.jar\n");

        WebFlutterServer.carryPortAcrossRelaunch(61003);

        assertThat(System.getProperty("eclipse.vmargs"))
                .startsWith("-D" + WebFlutterServer.RELAUNCH_PORT + "=61003\n-Xmx2g\n");
    }

    @Test
    void sessionsSharingTheJvmCarryNothing() {
        String vmargs = "-Xmx2g\n-jar\nplugins/launcher.jar\n";
        System.setProperty("eclipse.vmargs", vmargs);
        org.eclipse.swt.internal.cloudready.DefaultDisplayResolver.set(() -> null);

        WebFlutterServer.carryPortAcrossRelaunch(61003);

        assertThat(System.getProperty("eclipse.vmargs")).isEqualTo(vmargs);
    }

    @Test
    void anApplicationNotStartedByTheLauncherHasNothingToCarry() {
        WebFlutterServer.carryPortAcrossRelaunch(61003);

        assertThat(System.getProperty("eclipse.vmargs")).isNull();
    }

    @Test
    void theRelaunchedJvmServesOnThePortOfTheOneBefore() throws Exception {
        WebFlutterServer before = start();
        int port = before.getPort();
        before.stop();

        System.setProperty(WebFlutterServer.RELAUNCH_PORT, String.valueOf(port));
        WebFlutterServer after = start();

        assertThat(after.getPort()).isEqualTo(port);
        assertThat(after.servesRelaunchedPage()).isTrue();
    }

    @Test
    void aTakenRelaunchPortFallsBackToAnotherAndOpensNoPageOfItsOwn() throws Exception {
        try (ServerSocket taken = new ServerSocket()) {
            taken.bind(new InetSocketAddress("localhost", 0));
            System.setProperty(WebFlutterServer.RELAUNCH_PORT, String.valueOf(taken.getLocalPort()));

            WebFlutterServer server = start();

            assertThat(server.getPort()).isNotEqualTo(taken.getLocalPort());
            assertThat(server.servesRelaunchedPage()).isFalse();
        }
    }

    @Test
    void aPageServedByTheJvmBeforeLearnsItIsServedByAnotherNow() throws Exception {
        WebFlutterServer before = start();
        int port = before.getPort();
        String servedBy = instanceInPage(port);
        assertThat(get(port, WebFlutterServer.INSTANCE_PATH)).isEqualTo(servedBy);
        before.stop();

        System.setProperty(WebFlutterServer.RELAUNCH_PORT, String.valueOf(port));
        WebFlutterServer after = start();
        String answer = get(port, WebFlutterServer.INSTANCE_PATH);

        assertThat(servedBy).matches("[0-9a-f-]{36}");
        assertThat(answer).matches("[0-9a-f-]{36}").isNotEqualTo(servedBy);
        assertThat(instanceInPage(port)).isEqualTo(answer);
    }

    @Test
    void theRelaunchedJvmKnowsWhenThePageBeforeAsks() throws Exception {
        WebFlutterServer server = start();

        assertThat(server.awaitReturningPage(50)).as("no page has asked yet").isFalse();
        get(server.getPort(), WebFlutterServer.INSTANCE_PATH);
        assertThat(server.awaitReturningPage(50)).isTrue();
    }

    private WebFlutterServer start() throws Exception {
        WebFlutterServer server = new WebFlutterServer.Builder()
                .webDirectory(appDir)
                .commPort(0)
                .widgetId(1)
                .widgetName("Display")
                .serveServiceWorker(false)
                .build();
        server.start();
        servers.add(server);
        return server;
    }

    private String instanceInPage(int port) throws Exception {
        Matcher m = INSTANCE.matcher(get(port, "/"));
        assertThat(m.find()).isTrue();
        return m.group(1);
    }

    private String get(int port, String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).build();
        return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }
}
