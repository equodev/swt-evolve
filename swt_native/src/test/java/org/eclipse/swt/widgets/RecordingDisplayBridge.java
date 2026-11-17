package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.NoopBridge;
import dev.equo.swt.harness.RecordingComm;

import java.util.function.Function;

/**
 * A Display-level bridge with no client: what it sends is only recorded. It lives in this package
 * because {@link DartDisplay#setBridge} is package-private.
 */
class RecordingDisplayBridge extends DisplayBridge {
    final RecordingComm comm = new RecordingComm();

    RecordingDisplayBridge(DartDisplay display) {
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

    /** Creates a Display on this thread and installs on it the bridge {@code factory} makes. */
    static <B extends RecordingDisplayBridge> B install(Function<DartDisplay, B> factory) {
        FlutterBridge.set(new NoopBridge());
        Display display = new Display();
        FlutterBridge.set(null);
        DartDisplay dd = (DartDisplay) display.getImpl();
        B bridge = factory.apply(dd);
        dd.setBridge(bridge);
        bridge.start(dd);
        return bridge;
    }

    static void drainEventLoop(Display display) {
        while (display.readAndDispatch()) {
            /* drain */
        }
    }
}
