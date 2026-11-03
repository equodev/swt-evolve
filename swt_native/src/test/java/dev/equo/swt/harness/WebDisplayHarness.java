package dev.equo.swt.harness;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.HeadlessChrome;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.spi.FlutterBridgeSpi;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A web Display on the production path, rendered by headless Chrome: unlike
 * {@link WidgetFlutterHarness}, nothing is replaced. Waiting dispatches the Display.
 */
public final class WebDisplayHarness {

    private static final String Q_REQUEST = "evolve.test.query";
    private static final String Q_RESPONSE = "evolve.test.queryResponse";
    private static final String FRAME_SYNC = "evolve.test.frameSync";
    private static final String FRAME_SYNCED = "evolve.test.frameSynced";
    private static final String FRAME_COST = "evolve.test.frameCost";
    private static final String STYLEDTEXT_PERF = "evolve.test.styledTextPerf";
    private static final String STYLEDTEXT_PERF_RESPONSE = "evolve.test.styledTextPerfResponse";
    private static final String FRAME_COST_RESPONSE = "evolve.test.frameCostResponse";
    private static final long TIMEOUT_MS = Long.getLong("harness.queryTimeoutMs", 10_000);
    private static final int BOOT_ATTEMPTS = Integer.getInteger("harness.bootAttempts", 3);
    private static final long BOOT_ATTEMPT_MS = Long.getLong("harness.bootAttemptMs", 12_000);

    private final AtomicInteger ids = new AtomicInteger();
    private final Map<Integer, CompletableFuture<Map<String, Object>>> pending = new ConcurrentHashMap<>();

    private Display display;
    private CommService comm;
    private HeadlessChrome chrome;
    private DevTools devTools;
    private UserInput input;
    private final Map<String, String> overridden = new HashMap<>();
    /** Upcoming browser launches that load a page which never connects, to exercise the relaunch. */
    private int bootFailuresToInject = Integer.getInteger("harness.web.failBoots", 0);

    void injectBootFailures(int count) {
        bootFailuresToInject = count;
    }

    /** The browser's device pixel ratio, 0 for its own. */
    private double deviceScaleFactor;

    /** Boots the client as on a monitor at {@code factor} (1.5 for 150 %). Call before {@link #boot()}. */
    public WebDisplayHarness deviceScaleFactor(double factor) {
        deviceScaleFactor = factor;
        return this;
    }

    public Display boot() {
        File webDir = WidgetFlutterHarness.resolveWebDir();
        // The browser the build configured (CI's runs Chrome without its sandbox), read before
        // the override below hides it.
        String configured = System.getProperty("harness.chrome", System.getProperty("equo.swt.browser"));
        override("dev.equo.swt.web.dir", webDir.getAbsolutePath());
        // The Display must not open its own browser; this harness launches one it can drive.
        override("equo.swt.browser", "none");
        FlutterBridge.set(null);
        display = new Display();
        comm = FlutterBridge.resolveDisplayGcComm(display);
        if (comm == null) throw new IllegalStateException("the Display did not start a web bridge");
        dev.equo.swt.harness.Channels.on(comm, Q_RESPONSE, byte[].class, bytes -> complete(bytes, "queryId"));
        dev.equo.swt.harness.Channels.on(comm, FRAME_SYNCED, byte[].class, bytes -> complete(bytes, "syncId"));
        dev.equo.swt.harness.Channels.on(comm, FRAME_COST_RESPONSE, byte[].class, bytes -> complete(bytes, "costId"));
        dev.equo.swt.harness.Channels.on(comm, STYLEDTEXT_PERF_RESPONSE, byte[].class, bytes -> complete(bytes, "perfId"));
        String url = FlutterBridgeSpi.getWebServerUrl(display);
        String binary = HeadlessChrome.resolveBinary(configured);
        if (binary == null) throw new IllegalStateException("Chrome/Chromium not found; set -Dequo.swt.browser=<path>");
        boolean headless = Boolean.parseBoolean(System.getProperty("harness.web.headless", "true"));
        boolean console = Boolean.getBoolean("harness.web.console");
        // Headless Chrome intermittently wedges during startup in the CI container and never loads
        // the app; a fresh browser clears it, so each attempt is short and a wedged one is replaced.
        for (int attempt = 1; !awaitClient(binary, url, headless, console); attempt++) {
            if (attempt == BOOT_ATTEMPTS)
                throw new IllegalStateException("the web client did not connect after " + BOOT_ATTEMPTS
                        + " browser launches of " + BOOT_ATTEMPT_MS + "ms each");
        }
        devTools = DevTools.connect(chrome.profileDir(), url);
        input = new UserInput(devTools);
        return display;
    }

    /** Launches a browser on {@code url}; true once its client paints, false after it was closed for not doing so. */
    private boolean awaitClient(String binary, String url, boolean headless, boolean console) {
        String openUrl = url;
        if (bootFailuresToInject > 0) {
            bootFailuresToInject--;
            openUrl = "data:text/html,harness-forced-boot-failure";
        }
        try {
            List<String> args = new ArrayList<>(List.of("--remote-debugging-port=0",
                    "--disable-frame-rate-limit", "--disable-gpu-vsync"));
            if (deviceScaleFactor > 0) args.add("--force-device-scale-factor=" + deviceScaleFactor);
            chrome = HeadlessChrome.launch(binary, openUrl, headless, console,
                    console ? HeadlessChrome.Io.INHERIT : HeadlessChrome.Io.DISCARD, args.toArray(new String[0]));
        } catch (IOException e) {
            throw new IllegalStateException("could not launch Chrome", e);
        }
        long deadline = System.currentTimeMillis() + BOOT_ATTEMPT_MS;
        while (!syncFrame(1_000)) {
            if (System.currentTimeMillis() > deadline) {
                chrome.close();
                chrome = null;
                return false;
            }
        }
        return true;
    }

    public void teardown() {
        if (devTools != null) devTools.close();
        if (display != null && !display.isDisposed()) display.dispose();
        if (chrome != null) chrome.close();
        overridden.forEach((key, previous) -> {
            if (previous == null) System.clearProperty(key);
            else System.setProperty(key, previous);
        });
        overridden.clear();
    }

    /** Sets a JVM-wide property for this harness's lifetime; later harnesses in the JVM read it too. */
    private void override(String key, String value) {
        overridden.putIfAbsent(key, System.getProperty(key));
        System.setProperty(key, value);
    }

    public UserInput input() {
        return input;
    }

    public DevTools devTools() {
        return devTools;
    }

    /** Pushes Java's pending state and waits until the client has painted a frame with it. */
    public void flush() {
        FlutterBridge.update();
        if (!syncFrame(TIMEOUT_MS))
            throw new IllegalStateException("the client painted no frame within " + TIMEOUT_MS + "ms" + lastStall
                    + " (" + input.pageState() + ")");
    }

    /** Runs the Display's pending work; true when there was any. */
    public boolean drain() {
        boolean worked = false;
        while (display.readAndDispatch()) {
            worked = true;
            dispatched++;
        }
        return worked;
    }

    /** Drains and flushes until three rounds in a row run no Display work, or 40 rounds pass. */
    public void settle() {
        int quiet = 0;
        for (int round = 0; round < 40 && quiet < 3; round++) {
            long before = dispatched;
            drain();
            flush();
            drain();
            quiet = dispatched == before ? quiet + 1 : 0;
        }
    }

    /** Runs the Display until {@code condition} holds, or {@code timeoutMs} passes; whether it held. */
    public boolean pumpUntil(java.util.function.BooleanSupplier condition, long timeoutMs) {
        StallTolerantDeadline deadline = new StallTolerantDeadline(timeoutMs);
        while (!condition.getAsBoolean()) {
            if (!deadline.hasTimeLeft()) return false;
            if (!drain()) sleep();
        }
        return true;
    }

    /** Units of Display work run so far, to tell a quiet period. */
    public long dispatched() {
        return dispatched;
    }

    private long dispatched;

    private boolean syncFrame(long timeoutMs) {
        int id = ids.incrementAndGet();
        CompletableFuture<Map<String, Object>> f = new CompletableFuture<>();
        pending.put(id, f);
        dev.equo.swt.harness.Channels.send(comm, FRAME_SYNC, dev.equo.swt.comm.TaggedValues.encode(Map.of("syncId", id), null));
        return await(f, timeoutMs) != null;
    }

    /** The widget's live client state: {@code found}, {@code state} (its V*), {@code render}. */
    public Map<String, Object> queryState(Widget w) {
        int id = ids.incrementAndGet();
        CompletableFuture<Map<String, Object>> f = new CompletableFuture<>();
        pending.put(id, f);
        dev.equo.swt.harness.Channels.send(comm, Q_REQUEST, dev.equo.swt.comm.TaggedValues.encode(Map.of("queryId", id, "targetId", w.hashCode()), null));
        Map<String, Object> response = await(f, TIMEOUT_MS);
        if (response == null) throw new IllegalStateException("no answer to the state query for " + w + lastStall);
        Map<String, Object> named = new java.util.HashMap<>(response);
        named.put("state", WireFrames.named(response.get("state")));
        return named;
    }

    /** The engine's frame cost since the last call, as {@code {build, raster}} in ms. */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> frameCost() {
        int id = ids.incrementAndGet();
        CompletableFuture<Map<String, Object>> f = new CompletableFuture<>();
        pending.put(id, f);
        dev.equo.swt.harness.Channels.send(comm, FRAME_COST, dev.equo.swt.comm.TaggedValues.encode(Map.of("costId", id), null));
        Map<String, Object> response = await(f, TIMEOUT_MS);
        Object frames = response == null ? null : response.get("frames");
        return frames instanceof List ? (List<Map<String, Object>>) frames : List.of();
    }

    /** The counters of {@code window.evolveTest.styledTextPerf()}; reset on read. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> styledTextPerf() {
        int id = ids.incrementAndGet();
        CompletableFuture<Map<String, Object>> f = new CompletableFuture<>();
        pending.put(id, f);
        dev.equo.swt.harness.Channels.send(comm, STYLEDTEXT_PERF, dev.equo.swt.comm.TaggedValues.encode(Map.of("perfId", id), null));
        Map<String, Object> response = await(f, TIMEOUT_MS);
        Object perf = response == null ? null : response.get("perf");
        return perf instanceof Map ? (Map<String, Object>) perf : Map.of();
    }

    /** What {@code w} says it paints, or an empty map. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> renderedFacts(Widget w) {
        Map<String, Object> render = (Map<String, Object>) queryState(w).get("render");
        Object facts = render == null ? null : render.get("facts");
        return facts == null ? Map.of() : (Map<String, Object>) facts;
    }

    /** Where {@code w} is rendered on the page: {x, y, width, height}. */
    @SuppressWarnings("unchecked")
    public double[] renderedRect(Widget w) {
        // The client lays a widget out in the frame after its record lands, so one just made is asked
        // for again until it is drawn.
        StallTolerantDeadline deadline = new StallTolerantDeadline(5_000);
        while (true) {
            Map<String, Object> render = (Map<String, Object>) queryState(w).get("render");
            Map<String, Object> rect = render == null ? null : (Map<String, Object>) render.get("rect");
            if (rect != null) {
                return new double[] {
                        ((Number) rect.get("x")).doubleValue(), ((Number) rect.get("y")).doubleValue(),
                        ((Number) rect.get("width")).doubleValue(), ((Number) rect.get("height")).doubleValue() };
            }
            if (!deadline.hasTimeLeft()) throw new IllegalStateException(w + " is not rendered" + deadline.stallSuffix());
            if (!drain()) sleep();
        }
    }

    private Map<String, Object> await(CompletableFuture<Map<String, Object>> f, long timeoutMs) {
        StallTolerantDeadline deadline = new StallTolerantDeadline(timeoutMs);
        while (!f.isDone() && deadline.hasTimeLeft()) {
            if (!drain()) sleep();
        }
        lastStall = deadline.stallSuffix();
        return f.getNow(null);
    }

    /** {@link StallTolerantDeadline#stallSuffix()} of the last wait, for its failure message. */
    private String lastStall = "";

    /** A reply from the client: tagged values, a map at the top. */
    @SuppressWarnings("unchecked")
    private void complete(byte[] bytes, String idKey) {
        if (bytes == null) return;
        Object value = GcFrames.value(bytes);
        Map<String, Object> message = value instanceof Map ? (Map<String, Object>) value : null;
        if (message != null && message.get("error") != null) {
            System.err.println("[WebDisplayHarness] the client could not answer: " + message.get("error"));
        }
        Object id = message == null ? null : message.get(idKey);
        if (id == null) return;
        CompletableFuture<Map<String, Object>> f = pending.remove(((Number) id).intValue());
        if (f != null) f.complete(message);
    }

    private static void sleep() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
