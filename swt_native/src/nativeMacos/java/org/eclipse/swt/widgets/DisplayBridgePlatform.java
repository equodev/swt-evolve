package org.eclipse.swt.widgets;

import dev.equo.swt.ConfigFlags;
import org.eclipse.swt.internal.cocoa.NSApplication;
import org.eclipse.swt.internal.cocoa.NSEvent;
import org.eclipse.swt.internal.cocoa.NSThread;
import org.eclipse.swt.internal.cocoa.OS;

/**
 * Per-OS native init shared by the two Display surfaces on macOS — the web one ({@link WebDisplayBridge}) and the desktop-native one ({@link DeskDisplayBridge}). Creating the
 * window itself needs nothing from Java (the desktop-native window sets up {@code NSApp} natively
 * in {@code bridge.swift}, and the web surface drives a browser/Chromium), but the application menu
 * in the system menu bar is owned by the OS and has to be built here — see
 * {@link MacApplicationMenu}.
 */
final class DisplayBridgePlatform {

    private static boolean launched;

    private DisplayBridgePlatform() {
    }

    static void init() {
        // When the native SWT library isn't loaded (headless web/test, e.g. -Ddev.equo.swt.loadLibrary=false)
        // the Cocoa calls below would throw UnsatisfiedLinkError, and a headless run has no menu bar anyway.
        if ("false".equals(System.getProperty("dev.equo.swt.loadLibrary")))
            return;
        MacApplicationMenu.install();
        // The desktop window and a Chromium window bring their own native application loop.
        if (!ConfigFlags.isDesktopMode() && !ConfigFlags.isChromiumMode())
            launch();
    }

    /** The application menu as SWT widgets, for the menu bar Evolve draws inside the window. */
    static Menu systemMenu(Display display) {
        if ("false".equals(System.getProperty("dev.equo.swt.loadLibrary")))
            return null;
        return MacApplicationMenu.systemMenu(display);
    }

    /** Publishes a Shell's menu bar to the system menu bar — see {@link MacMenuBar}. */
    static void setMenuBar(Menu menu) {
        if ("false".equals(System.getProperty("dev.equo.swt.loadLibrary")))
            return;
        MacMenuBar.set(menu);
    }

    /**
     * Brings the system menu bar in step with the Shell's — see {@link MacMenuBar#sync}. Runs once
     * per {@code readAndDispatch}, so it also drains the Cocoa queue of an NSApp {@link #launch()}
     * started.
     */
    static void syncMenuBar() {
        if ("false".equals(System.getProperty("dev.equo.swt.loadLibrary")))
            return;
        MacMenuBar.sync();
        pumpEvents();
    }

    /**
     * Gives the Shell filling the native window that window's view.
     *
     * <p>A Dart-backed Control has no native handle, so {@code Control.view} stands empty and every
     * Cocoa message sent through it goes to nil and does nothing. An application is entitled to
     * reach for it — Eclipse's macOS Minimize, Zoom and Bring All to Front are implemented as
     * {@code shell.view.window()} rather than through SWT — and those commands are dead until it
     * holds something. Only the Shell that fills the window gets one: it is the only one with a
     * native window behind it.
     */
    static void bindWindowView(Control control, long view) {
        if (view == 0 || control == null || control.isDisposed())
            return;
        control.view = new org.eclipse.swt.internal.cocoa.NSView(view);
    }

    /**
     * Launches NSApp for the browser surface, which has no native window to do it. AppKit only hands
     * a launched application the system's open-application Apple Event ({@code aevt/oapp}), and an
     * application may block its startup until its handler for that event runs. Must precede any
     * handler the application installs: {@code -finishLaunching} replaces the registered handlers
     * with AppKit's own.
     */
    private static void launch() {
        // NSApp may only be driven from the main thread (-XstartOnFirstThread).
        if (launched || !NSThread.isMainThread())
            return;
        launched = true;
        NSApplication application = NSApplication.sharedApplication();
        // -finishLaunching is a one-time call; a running NSApp has already made it.
        if (!application.isRunning())
            application.finishLaunching();
    }

    /** Drains the Cocoa event queue of an NSApp {@link #launch()} started; nothing else would. */
    private static void pumpEvents() {
        if (!launched)
            return;
        NSApplication application = NSApplication.sharedApplication();
        NSEvent event;
        while ((event = application.nextEventMatchingMask(OS.NSAnyEventMask, null, OS.NSDefaultRunLoopMode, true)) != null)
            application.sendEvent(event);
    }
}
