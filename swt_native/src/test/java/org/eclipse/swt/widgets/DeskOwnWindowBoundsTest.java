package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import dev.equo.swt.DesktopAppearanceStub;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.ShellWindow;
import dev.equo.swt.WindowPolicy;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.NoopBridge;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.DPIUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Where the OS window of its own that draws a shell is, and when shells get and lose theirs. */
class DeskOwnWindowBoundsTest {

    private static final String[] KEYS = {WindowPolicy.MODE_PROPERTY, WindowPolicy.DIALOGS_PROPERTY,
            WindowPolicy.MODAL_PROPERTY, ConfigFlags.MODE_PROPERTY, "dev.equo.swt.desktop"};

    private final Map<String, String> saved = new HashMap<>();
    private int savedZoom;
    private DesktopAppearanceStub desktop;
    private Display display;
    private WindowsBridge bridge;
    private Shell main;
    private int changes;

    @BeforeEach
    void setUp() {
        for (String key : KEYS) saved.put(key, System.getProperty(key));
        savedZoom = DPIUtil.getDeviceZoom();
        desktop = new DesktopAppearanceStub("light");
        ConfigFlags.setMode(ConfigFlags.MODE_DESKTOP);
        System.setProperty(WindowPolicy.MODE_PROPERTY, "all");
        System.clearProperty(WindowPolicy.DIALOGS_PROPERTY);
        System.setProperty(WindowPolicy.MODAL_PROPERTY, "inline");
        FlutterBridge.set(new NoopBridge());
        display = new Display();
        FlutterBridge.set(null);
        DartDisplay dd = (DartDisplay) display.getImpl();
        bridge = new WindowsBridge(dd);
        dd.setBridge(bridge);
        bridge.start(dd);
        main = new Shell(display, SWT.SHELL_TRIM);
        main.setBounds(0, 32, 1920, 1168);
        main.open();
        drain();
        bridge.addShellWindowListener(() -> changes++);
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
        desktop.close();
        DPIUtil.setDeviceZoom(savedZoom);
        Config.setClientDeviceZoom(100);
        saved.forEach((key, value) -> {
            if (value == null) System.clearProperty(key);
            else System.setProperty(key, value);
        });
    }

    private void drain() {
        while (display.readAndDispatch()) {
            /* drain */
        }
    }

    private Shell dialog(Shell parent, int style) {
        Shell dialog = new Shell(parent, style);
        dialog.setBounds(524, 359, 873, 497);
        dialog.open();
        drain();
        return dialog;
    }

    private static Rectangle own(WindowsBridge bridge, Shell shell) {
        return bridge.ownWindowBounds(shell);
    }

    @Test
    void aShellInAWindowOfItsOwnIsWhereItsBoundsAre() {
        Shell dialog = dialog(main, SWT.DIALOG_TRIM);
        assertThat(bridge.windowedShells()).containsExactly(dialog);

        assertThat(own(bridge, dialog)).isEqualTo(dialog.getBounds());
        assertThat(changes).isEqualTo(1);
    }

    @Test
    void theStripItsClientDrawsAboveTheShellIsPartOfTheWindow() {
        Shell dialog = dialog(main, SWT.DIALOG_TRIM);

        bridge.makeRoomForShellWindowInset(dialog, 32);

        Rectangle shell = dialog.getBounds();
        assertThat(own(bridge, dialog))
                .isEqualTo(new Rectangle(shell.x, shell.y - 32, shell.width, shell.height + 32));
    }

    @Test
    void theMainWindowsShellsHaveNone() {
        Shell modal = dialog(main, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        assertThat(bridge.windowedShells()).isEmpty();

        assertThat(own(bridge, main)).isNull();
        assertThat(own(bridge, modal)).isNull();
        assertThat(own(bridge, null)).isNull();
        assertThat(changes).isZero();
    }

    @Test
    void anInlineShellOfAWindowedOneIsDrawnInThatWindow() {
        Shell dialog = dialog(main, SWT.DIALOG_TRIM);
        Shell modal = dialog(dialog, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        assertThat(bridge.windowedShells()).containsExactly(dialog);
        bridge.makeRoomForShellWindowInset(dialog, 32);

        assertThat(own(bridge, modal)).as("no window of its own").isNull();
        assertThat(bridge.windowOf(modal)).isSameAs(dialog);
        Rectangle shell = dialog.getBounds();
        assertThat(own(bridge, (Shell) bridge.windowOf(modal)))
                .isEqualTo(new Rectangle(shell.x, shell.y - 32, shell.width, shell.height + 32));
    }

    @Test
    void underThePolicyThatOpensNoWindowsThereIsNone() {
        System.setProperty(WindowPolicy.MODE_PROPERTY, "single");
        Shell dialog = dialog(main, SWT.DIALOG_TRIM);

        assertThat(own(bridge, dialog)).isNull();
        assertThat(changes).isZero();
    }

    @Test
    void aWindowTheOsTookDownIsNoneAndSaysSo() {
        Shell dialog = dialog(main, SWT.DIALOG_TRIM);
        changes = 0;

        bridge.osClosedWindowOf(dialog);
        assertThat(own(bridge, dialog)).as("before the next flush drops the window").isNull();
        bridge.sendDisplayUpdate((DartDisplay) display.getImpl());
        drain();

        assertThat(changes).isEqualTo(1);
    }

    @Test
    void aShellBackInTheMainWindowHasNoneAndSaysSo() {
        Shell dialog = dialog(main, SWT.DIALOG_TRIM);
        changes = 0;

        dialog.setData(WindowPolicy.SHELL_DATA_KEY, Boolean.FALSE);
        bridge.sendDisplayUpdate((DartDisplay) display.getImpl());
        drain();

        assertThat(bridge.windowedShells()).isEmpty();
        assertThat(own(bridge, dialog)).isNull();
        assertThat(changes).isEqualTo(1);
    }

    @Test
    void aRemovedListenerIsNotRun() {
        Runnable gone = () -> { throw new AssertionError("removed"); };
        bridge.addShellWindowListener(gone);
        bridge.removeShellWindowListener(gone);

        dialog(main, SWT.DIALOG_TRIM);

        assertThat(changes).isEqualTo(1);
    }

    /** The browser's surface: its windows are pages, each drawing one shell. */
    @Test
    void aShellInAPageOfItsOwnIsWhereItsBoundsAre() {
        display.dispose();
        RecordingDisplayBridge web = RecordingDisplayBridge.install(PagesBridge::new);
        display = Display.getCurrent();
        main = new Shell(display, SWT.SHELL_TRIM);
        main.open();
        Shell dialog = dialog(main, SWT.DIALOG_TRIM);
        assertThat(web.windowedShells()).containsExactly(dialog);

        assertThat(web.ownWindowBounds(dialog)).isEqualTo(dialog.getBounds());
        assertThat(web.ownWindowBounds(main)).as("the Display's client draws it").isNull();
    }

    @Test
    void anInlineShellOfAShellWithAPageIsDrawnInThatPage() {
        display.dispose();
        RecordingDisplayBridge web = RecordingDisplayBridge.install(PagesBridge::new);
        display = Display.getCurrent();
        main = new Shell(display, SWT.SHELL_TRIM);
        main.open();
        Shell dialog = dialog(main, SWT.DIALOG_TRIM);
        Shell modal = dialog(dialog, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        Shell ofMain = dialog(main, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        assertThat(web.windowedShells()).containsExactly(dialog);

        assertThat(web.windowOf(modal)).isSameAs(dialog);
        assertThat(web.ownWindowBounds(dialog)).isEqualTo(dialog.getBounds());
        assertThat(web.ownWindowBounds(modal)).as("no page of its own").isNull();
        assertThat(web.windowOf(ofMain)).as("drawn in the Display's page").isNull();
    }

    private static final class PagesBridge extends RecordingDisplayBridge {
        PagesBridge(DartDisplay display) {
            super(display);
        }

        @Override
        protected boolean supportsShellWindows() {
            return true;
        }

        @Override
        protected ShellWindow createShellWindow(Shell shell) {
            return new Page();
        }
    }

    private static final class Page implements ShellWindow {
        @Override
        public void setTitle(String title) {
        }

        @Override
        public void setBounds(Rectangle bounds) {
        }

        @Override
        public void setState(int state) {
        }

        @Override
        public void setVisible(boolean visible) {
        }

        @Override
        public void close() {
        }

        @Override
        public boolean isAlive() {
            return true;
        }
    }

    /** The desktop bridge with its native windows stood in by handles nothing native reads. */
    private static final class WindowsBridge extends DeskDisplayBridge {
        final RecordingComm comm = new RecordingComm();
        private long nextContext = 1;

        WindowsBridge(DartDisplay display) {
            super(display);
        }

        void osClosedWindowOf(Shell shell) {
            ((NativeShellWindow) shellWindowFor((DartShell) shell.getImpl())).markGone();
        }

        @Override
        protected java.util.Set<Shell> windowedShells() {
            return super.windowedShells();
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
        protected long openNativeWindow(long owner, long widgetId, String widgetName, int width, int height) {
            return nextContext++;
        }

        @Override
        protected void bindShellWindowView(Shell shell, long context) {
        }

        @Override
        protected Point shellWindowOrigin(long context) {
            return null;
        }

        @Override
        protected int workAreaTop(Rectangle window) {
            return 0;
        }

        @Override
        protected int pumpWindow() {
            return 0;
        }

        @Override
        protected int pumpShellWindow(long context) {
            return 0;
        }

        @Override
        protected void disposeShellWindow(long context) {
        }

        @Override
        protected void setShellWindowTitle(long context, String title) {
        }

        @Override
        protected void setShellWindowBounds(long context, Rectangle bounds) {
        }

        @Override
        protected void setShellWindowState(long context, int state) {
        }

        @Override
        protected void setShellWindowOwner(long context, long owner) {
        }

        @Override
        protected void setShellWindowVisible(long context, boolean visible) {
        }

        @Override
        protected long mainWindowContext() {
            return 0;
        }

        @Override
        protected void forwardWindowBounds(Rectangle bounds) {
        }

        @Override
        protected void forwardWindowFullScreen(boolean fullScreen) {
        }

        @Override
        protected boolean windowActive(long context) {
            return true;
        }

        @Override
        protected void waitNativeWindow(int millis) {
        }

        @Override
        protected void wakeNativeWindow() {
        }

        @Override
        protected void disposeNativeWindow() {
        }
    }
}
