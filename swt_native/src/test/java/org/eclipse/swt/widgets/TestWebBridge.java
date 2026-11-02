package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.NoopBridge;
import dev.equo.swt.harness.RecordingComm;

/**
 * A web Display bridge whose client is a {@link RecordingComm}: the test fires what the client would
 * send and reads what Java sent. It starts with only the ClientReady channel wired.
 */
class TestWebBridge extends WebDisplayBridge {
    final RecordingComm comm = new RecordingComm();

    /** The Display this bridge serves, when {@link #newDisplay()} made it. */
    Display display;

    TestWebBridge(DartDisplay display) {
        super(display);
        // Stands in for the client having connected: update() defers every pending send until then.
        clientReady.complete(true);
    }

    /** A new Display whose bridge is a started TestWebBridge. */
    static TestWebBridge newDisplay() {
        FlutterBridge.set(new NoopBridge());
        Display display = new Display();
        FlutterBridge.set(null);
        DartDisplay dartDisplay = (DartDisplay) display.getImpl();
        TestWebBridge bridge = new TestWebBridge(dartDisplay);
        dartDisplay.setBridge(bridge);
        bridge.start(dartDisplay);
        bridge.display = display;
        return bridge;
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
