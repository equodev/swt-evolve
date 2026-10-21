package org.eclipse.swt.widgets;

import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.RecordingComm;

/**
 * A web Display bridge whose client is a {@link RecordingComm}: the test fires what the client would
 * send and reads what Java sent. It starts with only the ClientReady channel wired.
 */
class TestWebBridge extends WebDisplayBridge {
    final RecordingComm comm = new RecordingComm();

    TestWebBridge(DartDisplay display) {
        super(display);
        // Stands in for the client having connected: update() defers every pending send until then.
        clientReady.complete(true);
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
