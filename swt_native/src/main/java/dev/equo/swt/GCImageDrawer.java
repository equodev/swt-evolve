package dev.equo.swt;

import dev.equo.swt.comm.CommService;
import org.eclipse.swt.graphics.DartImage;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
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
    // wait in DartImage#getImageData(int) which was always bounded. See the MR history for this fix.
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
    // Recorded by initFlutterView, consumed by start().
    private volatile Image dartImage;
    private volatile Consumer<byte[]> onImageResult;
    private boolean started;

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
        this.dartImage = dartImage;
        this.onImageResult = onImageResult;
        // Assigned synchronously, before any caller could possibly observe this drawer instance —
        // sendGcDispose() reads the same field later, with no window where it could be unset.
        // Resolving is free; start() is what puts anything on the comm.
        resolvedComm = resolveSharedComm(dartImage);
    }

    /**
     * Stands the Flutter-side drawer up — ClientReady handshake, {@code GC/create},
     * {@code imageInit}, then the buffered ops. Deferred until something actually needs the drawn
     * pixels back, which is never before the GC is disposed (or a mid-draw snapshot is asked for):
     * doing it at GC creation put a handshake and the image on the Display's shared comm — the
     * channel the rest of the UI runs on — for every {@code new GC(image)}, even one whose drawing
     * is abandoned. Idempotent.
     */
    private synchronized void start() {
        if (started) return;
        started = true;
        long gcId = this.gcId;
        Image dartImage = this.dartImage;
        CommService comm = resolvedComm;
        if (comm == null && !nativeWindowAvailable) {
            cancelAndWake(dartImage);
            return;
        }
        // Serialized synchronously, before endDrawCycle mints this cycle's ref, so it captures the
        // Image as the GC found it. Deferring it would describe the Image by the render it has not
        // produced yet, leaving the render side waiting on its own output as its base.
        byte[] initBytes;
        try {
            initBytes = serializer.to(dartImage);
        } catch (Exception e) {
            System.err.println("[GCImageDrawer] Failed to serialize imageInit: " + e.getMessage());
            initBytes = null;
        }
        final byte[] imageInit = initBytes;
        if (comm != null) {
            // No handshake: frames apply one at a time in arrival order, so the GC/create handler
            // has built the drawer before anything sent after it is applied.
            comm.send("GC/create", ByteBuffer.allocate(8).putLong(gcId).array());
            if (imageInit != null) comm.send("GC/" + gcId + "/imageInit", imageInit);
            flushOps();
            return;
        }
        // An isolated off-screen engine has to boot before it can be addressed at all.
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
                        if (imageInit != null) comm().send("GC/" + gcId + "/imageInit", imageInit);
                    } catch (Exception e) {
                        System.err.println("[GCImageDrawer] Failed to send imageInit: " + e.getMessage());
                    }
                    // Flush buffered GC ops (drawLine, drawRect, etc.) now that Flutter's
                    // GCDrawer.standalone has registered its listeners.
                    flushOps();
                });
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
     * Called from DartGC.destroy() — signals Flutter that GC operations are done, which is what
     * makes it render, and hands over the ref that render will live under. Starts the drawer, since
     * nothing has stood it up before now. Nothing comes back and nothing is waited for: the ref is
     * minted here, so the Image can treat the picture as Flutter-owned the moment this returns, and
     * the pixels are pulled across later only if some caller reads them
     * ({@link org.eclipse.swt.graphics.GCHelper#fetchRemotePixels}).
     *
     * @param needsPixelsForMirror this side has a native {@code SwtImage} mirror that has to be
     *                             filled with real bytes, so ask for the PNG anyway.
     * @return whether pixels are on their way back, i.e. whether the caller must wait for them.
     */
    public boolean endDrawCycle(boolean needsPixelsForMirror) {
        start();
        // Only the shared engine can own the picture: an isolated off-screen engine has its own
        // image cache, which the Display's engine cannot resolve a ref against.
        boolean flutterOwns = resolvedComm != null;
        boolean wantPixels = needsPixelsForMirror || !flutterOwns;
        CommService c = flutterOwns ? resolvedComm : super.comm();
        // Ref 0 means retain nothing there, since nothing here could read or release it.
        long remoteRef = flutterOwns ? org.eclipse.swt.graphics.GCHelper.nextRemoteRef() : 0L;
        if (wantPixels) {
            String resultEvent = "GC/" + gcId + "/imageResult";
            c.on(resultEvent, byte[].class, bytes -> {
                c.remove(resultEvent); // the shared comm outlives this one-shot render
                Consumer<byte[]> sink = onImageResult;
                if (sink != null) sink.accept(bytes);
            });
        }
        byte[] payload = ByteBuffer.allocate(9)
                .putLong(remoteRef)
                .put((byte) (wantPixels ? 1 : 0))
                .array();
        queueOp(() -> c.send("GC/" + gcId + "/gcDispose", payload));
        Image image = dartImage;
        if (image == null || remoteRef == 0) return wantPixels;
        if (image.isDisposed()) {
            // The Image was abandoned mid-construction (its drawer threw), so nothing will ever
            // dispose it and release the ref the render is about to register. Release it here.
            queueOp(() -> c.send("Image/releaseRemoteRef", ByteBuffer.allocate(8).putLong(remoteRef).array()));
        } else if (image.getImpl() instanceof DartImage di) {
            di._adoptRemoteRender(remoteRef, c);
        }
        return wantPixels;
    }

    /**
     * Ends the cycle without adopting a render: ref 0 tells the Flutter side to render nothing and
     * tear itself down. Used when the drawing was abandoned but the drawer was already started.
     */
    public void sendGcDispose() {
        start();
        CommService c = resolvedComm != null ? resolvedComm : super.comm();
        byte[] payload = ByteBuffer.allocate(9).putLong(0L).put((byte) 0).array();
        queueOp(() -> c.send("GC/" + gcId + "/gcDispose", payload));
    }

    /**
     * Starts the drawer because a caller is about to block waiting for Flutter to answer one of the
     * buffered ops (see {@link FlutterBridge#flushOps}). Ops sit in {@code pendingOps} until the
     * Flutter side exists, so without this the caller waits out its whole timeout for a request that
     * never left Java — {@code GC.copyArea(Image, int, int)} is the one such op today.
     */
    public void startForPendingReply() {
        start();
    }

    /**
     * Called from DartGC.destroy() instead of {@link #sendGcDispose()} when the drawing was
     * abandoned (the ImageGcDrawer threw): nothing will read the render, so an unstarted drawer
     * stays unstarted and its buffered ops are dropped. A drawer that was started still has a
     * Flutter-side counterpart, which only gcDispose tears down.
     */
    public synchronized void abandon() {
        if (started) {
            sendGcDispose();
        } else {
            pendingOps.clear();
        }
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
        start();
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