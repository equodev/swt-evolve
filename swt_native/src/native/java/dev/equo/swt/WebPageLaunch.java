package dev.equo.swt;

import org.eclipse.swt.widgets.Display;

/**
 * Serves a web Display's page and opens the browser on it as the Display starts being created, so the
 * page loads while the rest of the Display is built. The page runs the app only once the Display's
 * comm is listening and {@code WebDisplayBridge.start} has published its port, by which time every
 * channel the client sends on is wired.
 *
 * <p>Nothing here may initialize {@link FlutterBridge}: that registers every widget codec,
 * which is part of what the page load overlaps.
 */
public final class WebPageLaunch {

    /** Long enough for the page of the JVM this one was relaunched from to poll its server once. */
    private static final long RETURNING_PAGE_WAIT_MS = 5_000;

    private WebPageLaunch() {
    }

    /**
     * Starts the page's server and opens the browser on it, or returns null where the page is not
     * opened this way: a desktop window, a Chromium window (opened once the platform is initialized),
     * the {@code flutter run} dev path, and a harness that injected its own bridge.
     * {@code beforeBrowserLaunch} readies what the system browser launch needs on this OS.
     */
    public static WebFlutterServer start(Display display, Runnable beforeBrowserLaunch) {
        if (ConfigFlags.isDesktopMode() || ConfigFlags.isChromiumMode() || isDartDebug() || InjectedBridge.present())
            return null;
        WebFlutterServer server = build(display, new WebFlutterServer.Builder().commPortLater());
        serveAndOpen(server, beforeBrowserLaunch);
        return server;
    }

    /** Starts {@code server}; a server that fails to start is stopped. */
    public static void serve(WebFlutterServer server) {
        try {
            try (BootTrace.Span span = BootTrace.span("webServer.start")) {
                server.start();
            }
            WebFlutterServer.carryPortAcrossRelaunch(server.getPort());
        } catch (Exception e) {
            server.stop();
            throw new RuntimeException("Failed to start web server for Display", e);
        }
    }

    /** Starts {@code server} and opens the system browser (or the host's opener) on it. */
    static void serveAndOpen(WebFlutterServer server, Runnable beforeBrowserLaunch) {
        serve(server);
        try {
            if (server.servesRelaunchedPage()) {
                launchBrowserUnlessThePageReturns(server);
            } else {
                try (BootTrace.Span span = BootTrace.span("platform.beforeBrowserLaunch")) {
                    beforeBrowserLaunch.run();
                }
                try (BootTrace.Span span = BootTrace.span("webServer.launchBrowser")) {
                    server.launchBrowser();
                }
            }
        } catch (Exception e) {
            server.stop();
            throw new RuntimeException("Failed to open a browser for Display", e);
        }
    }

    /** Stops the server of a page whose Display failed to finish its creation; returns null. */
    public static WebFlutterServer abandon(WebFlutterServer server) {
        if (server != null) server.stop();
        return null;
    }

    /** The server for {@code display}'s page; the comm port is given to {@code builder} by the caller. */
    public static WebFlutterServer build(Display display, WebFlutterServer.Builder builder) {
        builder.widgetId(display.hashCode()).widgetName("Display");
        // Optional override of the served web build directory, so a combined/host app can serve its
        // own Flutter web build (with any extension hooks installed) instead of the one extracted from
        // Evolve's jar. Set via -Ddev.equo.swt.web.dir=<absolute dir>; otherwise, in a packaged product,
        // discover the external web bundle owner (EWT) via the ExternalWebBundleProvider SPI (mirrors
        // the desktop ExternalBundleProvider path). Standalone Evolve finds no provider.
        String webDirOverride = System.getProperty("dev.equo.swt.web.dir");
        if (webDirOverride == null || webDirOverride.trim().isEmpty()) {
            try (BootTrace.Span span = BootTrace.span("externalWebBundle")) {
                webDirOverride = FlutterLibraryLoader.firstExternalWebDir(
                        java.util.ServiceLoader.load(ExternalWebBundleProvider.class));
            }
        }
        if (webDirOverride != null && !webDirOverride.trim().isEmpty()) {
            builder.webDirectory(new java.io.File(webDirOverride));
        }
        try (BootTrace.Span span = BootTrace.span("webServer.build")) {
            return builder.build();
        }
    }

    /**
     * The page the previous JVM served reloads into this one when it is still open, so a browser is
     * only opened when no page asks within the wait: the user had closed it.
     */
    private static void launchBrowserUnlessThePageReturns(WebFlutterServer server) {
        Thread waiter = new Thread(() -> {
            try {
                if (!server.awaitReturningPage(RETURNING_PAGE_WAIT_MS)) server.launchBrowser();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (java.io.IOException e) {
                System.err.println("[WebDisplayBridge] Could not open a browser on " + server.getApplicationUrl() + ": " + e);
            }
        }, "relaunch-browser");
        waiter.setDaemon(true);
        waiter.start();
    }

    /** Whether the Phase 2 dev/introspection path is active (serve via `flutter run`, VM Service on). */
    public static boolean isDartDebug() {
        return Boolean.getBoolean("dev.equo.swt.dartDebug");
    }
}
