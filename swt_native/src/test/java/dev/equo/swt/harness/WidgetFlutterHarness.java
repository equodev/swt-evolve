package dev.equo.swt.harness;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import org.eclipse.swt.widgets.*;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link FlutterHarness} specialized for driving a real widget tree: it boots the renderer for a
 * root {@link Control} (rather than a bare comm channel), pushes state and waits for painted
 * frames, and reads back live rendered state via the dormant {@code evolve.test.query} channel
 * registered by Flutter's {@code main()} (see {@code flutter-lib/lib/test_harness.dart}).
 *
 * <p>This is the harness for any Java↔Flutter integration test that needs a real widget
 * (tag them {@code @Tag("flutter-it")}); {@link BrowserFlutterHarness} extends it with the
 * Browser-widget specifics. Subclasses that don't need a widget tree — a size-measurement bridge
 * that only exchanges custom request/response messages — should extend {@link FlutterHarness}
 * directly instead.
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * WidgetFlutterHarness flutter = new WidgetFlutterHarness();
 * flutter.init();                 // wire as the global bridge BEFORE creating widgets
 * Shell shell = new Shell(new Display());
 * Button r1 = new Button(new Composite(shell, SWT.NONE), SWT.RADIO);
 * flutter.show(shell);            // boot renderer for the root, await ClientReady
 * flutter.flush();                // push pending state and wait for the painted frame
 * Map<String,Object> s = flutter.queryState(r1);  // live Flutter state of r1
 * flutter.teardown();
 * }</pre>
 */
public class WidgetFlutterHarness extends FlutterHarness {

    private static final String Q_REQUEST = "evolve.test.query";
    private static final String Q_RESPONSE = "evolve.test.queryResponse";
    private static final String FRAME_SYNC = "evolve.test.frameSync";
    private static final String FRAME_SYNCED = "evolve.test.frameSynced";

    private final Gson gson = new Gson();
    private final AtomicInteger queryGen = new AtomicInteger();
    private final AtomicInteger syncGen = new AtomicInteger();
    private final Map<Integer, CompletableFuture<Map<String, Object>>> pendingQueries = new ConcurrentHashMap<>();
    private final Map<Integer, CompletableFuture<Void>> pendingSyncs = new ConcurrentHashMap<>();

    private boolean shown;

    // ---------------- lifecycle ----------------

    /**
     * Wire this harness as the global bridge and register the comm handlers. Call once before any
     * Display/widget is created, paired with {@link #teardown()}.
     */
    public void init() {
        // Injecting the bridge before any Display is created makes every widget route through the
        // harness, and makes the web Display skip standing up its own server + browser (see
        // WebDisplayBridge.initForDisplay) — so we don't double-boot.
        registerBridge();
        Config.forceEquo();
        registerCommHandlers();
    }

    /** Register the generic query + frame-sync handlers. Subclasses may override to add more. */
    protected void registerCommHandlers() {
        // Query-response handler (persistent). The far side replies with the live V*.toJson().
        comm().on(Q_RESPONSE, byte[].class, bytes -> {
            if (bytes == null) return;
            Map<String, Object> resp = parseJson(bytes);
            Integer qid = ((Number) resp.get("queryId")).intValue();
            CompletableFuture<Map<String, Object>> f = pendingQueries.remove(qid);
            if (f != null) f.complete(resp);
        });
        // Post-frame ack: completes the future for the matching syncId once Flutter has built +
        // painted the frame requested by flush() (see test_harness.dart).
        comm().on(FRAME_SYNCED, byte[].class, bytes -> {
            if (bytes == null) return;
            Map<String, Object> m = parseJson(bytes);
            Object sid = m == null ? null : m.get("syncId");
            if (sid == null) return;
            CompletableFuture<Void> f = pendingSyncs.remove(((Number) sid).intValue());
            if (f != null) f.complete(null);
        });
    }

    protected Map<String, Object> parseJson(byte[] bytes) {
        Type t = new TypeToken<Map<String, Object>>() {}.getType();
        return gson.fromJson(new String(bytes, StandardCharsets.UTF_8), t);
    }

    // ---------------- public API ----------------

    /** Boot the renderer for {@code root}, then block until Flutter signals ClientReady. */
    public void show(Control root) {
        if (shown) throw new IllegalStateException("show() already called");
        shown = true;
        DartControl impl = (DartControl) root.getImpl();
        long rootId = id(impl);
        String rootName = widgetName(impl);
        onReady(impl, Void.class); // registers the ClientReady handler + dirties the root
        bootRenderer(rootId, rootName);
        flush();
    }

    /**
     * Push all pending Java-side state to Flutter and block until Flutter has built + painted the
     * resulting frame (a deterministic frame barrier; see test_harness.dart {@code frameSync}).
     */
    public void flush() {
        FlutterBridge.update();
        awaitFrame();
    }

    private void awaitFrame() {
        int sid = syncGen.incrementAndGet();
        CompletableFuture<Void> f = new CompletableFuture<>();
        pendingSyncs.put(sid, f);
        comm().send(FRAME_SYNC, ("{\"syncId\":" + sid + "}").getBytes(StandardCharsets.UTF_8));
        awaitQuiet(f, QUERY_TIMEOUT_MS);
        pendingSyncs.remove(sid);
    }

    /**
     * Returns the live Flutter state (the widget's {@code V*.toJson()}) of {@code w}, by id. The map
     * contains {@code found} plus the serialized fields under {@code state} (e.g. {@code selection}).
     */
    public Map<String, Object> queryState(Widget w) {
        int qid = queryGen.incrementAndGet();
        CompletableFuture<Map<String, Object>> f = new CompletableFuture<>();
        pendingQueries.put(qid, f);
        String json = "{\"queryId\":" + qid + ",\"targetId\":" + w.hashCode() + "}";
        comm().send(Q_REQUEST, json.getBytes(StandardCharsets.UTF_8));
        return awaitFuture(f, QUERY_TIMEOUT_MS, "query for id " + w.hashCode());
    }

    /** Convenience: the rendered selection of a Check/Radio/Toggle Button (false if absent/null). */
    @SuppressWarnings("unchecked")
    public boolean renderedSelection(Button b) {
        Map<String, Object> resp = queryState(b);
        if (!Boolean.TRUE.equals(resp.get("found"))) return false;
        Map<String, Object> state = (Map<String, Object>) resp.get("state");
        Object sel = state == null ? null : state.get("selection");
        return Boolean.TRUE.equals(sel);
    }

    /** Convenience: the rendered enabled state of a Control (false if absent/null — the
     *  serializer's {@code skipDefaultValues} omits booleans equal to the Java default, false). */
    @SuppressWarnings("unchecked")
    public boolean renderedEnabled(Control c) {
        Map<String, Object> resp = queryState(c);
        if (!Boolean.TRUE.equals(resp.get("found"))) return false;
        Map<String, Object> state = (Map<String, Object>) resp.get("state");
        Object enabled = state == null ? null : state.get("enabled");
        return Boolean.TRUE.equals(enabled);
    }
}
