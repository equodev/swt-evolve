package dev.equo.swt.bench;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.FlutterLibraryLoader;
import dev.equo.swt.WebFlutterServer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Bench bridge that boots a headless Flutter window registered as widgetName "BenchBridge".
 *
 * <p>JSON-codec RTT echo in both directions: the receiver runs the real production codec
 * (V*.fromJson/toJson on J→D, Event/base64-image decode on D→J) before acking, so each timing
 * is a true cross-language round trip.
 *
 * <p>All handlers are registered ONCE at startup. Per-call state lives in atomic-reference
 * completer slots that the persistent handler completes when a response arrives. This avoids
 * any register/remove race between iterations — at high iteration counts the previous
 * "register, send, await, remove" pattern occasionally raced with WS-thread callbacks and hung.
 */
public class BenchBridge extends dev.equo.swt.harness.FlutterHarness implements BeforeAllCallback, AfterAllCallback {

    /**
     * Bench uses the JVM-wide desktop comm (one Flutter engine per JVM), not a per-instance comm —
     * preserving the original behavior now that {@link dev.equo.swt.harness.FlutterHarness} owns a
     * private comm by default.
     */
    @Override
    protected boolean ownComm() {
        return false;
    }

    // J→D
    static final String C_J2D_JSON_ECHO        = "bench/j2d/json/echo";
    static final String C_J2D_JSON_ECHO_RESP   = "bench/j2d/json/echoResponse";

    // D→J
    static final String C_D2J_JSON_RUN         = "bench/d2j/json/run";
    static final String C_D2J_JSON_PROBE       = "bench/d2j/json/probe";
    static final String C_D2J_JSON_PROBE_RESP  = "bench/d2j/json/probeResponse";
    static final String C_D2J_JSON_RESULT      = "bench/d2j/json/result";

    /** -Dbench.client=web drives a browser (web comm) instead of the native Flutter engine. */
    private static final boolean WEB = "web".equals(System.getProperty("bench.client"));

    private long ctx;
    private WebFlutterServer webServer;
    private Process browserProc;
    private final AtomicInteger seqGen = new AtomicInteger();

    // Persistent completer slots — one per direction echo path. The WS-thread response handler
    // does getAndSet(null) and completes whatever's pending. Per-call code does
    // compareAndSet(null, fresh) — if it fails, an unfinished prior call is still in flight.
    private final AtomicReference<CompletableFuture<Long>> j2dJsonPending = new AtomicReference<>();
    private final AtomicReference<PendingD2j> d2jJsonPending = new AtomicReference<>();

    // Selects how the D→J JSON probe handler decodes the in-flight payload: true → base64 image
    // String, false → org.eclipse.swt.widgets.Event. Set by d2jJsonEcho before each run is sent;
    // safe because D→J echo is strictly single-in-flight.
    private volatile boolean d2jJsonProbeIsImage = false;

    private static boolean isImageShape(String shape) {
        return shape != null && shape.startsWith("IMG");
    }

    public BenchBridge() {
        super();
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        if (!WEB) FlutterLibraryLoader.initialize();
        FlutterBridge.set(this);
        initFlutterView();
        // Touch BenchPayloads — its static initializer flips to MockFlutterBridge while it
        // builds real widget trees, so widget setters' dirty() calls don't leak through the
        // live socket. Re-assert the bench bridge as the default once construction is done.
        BenchPayloads.SMALL.toString();
        FlutterBridge.set(this);
        Config.defaultToEquo();
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        if (browserProc != null) browserProc.destroy();
        if (webServer != null) webServer.stop();
        if (ctx != 0) dev.equo.swt.FlutterNative.dispose(ctx);
        FlutterBridge.set(null);
        Config.defaultToEclipse();
    }

    // In web mode there is no native engine to pump; comm messages arrive on the
    // java-websocket thread, so the await loop just yields briefly.
    static void pump(int maxMessages) {
        if (WEB) {
            try { Thread.sleep(1); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        } else {
            dev.equo.swt.FlutterNative.pumpMessages(maxMessages);
        }
    }

    private void initFlutterView() {
        CompletableFuture<Point> windowReadyFuture = super.onReady(this, Point.class);
        windowReadyFuture.thenAccept(p -> System.out.println("[BenchBridge] Flutter window ready: " + p));
        if (WEB) {
            startWebClient();
        } else {
            ctx = dev.equo.swt.FlutterNative.initialize(comm().getPort(), 0, id(this), widgetName(this), "", 0, 0, 0, 0);
        }

        // ---------- J→D echo response handler (persistent) ----------
        comm().on(eventName(this, C_J2D_JSON_ECHO_RESP), byte[].class, bytes -> {
            CompletableFuture<Long> p = j2dJsonPending.getAndSet(null);
            if (p != null) p.complete(System.nanoTime());
        });

        // ---------- D→J probe handler (persistent) ----------
        // Decode the incoming payload via the real production receive path, then ack.
        //   - event shapes → org.eclipse.swt.widgets.Event (the path FlutterBridge.on(...) uses)
        //   - image shapes → a base64 PNG String, then base64-decode it (the imageResult path
        //     GCImageDrawer/GCHelper.updateImageFromPng uses).
        // The expected decode type for the in-flight run is set by d2jJsonEcho before the run is sent.
        comm().on(eventName(this, C_D2J_JSON_PROBE), byte[].class, bytes -> {
            if (bytes != null) {
                try {
                    if (d2jJsonProbeIsImage) {
                        String b64 = serializer.from(String.class, bytes);
                        if (b64 != null) java.util.Base64.getDecoder().decode(b64);
                    } else {
                        serializer.from(Event.class, bytes);
                    }
                } catch (Exception e) {
                    System.err.println("[BenchBridge] D→J decode failed: " + e);
                }
            }
            comm().send(eventName(this, C_D2J_JSON_PROBE_RESP), EMPTY_ACK);
        });

        // ---------- D→J result handler (persistent) ----------
        comm().on(eventName(this, C_D2J_JSON_RESULT), byte[].class, bytes -> handleD2jResult(d2jJsonPending, bytes));
    }

    private void startWebClient() {
        File webDir = resolveWebDir();
        // Reuse the production WebFlutterServer to serve the web build (placeholder substitution +
        // COOP/COEP/CORS headers). browserCommand("none") suppresses its own launch — we open
        // Chrome ourselves with background-timer throttling disabled (see launchBenchBrowser).
        webServer = new WebFlutterServer.Builder()
                .webDirectory(webDir)
                .commPort(comm().getPort())
                .widgetId(id(this))
                .widgetName(widgetName(this))
                .browserCommand("none")
                .build();
        try {
            int httpPort = webServer.start();
            String url = "http://localhost:" + httpPort + "/";
            System.out.println("[BenchBridge] web bench at " + url
                    + " serving " + webDir + " (comm port " + comm().getPort() + ")");
            launchBenchBrowser(url);
        } catch (IOException e) {
            throw new RuntimeException("Failed to start web bench client", e);
        }
    }

    /**
     * Launches Chrome with background-timer throttling DISABLED — critical for accurate bench
     * timings (a backgrounded tab clamps timers to ~1/sec, inflating every RTT ~1000x), which is
     * why the bench can't reuse WebFlutterServer's default-browser launch. Falls back to the OS
     * default browser only if Chrome can't be found (with a loud warning, since that path may
     * throttle). {@code -Dbench.web.headless=true} runs Chrome headless; {@code -Dequo.swt.browser}
     * overrides the Chrome path or, set to {@code none}, disables launch.
     */
    private void launchBenchBrowser(String url) throws IOException {
        String override = System.getProperty("equo.swt.browser");
        if ("none".equalsIgnoreCase(override)) {
            System.out.println("[BenchBridge] browser launch disabled — open: " + url);
            return;
        }
        String chrome = chromeBinary(override);
        if (chrome != null) {
            boolean headless = Boolean.getBoolean("bench.web.headless");
            List<String> cmd = new ArrayList<>();
            cmd.add(chrome);
            if (headless) cmd.add("--headless=new");
            cmd.add("--disable-background-timer-throttling");
            cmd.add("--disable-renderer-backgrounding");
            cmd.add("--disable-backgrounding-occluded-windows");
            cmd.add("--no-first-run");
            cmd.add("--no-default-browser-check");
            cmd.add("--user-data-dir=" + System.getProperty("java.io.tmpdir") + "equo-webbench-profile");
            cmd.add(url);
            browserProc = new ProcessBuilder(cmd).inheritIO().start();
            System.out.println("[BenchBridge] launched Chrome (throttling disabled, headless="
                    + headless + ") at " + url);
            return;
        }
        String os = System.getProperty("os.name", "").toLowerCase();
        ProcessBuilder pb = os.contains("mac") ? new ProcessBuilder("open", url)
                : os.contains("win") ? new ProcessBuilder("cmd", "/c", "start", "", url)
                : new ProcessBuilder("xdg-open", url);
        browserProc = pb.inheritIO().start();
        System.out.println("[BenchBridge] WARNING: Chrome not found; launched default browser at "
                + url + " — background-tab throttling may inflate results. Set -Dequo.swt.browser=<chrome path>.");
    }

    // chromeBinary() and resolveWebDir() are inherited from FlutterHarness.

    private void handleD2jResult(AtomicReference<PendingD2j> slot, byte[] bytes) {
        if (bytes == null || bytes.length < 12) return;
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        int respSeq = buf.getInt();
        long rttMicros = buf.getLong();
        PendingD2j p = slot.get();
        if (p != null && p.seq == respSeq) {
            if (slot.compareAndSet(p, null)) {
                EchoResult r = new EchoResult();
                r.seq = respSeq;
                r.rttMicros = rttMicros;
                p.future.complete(r);
            }
        }
    }

    // ====== J→D Echo (single round-trip RTT) ======

    public CompletableFuture<Long> j2dJsonEcho(BenchPayloads.Shape shape) {
        CompletableFuture<Long> result = new CompletableFuture<>();
        if (!j2dJsonPending.compareAndSet(null, result)) {
            CompletableFuture<Long> failed = new CompletableFuture<>();
            failed.completeExceptionally(new IllegalStateException(
                    "J→D echo already in flight — prior call did not complete"));
            return failed;
        }
        byte[] payload = shape.serialize();
        clientReady.thenRun(() -> comm().send(eventName(this, C_J2D_JSON_ECHO), payload));
        return result;
    }

    // ====== D→J Echo (Java drives, Dart times) ======

    public CompletableFuture<EchoResult> d2jJsonEcho(String shape) {
        d2jJsonProbeIsImage = isImageShape(shape);
        int seq = seqGen.incrementAndGet();
        CompletableFuture<EchoResult> result = new CompletableFuture<>();
        PendingD2j pending = new PendingD2j(seq, result);
        if (!d2jJsonPending.compareAndSet(null, pending)) {
            result.completeExceptionally(new IllegalStateException("D→J echo already in flight"));
            return result;
        }
        byte[] shapeBytes = shape.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        ByteBuffer buf = ByteBuffer.allocate(4 + 1 + shapeBytes.length);
        buf.putInt(seq);
        buf.put((byte) shapeBytes.length);
        buf.put(shapeBytes);
        clientReady.thenRun(() -> comm().send(eventName(this, C_D2J_JSON_RUN), buf.array()));
        return result;
    }

    // ====== helpers ======

    private static final byte[] EMPTY_ACK = new byte[]{0, 0, 0, 0};

    public static class EchoResult {
        public int seq;
        public long rttMicros;
    }

    private static final class PendingD2j {
        final int seq;
        final CompletableFuture<EchoResult> future;
        PendingD2j(int seq, CompletableFuture<EchoResult> future) {
            this.seq = seq; this.future = future;
        }
    }

    // The unused EmbeddedBridge overrides (getHandle/setHandle/destroy/destroyHandle/
    // container/reparent/sendSwtEvolveProperties) are inherited from FlutterHarness.
}
