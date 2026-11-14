package dev.equo.swt;

import java.util.Iterator;
import java.util.ServiceLoader;

import org.eclipse.swt.widgets.Display;

/** Picks the surface a Display renders on: the {@link BrowserSurface} this build includes, if any. */
public final class BrowserSurfaces {

    private BrowserSurfaces() {
    }

    /** The browser surface this build includes, or null. What plugs in is fixed by the classpath. */
    public static BrowserSurface find() {
        return Found.SURFACE;
    }

    /**
     * Opens the surface of a Display being created: the browser's page when this build has a browser
     * surface, null otherwise. Without one, an unset {@link ConfigFlags#MODE_PROPERTY} becomes
     * {@link ConfigFlags#MODE_DESKTOP}.
     */
    public static AutoCloseable open(Display display, Runnable beforeBrowserLaunch) {
        BrowserSurface surface = find();
        if (surface != null) return surface.startPage(display, beforeBrowserLaunch);
        if (ConfigFlags.mode() == null) ConfigFlags.setMode(ConfigFlags.MODE_DESKTOP);
        return null;
    }

    /** Closes the page {@link #open} served for a Display that failed to finish its creation; returns null. */
    public static AutoCloseable abandon(AutoCloseable page) {
        if (page != null) {
            try {
                page.close();
            } catch (Exception e) {
                throw new RuntimeException("Failed to close the page of a Display that was not created", e);
            }
        }
        return null;
    }

    /** The surface a non-desktop mode needs; a plain error in a build without one. */
    public static BrowserSurface require() {
        BrowserSurface surface = find();
        if (surface == null)
            throw new UnsupportedOperationException(ConfigFlags.MODE_PROPERTY + "=" + ConfigFlags.mode()
                    + " is not available in this build, which renders in a native window only ("
                    + ConfigFlags.MODE_PROPERTY + "=" + ConfigFlags.MODE_DESKTOP + ").");
        return surface;
    }

    private static final class Found {
        static final BrowserSurface SURFACE = first();

        private static BrowserSurface first() {
            Iterator<BrowserSurface> it =
                    ServiceLoader.load(BrowserSurface.class, BrowserSurface.class.getClassLoader()).iterator();
            return it.hasNext() ? it.next() : null;
        }
    }
}
