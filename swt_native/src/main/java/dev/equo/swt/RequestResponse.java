package dev.equo.swt;

import org.eclipse.swt.graphics.DartGC;
import org.eclipse.swt.graphics.DartResource;
import org.eclipse.swt.widgets.DartWidget;
import org.eclipse.swt.widgets.Display;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Abstracts the Flutter request/response pattern over the
 * {@code widget/id/operation} topic convention.
 *
 * <p>The response topic is derived automatically by appending {@code "Response"}
 * to the {@code eventName} (e.g. {@code "GetItemBounds"} → {@code "GetItemBoundsResponse"}).
 *
 * <p>Two strategies are provided:
 * <ul>
 *   <li>{@link #call} — blocks via {@code CompletableFuture.get}; suitable for
 *       calls made off the SWT UI thread or with short timeouts.</li>
 *   <li>{@link #callOnDisplay} — blocks via the SWT event loop
 *       ({@code readAndDispatch} + 500 ms sleep); safe to call from the SWT UI
 *       thread without freezing it.</li>
 * </ul>
 */
public class RequestResponse {

    private static final String RESPONSE_SUFFIX = "Response";

    /**
     * Sends {@code eventName} to Flutter and blocks until a response arrives on
     * {@code eventName + "Response"}. The JSON payload is deserialized to {@code cls}.
     * Returns {@code fallback} on timeout or error.
     */
    public static <T> T call(Object widget, String eventName, Object args,
                              Class<T> cls, long timeoutMs, T fallback) {
        String receiveEvent = eventName + RESPONSE_SUFFIX;
        var future = new CompletableFuture<T>();
        FlutterBridge.onPayload(widget, receiveEvent, cls,
                p -> future.complete(p != null ? p : fallback));
        send(widget, eventName, args);
        try {
            return future.get(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            return fallback;
        } finally {
            FlutterBridge.removeEvent(widget, receiveEvent);
        }
    }

    /**
     * Sends {@code eventName} to Flutter and blocks using the SWT event loop
     * until the response arrives on {@code eventName + "Response"} or
     * {@code timeoutMs} elapses. The payload is deserialized to {@code cls}
     * and passed to {@code handler}. The {@link Display} is obtained automatically
     * from the widget.
     */
    public static <T> void callOnDisplay(Object widget, String eventName, Object args,
                                         Class<T> cls, Consumer<T> handler, long timeoutMs) {
        Display display = getDisplay(widget);
        String receiveEvent = eventName + RESPONSE_SUFFIX;
        var future = new CompletableFuture<Void>();
        FlutterBridge.onPayload(widget, receiveEvent, cls, p -> {
            FlutterBridge.removeEvent(widget, receiveEvent);
            handler.accept(p);
            future.complete(null);
            if (display != null && !display.isDisposed()) display.wake();
        });
        send(widget, eventName, args);
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (!future.isDone() && System.currentTimeMillis() < deadline) {
            if (display != null && !display.isDisposed() && !display.readAndDispatch()) {
                display.sleep();
            }
        }
        if (!future.isDone()) FlutterBridge.removeEvent(widget, receiveEvent);
    }

    private static Display getDisplay(Object widget) {
        if (widget instanceof DartWidget w) return w.getDisplay();
        if (widget instanceof DartGC gc) return gc.getDisplay();
        return null;
    }

    private static void send(Object widget, String event, Object args) {
        if (widget instanceof DartWidget w) FlutterBridge.send(w, event, args);
        else if (widget instanceof DartResource r) FlutterBridge.send(r, event, args);
    }
}