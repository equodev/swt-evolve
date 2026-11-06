package dev.equo.swt.harness;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.CommService;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DartControl;
import org.eclipse.swt.widgets.DartWidget;

/**
 * A bridge installed only while a test constructs its {@code Display}, so {@code Display.init()}
 * skips creating a real surface bridge. The test then hands the Display the bridge it drives.
 */
public final class NoopBridge extends FlutterBridge {
    public final RecordingComm comm = new RecordingComm();

    public NoopBridge() {
        clientReady.complete(true);
    }

    @Override
    protected CommService comm() {
        return comm;
    }

    @Override
    public void initFlutterView(Composite parent, DartControl control) {
    }

    @Override
    public void destroy(DartWidget control) {
    }
}
