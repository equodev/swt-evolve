package dev.equo.swt.harness;

import java.util.function.Function;

import org.eclipse.swt.browser.AuthenticationListener;
import org.eclipse.swt.browser.CloseWindowListener;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.browser.StatusTextListener;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.browser.VisibilityWindowListener;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;

import com.equo.chromium.swt.Browser;
import com.equo.chromium.swt.BrowserFunction;
import com.equo.chromium.swt.OpenWindowListener;

/** {@link BrowserKit} bound to the {@code com.equo.chromium.swt} Browser API. */
final class ChromiumBrowserKit implements BrowserKit {

    @Override
    public String id() {
        return "chromium";
    }

    @Override
    public Handle newBrowser(Composite parent, int style) {
        return new H(new Browser(parent, style));
    }

    @Override
    public String getCookie(String name, String url) {
        return Browser.getCookie(name, url);
    }

    @Override
    public boolean setCookie(String value, String url) {
        return Browser.setCookie(value, url);
    }

    @Override
    public void clearSessions() {
        Browser.clearSessions();
    }

    private static final class H implements Handle {
        private final Browser b;

        H(Browser b) {
            this.b = b;
        }

        @Override public boolean setUrl(String url) { return b.setUrl(url); }
        @Override public boolean setUrl(String url, String postData, String[] headers) { return b.setUrl(url, postData, headers); }
        @Override public String getUrl() { return b.getUrl(); }
        @Override public boolean setText(String html) { return b.setText(html); }
        @Override public boolean setText(String html, boolean trusted) { return b.setText(html, trusted); }
        @Override public String getText() { return b.getText(); }
        @Override public Object evaluate(String script) { return b.evaluate(script); }
        @Override public Object evaluate(String script, boolean trusted) { return b.evaluate(script, trusted); }
        @Override public boolean execute(String script) { return b.execute(script); }
        @Override public boolean back() { return b.back(); }
        @Override public boolean forward() { return b.forward(); }
        @Override public boolean isBackEnabled() { return b.isBackEnabled(); }
        @Override public boolean isForwardEnabled() { return b.isForwardEnabled(); }
        @Override public void refresh() { b.refresh(); }
        @Override public void stop() { b.stop(); }
        @Override public String getBrowserType() { return b.getBrowserType(); }
        @Override public boolean getJavascriptEnabled() { return b.getJavascriptEnabled(); }
        @Override public void setJavascriptEnabled(boolean enabled) { b.setJavascriptEnabled(enabled); }
        @Override public void addLocationListener(LocationListener l) { b.addLocationListener(l); }
        @Override public void removeLocationListener(LocationListener l) { b.removeLocationListener(l); }
        @Override public void addTitleListener(TitleListener l) { b.addTitleListener(l); }
        @Override public void addStatusTextListener(StatusTextListener l) { b.addStatusTextListener(l); }
        @Override public void addProgressListener(ProgressListener l) { b.addProgressListener(l); }
        @Override public void removeProgressListener(ProgressListener l) { b.removeProgressListener(l); }
        @Override public void addCloseWindowListener(CloseWindowListener l) { b.addCloseWindowListener(l); }
        @Override public void addVisibilityWindowListener(VisibilityWindowListener l) { b.addVisibilityWindowListener(l); }
        @Override public void addAuthenticationListener(AuthenticationListener l) { b.addAuthenticationListener(l); }
        @Override public void addOpenWindowListener(Runnable onOpen) { b.addOpenWindowListener((OpenWindowListener) e -> onOpen.run()); }
        @Override public void addMenuDetectListener(MenuDetectListener l) { b.addMenuDetectListener(l); }
        @Override public void removeMenuDetectListener(MenuDetectListener l) { b.removeMenuDetectListener(l); }
        @Override public boolean close() { return b.close(); }
        @Override public void dispose() { b.dispose(); }
        @Override public boolean isDisposed() { return b.isDisposed(); }
        @Override public Widget raw() { return b; }

        @Override
        public Fn newFunction(String name, Function<Object[], Object> fn) {
            return new F(new BrowserFunction(b, name) {
                @Override
                public Object function(Object[] arguments) {
                    return fn.apply(arguments);
                }
            });
        }
    }

    private static final class F implements Fn {
        private final BrowserFunction f;

        F(BrowserFunction f) {
            this.f = f;
        }

        @Override public String getName() { return f.getName(); }
        @Override public Object getBrowser() { return f.getBrowser(); }
        @Override public boolean isDisposed() { return f.isDisposed(); }
        @Override public void dispose() { f.dispose(); }
    }
}
