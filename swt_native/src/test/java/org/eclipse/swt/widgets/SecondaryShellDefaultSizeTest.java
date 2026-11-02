package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

/**
 * A dialog shell the application never sizes gets the size native SWT creates it at, and the client is
 * sent that size rather than an unsized shell it would draw at its own minimum. Cocoa creates every
 * shell, dialog or not, at 5/8 of the screen each way, rounded: 945x614 on a 1512x982 screen. GTK gives
 * the client area 5/8, truncated, and adds its default trim: 951x642 for a resizable shell, 950x641 for a
 * dialog (GTK SWT 3.135 under Xvnc). Win32 leaves it to the system, which sizes a top-level shell from the work area
 * and a dialog from its parent's position: 1134x625 and 1060x551 on a 1512x909 screen with a 48-pixel
 * taskbar.
 *
 * <p>Both surfaces: the web one, and the desktop-native one with its window stubbed.
 */
@Tag("flutter-it")
class SecondaryShellDefaultSizeTest {

    private static final Rectangle SCREEN = new Rectangle(0, 0, 1512, 982);

    private Display display;
    private String savedMode;

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
        if (savedMode == null)
            System.clearProperty("dev.equo.swt.mode");
        else
            System.setProperty("dev.equo.swt.mode", savedMode);
    }

    /** The size native SWT creates a dialog at on {@link #SCREEN}. */
    private static Point nativeDefault() {
        switch (SWT.getPlatform()) {
            case "cocoa": return new Point(945, 614);
            case "gtk": return new Point(950, 641);
            case "win32": return new Point(1060, 606);
            default: throw new IllegalStateException("not measured on " + SWT.getPlatform());
        }
    }

    /** The size native SWT creates a top-level shell at on {@link #SCREEN}. */
    private static Point nativeTopLevelDefault() {
        switch (SWT.getPlatform()) {
            case "cocoa": return new Point(945, 614);
            case "gtk": return new Point(951, 642);
            case "win32": return new Point(1134, 680);
            default: throw new IllegalStateException("not measured on " + SWT.getPlatform());
        }
    }

    @Test
    void web_aDialogCreatedWithoutASize_hasTheNativeDefault_andIsSentSized() {
        TestWebBridge web = install(TestWebBridge::new);
        clientReady(web.comm);
        Shell main = new Shell(display);
        main.open();

        Shell dialog = new Shell(main);
        List<Point> resizes = resizes(dialog);
        dialog.open();

        assertSoftly(s -> {
            s.assertThat(dialog.getSize()).isEqualTo(nativeDefault());
            s.assertThat(resizes).as("its size does not change when shown").allMatch(nativeDefault()::equals);
            s.assertThat(sentBounds(dialog))
                    .as("sent sized, so the client draws it at that size instead of at its own minimum")
                    .isEqualTo(dialog.getBounds());
        });
    }

    @Test
    void desk_aDialogCreatedWithoutASize_hasTheNativeDefault_andIsSentSized() {
        TestDeskBridge desk = install(TestDeskBridge::new);
        clientReady(desk.comm);
        Shell main = new Shell(display);
        main.open();
        Rectangle mainBounds = main.getBounds();

        Shell dialog = new Shell(main, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        dialog.open();

        assertSoftly(s -> {
            s.assertThat(dialog.getSize()).isEqualTo(nativeDefault());
            s.assertThat(sentBounds(dialog))
                    .as("sent sized, so the client draws it at that size instead of at its own minimum")
                    .isEqualTo(dialog.getBounds());
            s.assertThat(main.getBounds()).as("the main shell is left as it was").isEqualTo(mainBounds);
        });
    }

    @Test
    void desk_aSecondTopLevelShellCreatedWithoutASize_hasTheNativeDefault() {
        TestDeskBridge desk = install(TestDeskBridge::new);
        clientReady(desk.comm);
        Shell main = new Shell(display);
        main.open();

        Shell second = new Shell(display);

        assertThat(second.getSize()).isEqualTo(nativeTopLevelDefault());
    }

    @Test
    void web_aDialogTheApplicationSized_keepsItsSize() {
        TestWebBridge web = install(TestWebBridge::new);
        clientReady(web.comm);
        Shell main = new Shell(display);
        main.open();
        Shell dialog = new Shell(main);
        dialog.setSize(333, 222);

        dialog.open();

        assertThat(dialog.getSize()).isEqualTo(new Point(333, 222));
    }

    private <B extends DisplayBridge> B install(Function<DartDisplay, B> factory) {
        savedMode = System.getProperty("dev.equo.swt.mode");
        FlutterBridge.set(new RecordingBridge());
        display = new Display();
        FlutterBridge.set(null);
        DartDisplay dd = (DartDisplay) display.getImpl();
        B bridge = factory.apply(dd);
        dd.setBridge(bridge);
        bridge.start(dd);
        return bridge;
    }

    /** The client's first report: on the desktop the monitor is {@link #SCREEN}, on the web the viewport is. */
    private static void clientReady(RecordingComm comm) {
        ClientReadyPayload p = new ClientReadyPayload();
        p.wireFingerprint = dev.equo.swt.WireTable.FINGERPRINT;
        p.width = SCREEN.width;
        p.height = SCREEN.height;
        p.displayWidth = SCREEN.width;
        p.displayHeight = SCREEN.height;
        p.isFirst = true;
        comm.fireContaining("ClientReady", p);
    }

    /** What the client is sent as the shell's bounds. */
    private static Rectangle sentBounds(Shell shell) {
        return ((DartShell) shell.getImpl()).getValue().getBounds();
    }

    private static List<Point> resizes(Shell shell) {
        List<Point> sizes = new ArrayList<>();
        shell.addListener(SWT.Resize, e -> sizes.add(shell.getSize()));
        return sizes;
    }

    /** Desktop-native surface bridge with the native window stubbed. */
    private static final class TestDeskBridge extends DeskDisplayBridge {
        final RecordingComm comm = new RecordingComm();

        TestDeskBridge(DartDisplay display) {
            super(display);
        }

        @Override
        protected CommService comm() {
            return comm;
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
        protected void wakeNativeWindow() {
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
        protected int pumpWindow() {
            return 0;
        }

        @Override
        protected void disposeNativeWindow() {
        }

        @Override
        protected void setShellWindowTitle(long context, String title) {
        }

        @Override
        protected void setShellWindowState(long context, int state) {
        }

        @Override
        protected void setShellWindowVisible(long context, boolean visible) {
        }

        @Override
        protected void forwardWindowBounds(Rectangle bounds) {
        }

        @Override
        public void setWindowTitle(DartControl control, String title) {
        }
    }

    /** Web (browser) surface bridge; pure-web has no window to forward to. */
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
            registerWindowControls(display);
        }
    }
}
