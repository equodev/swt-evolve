package dev.equo.swt;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * The window events of a window that hosts the whole app (window.open, a popup created, window.close,
 * a frame about to navigate), routed to the Browser widgets that listen for them. A Browser registers
 * unconditionally, since it is usually created before such a window opens; without one nothing fires.
 * The events carry no opener identity, so all but before-browse are broadcast to every handler.
 */
public final class BrowserWindowEvents {

    private BrowserWindowEvents() {
    }

    /** window.open handlers; each returns whether to block the popup (its OpenWindowListener gave no Browser). */
    private static final Map<String, BooleanSupplier> beforePopup = new ConcurrentHashMap<>();

    /** Popup-created handlers, for {@code VisibilityWindowListener}. */
    private static final Map<String, Runnable> afterCreated = new ConcurrentHashMap<>();

    /** window.close handlers, for {@code CloseWindowListener}. */
    private static final Map<String, Runnable> beforeClose = new ConcurrentHashMap<>();

    /** Per-frame navigation handlers, by frame name; the event is the hosting window's own type. */
    private static final Map<String, Consumer<Object>> beforeBrowse = new ConcurrentHashMap<>();

    public static void registerBeforePopup(String key, BooleanSupplier handler) {
        beforePopup.put(key, handler);
    }

    public static void unregisterBeforePopup(String key) {
        beforePopup.remove(key);
    }

    public static void registerAfterCreated(String key, Runnable handler) {
        afterCreated.put(key, handler);
    }

    public static void unregisterAfterCreated(String key) {
        afterCreated.remove(key);
    }

    public static void registerBeforeClose(String key, Runnable handler) {
        beforeClose.put(key, handler);
    }

    public static void unregisterBeforeClose(String key) {
        beforeClose.remove(key);
    }

    /** {@code handler} runs on the hosting window's thread and must decide synchronously whether to cancel. */
    public static void registerBeforeBrowse(String frameName, Consumer<Object> handler) {
        beforeBrowse.put(frameName, handler);
    }

    public static void unregisterBeforeBrowse(String frameName) {
        beforeBrowse.remove(frameName);
    }

    /** Runs every window.open handler; true if any asks to block the popup. */
    public static boolean firePopup() {
        boolean block = false;
        for (BooleanSupplier h : beforePopup.values()) {
            try {
                if (h.getAsBoolean()) block = true;
            } catch (Throwable ignored) {
                // the handler's Browser or Display is gone
            }
        }
        return block;
    }

    public static void fireCreated() {
        broadcast(afterCreated);
    }

    public static void fireClosed() {
        broadcast(beforeClose);
    }

    public static void fireBeforeBrowse(String frameName, Object event) {
        Consumer<Object> handler = beforeBrowse.get(frameName);
        if (handler != null) handler.accept(event);
    }

    /**
     * Runs every handler, isolating failures: a handler whose Browser or Display is being torn down
     * may throw, and must not stop the rest.
     */
    private static void broadcast(Map<String, Runnable> handlers) {
        for (Runnable r : handlers.values()) {
            try {
                r.run();
            } catch (Throwable ignored) {
                // the handler's Browser or Display is gone
            }
        }
    }
}
