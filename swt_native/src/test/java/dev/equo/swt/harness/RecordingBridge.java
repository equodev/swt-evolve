package dev.equo.swt.harness;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.CommService;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DartControl;
import org.eclipse.swt.widgets.DartWidget;

/**
 * A no-op {@link FlutterBridge} backed by a {@link RecordingComm}. Inject it via
 * {@code FlutterBridge.set(...)} before creating any widget so they route through it (and, on web,
 * {@code Display.init()} skips standing up a real {@code WebFlutterServer}). {@code clientReady} is
 * pre-completed so {@code update()} flushes immediately rather than deferring until a real client
 * connects.
 */
public class RecordingBridge extends FlutterBridge {

    public final RecordingComm comm = new RecordingComm();

    public RecordingBridge() {
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
