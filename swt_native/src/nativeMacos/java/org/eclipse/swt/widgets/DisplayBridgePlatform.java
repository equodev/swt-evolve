package org.eclipse.swt.widgets;

import dev.equo.swt.ConfigFlags;
import org.eclipse.swt.internal.cocoa.NSApplication;
import org.eclipse.swt.internal.cocoa.NSEvent;
import org.eclipse.swt.internal.cocoa.NSThread;
import org.eclipse.swt.internal.cocoa.NSWindow;
import org.eclipse.swt.internal.cocoa.OS;

/**
 * Per-OS native init shared by the two Display surfaces on macOS — the web one ({@link WebDisplayBridge}) and the desktop-native one ({@link DeskDisplayBridge}). Creating the
 * window itself needs nothing from Java (the desktop-native window sets up {@code NSApp} natively
 * in {@code bridge.swift}, and the web surface drives a browser/Chromium), but the application menu
 * in the system menu bar is owned by the OS and has to be built here — see
 * {@link MacApplicationMenu}.
 */
final class DisplayBridgePlatform {

    static final String LOAD_LIBRARY = "dev.equo.swt.loadLibrary";

    private static final long NSWindowAnimationBehaviorNone = 2;

    private static boolean launched;

    private DisplayBridgePlatform() {
    }

    static void init() {
        // When the native SWT library isn't loaded (headless web/test, e.g. -Ddev.equo.swt.loadLibrary=false)
        // the Cocoa calls below would throw UnsatisfiedLinkError, and a headless run has no menu bar anyway.
        if (nativeLibraryOff())
            return;
        MacApplicationMenu.install();
        // The desktop window and a Chromium window bring their own native application loop.
        if (appleEventPumpRequested() && !ConfigFlags.isDesktopMode() && !ConfigFlags.isChromiumMode())
            launch();
    }

    /** The application menu as SWT widgets, for the menu bar Evolve draws inside the window. */
    static Menu systemMenu(Display display) {
        if (nativeLibraryOff())
            return null;
        return MacApplicationMenu.systemMenu(display);
    }

    /** Publishes a Shell's menu bar to the system menu bar — see {@link MacMenuBar}. */
    static void setMenuBar(Menu menu) {
        if (nativeLibraryOff())
            return;
        MacMenuBar.set(menu);
    }

    /**
     * Brings the system menu bar in step with the Shell's — see {@link MacMenuBar#sync}. Runs once
     * per {@code readAndDispatch}, so it also drains the Cocoa queue of an NSApp {@link #launch()}
     * started.
     */
    static void syncMenuBar() {
        if (nativeLibraryOff())
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
     * Orders the launcher's splash window out. It belongs to the main thread, which runs no Cocoa
     * event loop while the Display renders in a browser, so the window would otherwise stay on screen.
     * With no loop to commit it, the window server only drops the window when the order-out is not
     * animated and its transaction is flushed. It is not closed: the launcher releases it when it
     * takes the splash down.
     *
     * @return whether the window was ordered out
     */
    static boolean dismissLauncherSplash() {
        long window = Long.getLong("org.eclipse.equinox.launcher.splash.handle", 0);
        if (window == 0 || nativeLibraryOff() || !NSThread.isMainThread())
            return false;
        OS.objc_msgSend(window, OS.sel_registerName("setAnimationBehavior:"), NSWindowAnimationBehaviorNone);
        new NSWindow(window).orderOut(null);
        OS.objc_msgSend(OS.objc_getClass("CATransaction"), OS.sel_registerName("flush"));
        return true;
    }

    /** Whether the native SWT library is switched off ({@code -Ddev.equo.swt.loadLibrary=false}). */
    private static boolean nativeLibraryOff() {
        return "false".equals(System.getProperty(LOAD_LIBRARY));
    }

    /**
     * Whether this run asks for the NSApp launch and the Cocoa pump that goes with it
     * ({@code -Ddev.equo.swt.mac.appleEventPump=true}). Off by default: only an application that
     * blocks its startup on the open-application Apple Event needs them, and the pump costs every
     * other one its Display thread — {@link #pumpEvents()} blocks indefinitely on an NSApp that was
     * finish-launched but never run, which is every browser-surface run, so the thread never returns
     * from its first {@code readAndDispatch} and nothing posted to it with {@code asyncExec} runs.
     */
    private static boolean appleEventPumpRequested() {
        return Boolean.getBoolean("dev.equo.swt.mac.appleEventPump");
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
