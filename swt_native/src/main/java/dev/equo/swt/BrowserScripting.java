package dev.equo.swt;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.DartWidget;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

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

    /**
     * Synchronous {@code Browser.evaluate}: ships the script to the Flutter side,
     * pumps the SWT event loop while awaiting the reply (the comm delivers it on
     * the UI thread, so blocking on {@code future.get()} would deadlock), and maps
     * the JSON result to the SWT type contract via {@link EvalJson}.
     */
    public static Object evaluate(DartWidget widget, Display display, String script)
            throws SWTException {
        String reqId = UUID.randomUUID().toString();
        CompletableFuture<Object> future = new CompletableFuture<>();
        FlutterBridge.onPayload(widget, "evaluate/" + reqId, Event.class, result -> {
            String text = result != null ? result.text : null;
            if (text != null && text.startsWith("__error__:")) {
                future.completeExceptionally(new SWTException(SWT.ERROR_FAILED_EVALUATE));
            } else {
                future.complete(EvalJson.parse(text));
            }
        });
        FlutterBridge.send(widget, "evaluate", Map.of("script", script, "reqId", reqId));
        Display d = display != null ? display : Display.getCurrent();
        long end = System.currentTimeMillis() + 5000;
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
        if (!future.isDone()) throw new SWTException(SWT.ERROR_FAILED_EVALUATE);
        try {
            return future.get();
        } catch (ExecutionException ex) {
            if (ex.getCause() instanceof SWTException) throw (SWTException) ex.getCause();
            throw new SWTException(SWT.ERROR_FAILED_EVALUATE);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new SWTException(SWT.ERROR_FAILED_EVALUATE);
        }
    }

    /**
     * Records a {@code BrowserFunction} callback in {@link BrowserFunctionRegistry}
     * (keyed by this Browser's id) and asks the Dart side to inject the page-side
     * XHR shim. The callback is passed as a plain {@link Function} so this stays
     * agnostic of which package's {@code BrowserFunction} type is in play.
     */
    public static void registerFunction(DartWidget widget, Display display, String name,
            Function<Object[], Object> fn) {
        long id = FlutterBridge.id(widget);
        Display d = display != null ? display : Display.getCurrent();
        BrowserFunctionRegistry.register(id, name, d, fn);
        FlutterBridge.send(widget, "registerFunction", Map.of("name", name));
    }

    public static void unregisterFunction(DartWidget widget, String name) {
        long id = FlutterBridge.id(widget);
        BrowserFunctionRegistry.unregister(id, name);
        FlutterBridge.send(widget, "unregisterFunction", Map.of("name", name));
    }
}
