package dev.equo.swt;

import dev.equo.swt.comm.CommService;
import org.eclipse.swt.graphics.DartImage;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Bridge for GC(Image) operations. When Flutter is ready, sends the original image as a VImage
 * on imageInit. Call sendGcDispose() when GC operations are done to trigger rendering. The result
 * PNG is delivered via onImageResult callback.
 *
 * <p>If the Display already has a live, shared Flutter engine ({@link FlutterBridge#resolveDisplayGcComm}),
 * this piggybacks on it via a {@code GC/create} handshake instead of starting a headless view.
 *
 * GC ops (drawLine, drawRect, etc.) arrive from Java before Flutter has registered
 * its listeners.  We queue them here and flush in order after imageInit is sent,
 * guaranteeing they reach the GCDrawer.standalone after _registerOps() ran.
 */
public class GCImageDrawer extends EmbeddedBridge {

    // Bound on the wait for the engine's ClientReady handshake in initFlutterView(). That wait was
    // previously unbounded (a bare .thenRun() on the ClientReady future): if the target engine —
    // shared Display engine or isolated off-screen one — never answers (crashed, dead comm, GLX
    // context lost), the Java caller hung forever with no escape hatch, unlike the render-result
    // wait in DartImage#getImageData(int) which was always bounded. See issue #763 MR history.
    private static final long CLIENT_READY_TIMEOUT_SECONDS = 10;

    private static volatile boolean nativeWindowAvailable = true;

    // Both set from initFlutterView, which can run on a different thread than whatever created
    // this drawer — volatile for cross-thread visibility.
    private volatile long ctx;
    private volatile long gcId;
    // Resolved once here (not re-resolved in sendGcDispose) so gcDispose always targets the same
    // comm that received GC/create/imageInit/ops — a fresh resolve could pick a different comm if
    // Display state changed meanwhile, stranding gcDispose on a channel nothing is listening on.
    private volatile CommService resolvedComm;

    /** Ops buffered until Flutter's GCDrawer listeners are registered. */
    private final List<Runnable> pendingOps = new ArrayList<>();
    private boolean opsReady = false;

    public GCImageDrawer() {
        super(null);
        // Headless web/test mode (-Ddev.equo.swt.loadLibrary=false): do NOT spin up the native off-screen
        // Flutter engine. On the Linux CI container the library is simply absent (initialize() throws), but
        // on a macOS dev machine it IS present and its IOSurface init intermittently hard-aborts the JVM
        // (SIGABRT "_iosConnectInitalize unable to open IOSurface kernel service"), making the web suite
        // flaky. Skip it and degrade gracefully — GC-to-Image pixel readback just won't be produced (those
        // tests are method-blacklisted). Desk mode leaves loadLibrary unset, so it still loads normally.
        if ("false".equals(System.getProperty("dev.equo.swt.loadLibrary"))) {
            nativeWindowAvailable = false;
            return;
        }
        try {
            FlutterLibraryLoader.initialize();
        } catch (Throwable t) {
            // The native Flutter GC library isn't available (e.g. the headless CI container). Degrade
            // gracefully so GC-to-Image operations cancel instead of throwing; results that depend on
            // actual off-screen rendering (pixel readback) simply won't be produced. No effect in desk
            // mode, where initialize() succeeds.
            nativeWindowAvailable = false;
        }
    }

    @Override
    public int hashCode() {
        return (int) gcId;
    }

    @Override
    protected CommService comm() {
        return resolvedComm != null ? resolvedComm : super.comm();
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

    public void initFlutterView(long gcId, Image dartImage, Consumer<byte[]> onImageResult) {
        this.gcId = gcId;
        // Assigned synchronously, before any caller could possibly observe this drawer instance —
        // sendGcDispose() reads the same field later, with no window where it could be unset.
        CommService comm = resolveSharedComm(dartImage);
        resolvedComm = comm;
        if (comm == null && !nativeWindowAvailable) {
            cancelAndWake(dartImage);
            return;
        }
        super.onReady(this, Void.class)
                .orTimeout(CLIENT_READY_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .whenComplete((ignored, err) -> {
                    if (err != null) {
                        System.err.println("[GCImageDrawer] Engine did not answer ClientReady within "
                                + CLIENT_READY_TIMEOUT_SECONDS + "s — off-screen GC will be a no-op: " + err);
                        cancelAndWake(dartImage);
                        return;
                    }
                    try {
                        serializeAndSend("GC/" + gcId + "/imageInit", dartImage);
                    } catch (Exception e) {
                        System.err.println("[GCImageDrawer] Failed to send imageInit: " + e.getMessage());
                    }
                    String resultEvent = "GC/" + gcId + "/imageResult";
                    // Desktop binary path: the rendered PNG arrives as raw bytes via sendBytes — no base64.
                    // Payload is 8-byte remoteRef + PNG; only trust the ref on the shared-engine path.
                    comm().on(resultEvent, byte[].class, bytes -> {
                        comm().remove(resultEvent); // the shared comm outlives this one-shot render
                        long remoteRef = ByteBuffer.wrap(bytes).getLong();
                        byte[] pngBytes = Arrays.copyOfRange(bytes, 8, bytes.length);
                        if (comm != null && dartImage != null && !dartImage.isDisposed()
                                && dartImage.getImpl() instanceof DartImage di) {
                            di._setRemoteRef(remoteRef);
                        }
                        onImageResult.accept(pngBytes);
                    });
                    // Flush buffered GC ops (drawLine, drawRect, etc.) now that Flutter's
                    // GCDrawer.standalone has registered its listeners.
                    flushOps();
                });
        if (comm != null) {
            comm.send("GC/create", ByteBuffer.allocate(8).putLong(gcId).array());
            return;
        }
        try {
            ctx = FlutterNative.initialize(comm().getPort(), 0, gcId, widgetName(this), "", 0, 0, 0, 0);
        } catch (Error e) {
            nativeWindowAvailable = false;
            System.err.println("[GCImageDrawer] Native Flutter window unavailable — off-screen GC will be a no-op: " + e.getMessage());
            cancelAndWake(dartImage);
        }
    }

    /** The Display's shared engine comm if one is live for {@code dartImage}'s Device, or null
     *  if there is none (embed mode, or the Device isn't a Display) — see
     *  {@link FlutterBridge#resolveDisplayGcComm}. */
    private static CommService resolveSharedComm(Image dartImage) {
        if (dartImage == null || dartImage.isDisposed() || !(dartImage.getDevice() instanceof Display display)) {
            return null;
        }
        return FlutterBridge.resolveDisplayGcComm(display);
    }

    private static void cancelAndWake(Image dartImage) {
        if (dartImage != null && dartImage.getImpl() instanceof org.eclipse.swt.graphics.DartImage di) {
            di.cancelRenderFuture();
        }
    }

    /**
     * Called from DartGC.destroy() — signals Flutter that GC operations are done.
     * Queued so it is sent after all other buffered ops have been flushed.
     */
    public void sendGcDispose() {
        CommService c = resolvedComm != null ? resolvedComm : super.comm();
        queueOp(() -> c.send("GC/" + gcId + "/gcDispose"));
    }

    /**
     * Requests a one-off render of the current draw state without tearing down the drawer —
     * unlike {@link #sendGcDispose()}, listeners stay registered so more ops (and a later real
     * dispose, or another snapshot) still work afterward. Used so GC(Image)#getImageData()
     * reflects what's been drawn so far even when the GC hasn't been disposed yet — this backend
     * only paints in response to an explicit signal, unlike real SWT where GC draws are
     * immediately visible in the image.
     */
    public void requestRenderSnapshot(Consumer<byte[]> onSnapshot) {
        CommService c = resolvedComm != null ? resolvedComm : super.comm();
        String snapshotEvent = "GC/" + gcId + "/imageSnapshotResult";
        c.on(snapshotEvent, byte[].class, bytes -> {
            c.remove(snapshotEvent);
            onSnapshot.accept(bytes);
        });
        queueOp(() -> c.send("GC/" + gcId + "/renderSnapshot"));
    }

    public void disposeView() {
        if (ctx == 0) return;
        FlutterNative.dispose(ctx);
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