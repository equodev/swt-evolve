package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.RecordingComm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins the fix for #821 ("Late ConfgFlags cause flicker"): the config flags ride in the Display value.
 *
 * <p>The old ordering was not a race, it was structural. A Display update is buffered before the
 * client connects and flushed on connect, so the shells reach Dart as soon as the socket opens. The
 * {@code swt.evolve.properties} message, by contrast, is only pushed on the first {@code ClientReady}
 * — which Flutter only sends once it has a frame's worth of viewport metrics. The flags therefore
 * always landed at least one frame after the content: Dart painted with default flags, then rebuilt
 * the whole MaterialApp when the real ones arrived. Carrying them in {@link VDisplay} makes them
 * arrive in the same payload as the shells they belong to. The channel stays as the fallback for the
 * surfaces that have no Display value (the embedded per-widget bridge), so it is asserted here too.
 *
 * <p>Same shape as {@link DisplayResizeFlutterTest}: a {@code @Tag("flutter-it")} test driven by the
 * {@code nativeTest} task, with a {@link RecordingComm} standing in for the Flutter client.
 */
@Tag("flutter-it")
class DisplayConfigFlagsFlutterTest {

    private Display display;
    private ConfigFlags savedFlags;
    private String savedMode;

    @BeforeEach
    void setUp() {
        // The flags are a JVM singleton, so stash the real ones and drive the test off a distinctive
        // set — a theme that is nobody's default, so seeing it on the wire can only come from here.
        savedFlags = Config.getConfigFlags();
        ConfigFlags flags = new ConfigFlags();
        flags.force_theme = "light";
        flags.theme_name = "marketplace";
        Config.setConfigFlags(flags);
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
        Config.setConfigFlags(savedFlags);
        if (savedMode == null)
            System.clearProperty("dev.equo.swt.mode");
        else
            System.setProperty("dev.equo.swt.mode", savedMode);
    }

    @Test
    void displayUpdate_carriesTheConfigFlags() {
        TestWebBridge web = install(TestWebBridge::new);
        newMainShell();

        web.comm.sent.clear();
        web.sendDisplayUpdate(dartDisplay());

        assertThat(lastFrameOn(web, "Display/" + dartDisplay().getApi().hashCode()))
                .as("the flags ride with the shells, so Flutter applies both in the same frame")
                .contains("\"config\":")
                .contains("\"force_theme\":\"light\"")
                .contains("\"theme_name\":\"marketplace\"");
    }

    @Test
    void propertiesChannel_isStillPushedAsTheFallback() {
        TestWebBridge web = install(TestWebBridge::new);
        newMainShell();
        web.comm.sent.clear();

        clientReady(web.comm, 1024, 768, true);

        assertThat(web.comm.sent)
                .as("the standalone channel remains, for the surfaces that have no Display value")
                .anyMatch(f -> f.event.equals("swt.evolve.properties"));
    }

    // ---- harness ----------------------------------------------------------------------------------

    private DartDisplay dartDisplay() {
        return (DartDisplay) display.getImpl();
    }

    private static String lastFrameOn(TestWebBridge web, String event) {
        return web.comm.sent.stream()
                .filter(f -> f.event.equals(event))
                .reduce((a, b) -> b)
                .orElseThrow(() -> new AssertionError("no frame on '" + event + "'"))
                .json;
    }

    /** See {@code DisplayResizeFlutterTest.install} — a Display whose bridge is the test bridge. */
    private <B extends DisplayBridge> B install(Function<DartDisplay, B> factory) {
        savedMode = System.getProperty("dev.equo.swt.mode");
        FlutterBridge.set(new NoopBridge());
        display = new Display();
        FlutterBridge.set(null);
        DartDisplay dd = dartDisplay();
        B bridge = factory.apply(dd);
        dd.setBridge(bridge);
        bridge.start(dd);
        return bridge;
    }

    private Shell newMainShell() {
        return new Shell(display);
    }

    private static void clientReady(RecordingComm comm, int width, int height, boolean isFirst) {
        ClientReadyPayload p = new ClientReadyPayload();
        p.width = width;
        p.height = height;
        p.isFirst = isFirst;
        comm.fireContaining("ClientReady", p);
    }

    /** A stub injected only so {@code Display.init()} skips creating a real surface bridge. */
    private static final class NoopBridge extends FlutterBridge {
        final RecordingComm comm = new RecordingComm();

        NoopBridge() {
            clientReady.complete(true);
        }

        @Override
        protected CommService comm() {
            return comm;
        }

        @Override
        public void initFlutterView(Composite parent, DartControl control) {
        }

        @Override
        public void destroy(DartWidget control) {
        }
    }

    private static final class TestWebBridge extends WebDisplayBridge {
        final RecordingComm comm = new RecordingComm();

        TestWebBridge(DartDisplay display) {
            super(display);
        }

        @Override
        protected CommService comm() {
            return comm;
        }

        @Override
        protected void start(DartDisplay display) {
            registerDisplayClientReady(display);
        }
    }
}
