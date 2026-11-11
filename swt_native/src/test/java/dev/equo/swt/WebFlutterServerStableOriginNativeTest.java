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
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A web application's page is served on the same origin launch after launch, so the browser reuses
 * what it downloaded and compiled; it revalidates every file of the bundle, so a new build is never
 * served from its cache.
 */
@Tag("native-unit")
class WebFlutterServerStableOriginNativeTest {

    private File appDir;
    private final List<WebFlutterServer> servers = new ArrayList<>();
    private final List<ServerSocket> held = new ArrayList<>();
    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    void webDir() throws Exception {
        appDir = Files.createTempDirectory("equo-stable-origin-app").toFile();
        Files.writeString(appDir.toPath().resolve("index.html"),
                "<script>window.evolve.instance = \"{{EQUO_INSTANCE}}\";</script>", StandardCharsets.UTF_8);
        Files.writeString(appDir.toPath().resolve("main.dart.js"), "console.log('build 1');", StandardCharsets.UTF_8);
    }

    @AfterEach
    void teardown() throws Exception {
        servers.forEach(WebFlutterServer::stop);
        for (ServerSocket s : held) s.close();
        System.clearProperty(WebFlutterServer.RELAUNCH_PORT);
        System.clearProperty(StableWebPort.PROPERTY);
        System.clearProperty("eclipse.product");
        System.clearProperty("osgi.install.area");
        org.eclipse.swt.internal.cloudready.DefaultDisplayResolver.set(null);
        try (java.util.stream.Stream<Path> walk = Files.walk(appDir.toPath())) {
            walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
    }

    // ---- which ports ----

    @Test
    void anApplicationGetsTheSamePortsEveryLaunch() {
        int[] first = StableWebPort.candidates("ada", "com.example.App");
        int[] again = StableWebPort.candidates("ada", "com.example.App");

        assertThat(again).containsExactly(first);
        assertThat(first).hasSize(StableWebPort.CANDIDATES).doesNotHaveDuplicates();
        assertThat(Arrays.stream(first).boxed().toList())
                .allSatisfy(p -> assertThat(p).isBetween(StableWebPort.FIRST, StableWebPort.FIRST + StableWebPort.COUNT - 1));
    }

    @Test
    void anotherApplicationOrAnotherUserGetsOtherPorts() {
        int[] app = StableWebPort.candidates("ada", "com.example.App");

        assertThat(StableWebPort.candidates("ada", "com.example.Other")[0]).isNotEqualTo(app[0]);
        assertThat(StableWebPort.candidates("grace", "com.example.App")[0]).isNotEqualTo(app[0]);
    }

    @Test
    void anEclipseProductIsKnownByItsIdAndWhereItIsInstalled() {
        System.setProperty("eclipse.product", "com.example.product");
        System.setProperty("osgi.install.area", "file:/opt/example/");

        assertThat(StableWebPort.applicationIdentity()).isEqualTo("com.example.product@file:/opt/example/");
    }

    @Test
    void anApplicationOutsideEclipseIsKnownByItsMainClass() {
        String command = System.getProperty("sun.java.command");
        try {
            System.setProperty("sun.java.command", "com.example.Main --flag value");

            assertThat(StableWebPort.applicationIdentity()).isEqualTo("com.example.Main");
        } finally {
            if (command == null) System.clearProperty("sun.java.command");
            else System.setProperty("sun.java.command", command);
        }
    }

    @Test
    void sessionsSharingTheJvmGetNoStablePort() {
        org.eclipse.swt.internal.cloudready.DefaultDisplayResolver.set(() -> null);

        assertThat(StableWebPort.candidates()).isEmpty();
    }

    @Test
    void theStableOriginCanBeTurnedOff() {
        System.setProperty(StableWebPort.PROPERTY, "false");

        assertThat(StableWebPort.candidates()).isEmpty();
    }

    @Test
    void byDefaultAServerIsOnTheApplicationsFirstStablePort() throws Exception {
        // Every Gradle test JVM of this user is the same application, so other forks and other builds
        // on the host hold its ports: an identity of its own gives this test ports no one else uses.
        System.setProperty("eclipse.product", "stable-origin-test-" + UUID.randomUUID());
        int[] ports = StableWebPort.candidates();
        assertThat(ports).isNotEmpty();
        int expected = Arrays.stream(ports).filter(WebFlutterServerStableOriginNativeTest::isFree).findFirst().orElseThrow();

        WebFlutterServer server = new WebFlutterServer.Builder().webDirectory(appDir).commPort(0).build();
        server.start();
        servers.add(server);

        assertThat(server.getPort()).isEqualTo(expected);
    }

    // ---- binding ----

    @Test
    void aServerBindsTheFirstStablePort() throws Exception {
        int[] ports = freePorts(3);

        assertThat(start(ports).getPort()).isEqualTo(ports[0]);
    }

    @Test
    void aSecondInstanceTakesTheNextStablePort() throws Exception {
        int[] ports = freePorts(3);

        WebFlutterServer first = start(ports);
        WebFlutterServer second = start(ports);

        assertThat(first.getPort()).isEqualTo(ports[0]);
        assertThat(second.getPort()).isEqualTo(ports[1]);
    }

    @Test
    void aStablePortAnotherProcessHoldsIsSkipped() throws Exception {
        int[] ports = freePorts(2);
        hold(ports[0]);

        assertThat(start(ports).getPort()).isEqualTo(ports[1]);
    }

    @Test
    void withEveryStablePortTakenTheServerIsOnAnEphemeralOne() throws Exception {
        int[] ports = freePorts(2);
        hold(ports[0]);
        hold(ports[1]);

        WebFlutterServer server = start(ports);

        assertThat(server.getPort()).isPositive().isNotIn(ports[0], ports[1]);
    }

    @Test
    void aRelaunchedJvmTakesThePortItWasRelaunchedFromBeforeAStableOne() throws Exception {
        int[] three = freePorts(3);
        int[] ports = {three[0], three[1]};
        int relaunch = three[2];
        System.setProperty(WebFlutterServer.RELAUNCH_PORT, String.valueOf(relaunch));

        WebFlutterServer server = start(ports);

        assertThat(server.getPort()).isEqualTo(relaunch);
        assertThat(server.servesRelaunchedPage()).isTrue();
    }

    @Test
    void aTakenRelaunchPortFallsBackToAStablePort() throws Exception {
        int[] three = freePorts(3);
        int[] ports = {three[0], three[1]};
        int relaunch = three[2];
        hold(relaunch);
        System.setProperty(WebFlutterServer.RELAUNCH_PORT, String.valueOf(relaunch));

        WebFlutterServer server = start(ports);

        assertThat(server.getPort()).isEqualTo(ports[0]);
        assertThat(server.servesRelaunchedPage()).isFalse();
    }

    @Test
    void aConfiguredPortIgnoresTheStablePorts() throws Exception {
        int[] three = freePorts(3);
        int[] ports = {three[0], three[1]};
        int configured = three[2];

        WebFlutterServer server = new WebFlutterServer.Builder().webDirectory(appDir).commPort(0)
                .httpPort(configured).stablePorts(ports).build();
        server.start();
        servers.add(server);

        assertThat(server.getPort()).isEqualTo(configured);
    }

    // ---- an earlier run's page ----

    @Test
    void aTabAnEarlierRunLeftOpenIsNotCalledBackIntoALaterLaunch() throws Exception {
        int[] ports = freePorts(1);
        WebFlutterServer earlier = start(ports);
        earlier.stop();

        WebFlutterServer later = start(ports);
        HttpResponse<String> answer = get(later.getPort(), WebFlutterServer.INSTANCE_PATH, null);

        assertThat(later.getPort()).isEqualTo(ports[0]);
        assertThat(answer.statusCode()).isEqualTo(404);
        assertThat(answer.body()).isEmpty();
        assertThat(later.awaitReturningPage(50)).isFalse();
    }

    // ---- caching ----

    @Test
    void theBundleIsRevalidatedAgainstATagOfItsContent() throws Exception {
        WebFlutterServer server = start(freePorts(1));

        HttpResponse<String> script = get(server.getPort(), "/main.dart.js", null);

        assertThat(script.statusCode()).isEqualTo(200);
        assertThat(script.headers().firstValue("Cache-Control")).hasValue("no-cache");
        assertThat(script.headers().firstValue("ETag")).hasValueSatisfying(t -> assertThat(t).matches("\"[0-9a-f]+-[0-9a-f]+\""));
    }

    @Test
    void anUnchangedFileIsNotSentAgain() throws Exception {
        WebFlutterServer server = start(freePorts(1));
        String tag = get(server.getPort(), "/main.dart.js", null).headers().firstValue("ETag").orElseThrow();

        HttpResponse<String> revalidated = get(server.getPort(), "/main.dart.js", tag);

        assertThat(revalidated.statusCode()).isEqualTo(304);
        assertThat(revalidated.body()).isEmpty();
        assertThat(revalidated.headers().firstValue("ETag")).hasValue(tag);
    }

    @Test
    void aFileOfANewBuildIsSentAgainEvenWithTheSameSizeAndTime() throws Exception {
        Path script = appDir.toPath().resolve("main.dart.js");
        FileTime time = Files.getLastModifiedTime(script);
        int[] ports = freePorts(1);
        WebFlutterServer before = start(ports);
        String tag = get(before.getPort(), "/main.dart.js", null).headers().firstValue("ETag").orElseThrow();
        before.stop();

        Files.writeString(script, "console.log('build 2');", StandardCharsets.UTF_8);
        Files.setLastModifiedTime(script, time);
        WebFlutterServer after = start(ports);
        HttpResponse<String> response = get(after.getPort(), "/main.dart.js", tag);

        assertThat(after.getPort()).isEqualTo(before.getPort());
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("console.log('build 2');");
        assertThat(response.headers().firstValue("ETag")).isPresent().get().isNotEqualTo(tag);
    }

    @Test
    void aFileChangedWhileServingGetsANewTag() throws Exception {
        WebFlutterServer server = start(freePorts(1));
        String tag = get(server.getPort(), "/main.dart.js", null).headers().firstValue("ETag").orElseThrow();

        Path script = appDir.toPath().resolve("main.dart.js");
        Files.writeString(script, "console.log('rebuilt, longer');", StandardCharsets.UTF_8);
        HttpResponse<String> response = get(server.getPort(), "/main.dart.js", tag);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("console.log('rebuilt, longer');");
    }

    @Test
    void thePageIsNeverCached() throws Exception {
        WebFlutterServer server = start(freePorts(1));

        HttpResponse<String> page = get(server.getPort(), "/", null);

        assertThat(page.headers().firstValue("Cache-Control")).hasValue("no-cache, no-store, must-revalidate");
        assertThat(page.headers().firstValue("ETag")).isEmpty();
    }

    @Test
    void aListOfTagsOrAWeakOneMatches() {
        assertThat(WebFlutterServer.StaticFileHandler.matches("\"a-1\", \"b-2\"", "\"b-2\"")).isTrue();
        assertThat(WebFlutterServer.StaticFileHandler.matches("W/\"b-2\"", "\"b-2\"")).isTrue();
        assertThat(WebFlutterServer.StaticFileHandler.matches("*", "\"b-2\"")).isTrue();
        assertThat(WebFlutterServer.StaticFileHandler.matches("\"a-1\"", "\"b-2\"")).isFalse();
        assertThat(WebFlutterServer.StaticFileHandler.matches(null, "\"b-2\"")).isFalse();
    }

    private WebFlutterServer start(int[] stablePorts) throws Exception {
        WebFlutterServer server = new WebFlutterServer.Builder()
                .webDirectory(appDir)
                .commPort(0)
                .widgetId(1)
                .widgetName("Display")
                .serveServiceWorker(false)
                .stablePorts(stablePorts)
                .build();
        server.start();
        servers.add(server);
        return server;
    }

    /**
     * Ports free right now, all different, picked where an application's stable ones are. That range is
     * below the OS's ephemeral one, so between this check and the server binding them no other process
     * is handed one of them for its own ephemeral bind or outgoing connection.
     */
    private static int[] freePorts(int count) {
        return ThreadLocalRandom.current().ints(StableWebPort.FIRST, StableWebPort.FIRST + StableWebPort.COUNT)
                .distinct()
                .filter(WebFlutterServerStableOriginNativeTest::isFree)
                .limit(count)
                .toArray();
    }

    private void hold(int port) throws Exception {
        ServerSocket s = new ServerSocket();
        s.bind(new InetSocketAddress("localhost", port));
        held.add(s);
    }

    private static boolean isFree(int port) {
        try (ServerSocket s = new ServerSocket()) {
            s.bind(new InetSocketAddress("localhost", port));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private HttpResponse<String> get(int port, String path, String ifNoneMatch) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path));
        if (ifNoneMatch != null) request.header("If-None-Match", ifNoneMatch);
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }
}
