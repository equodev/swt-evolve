package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.NoopBridge;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * The launcher's splash on the desktop: a shell made around the launcher's own window, which keeps
 * drawing it. The Display's window stays off screen while the splash is all there is, and never
 * draws the splash itself, so the user sees one splash, as made, and then the application.
 */
@Tag("flutter-it")
class DeskLauncherSplashFlutterTest {

    private static final long LAUNCHER_WINDOW = 0x5150L;

    private Display display;
    private String savedMode;

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
        if (savedMode == null) System.clearProperty("dev.equo.swt.mode");
        else System.setProperty("dev.equo.swt.mode", savedMode);
    }

    @Test
    void theDisplaysWindowStaysOffScreenWhileTheSplashIsAllThereIs() {
        TestDeskBridge desk = install();
        Shell splash = Shell.internal_new(display, LAUNCHER_WINDOW);
        splash.setVisible(true);

        assertThat(desk.mainWindowVisibility).containsExactly(false);

        Shell workbench = new Shell(display, SWT.SHELL_TRIM);
        workbench.setVisible(true);

        assertThat(desk.mainWindowVisibility).containsExactly(false, true);

        splash.dispose();
        assertThat(desk.mainWindowVisibility).as("the application's window stays up").containsExactly(false, true);
    }

    @Test
    void aDialogOpenedOverTheSplashBringsTheDisplaysWindowUp() {
        TestDeskBridge desk = install();
        Shell splash = Shell.internal_new(display, LAUNCHER_WINDOW);
        splash.setVisible(true);

        Shell workspaceChooser = new Shell(splash, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        workspaceChooser.setVisible(true);

        assertThat(desk.mainWindowVisibility).containsExactly(false, true);
    }

    @Test
    void theSplashIsLeftToItsOwnWindowAndIsNeverTheMainShell() {
        TestDeskBridge desk = install();
        Shell splash = Shell.internal_new(display, LAUNCHER_WINDOW);
        splash.setVisible(true);

        assertThat(windowedShellIdsSent(desk)).isEqualTo(String.valueOf(splash.hashCode()));
        assertThat(desk.hostsAsMainShell(splash)).isFalse();
    }

    @Test
    void aShellWithoutTrimOfTheApplicationsOwnIsDrawnAsUsual() {
        TestDeskBridge desk = install();
        Shell plain = new Shell(display, SWT.NO_TRIM);
        plain.setVisible(true);
        Shell wrapsNothing = Shell.internal_new(display, 0);
        wrapsNothing.setVisible(true);

        assertThat(desk.mainWindowVisibility).isEmpty();
        assertThat(windowedShellIdsSent(desk)).isEmpty();
    }

    /** The ids the last Display frame told the client to leave out of the Display's window. */
    private String windowedShellIdsSent(TestDeskBridge desk) {
        String channel = "Display/" + display.hashCode();
        List<RecordingComm.Frame> frames =
                desk.comm.sent.stream().filter(f -> f.event.equals(channel)).toList();
        assertThat(frames).as("no Display frame was ever produced").isNotEmpty();
        Matcher ids = Pattern.compile("\"windowedShellIds\":\\[([^\\]]*)\\]")
                .matcher(frames.get(frames.size() - 1).json);
        return ids.find() ? ids.group(1) : "";
    }

    private TestDeskBridge install() {
        savedMode = System.getProperty("dev.equo.swt.mode");
        System.setProperty("dev.equo.swt.mode", "desktop");
        FlutterBridge.set(new NoopBridge());
        display = new Display();
        FlutterBridge.set(null);
        DartDisplay dd = (DartDisplay) display.getImpl();
        TestDeskBridge bridge = new TestDeskBridge(dd);
        dd.setBridge(bridge);
        bridge.start(dd);
        return bridge;
    }

    /** The desktop bridge with its own window stood in, and what it is told to show recorded. */
    private static final class TestDeskBridge extends DeskDisplayBridge {
        final RecordingComm comm = new RecordingComm();
        final List<Boolean> mainWindowVisibility = new ArrayList<>();

        TestDeskBridge(DartDisplay display) {
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

        @Override
        protected boolean hasWindowSurface() {
            return true;
        }

        @Override
        protected void setShellWindowVisible(long context, boolean visible) {
            mainWindowVisibility.add(visible);
        }

        @Override
        protected void setShellWindowTitle(long context, String title) {
        }

        @Override
        protected void setShellWindowState(long context, int state) {
        }

        @Override
        protected void forwardWindowBounds(Rectangle bounds) {
        }

        @Override
        protected void disposeNativeWindow() {
        }

        @Override
        protected int pumpWindow() {
            return 0;
        }

        @Override
        protected org.eclipse.swt.graphics.Point shellWindowOrigin(long context) {
            return null;
        }

        @Override
        protected void wakeNativeWindow() {
        }

        @Override
        protected long mainWindowView() {
            return 1L;
        }

        @Override
        protected void bindShellWindowView(Shell shell, long context) {
        }

        @Override
        public void setWindowTitle(DartControl control, String title) {
        }

        @Override
        public void setBounds(DartControl control, Rectangle bounds) {
        }
    }
}
