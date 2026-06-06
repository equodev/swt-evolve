package dev.equo.swt;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.equo.chromium.ChromiumBrowser;
import com.equo.chromium.ChromiumBrowserStandalone;

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
        boolean csd = "true".equalsIgnoreCase(System.getProperty("dev.equo.chromium.csd", "false"));
        browser = ChromiumBrowser.standalone(url, 0, 0, 1280, 1024, csd);
        current = this;
        var sub = browser.subscribe();
        sub.onBeforeClose(event -> {
            Runnable r = onWindowClosed;
            if (r != null) r.run();
            browser = null;
        });
        if (!isMac)
            ChromiumBrowser.startBrowsers();
    }

    /**
     * Closes the Chromium standalone window. No-op if no window was opened.
     */
    public void close() {
        if (browser != null) {
            browser.close();
            browser = null;
        }
        // With the default chromium.manual_stop_browsers=true, CEF does not stop its message loop
        // or release native helper processes when the last browser closes — the embedder must do
        // it. Skipping this leaves CEF native threads/subprocesses alive as the JVM exits, which
        // traps (SIGTRAP, exit 133). stopBrowsers() no-ops if CEF was never initialized, so it is
        // safe even when the window was already torn down via onBeforeClose (browser == null).
        if (isMac)
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