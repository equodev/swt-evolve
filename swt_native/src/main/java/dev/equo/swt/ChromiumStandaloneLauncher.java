package dev.equo.swt;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import com.equo.chromium.ChromiumBrowser;
import com.equo.chromium.ChromiumBrowserStandalone;
import com.equo.chromium.events.BeforeBrowseEvent;
import com.equo.chromium.utils.ISubscriber;

/**
 * Launches and manages an Equo Chromium standalone window to host the Flutter web app.
 * <p>
 * Activation: set the system property {@code dev.equo.swt.mode=chromium}.
 */
public class ChromiumStandaloneLauncher {

    /**
     * The active launcher for the current process. There is at most one standalone window hosting
     * the Flutter app, so child Browser widgets (in the {@code main} source set, which cannot see
     * the web-only bridge) reach it through this accessor. {@code null} when not in Chromium mode.
     */
    private static volatile ChromiumStandaloneLauncher current;

    private ChromiumBrowserStandalone browser;
    private ISubscriber sub;

    /**
     * Per-iframe {@code onBeforeBrowse} handlers, keyed by CEF frame name ({@code equo-browser-<id>}),
     * routing the window-level nav hook to the right Browser (cancellable LocationListener.changing).
     */
    private final Map<String, Consumer<BeforeBrowseEvent>> beforeBrowse = new ConcurrentHashMap<>();

    /**
     * CEF {@code onBeforePopup} (window.open) handlers, broadcast since CEF exposes no opener-frame
     * identity. Each returns whether to BLOCK the popup — true when its OpenWindowListener provided no
     * Browser to host it (the SWT veto, cf. LocationListener.changing's doit) → {@code prevent()}.
     * A non-chromium type, so callers register unconditionally; static to survive a Browser created
     * before the launcher opens. Same applies to {@link #afterCreated}/{@link #beforeClose}.
     */
    private static final Map<String, BooleanSupplier> beforePopup = new ConcurrentHashMap<>();

    /** CEF {@code onAfterCreated} handlers (popup window created) → {@code VisibilityWindowListener}. */
    private static final Map<String, Runnable> afterCreated = new ConcurrentHashMap<>();

    /** CEF {@code onBeforeClose} handlers (popup window.close) → {@code CloseWindowListener}. */
    private static final Map<String, Runnable> beforeClose = new ConcurrentHashMap<>();

    private boolean isMac = System.getProperty("os.name").startsWith("Mac");

    /**
     * Invoked just before the Chromium window's native resources are released (window closed by
     * the user). Lets the hosting bridge dispose the SWT side so the app shuts down. {@code null}
     * until a bridge registers one via {@link #setOnWindowClosed}.
     */
    private volatile Runnable onWindowClosed;

    public static ChromiumStandaloneLauncher current() {
        return current;
    }

    /**
     * Registers a callback to run when the standalone window is about to close. The web bridge
     * uses this to dispose the SWT {@code Shell}. The callback may fire on the CEF thread, so it
     * is responsible for marshalling onto the SWT UI thread.
     */
    public void setOnWindowClosed(Runnable r) {
        this.onWindowClosed = r;
    }

    /**
     * Opens a Chromium standalone window pointing at {@code url}.
     * Sets {@code chromium.multi_threaded_message_loop=true} so Chromium runs its
     * event loop on a background thread without blocking the SWT event loop.
     */
    public void open(String url) {
        System.setProperty("chromium.disable-frame-restrictions", "true");
        System.setProperty("chromium.external_message_pump", "false");
        System.setProperty("chromium.multi_threaded_message_loop", Boolean.toString(!isMac));
        // CSD is opt-out: frameless window by default; restore the native frame with
        // -Ddev.equo.swt.csd=false. Kept in sync with Config.getConfigFlags().csd.
        boolean csd = !"false".equalsIgnoreCase(System.getProperty("dev.equo.swt.csd", "toolbar"));
        browser = ChromiumBrowser.standalone(url, 0, 0, 1280, 1024, csd);
        current = this;
        sub = browser.subscribe();
        // OpenWindowListener: CEF fires onBeforePopup when a frame calls window.open(). It exposes no
        // opener-frame identity, so we broadcast to every registered handler (only a Browser with an
        // OpenWindowListener reacts). Each handler fires its listeners synchronously and reports
        // whether the popup should be blocked (no Browser was provided to host it); if so, prevent the
        // native popup. onBeforePopup fires only on window.open (rare), so it doesn't perturb nav.
        sub.onBeforePopup(event -> {
            if (broadcastPopup()) event.prevent();
        });
        // VisibilityWindowListener: a window.open popup triggers onAfterCreated for the new browser.
        // SimpleEvent carries no identity, so broadcast (only a Browser with a visibility listener
        // reacts). Startup creations (main window + iframes) fire before any listener is added → no-op.
        sub.onAfterCreated(event -> broadcast(afterCreated));
        // onBeforeClose fires once, when the standalone window is closing. CEF only delivers it to a
        // single subscription, so the two distinct concerns ride together here:
        //   1. CloseWindowListener — a page called window.close() (only meaningful for a real popup
        //      window; an embedded iframe can't self-close, so this is usually a no-op).
        //   2. onWindowClosed — the host (e.g. SwtFlutterBridgeWeb) disposes the SWT side so the app
        //      exits cleanly; without it the SWT event loop spins on with no visible window.
        // Both must run, so don't drop either. browser is nulled last since it's being destroyed —
        // that makes pump()/close() no-op instead of touching freed native resources.
        sub.onBeforeClose(event -> {
            broadcast(beforeClose);
            Runnable r = onWindowClosed;
            if (r != null) r.run();
            browser = null;
        });
        sub.onBeforeBrowse(event -> {
            Consumer<BeforeBrowseEvent> handler = beforeBrowse.get(event.getFrameName());
            if (handler != null) handler.accept(event);
        });
        if (!isMac)
            ChromiumBrowser.startBrowsers();
    }

    /**
     * Registers a handler invoked when the frame named {@code frameName} is about
     * to navigate. Used by {@code EvolveBrowser} to implement the cancellable
     * {@code LocationListener.changing} for its iframe. The handler runs on
     * CEF's thread synchronously, so it must decide whether to
     * {@link BeforeBrowseEvent#prevent()} before returning.
     */
    public void registerBeforeBrowse(String frameName, Consumer<BeforeBrowseEvent> handler) {
        beforeBrowse.put(frameName, handler);
    }

    public void unregisterBeforeBrowse(String frameName) {
        beforeBrowse.remove(frameName);
    }

    /**
     * Registers a handler invoked when any frame calls {@code window.open} (CEF {@code onBeforePopup})
     * — used by {@code EvolveBrowser} to fire {@code OpenWindowListener}. Broadcast (see
     * {@link #beforePopup}). The handler runs on CEF's thread.
     */
    public static void registerBeforePopup(String key, BooleanSupplier handler) {
        beforePopup.put(key, handler);
    }

    public static void unregisterBeforePopup(String key) {
        beforePopup.remove(key);
    }

    /**
     * Registers a handler invoked on CEF {@code onAfterCreated} (a new browser/popup was created) —
     * used by {@code EvolveBrowser} to fire {@code VisibilityWindowListener.show}. Broadcast, since
     * the event carries no source identity (see {@link #afterCreated}).
     */
    public static void registerAfterCreated(String key, Runnable handler) {
        afterCreated.put(key, handler);
    }

    public static void unregisterAfterCreated(String key) {
        afterCreated.remove(key);
    }

    /**
     * Registers a handler invoked on CEF {@code onBeforeClose} ({@code window.close()}) — used by
     * {@code EvolveBrowser} to fire {@code CloseWindowListener}. Broadcast (see {@link #beforeClose}).
     */
    public static void registerBeforeClose(String key, Runnable handler) {
        beforeClose.put(key, handler);
    }

    public static void unregisterBeforeClose(String key) {
        beforeClose.remove(key);
    }

    /**
     * Runs every handler, isolating failures: a stale handler whose Browser/Display is being torn
     * down may throw ({@code Device is disposed}) and must not abort the rest of the broadcast.
     */
    private static void broadcast(Map<String, Runnable> handlers) {
        for (Runnable r : handlers.values()) {
            try {
                r.run();
            } catch (Throwable ignored) {
                // handler's Browser/Display is gone — skip it
            }
        }
    }

    /** {@link #broadcast} for {@link #beforePopup}: returns true if any handler asks to block. */
    private static boolean broadcastPopup() {
        boolean block = false;
        for (BooleanSupplier h : beforePopup.values()) {
            try {
                if (h.getAsBoolean()) block = true;
            } catch (Throwable ignored) {
                // handler's Browser/Display is gone — skip it
            }
        }
        return block;
    }

    /**
     * Closes the Chromium standalone window. No-op if no window was opened.
     */
    public void close(boolean shutdown) {
        if (browser != null) {
            browser.close();
            browser = null;
        }
        // With the default chromium.manual_stop_browsers=true, CEF does not stop its message loop
        // or release native helper processes when the last browser closes — the embedder must do
        // it. Skipping this leaves CEF native threads/subprocesses alive as the JVM exits, which
        // traps (SIGTRAP, exit 133). stopBrowsers() no-ops if CEF was never initialized, so it is
        // safe even when the window was already torn down via onBeforeClose (browser == null).
        if (isMac && shutdown)
            ChromiumBrowserStandalone.stopBrowsers();
        if (current == this) {
            current = null;
        }
    }

    /**
     * Returns whether a Chromium window is currently open.
     */
    public boolean isOpen() {
        return browser != null && !browser.isClosed();
    }

    public void pump() {
        if (!isMac || browser == null || browser.isClosed()) {
            return;
        }
        ChromiumBrowser.pumpOnce();
    }

    /**
     * Sets the bounds (position and size) of the standalone window. No-op if not open.
     */
    public void setWindowBounds(int x, int y, int width, int height) {
        if (browser != null)
            browser.setWindowBounds(x, y, width, height);
    }

    /**
     * Maximizes the standalone window. No-op if not open.
     */
    public void maximizeWindow() {
        if (browser != null)
            browser.maximizeWindow();
    }

    /**
     * Minimizes the standalone window. No-op if not open.
     */
    public void minimizeWindow() {
        if (browser != null)
            browser.minimizeWindow();
    }

    /**
     * Restores the standalone window from a maximized/minimized state. No-op if not open.
     */
    public void restoreWindow() {
        if (browser != null)
            browser.restoreWindow();
    }

    /**
     * Toggles fullscreen on the standalone window. No-op if not open.
     */
    public void setFullscreen(boolean fullscreen) {
        if (browser != null)
            browser.setFullscreen(fullscreen);
    }

    /**
     * Sets the standalone window title. No-op if not open.
     */
    public void setWindowTitle(String title) {
        if (browser != null)
            browser.setWindowTitle(title);
    }
}