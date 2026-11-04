package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * {@code Display.getTouchEnabled} answers what the client reported about the host's pointer devices,
 * which is the meaning its javadoc fixes: a touch-aware input device is attached and enabled.
 */
@Tag("flutter-it")
class DisplayTouchDeviceFlutterTest {

    private Display display;

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    void withNoClientConnectedNoTouchDeviceIsKnown() {
        install();
        assertThat(display.getTouchEnabled()).isFalse();
    }

    @Test
    void aClientReportingATouchDeviceMakesTheDisplayReportOne() {
        TestWebBridge web = install();

        clientReady(web.comm, true, true);
        assertThat(display.getTouchEnabled()).isTrue();

        // A client that loses its touch device (or a different one reconnecting) is believed too.
        clientReady(web.comm, false, false);
        assertThat(display.getTouchEnabled()).isFalse();
    }

    @Test
    void aControlSeesTheTouchDeviceTheClientReported() {
        TestWebBridge web = install();
        Shell shell = new Shell(display);
        Control control = new Composite(shell, SWT.NONE);
        control.setTouchEnabled(true);
        // Without a touch device the control cannot be touch-enabled whatever it was asked for.
        assertThat(control.getTouchEnabled()).isFalse();

        clientReady(web.comm, true, true);

        assertThat(control.getTouchEnabled()).isTrue();
    }

    private TestWebBridge install() {
        FlutterBridge.set(new RecordingBridge());
        display = new Display();
        FlutterBridge.set(null);
        DartDisplay impl = (DartDisplay) display.getImpl();
        TestWebBridge bridge = new TestWebBridge(impl);
        impl.setBridge(bridge);
        bridge.start(impl);
        return bridge;
    }

    /** Fired on the Display thread, so the report is applied before this returns. */
    private static void clientReady(RecordingComm comm, boolean touchEnabled, boolean isFirst) {
        ClientReadyPayload p = new ClientReadyPayload();
        p.width = 1280;
        p.height = 720;
        p.isFirst = isFirst;
        p.touchEnabled = touchEnabled;
        p.wireFingerprint = dev.equo.swt.WireTable.FINGERPRINT;
        comm.fireContaining("ClientReady", p);
    }
}
