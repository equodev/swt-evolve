package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.RecordingComm;

/** A web Display's bridge whose client is ready at once and whose sends are recorded, not sent. */
final class RecordingWebBridge extends WebDisplayBridge {
    final RecordingComm comm = new RecordingComm();
    final Display display;

    private RecordingWebBridge(Display display) {
        super((DartDisplay) display.getImpl());
        this.display = display;
        clientReady.complete(true);
    }

    /** A new Display whose bridge is a recording one; the test disposes the Display. */
    static RecordingWebBridge install() {
        // The Display is created under a stand-in bridge, then handed the recording one.
        FlutterBridge.set(new RecordingBridge());
        Display display = new Display();
        FlutterBridge.set(null);
        DartDisplay dd = (DartDisplay) display.getImpl();
        RecordingWebBridge bridge = new RecordingWebBridge(display);
        dd.setBridge(bridge);
        bridge.start(dd);
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
