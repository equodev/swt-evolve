package com.equo.chromium.swt;

import org.eclipse.swt.SWTException;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DartWidget;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

import com.equo.chromium.events.BeforeBrowseEvent;

import dev.equo.swt.BrowserScripting;
import dev.equo.swt.ChromiumStandaloneLauncher;
import dev.equo.swt.FlutterBridge;

public class EvolveBrowser extends WebBrowser {
    private String url = "";
    private String text = "";
    private volatile boolean canGoBack = false;
    private volatile boolean canGoForward = false;
    private Display display;
    private String beforeBrowseFrameName;
    private String popupFrameName;
    private String visibilityKey;
    private String closeKey;
    // Bumped on every setUrl/setText so the Flutter side can distinguish a real
    // (re-)navigation from an incidental state push and reload even the same URL.
    private int navSeq = 0;

    private DartWidget getDartWidget() {
        return (DartWidget) browser.getImpl();
    }

    @Override
    public Object evaluate(String script, boolean trusted) throws SWTException {
        return BrowserScripting.evaluate(getDartWidget(), display, script);
    }

    @Override
    public boolean back() {
        FlutterBridge.send(getDartWidget(), "back", java.util.Map.of());
        return true;
    }

    @Override
    public void create(Composite parent, int style) {
        display = browser.getDisplay();
        // Flutter pushes the webview history state (x = canGoBack, y =
        // canGoForward as 0/1) after each navigation; cache it for
        // isBackEnabled()/isForwardEnabled()
        FlutterBridge.onPayload(browser.getImpl(), "navigationState", Event.class, e -> {
            if (e == null) return;
            canGoBack = e.x == 1;
            canGoForward = e.y == 1;
            // The resolved current URL (after history navigation / redirects), so
            // getUrl() reflects where the Browser actually is, not the last setUrl.
            if (e.text != null && !e.text.isEmpty()) url = e.text;
        });

        String frameName = "equo-browser-" + FlutterBridge.id(getDartWidget());
        // The window listeners (open/visibility/close) are driven by the launcher's window-level CEF
        // events (onBeforePopup/onAfterCreated/onBeforeClose). These registries are static and the
        // handlers are plain Runnables (no chromium event types), so we register UNCONDITIONALLY:
        // this Browser is created before the launcher window opens, and the launcher reads the
        // registry once it's up. The launcher being absent (plain web mode) just means nothing ever
        // fires them — registration is inert.
        popupFrameName = frameName;
        ChromiumStandaloneLauncher.registerBeforePopup(popupFrameName, this::onBeforePopup);
        visibilityKey = "vis-" + frameName;
        ChromiumStandaloneLauncher.registerAfterCreated(visibilityKey, this::onPopupCreated);
        closeKey = "close-" + frameName;
        ChromiumStandaloneLauncher.registerBeforeClose(closeKey, this::onPopupClosed);
        // onBeforeBrowse (cancellable LocationListener.changing) is different: it needs the live
        // launcher instance AND references a chromium event type (BeforeBrowseEvent), which is only
        // on the classpath in Chromium mode. So it stays gated — touching it when launcher == null
        // would throw NoClassDefFoundError in plain web mode.
        ChromiumStandaloneLauncher launcher = ChromiumStandaloneLauncher.current();
        if (launcher != null) {
            beforeBrowseFrameName = frameName;
            launcher.registerBeforeBrowse(beforeBrowseFrameName, this::onBeforeBrowse);
        }
    }

    /**
     * CEF onBeforeClose ({@code window.close()}): fires {@code CloseWindowListener.close} on the SWT
     * UI thread. Like {@link #onPopupCreated}, {@code closeWindowListeners} is the
     * org.eclipse.swt.browser type, so fully-qualify.
     */
    private void onPopupClosed() {
        Runnable fire = () -> {
            org.eclipse.swt.browser.WindowEvent we = new org.eclipse.swt.browser.WindowEvent(browser);
            for (org.eclipse.swt.browser.CloseWindowListener l : closeWindowListeners) l.close(we);
        };
        if (display != null && display.getThread() == Thread.currentThread()) {
            fire.run();
        } else if (display != null && !display.isDisposed()) {
            display.asyncExec(fire);
        }
    }

    /**
     * CEF onAfterCreated (a window.open popup was created): fires {@code VisibilityWindowListener.show}
     * on the SWT UI thread. We don't host the popup, so there is no real window to size; the test only
     * observes the listener firing.
     */
    private void onPopupCreated() {
        // visibilityWindowListeners is org.eclipse.swt.browser.VisibilityWindowListener[] (WebBrowser
        // resolves it via its org.eclipse.swt.browser.* wildcard), so fully-qualify here — we can't
        // import org.eclipse.swt.browser.WindowEvent without shadowing the com.equo.chromium.swt
        // WindowEvent that onBeforePopup needs for OpenWindowListener.
        Runnable fire = () -> {
            org.eclipse.swt.browser.WindowEvent we = new org.eclipse.swt.browser.WindowEvent(browser);
            for (org.eclipse.swt.browser.VisibilityWindowListener l : visibilityWindowListeners) l.show(we);
        };
        if (display != null && display.getThread() == Thread.currentThread()) {
            fire.run();
        } else if (display != null && !display.isDisposed()) {
            display.asyncExec(fire);
        }
    }

    /**
     * CEF onBeforePopup (a {@code window.open()} call): fires {@code OpenWindowListener.open} on the
     * SWT UI thread and returns whether the popup should be BLOCKED. Per SWT semantics a listener
     * allows the popup by setting {@code event.browser} to host it and blocks it by leaving that null
     * — mirroring {@code LocationListener.changing} clearing {@code doit}. Fired synchronously because
     * the prevent() decision must be made before this CEF callback returns.
     */
    private boolean onBeforePopup() {
        boolean[] block = {false};
        Runnable fire = () -> {
            WindowEvent we = new WindowEvent(browser);
            we.required = true;
            we.browser = null;
            for (OpenWindowListener l : openWindowListeners) l.open(we);
            block[0] = openWindowListeners.length > 0 && we.browser == null;
        };
        if (display != null && display.getThread() == Thread.currentThread()) {
            fire.run();
        } else if (display != null && !display.isDisposed()) {
            display.syncExec(fire);
        }
        return block[0];
    }

    /**
     * CEF window-level navigation hook for this Browser's iframe. Only handles
     * user-initiated navigations (programmatic setUrl keeps flowing through the
     * Dart locationchanging path, so there is no double-firing). Fires
     * LocationListener.changing on the SWT UI thread and cancels the navigation
     * via {@link BeforeBrowseEvent#prevent()} when a listener clears doit.
     */
    private void onBeforeBrowse(BeforeBrowseEvent event) {
        if (!event.isUserGesture()) return;
        Runnable fire = () -> {
            LocationEvent le = new LocationEvent(browser);
            le.location = event.getUrl();
            le.top = false;
            le.doit = true;
            for (LocationListener l : locationListeners) l.changing(le);
            if (!le.doit) event.prevent();
        };
        // onBeforeBrowse is synchronous; the prevent() decision must be made
        // before returning, so run on the UI thread synchronously.
        if (display != null && display.getThread() == Thread.currentThread()) {
            fire.run();
        } else if (display != null && !display.isDisposed()) {
            display.syncExec(fire);
        }
    }

    @Override
    public boolean close() {
        if (popupFrameName != null) ChromiumStandaloneLauncher.unregisterBeforePopup(popupFrameName);
        if (visibilityKey != null) ChromiumStandaloneLauncher.unregisterAfterCreated(visibilityKey);
        if (closeKey != null) ChromiumStandaloneLauncher.unregisterBeforeClose(closeKey);
        if (beforeBrowseFrameName != null) {
            ChromiumStandaloneLauncher launcher = ChromiumStandaloneLauncher.current();
            if (launcher != null) launcher.unregisterBeforeBrowse(beforeBrowseFrameName);
        }
        beforeBrowseFrameName = null;
        popupFrameName = null;
        visibilityKey = null;
        closeKey = null;
        return super.close();
    }

    @Override
    public boolean execute(String script) {
        FlutterBridge.send(getDartWidget(), "execute", java.util.Map.of("script", script));
        return true;
    }

    /**
     * Exposes a {@code BrowserFunction} to the iframe's page. The Java callback is
     * recorded in {@link dev.equo.swt.BrowserFunctionRegistry} (keyed by this
     * Browser's id) and the Dart side injects a JS shim that, when invoked,
     * performs a blocking XHR to {@code WebFlutterServer}'s
     * {@code /equo-browser-function} endpoint — which calls back into the
     * registry on this display thread. Requires same-origin content (proxy on);
     * a cross-origin iframe cannot be scripted, so the shim simply never installs.
     */
    @Override
    public void createFunction(BrowserFunction function) {
        BrowserScripting.registerFunction(
                getDartWidget(), display, function.getName(), function::function);
    }

    @Override
    public void destroyFunction(BrowserFunction function) {
        BrowserScripting.unregisterFunction(getDartWidget(), function.getName());
    }

    @Override
    public boolean forward() {
        FlutterBridge.send(getDartWidget(), "forward", java.util.Map.of());
        return true;
    }

    @Override
    public String getBrowserType() {
        return "Evolve";
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public boolean isBackEnabled() {
        return canGoBack;
    }

    @Override
    public boolean isForwardEnabled() {
        return canGoForward;
    }

    @Override
    public void refresh() {
        FlutterBridge.send(getDartWidget(), "reload", java.util.Map.of());
    }

    @Override
    public boolean setText(String html, boolean trusted) {
        text = html;
        FlutterBridge.send(getDartWidget(), "navigate",
                java.util.Map.of("text", html == null ? "" : html, "seq", ++navSeq));
        return true;
    }

    @Override
    public boolean setUrl(String url, String postData, String[] headers) {
        this.url = url;
        java.util.Map<String, Object> args = new java.util.HashMap<>();
        args.put("url", url == null ? "" : url);
        args.put("seq", ++navSeq);
        if (postData != null) args.put("postData", postData);
        if (headers != null && headers.length > 0)
            args.put("headers", java.util.Arrays.asList(headers));
        dev.equo.swt.LocalFileServing.Served served = dev.equo.swt.LocalFileServing.registerIfLocalFile(url);
        if (served != null) args.put("localFilePath", served.tokenPath());
        FlutterBridge.send(getDartWidget(), "navigate", args);
        return true;
    }

    @Override
    public void stop() {

    }
}
