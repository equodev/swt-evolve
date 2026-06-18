package dev.equo.swt;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.eclipse.swt.widgets.Display;

/**
 * Routes synchronous JavaScript-to-Java {@code BrowserFunction} calls for web
 * (iframe) Browsers. When the iframe content is same-origin (served through the
 * proxy), a registered function is exposed in the page as a JS shim that does a
 * <em>blocking</em> XHR to the {@code /equo-browser-function} endpoint of
 * {@code WebFlutterServer}; that handler calls {@link #invoke} to run the Java
 * callback on its SWT {@link Display} thread and returns the result.
 *
 * <p>The registry lives in the {@code main} source set so both the bridge
 * ({@code EvolveBrowser}, which registers) and the {@code webMain} HTTP server
 * (which invokes) can reach it without depending on each other.
 */
public final class BrowserFunctionRegistry {

    private BrowserFunctionRegistry() {
    }

    private static final class Entry {
        final Display display;
        final Function<Object[], Object> fn;

        Entry(Display display, Function<Object[], Object> fn) {
            this.display = display;
            this.fn = fn;
        }
    }

    private static final Map<String, Entry> ENTRIES = new ConcurrentHashMap<>();

    private static String key(long browserId, String name) {
        return browserId + "/" + name;
    }

    /** Registers (or replaces) the callback for {@code name} on the Browser {@code browserId}. */
    public static void register(long browserId, String name, Display display,
            Function<Object[], Object> fn) {
        ENTRIES.put(key(browserId, name), new Entry(display, fn));
    }

    public static void unregister(long browserId, String name) {
        ENTRIES.remove(key(browserId, name));
    }

    public static boolean isRegistered(long browserId, String name) {
        return ENTRIES.containsKey(key(browserId, name));
    }

    /**
     * Invokes the registered callback on its display thread and returns the
     * result (mapped per the SWT type contract by the caller).
     *
     * @throws IllegalStateException if no function is registered under
     *     {@code (browserId, name)}, or the callback threw.
     */
    public static Object invoke(long browserId, String name, Object[] args) {
        Entry e = ENTRIES.get(key(browserId, name));
        if (e == null) {
            throw new IllegalStateException("no browser function: " + name);
        }
        Object[] result = new Object[1];
        Throwable[] error = new Throwable[1];
        Runnable r = () -> {
            try {
                result[0] = e.fn.apply(args);
            } catch (Throwable t) {
                error[0] = t;
            }
        };
        if (e.display == null || e.display.isDisposed()) {
            r.run();
        } else {
            e.display.syncExec(r);
        }
        if (error[0] != null) {
            throw new IllegalStateException(
                    error[0].getMessage() != null ? error[0].getMessage() : error[0].toString(),
                    error[0]);
        }
        return result[0];
    }
}
