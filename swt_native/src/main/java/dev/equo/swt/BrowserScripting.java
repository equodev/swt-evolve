package dev.equo.swt;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.DartWidget;
import org.eclipse.swt.widgets.Display;

/**
 * Shared Browser scripting bridge used by both {@code EvolveBrowser}
 * implementations (the {@code com.equo.chromium.swt} and
 * {@code org.eclipse.swt.browser} mirrors). The two impls differ only in their
 * package and base class; the JS round-trips — {@code evaluate} and
 * {@code BrowserFunction} registration — are identical and live here so they are
 * defined once.
 */
public final class BrowserScripting {

    private BrowserScripting() {
    }

    /** Prefix the Dart side stamps on a reply carrying a JS exception instead of a value. */

    /** How long a synchronous evaluate() waits for the Flutter side before giving up. */
    private static final long TIMEOUT_MS = 5000;

    /**
     * Wraps {@code script} as an immediately-invoked function body, per {@code Browser.evaluate}'s
     * contract. The newlines keep a trailing {@code //} comment off the closing braces.
     */
    static String asFunctionBody(String script) {
        return "(function() {\n" + (script == null ? "" : script) + "\n})();";
    }

    /**
     * Synchronous {@code Browser.evaluate}: ships the script to the Flutter side,
     * pumps the SWT event loop while awaiting the reply (the comm delivers it on
     * the UI thread, so blocking on {@code future.get()} would deadlock), and maps
     * the JSON result to the SWT type contract via {@link EvalJson}.
     */
    public static Object evaluate(DartWidget widget, Display display, String script)
            throws SWTException {
        int reqId = NEXT_REQUEST.incrementAndGet();
        CompletableFuture<Object> future = new CompletableFuture<>();
        PENDING.put(reqId, future);
        // One channel for every reply; each names the request it answers.
        FlutterBridge.onPayload(widget, dev.equo.swt.WireTable.CHANNEL_EVALUATE_RESULT, Object.class, BrowserScripting::completeEvaluate);
        FlutterBridge.send(widget, dev.equo.swt.WireTable.CHANNEL_EVALUATE,
                Java8.map("script", asFunctionBody(script), "reqId", reqId));
        boolean answered = awaitReply(display, future);
        PENDING.remove(reqId);
        if (!answered) throw failed("no reply from the browser within " + TIMEOUT_MS + "ms");
        try {
            return future.get();
        } catch (ExecutionException ex) {
            if (ex.getCause() instanceof SWTException) throw (SWTException) ex.getCause();
            throw failed(String.valueOf(ex.getCause()));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw failed("interrupted while awaiting the result");
        }
    }

    private static final java.util.concurrent.atomic.AtomicInteger NEXT_REQUEST = new java.util.concurrent.atomic.AtomicInteger();

    /** The evaluate() calls waiting for their reply, by request number. */
    private static final Map<Integer, CompletableFuture<Object>> PENDING = new java.util.concurrent.ConcurrentHashMap<>();

    /** A reply, {@code {reqId, value}} or {@code {reqId, error}}: completes the call it names. */
    private static void completeEvaluate(Object reply) {
        if (!(reply instanceof Map)) return;
        Map<?, ?> m = (Map<?, ?>) reply;
        if (!(m.get("reqId") instanceof Number)) return;
        CompletableFuture<Object> future = PENDING.remove(((Number) m.get("reqId")).intValue());
        if (future == null) return;
        if (m.get("error") != null) future.completeExceptionally(failed(String.valueOf(m.get("error"))));
        else future.complete(EvalJson.toSwt(m.get("value")));
    }

    /**
     * Pumps the SWT event loop until {@code future} completes or {@link #TIMEOUT_MS} passes: the
     * comm delivers replies on the UI thread, so blocking on {@code future.get()} would deadlock.
     * Returns whether the reply arrived.
     */
    static boolean awaitReply(Display display, CompletableFuture<?> future) {
        Display d = display != null ? display : Display.getCurrent();
        long end = System.currentTimeMillis() + TIMEOUT_MS;
        while (!future.isDone() && System.currentTimeMillis() < end) {
            if (d == null || !d.readAndDispatch()) {
                try {
                    Thread.sleep(2);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        return future.isDone();
    }

    private static SWTException failed(String detail) {
        return detail == null || detail.isEmpty()
                ? new SWTException(SWT.ERROR_FAILED_EVALUATE)
                : new SWTException(SWT.ERROR_FAILED_EVALUATE, detail);
    }

    /**
     * Records a {@code BrowserFunction} callback in {@link BrowserFunctionRegistry}
     * (keyed by this Browser's id) and marks the widget dirty so the next state
     * snapshot carries the updated {@code VBrowser.functionNames} to Dart, which
     * injects the page-side XHR shim. The callback is passed as a plain
     * {@link Function} so this stays agnostic of which package's
     * {@code BrowserFunction} type is in play.
     */
    public static void registerFunction(DartWidget widget, Display display, String name,
            Function<Object[], Object> fn) {
        long id = FlutterBridge.id(widget);
        Display d = display != null ? display : Display.getCurrent();
        BrowserFunctionRegistry.register(id, name, d, fn);
        markDirty(widget);
    }

    public static void unregisterFunction(DartWidget widget, String name) {
        long id = FlutterBridge.id(widget);
        BrowserFunctionRegistry.unregister(id, name);
        markDirty(widget);
    }

    private static void markDirty(DartWidget widget) {
        FlutterBridge bridge = widget.getBridge();
        if (bridge != null) bridge.dirty(widget);
    }

    /**
     * Wires the desktop-webview {@code BrowserFunction} call path: a native webview has no
     * synchronous JS&lt;-&gt;Dart bridge, so its {@code window[name]()} shim posts {@code
     * {name, args}} through a one-way JS channel instead of calling back directly. Runs the
     * callback fire-and-forget -- unlike {@link #evaluate}, nothing waits on a reply. Safe to
     * call unconditionally: the web backend never sends this payload.
     */
    public static void listenForFunctionCalls(DartWidget widget, Display display) {
        long id = FlutterBridge.id(widget);
        FlutterBridge.onPayload(widget, dev.equo.swt.WireTable.CHANNEL_BROWSER_FUNCTION_CALL, Object.class, call -> {
            if (!(call instanceof Map)) return;
            Map<?, ?> m = (Map<?, ?>) EvalJson.toSwt(call);
            Object nameObj = m.get("name");
            if (!(nameObj instanceof String)) return;
            Object argsObj = m.get("args");
            Object[] args = (argsObj instanceof Object[]) ? (Object[]) argsObj : new Object[0];
            try {
                BrowserFunctionRegistry.invoke(id, (String) nameObj, args);
            } catch (RuntimeException ignored) {
                // No synchronous caller here to surface this to (unlike the web/evaluate path).
            }
        });
    }
}
