package dev.equo.swt;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Bridge that creates a headless Flutter view (parent=0) for GC(Image) operations.
 * When Flutter is ready, sends the original image as a VImage on imageInit.
 * Call sendGcDispose() when GC operations are done to trigger rendering.
 * The result PNG is delivered via onImageResult callback.
 *
 * GC ops (drawLine, drawRect, etc.) arrive from Java before Flutter has registered
 * its listeners.  We queue them here and flush in order after imageInit is sent,
 * guaranteeing they reach the GCDrawer.standalone after _registerOps() ran.
 */
public class GCImageDrawer extends SwtFlutterBridgeBase {

    private long ctx;
    private long gcId;

    /** Ops buffered until Flutter's GCDrawer listeners are registered. */
    private final List<Runnable> pendingOps = new ArrayList<>();
    private boolean opsReady = false;

    public GCImageDrawer() {
        super(null);
    }

    @Override
    public int hashCode() {
        return (int) gcId;
    }

    /**
     * Queue a send op.  If Flutter is already ready the op runs immediately;
     * otherwise it is appended to the pending list and flushed by flushOps().
     */
    public synchronized void queueOp(Runnable op) {
        if (opsReady) {
            op.run();
        } else {
            pendingOps.add(op);
        }
    }

    /**
     * Send all buffered ops in order and mark the drawer as ready so future
     * calls to queueOp() are dispatched immediately.
     * Must be called after imageInit has been sent (from the clientReady thread).
     */
    private synchronized void flushOps() {
        opsReady = true;
        for (Runnable op : pendingOps) {
            op.run();
        }
        pendingOps.clear();
    }

    public void initFlutterView(long gcId, Image dartImage, Consumer<String> onImageResult) {
        this.gcId = gcId;
        super.onReady(this, Void.class).thenRun(() -> {
            try {
                serializeAndSend("GC/" + gcId + "/imageInit", dartImage);
            } catch (Exception e) {
                System.err.println("[GCImageDrawer] Failed to send imageInit: " + e.getMessage());
            }
            client.getComm().on("GC/" + gcId + "/imageResult", String.class, json -> {
                onImageResult.accept(json);
                disposeView();
            });
            // Flush buffered GC ops (drawLine, drawRect, etc.) now that Flutter's
            // GCDrawer.standalone has registered its listeners.
            flushOps();
        });
        ctx = InitializeFlutterWindow(client.getPort(), 0, gcId, widgetName(this), "", 0, 0);
    }

    /**
     * Called from DartGC.destroy() — signals Flutter that GC operations are done.
     * Queued so it is sent after all other buffered ops have been flushed.
     */
    public void sendGcDispose() {
        queueOp(() -> client.getComm().send("GC/" + gcId + "/gcDispose"));
    }

    public void disposeView() {
        Dispose(ctx);
        ctx = 0;
    }

    @Override
    protected long getHandle(Control control) {
        return 0;
    }

    @Override
    protected void setHandle(DartControl control, long view) {
    }

    @Override
    public Object container(DartComposite parent) {
        return null;
    }

    @Override
    public void reparent(DartControl control, Composite newParent) {
    }

    @Override
    public void destroy(DartWidget control) {
        super.destroy(control);
        disposeView();
    }

    @Override
    protected void destroyHandle(DartControl dartControl) {
    }

    @Override
    protected void sendSwtEvolveProperties() {
    }
}