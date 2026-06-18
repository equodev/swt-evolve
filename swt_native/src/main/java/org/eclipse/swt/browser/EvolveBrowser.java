package org.eclipse.swt.browser;

import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DartWidget;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

import dev.equo.swt.BrowserScripting;
import dev.equo.swt.ChromiumStandaloneLauncher;
import dev.equo.swt.FlutterBridge;

public class EvolveBrowser extends WebBrowser {
    private String url = "";
    private String text = "";
    private volatile boolean canGoBack = false;
    private volatile boolean canGoForward = false;
    private Display display;
    // Bumped on every setUrl/setText so the Flutter side can distinguish a real
    // (re-)navigation from an incidental state push and reload even the same URL.
    private int navSeq = 0;
    private String popupKey;
    private String visibilityKey;
    private String closeKey;

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
        // isBackEnabled()/isForwardEnabled().
        FlutterBridge.onPayload(browser.getImpl(), "navigationState", Event.class, e -> {
            if (e == null) return;
            canGoBack = e.x == 1;
            canGoForward = e.y == 1;
            // The resolved current URL (after history navigation / redirects), so
            // getUrl() reflects where the Browser actually is, not the last setUrl.
            if (e.text != null && !e.text.isEmpty()) url = e.text;
        });

        // When hosted in an Equo Chromium standalone window, this Browser is a CEF sub-frame. The
        // Flutter side cannot observe window.open/window.close from inside the (proxied) iframe, so
        // the OpenWindow/VisibilityWindow/CloseWindow listeners are driven by the window-level CEF
        // events the launcher exposes (onBeforePopup/onAfterCreated/onBeforeClose), broadcast to
        // every Browser. The registries are static + Runnable-based, so we register UNCONDITIONALLY:
        // this Browser is typically created before the launcher window opens, and the launcher reads
        // the registry once it's up. Without a launcher (plain web mode) nothing ever fires them.
        long id = FlutterBridge.id(getDartWidget());
        popupKey = "open-" + id;
        ChromiumStandaloneLauncher.registerBeforePopup(popupKey, this::onPopupOpened);
        visibilityKey = "vis-" + id;
        ChromiumStandaloneLauncher.registerAfterCreated(visibilityKey, this::onPopupCreated);
        closeKey = "close-" + id;
        ChromiumStandaloneLauncher.registerBeforeClose(closeKey, this::onPopupClosed);
    }

    /**
     * CEF onBeforePopup (window.open): fire OpenWindowListener.open and return whether the popup must
     * be BLOCKED. Per SWT, a listener allows the popup by setting {@code event.browser} to host it and
     * blocks it by leaving that null (cf. LocationListener.changing clearing doit). Fired synchronously
     * because the prevent() decision must be made before the CEF callback returns.
     */
    private boolean onPopupOpened() {
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

    /** CEF onAfterCreated (a popup browser was created): fire VisibilityWindowListener.show. */
    private void onPopupCreated() {
        fireOnUi(() -> {
            WindowEvent we = new WindowEvent(browser);
            for (VisibilityWindowListener l : visibilityWindowListeners) l.show(we);
        });
    }

    /** CEF onBeforeClose (window.close on a script-opened popup): fire CloseWindowListener.close. */
    private void onPopupClosed() {
        fireOnUi(() -> {
            WindowEvent we = new WindowEvent(browser);
            for (CloseWindowListener l : closeWindowListeners) l.close(we);
        });
    }

    private void fireOnUi(Runnable r) {
        if (display != null && display.getThread() == Thread.currentThread()) {
            r.run();
        } else if (display != null && !display.isDisposed()) {
            display.asyncExec(r);
        }
    }

    @Override
    public boolean close() {
        if (popupKey != null) ChromiumStandaloneLauncher.unregisterBeforePopup(popupKey);
        if (visibilityKey != null) ChromiumStandaloneLauncher.unregisterAfterCreated(visibilityKey);
        if (closeKey != null) ChromiumStandaloneLauncher.unregisterBeforeClose(closeKey);
        popupKey = null;
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
        FlutterBridge.send(getDartWidget(), "navigate", args);
        return true;
    }

    @Override
    public void stop() {

    }
}
