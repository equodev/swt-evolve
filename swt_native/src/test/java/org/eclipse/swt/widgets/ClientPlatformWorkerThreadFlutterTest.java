package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;

import dev.equo.swt.ClientPlatform;
import dev.equo.swt.ServerPlatform;
import dev.equo.swt.harness.WidgetFlutterHarness;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.eclipse.swt.internal.cloudready.DefaultDisplayResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * A worker thread has no {@code Display.getCurrent()}; when it carries a session it still answers
 * with that session's client platform, and without one with the application's.
 */
@Tag("flutter-it")
class ClientPlatformWorkerThreadFlutterTest {

    private WidgetFlutterHarness flutter;
    private Display display;

    @BeforeEach
    void setUp() {
        flutter = new WidgetFlutterHarness();
        flutter.init();
        display = new Display();
        // a client unlike the application's machine, so the two answers differ
        ((DartDisplay) display.getImpl()).clientPlatform = "cocoa".equals(ServerPlatform.get()) ? "windows" : "macOS";
    }

    @AfterEach
    void tearDown() {
        DefaultDisplayResolver.set(null);
        if (display != null && !display.isDisposed()) display.dispose();
        if (flutter != null) flutter.teardown();
    }

    @Test
    void aWorkerThreadOfASessionAnswersWithTheSessionsClient() throws Exception {
        DefaultDisplayResolver.set(() -> display);
        assertThat(onWorkerThread(ClientPlatform::current))
                .isEqualTo(ClientPlatform.of(display))
                .isNotEqualTo(ServerPlatform.get());
    }

    @Test
    void aWorkerThreadWithoutASessionAnswersWithTheApplicationPlatform() throws Exception {
        assertThat(onWorkerThread(ClientPlatform::current)).isEqualTo(ServerPlatform.get());
    }

    private static String onWorkerThread(Callable<String> call) throws Exception {
        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            return pool.submit(call).get();
        } finally {
            pool.shutdown();
        }
    }
}
