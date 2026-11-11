package org.eclipse.swt.widgets;

import dev.equo.swt.ConfigFlags;
import dev.equo.swt.DesktopAppearanceStub;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.NoopBridge;
import dev.equo.swt.harness.RecordingComm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A desktop Display starts its bridge, and with it the native window and engine, early in
 * {@code Display.init()}. The call that ends {@code init()} then finds that bridge and keeps it: a
 * second one would open a second window with its own engine. The desktop's appearance, read ahead for
 * the window's background, is the one the Display keeps too.
 */
class DisplayBridgeStartEarlyTest {

    private Display display;
    private String savedMode;
    private String savedDesktop;

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
        restore(ConfigFlags.MODE_PROPERTY, savedMode);
        restore("dev.equo.swt.desktop", savedDesktop);
    }

    private static void restore(String key, String value) {
        if (value == null) System.clearProperty(key);
        else System.setProperty(key, value);
    }

    private DartDisplay desktopDisplay() {
        savedMode = System.getProperty(ConfigFlags.MODE_PROPERTY);
        savedDesktop = System.getProperty("dev.equo.swt.desktop");
        ConfigFlags.setMode(ConfigFlags.MODE_DESKTOP);
        FlutterBridge.set(new NoopBridge());
        display = new Display();
        FlutterBridge.set(null);
        return (DartDisplay) display.getImpl();
    }

    @Test
    void theDesktopAppearanceReadAheadIsTheOneTheDisplayKeeps() throws InterruptedException {
        try (DesktopAppearanceStub desktop = new DesktopAppearanceStub("dark")) {
            DartDisplay dd = desktopDisplay();

            assertThat(dd.darkTheme()).isTrue();
            assertThat(dd.darkTheme()).isTrue();
            desktop.awaitReadThreads();
            assertThat(desktop.readOn).as("the system command runs once per Display").hasSize(1);
        }
    }

    @Test
    void theBridgeStartedEarlyIsTheOneTheDisplayKeeps() {
        try (DesktopAppearanceStub desktop = new DesktopAppearanceStub("light")) {
            DartDisplay dd = desktopDisplay();
            CountingDeskBridge early = new CountingDeskBridge(dd);
            dd.setBridge(early);
            early.start(dd);

            DisplayBridge kept = DisplayBridge.initForDisplay(dd);

            assertThat(kept).isSameAs(early);
            assertThat(dd.displayBridge).isSameAs(early);
            assertThat(early.starts).as("the window is opened once").isEqualTo(1);
            assertThat(dd.darkTheme()).as("the stand-in desktop answered").isFalse();
        }
    }

    /** The desktop bridge with its window stood in: it only counts the starts it is asked for. */
    private static final class CountingDeskBridge extends DeskDisplayBridge {
        final RecordingComm comm = new RecordingComm();
        int starts;

        CountingDeskBridge(DartDisplay display) {
            super(display);
        }

        @Override
        protected CommService comm() {
            return comm;
        }

        @Override
        protected void start(DartDisplay display) {
            starts++;
        }
    }
}
