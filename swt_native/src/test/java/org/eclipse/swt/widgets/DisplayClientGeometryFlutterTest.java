package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * On the web the Display is created before any browser has connected, and an application places its
 * first window from the Display's default geometry right away. Nothing waits for the client: once it
 * reports its viewport, a window centered before is centered again and any other stays inside it.
 */
@Tag("flutter-it")
class DisplayClientGeometryFlutterTest {

    private Display display;

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    void aDialogCenteredBeforeTheClientConnectsIsCenteredOnItsViewport() {
        TestWebBridge web = install();
        Shell login = centeredOnMonitor(850, 800);
        assertThat(display.getPrimaryMonitor().getBounds()).isEqualTo(new Rectangle(0, 0, 1920, 1080));
        assertThat(login.getLocation()).isEqualTo(new Point(535, 140));

        clientReady(web.comm, 1600, 900);

        assertThat(display.getBounds()).isEqualTo(new Rectangle(0, 0, 1600, 900));
        assertThat(login.getLocation()).isEqualTo(new Point(375, 50));
    }

    @Test
    void aDialogTallerThanTheViewportKeepsItsTitleOnScreen() {
        TestWebBridge web = install();
        Shell login = centeredOnMonitor(850, 800);

        clientReady(web.comm, 1280, 720);

        assertThat(login.getLocation()).isEqualTo(new Point(215, 0));
    }

    @Test
    void aDialogCenteredOnItsParentIsCenteredOnWhereTheParentMoved() {
        TestWebBridge web = install();
        Shell parent = new Shell(display, SWT.CLOSE | SWT.TITLE | SWT.ON_TOP);
        parent.setBounds(535, 50, 850, 600);
        Shell child = centeredOn(parent, 400, 300);
        assertThat(child.getLocation()).isEqualTo(new Point(760, 200));

        clientReady(web.comm, 1600, 900);

        assertThat(parent.getLocation()).as("centered across, kept down").isEqualTo(new Point(375, 50));
        assertThat(child.getLocation()).as("on the parent, not on the viewport").isEqualTo(new Point(600, 200));
    }

    @Test
    void aDialogCenteredOnTheMainShellIsCenteredOnTheViewport() {
        TestWebBridge web = install();
        Shell main = new Shell(display);
        main.open();
        Shell child = centeredOn(main, 400, 300);

        clientReady(web.comm, 1600, 900);

        assertThat(main.getBounds()).isEqualTo(new Rectangle(0, 0, 1600, 900));
        assertThat(child.getLocation()).isEqualTo(new Point(600, 300));
    }

    @Test
    void aDialogCenteredOnAParentThatWasNeverSizedIsCenteredOnTheViewport() {
        TestWebBridge web = install();
        Shell owner = new Shell(display, SWT.ON_TOP);
        Shell login = new Shell(owner, SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.ON_TOP);
        login.setSize(850, 800);
        Rectangle o = owner.getBounds();
        login.setLocation(o.x + (o.width - 850) / 2, o.y + (o.height - 800) / 2);
        assertThat(login.getLocation()).isEqualTo(new Point(535, 140));

        clientReady(web.comm, 1280, 720);

        assertThat(login.getLocation()).isEqualTo(new Point(215, 0));
    }

    @Test
    void theClientsFirstReportOfAMovedDialogKeepsItsPositionAndSetsItsSize() {
        TestWebBridge web = install();
        Shell login = centeredOnMonitor(850, 800);
        login.setVisible(true);
        clientReady(web.comm, 1280, 720);

        // Drawn at the old position, its body capped to the viewport.
        clientBounds(web.comm, 430, 0, 850, 648, DisplayBridge.INITIAL_CLIENT_BOUNDS);
        assertThat(login.getBounds()).isEqualTo(new Rectangle(215, 0, 850, 648));

        clientBounds(web.comm, 300, 40, 850, 648, 0);
        assertThat(login.getLocation()).as("moved by the user").isEqualTo(new Point(300, 40));
    }

    @Test
    void aDialogMovedWhileHiddenTakesTheFirstReportOnceShown() {
        TestWebBridge web = install();
        Shell login = centeredOnMonitor(850, 800);
        clientReady(web.comm, 1600, 900);
        login.setVisible(true);

        clientBounds(web.comm, 380, 60, 850, 800, DisplayBridge.INITIAL_CLIENT_BOUNDS);

        assertThat(login.getLocation()).isEqualTo(new Point(380, 60));
    }

    @Test
    void aRefreshedPageSendsNoStaleReport() {
        TestWebBridge web = install();
        Shell login = centeredOnMonitor(850, 800);
        login.setVisible(true);
        clientReady(web.comm, 1600, 900);

        clientReady(web.comm, 1600, 900);
        clientBounds(web.comm, 380, 60, 850, 800, DisplayBridge.INITIAL_CLIENT_BOUNDS);

        assertThat(login.getLocation()).isEqualTo(new Point(380, 60));
    }

    @Test
    void aDialogNotCenteredKeepsItsPositionWithinTheViewport() {
        TestWebBridge web = install();
        Shell dialog = new Shell(display, SWT.CLOSE | SWT.TITLE | SWT.ON_TOP);
        dialog.setBounds(100, 900, 400, 150);

        clientReady(web.comm, 1600, 900);

        assertThat(dialog.getLocation()).isEqualTo(new Point(100, 750));
    }

    @Test
    void aShellPlacedOffScreenStaysThere() {
        TestWebBridge web = install();
        Shell hidden = new Shell(display, SWT.NO_TRIM);
        hidden.setBounds(-5000, -5000, 300, 200);

        clientReady(web.comm, 1600, 900);

        assertThat(hidden.getLocation()).isEqualTo(new Point(-5000, -5000));
    }

    @Test
    void onlyTheFirstReportMovesShells() {
        TestWebBridge web = install();
        Shell login = centeredOnMonitor(850, 800);
        clientReady(web.comm, 1600, 900);
        login.setLocation(10, 10);

        clientReady(web.comm, 1280, 720);

        assertThat(login.getLocation()).isEqualTo(new Point(10, 10));
    }

    /**
     * The launcher makes its splash window the size of the splash image and centers it on the
     * screen; the workbench then sets that image as the background of the shell around it. In the
     * page the splash keeps that size, so its image is drawn once rather than tiled.
     */
    @Test
    void theLaunchersSplashKeepsItsImagesSizeCenteredOnTheViewport() {
        TestWebBridge web = install();
        Shell splash = Shell.internal_new(display, 0x5150L);
        Image image = new Image(display, 499, 277);
        try {
            splash.setBackgroundImage(image);
            splash.setVisible(true);

            clientReady(web.comm, 1576, 864);

            assertThat(splash.getBounds()).isEqualTo(new Rectangle(538, 293, 499, 277));
        } finally {
            image.dispose();
        }
    }

    /** A relaunch: the browser has reported its viewport before the workbench makes the splash. */
    @Test
    void theLaunchersSplashMadeAfterTheClientReportedTakesItsImagesSizeWhenShown() {
        TestWebBridge web = install();
        clientReady(web.comm, 1576, 864);
        Shell splash = Shell.internal_new(display, 0x5150L);
        Image image = new Image(display, 499, 277);
        try {
            splash.setBackgroundImage(image);
            splash.setVisible(true);

            assertThat(splash.getBounds()).isEqualTo(new Rectangle(538, 293, 499, 277));
        } finally {
            image.dispose();
        }
    }

    // ---- harness ----------------------------------------------------------------------------------

    private Shell centeredOnMonitor(int width, int height) {
        Shell shell = new Shell(display, SWT.CLOSE | SWT.TITLE | SWT.ON_TOP);
        shell.setSize(width, height);
        Rectangle m = display.getPrimaryMonitor().getBounds();
        shell.setLocation(m.x + (m.width - width) / 2, m.y + (m.height - height) / 2);
        return shell;
    }

    private static Shell centeredOn(Shell parent, int width, int height) {
        Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        shell.setSize(width, height);
        Rectangle p = parent.getBounds();
        shell.setLocation(p.x + (p.width - width) / 2, p.y + (p.height - height) / 2);
        return shell;
    }

    private TestWebBridge install() {
        FlutterBridge.set(new RecordingBridge());
        display = new Display();
        FlutterBridge.set(null);
        DartDisplay dd = (DartDisplay) display.getImpl();
        TestWebBridge bridge = new TestWebBridge(dd);
        dd.setBridge(bridge);
        bridge.start(dd);
        return bridge;
    }

    /** Fired on the Display thread, so the report is applied before this returns. */
    private static void clientReady(RecordingComm comm, int width, int height) {
        ClientReadyPayload p = new ClientReadyPayload();
        p.wireFingerprint = dev.equo.swt.WireTable.FINGERPRINT;
        p.width = width;
        p.height = height;
        p.isFirst = true;
        comm.fireContaining("ClientReady", p);
    }

    /** The client's report of the only shell's bounds, as it arrives on the Display thread. */
    private void clientBounds(RecordingComm comm, int x, int y, int width, int height, int detail) {
        Event e = new Event();
        e.x = x;
        e.y = y;
        e.width = width;
        e.height = height;
        e.detail = detail;
        comm.fireContaining("/Shell/SetBounds", e);
        while (display.readAndDispatch()) {
        }
    }

    /** The web surface's handshake over a recorded comm, with no server or browser behind it. */
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
