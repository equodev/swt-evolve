package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import dev.equo.swt.ConfigFlags;
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
 * draws the splash itself, so the user sees one splash, as made, and then the application. Outside
 * the desktop the launcher's window is not on the user's screen, so the splash is drawn as any shell.
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
        if (savedMode == null) System.clearProperty(ConfigFlags.MODE_PROPERTY);
        else System.setProperty(ConfigFlags.MODE_PROPERTY, savedMode);
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

    /** A login dialog parented to a shell the application never shows, before any workbench exists. */
    @Test
    void aDialogOverAHiddenShellTakesTheDisplaysWindow() {
        TestDeskBridge desk = install();
        Shell hidden = new Shell(display, SWT.ON_TOP);
        Shell login = new Shell(hidden, SWT.RESIZE | SWT.TITLE | SWT.CLOSE | SWT.ON_TOP);
        login.setBounds(535, 140, 850, 800);
        login.setVisible(true);

        assertThat(desk.hostsAsMainShell(login)).as("the login is the shell of the window it is drawn in").isTrue();
        assertThat(desk.windowBounds)
                .as("the window has the place and size the login's own window would have")
                .last().isEqualTo(new Rectangle(535, 140, 850, 800));
        assertThat(desk.mainWindowVisibility).as("the window is never taken off screen").doesNotContain(false);
    }

    @Test
    void theShellOpenedAfterThatDialogKeepsItsOwnBounds() {
        TestDeskBridge desk = install();
        Shell hidden = new Shell(display, SWT.ON_TOP);
        Shell login = new Shell(hidden, SWT.RESIZE | SWT.TITLE | SWT.CLOSE | SWT.ON_TOP);
        login.setBounds(535, 140, 850, 800);
        login.setVisible(true);
        clientReady(desk.comm, 850, 800);
        login.dispose();

        Shell workbench = new Shell(display, SWT.SHELL_TRIM);
        workbench.setBounds(100, 60, 1600, 900);
        workbench.setVisible(true);
        while (display.readAndDispatch()) {
            // drain
        }

        assertThat(workbench.getBounds()).as("not fitted to the window the login had").isEqualTo(new Rectangle(100, 60, 1600, 900));
        assertThat(desk.windowBounds).last().isEqualTo(new Rectangle(100, 60, 1600, 900));
    }

    @Test
    void aDialogOverAMainShellHiddenAgainTakesTheDisplaysWindow() {
        TestDeskBridge desk = install();
        Shell main = new Shell(display, SWT.SHELL_TRIM);
        main.setBounds(100, 60, 1600, 900);
        main.setVisible(true);
        main.setVisible(false);
        Shell dialog = new Shell(main, SWT.DIALOG_TRIM);
        dialog.setBounds(535, 140, 850, 800);
        dialog.setVisible(true);

        assertThat(desk.hostsAsMainShell(dialog)).as("drawn in a hidden window, it would be on no screen").isTrue();
        assertThat(desk.mainWindowVisibility).last().isEqualTo(true);
        assertThat(desk.windowBounds).last().isEqualTo(new Rectangle(535, 140, 850, 800));
    }

    private static void clientReady(RecordingComm comm, int width, int height) {
        ClientReadyPayload p = new ClientReadyPayload();
        p.wireFingerprint = dev.equo.swt.WireTable.FINGERPRINT;
        p.width = width;
        p.height = height;
        p.isFirst = true;
        comm.fireContaining("ClientReady", p);
    }

    @Test
    void theSplashIsLeftToItsOwnWindowAndIsNeverTheMainShell() {
        TestDeskBridge desk = install();
        Shell splash = Shell.internal_new(display, LAUNCHER_WINDOW);
        splash.setVisible(true);

        assertThat(windowedShellIdsSent(desk.comm)).isEqualTo(String.valueOf(splash.hashCode()));
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
        assertThat(windowedShellIdsSent(desk.comm)).isEmpty();
    }

    @Test
    void outsideTheDesktopTheSplashIsTheMainShellAndDrawnInTheDisplaysWindow() {
        TestWebBridge web = install(ConfigFlags.MODE_CHROMIUM, TestWebBridge::new);
        Shell splash = Shell.internal_new(display, LAUNCHER_WINDOW);
        splash.setVisible(true);

        assertThat(web.hostsAsMainShell(splash)).isTrue();
        assertThat(windowedShellIdsSent(web.comm)).isEmpty();
    }

    /** The ids the last Display frame told the client to leave out of the Display's window. */
    private String windowedShellIdsSent(RecordingComm comm) {
        String channel = "Display/" + display.hashCode();
        List<RecordingComm.Frame> frames =
                comm.sent.stream().filter(f -> f.event.equals(channel)).toList();
        assertThat(frames).as("no Display frame was ever produced").isNotEmpty();
        Object ids = frames.get(frames.size() - 1).state().get("windowedShellIds");
        if (!(ids instanceof List<?> list)) return "";
        return list.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private TestDeskBridge install() {
        return install(ConfigFlags.MODE_DESKTOP, TestDeskBridge::new);
    }

    private <B extends DisplayBridge> B install(String mode, Function<DartDisplay, B> factory) {
        savedMode = System.getProperty(ConfigFlags.MODE_PROPERTY);
        System.setProperty(ConfigFlags.MODE_PROPERTY, mode);
        FlutterBridge.set(new NoopBridge());
        display = new Display();
        FlutterBridge.set(null);
        DartDisplay dd = (DartDisplay) display.getImpl();
        B bridge = factory.apply(dd);
        dd.setBridge(bridge);
        bridge.start(dd);
        return bridge;
    }

    /** The desktop bridge with its own window stood in, and what it is told to show recorded. */
    private static final class TestDeskBridge extends DeskDisplayBridge {
        final RecordingComm comm = new RecordingComm();
        final List<Boolean> mainWindowVisibility = new ArrayList<>();
        final List<Rectangle> windowBounds = new ArrayList<>();

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
            windowBounds.add(bounds);
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
