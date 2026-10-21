package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * On the web the Display is created before any browser has connected, and an application places its
 * first window from the Display's and the primary Monitor's geometry right away. Those must describe
 * the viewport the client reports, not the default the Display starts with.
 *
 * <p>The client's ClientReady arrives on another thread here, as it does on the comm thread in
 * production, while the test thread is the Display thread.
 */
@Tag("flutter-it")
class DisplayClientGeometryFlutterTest {

    private static final Rectangle VIEWPORT = new Rectangle(0, 0, 1600, 900);

    private Display display;
    private Thread client;

    @AfterEach
    void tearDown() throws InterruptedException {
        if (client != null) client.join(5_000);
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    void primaryMonitorQueriedBeforeTheClientConnectsReportsItsViewport() {
        TestWebBridge web = install(10_000);
        clientConnectsAfter(web.comm, 300);

        Rectangle monitor = display.getPrimaryMonitor().getBounds();

        assertThat(monitor).isEqualTo(VIEWPORT);
        assertThat(display.getBounds()).isEqualTo(VIEWPORT);
        assertThat(display.getClientArea()).isEqualTo(VIEWPORT);
        assertThat(display.getMonitors()[0].getBounds()).isEqualTo(VIEWPORT);
    }

    @Test
    void aDialogCenteredAtStartupIsCenteredOnTheViewport() {
        TestWebBridge web = install(10_000);
        clientConnectsAfter(web.comm, 300);

        Shell login = new Shell(display, org.eclipse.swt.SWT.CLOSE | org.eclipse.swt.SWT.TITLE | org.eclipse.swt.SWT.RESIZE | org.eclipse.swt.SWT.ON_TOP);
        login.setSize(850, 800);
        Rectangle m = display.getPrimaryMonitor().getBounds();
        login.setLocation(m.x + (m.width - 850) / 2, m.y + (m.height - 800) / 2);

        assertThat(login.getLocation()).isEqualTo(new org.eclipse.swt.graphics.Point(375, 50));
    }

    @Test
    void withoutAClientTheDefaultIsReportedOnceTheWaitRunsOut() {
        install(200);

        long start = System.nanoTime();
        Rectangle first = display.getBounds();
        long firstMs = (System.nanoTime() - start) / 1_000_000;
        start = System.nanoTime();
        display.getBounds();
        long secondMs = (System.nanoTime() - start) / 1_000_000;

        assertThat(first).as("the Display's default").isEqualTo(new Rectangle(0, 0, 1920, 1080));
        assertThat(firstMs).as("the first query waited for the client").isGreaterThanOrEqualTo(150);
        assertThat(secondMs).as("later queries do not wait again").isLessThan(150);
    }

    @Test
    void aQueryOffTheDisplayThreadDoesNotWait() throws Exception {
        install(10_000);
        AtomicReference<Rectangle> seen = new AtomicReference<>();

        Thread other = new Thread(() -> seen.set(((DartDisplay) display.getImpl()).getBounds()));
        other.start();
        other.join(5_000);

        assertThat(seen.get()).isEqualTo(new Rectangle(0, 0, 1920, 1080));
    }

    @Test
    void aClientAlreadyConnectedIsAppliedWithoutWaiting() {
        TestWebBridge web = install(10_000);
        clientReady(web.comm);

        long start = System.nanoTime();
        Rectangle bounds = display.getBounds();

        assertThat(bounds).isEqualTo(VIEWPORT);
        assertThat((System.nanoTime() - start) / 1_000_000).isLessThan(1_000);
    }

    @Test
    void aWindowThisThreadPumpsDoesNotHoldIt() {
        install(10_000, true);

        long start = System.nanoTime();
        Rectangle bounds = display.getBounds();

        assertThat(bounds).as("the default, without waiting").isEqualTo(new Rectangle(0, 0, 1920, 1080));
        assertThat((System.nanoTime() - start) / 1_000_000).isLessThan(1_000);
    }

    // ---- harness ----------------------------------------------------------------------------------

    private TestWebBridge install(long waitMs) {
        return install(waitMs, false);
    }

    private TestWebBridge install(long waitMs, boolean pumped) {
        FlutterBridge.set(new RecordingBridge());
        display = new Display();
        FlutterBridge.set(null);
        DartDisplay dd = (DartDisplay) display.getImpl();
        TestWebBridge bridge = new TestWebBridge(dd, waitMs, pumped);
        dd.setBridge(bridge);
        bridge.start(dd);
        return bridge;
    }

    private void clientConnectsAfter(RecordingComm comm, long delayMs) {
        client = new Thread(() -> {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                return;
            }
            clientReady(comm);
        }, "client");
        client.start();
    }

    private static void clientReady(RecordingComm comm) {
        ClientReadyPayload p = new ClientReadyPayload();
        p.width = VIEWPORT.width;
        p.height = VIEWPORT.height;
        p.isFirst = true;
        comm.fireContaining("ClientReady", p);
    }

    /** The web surface's handshake over a recorded comm, with no server or browser behind it. */
    private static final class TestWebBridge extends WebDisplayBridge {
        final RecordingComm comm = new RecordingComm();
        private final long waitMs;
        private final boolean pumped;

        TestWebBridge(DartDisplay display, long waitMs, boolean pumped) {
            super(display);
            this.waitMs = waitMs;
            this.pumped = pumped;
        }

        @Override
        protected CommService comm() {
            return comm;
        }

        @Override
        protected void start(DartDisplay display) {
            registerDisplayClientReady(display);
            armClientGeometryWait();
        }

        @Override
        public boolean needsPump() {
            return pumped;
        }

        @Override
        protected long clientGeometryWaitMs() {
            return waitMs;
        }
    }
}
