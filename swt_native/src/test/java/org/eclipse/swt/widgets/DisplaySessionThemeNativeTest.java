package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import dev.equo.swt.FlutterBridge;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Every browser on one session paints the theme the application took its colours from: the one the
 * session's first client reported.
 */
class DisplaySessionThemeNativeTest {

    private ConfigFlags savedFlags;
    private Display display;

    @BeforeEach
    void setUp() {
        savedFlags = Config.getConfigFlags();
        Config.setConfigFlags(new ConfigFlags());
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
        Config.setConfigFlags(savedFlags);
    }

    @Test
    void aLaterClientIsSentTheThemeOfTheSessionsFirstClient() {
        ThemeRecordingBridge bridge = RecordingDisplayBridge.install(ThemeRecordingBridge::new);
        display = Display.getCurrent();
        DartDisplay impl = (DartDisplay) display.getImpl();

        clientReady(bridge, "dark");
        bridge.comm.sent.clear();
        bridge.themesSent.clear();
        clientReady(bridge, "light");

        assertThat(impl.darkTheme()).as("the application's colours were picked from the first report").isTrue();
        assertThat(bridge.comm.sent).as("the later client is sent the properties")
                .anyMatch(f -> f.event.endsWith("swt.evolve.properties") && f.bytes != null);
        assertThat(bridge.themesSent)
                .as("the later client paints the session's theme, not its own system's")
                .isNotEmpty()
                .containsOnly("dark");
        assertThat(Config.getConfigFlags().force_theme).as("the flags every session shares").isNull();
    }

    private void clientReady(RecordingDisplayBridge bridge, String appearance) {
        ClientReadyPayload p = new ClientReadyPayload();
        p.wireFingerprint = dev.equo.swt.WireTable.FINGERPRINT;
        p.width = 1280;
        p.height = 800;
        p.isFirst = true;
        p.appearance = appearance;
        bridge.comm.fireContaining("Display/" + display.hashCode() + "/ClientReady", p);
        RecordingDisplayBridge.drainEventLoop(display);
    }

    /** Records the theme of each set of flags the bridge sends its clients. */
    static final class ThemeRecordingBridge extends RecordingDisplayBridge {
        final List<String> themesSent = new ArrayList<>();

        ThemeRecordingBridge(DartDisplay display) {
            super(display);
        }

        @Override
        protected ConfigFlags swtEvolveProperties() {
            ConfigFlags flags = super.swtEvolveProperties();
            themesSent.add(flags.force_theme);
            return flags;
        }
    }
}
