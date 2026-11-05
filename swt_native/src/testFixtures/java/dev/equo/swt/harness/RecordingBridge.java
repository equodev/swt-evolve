package dev.equo.swt.harness;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.CommService;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DartControl;
import org.eclipse.swt.widgets.DartWidget;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A no-op {@link FlutterBridge} backed by a {@link RecordingComm}. Inject it via
 * {@code FlutterBridge.set(...)} before creating any widget so they route through it (and, on web,
 * {@code Display.init()} skips standing up a real {@code WebFlutterServer}). {@code clientReady} is
 * pre-completed so {@code update()} flushes immediately rather than deferring until a real client
 * connects.
 */
public class RecordingBridge extends FlutterBridge {

    public final RecordingComm comm = new RecordingComm();

    /** The comm handed to the product: {@link #comm} unless a test supplied its own. */
    private final CommService commService;

    /**
     * Every widget enrolled for a flush, in order. A test asserting on what was <em>scheduled</em>
     * reads this; one asserting on what was <em>sent</em> reads {@link RecordingComm#sent}, which
     * only fills once a flush runs. Typed loosely so a test can hand it a {@code getImpl()} result.
     */
    public final List<Object> dirtied = new CopyOnWriteArrayList<>();

    public RecordingBridge() {
        this(null);
    }

    /**
     * Uses {@code comm} instead of the recording one — for a test that needs the real transport, or
     * a comm of its own that answers or intercepts sends. {@link #comm} then records nothing.
     */
    public RecordingBridge(CommService comm) {
        this.commService = comm;
        clientReady.complete(true);
    }

    @Override
    protected CommService comm() {
        return commService != null ? commService : comm;
    }

    @Override
    public void dirty(DartWidget widget) {
        dirtied.add(widget);
        super.dirty(widget);
    }

    @Override
    public void initFlutterView(Composite parent, DartControl control) {
    }

    @Override
    public void destroy(DartWidget control) {
    }
}
