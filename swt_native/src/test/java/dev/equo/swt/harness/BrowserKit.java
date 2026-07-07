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

/**
 * Test facade over the two mirrored Browser APIs — {@code com.equo.chromium.swt}
 * and {@code org.eclipse.swt.browser} — so {@code BrowserFlutterTest} runs
 * unchanged against either package. The two APIs are identical apart from three
 * types ({@code Browser}, {@code BrowserFunction}, and the chromium-only
 * {@code OpenWindowListener}); every other listener/event is the shared
 * {@code org.eclipse.swt.browser} type and is used directly.
 *
 * <p>Each concrete {@code *BrowserFlutterTest} subclass supplies one of these via
 * {@link #chromium()} / {@link #swt()}, so a single {@code nativeTest} run exercises
 * both packages. The facade methods mirror the Browser API names so the test
 * bodies need no rewrite.
 */
public interface BrowserKit {

    /** The {@code com.equo.chromium.swt} Browser API. */
    static BrowserKit chromium() {
        return new ChromiumBrowserKit();
    }

    /** The {@code org.eclipse.swt.browser} Browser API. */
    static BrowserKit swt() {
        return new SwtBrowserKit();
    }

    String id();

    Handle newBrowser(Composite parent, int style);

    String getCookie(String name, String url);

    boolean setCookie(String value, String url);

    void clearSessions();

    /** Facade over a single Browser instance; method names mirror the Browser API. */
    interface Handle {
        boolean setUrl(String url);

        boolean setUrl(String url, String postData, String[] headers);

        String getUrl();

        boolean setText(String html);

        boolean setText(String html, boolean trusted);

        String getText();

        Object evaluate(String script);

        Object evaluate(String script, boolean trusted);

        boolean execute(String script);

        boolean back();

        boolean forward();

        boolean isBackEnabled();

        boolean isForwardEnabled();

        void refresh();

        void stop();

        String getBrowserType();

        boolean getJavascriptEnabled();

        void setJavascriptEnabled(boolean enabled);

        void addLocationListener(LocationListener l);

        void removeLocationListener(LocationListener l);

        void addTitleListener(TitleListener l);

        void addStatusTextListener(StatusTextListener l);

        void addProgressListener(ProgressListener l);

        void removeProgressListener(ProgressListener l);

        void addCloseWindowListener(CloseWindowListener l);

        void addVisibilityWindowListener(VisibilityWindowListener l);

        void addAuthenticationListener(AuthenticationListener l);

        /** The OpenWindowListener type differs per package; the tests only flag the firing. */
        void addOpenWindowListener(Runnable onOpen);

        void addMenuDetectListener(MenuDetectListener l);

        void removeMenuDetectListener(MenuDetectListener l);

        boolean close();

        void dispose();

        boolean isDisposed();

        /** The underlying Browser as a {@link Widget} (e.g. for harness queries / identity checks). */
        Widget raw();

        /** Creates a BrowserFunction whose {@code function(Object[])} is {@code fn}. */
        Fn newFunction(String name, Function<Object[], Object> fn);
    }

    /** Facade over a BrowserFunction. */
    interface Fn {
        String getName();

        Object getBrowser();

        boolean isDisposed();

        void dispose();
    }
}
