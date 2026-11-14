package dev.equo.swt;

import org.eclipse.swt.widgets.Display;

/**
 * A surface that renders a Display in a browser instead of a native window, found through
 * {@link java.util.ServiceLoader}. {@link BrowserSurfaces} picks it; a build without one renders in
 * a native window only.
 */
public interface BrowserSurface {

    /**
     * Serves {@code display}'s page as the Display starts being created, so the page loads while the
     * rest of the Display is built; null where the page is not opened this way.
     * {@code beforeBrowserLaunch} readies what launching the system browser needs on this OS.
     */
    AutoCloseable startPage(Display display, Runnable beforeBrowserLaunch);

    /** The Display-level bridge of {@code display}, rendered on this surface. */
    FlutterBridge newDisplayBridge(Display display);
}
