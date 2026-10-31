package org.eclipse.swt.widgets;

import dev.equo.swt.BootTrace;
import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.Callback;
import org.eclipse.swt.internal.cocoa.CGPoint;
import org.eclipse.swt.internal.cocoa.NSApplication;
import org.eclipse.swt.internal.cocoa.NSEvent;
import org.eclipse.swt.internal.cocoa.NSPoint;
import org.eclipse.swt.internal.cocoa.NSRect;
import org.eclipse.swt.internal.cocoa.NSScreen;
import org.eclipse.swt.internal.cocoa.NSThread;
import org.eclipse.swt.internal.cocoa.NSWindow;
import org.eclipse.swt.internal.cocoa.OS;

/**
 * Per-OS native init shared by the two Display surfaces on macOS — the web one ({@link WebDisplayBridge}) and the desktop-native one ({@link DeskDisplayBridge}). Creating the
 * window itself needs nothing from Java (the desktop-native window sets up {@code NSApp} natively
 * in {@code bridge.swift}, and the web surface drives a browser/Chromium), but the application menu
 * in the system menu bar is owned by the OS and has to be built here — see
 * {@link MacApplicationMenu}. None of it is on the way to the first frame: SWT's Cocoa library loads
 * on a thread of its own ({@link #preload}) and the menu is installed from the event loop
 * ({@link #syncMenuBar}).
 */
final class DisplayBridgePlatform {

    static final String LOAD_LIBRARY = "dev.equo.swt.loadLibrary";

    private static final long NSWindowAnimationBehaviorNone = 2;

    private static boolean launched;

    private static boolean pump;

    private static final String APPLICATION_CLASS = "EvolveApplication";

    private static Callback isRunningCallback;

    /** NSApp's class before {@link #markRunning} swapped it, or 0 when it is not swapped. */
    private static long applicationClass;

    /** Live browser-surface Displays; {@code isRunning} answers YES while there is one. */
    private static int webDisplays;

    private DisplayBridgePlatform() {
    }

    /**
     * Starts loading SWT's Cocoa library on a thread of its own, so that the Display is created while
     * it loads. Its static init resolves thousands of Objective-C classes and selectors, which is most
     * of what the menu bar and the browser launch cost; the first Cocoa call on the Display thread
     * waits for it to finish, which the JVM's class initialization guarantees.
     */
    static void preload() {
        if (nativeLibraryOff())
            return;
        CocoaPreload.start();
    }

    static void init() {
        // When the native SWT library isn't loaded (headless web/test, e.g. -Ddev.equo.swt.loadLibrary=false)
        // the Cocoa calls below would throw UnsatisfiedLinkError, and a headless run has no menu bar anyway.
        if (nativeLibraryOff())
            return;
        // The desktop window and a Chromium window bring their own native application loop.
        if (!ConfigFlags.isDesktopMode() && !ConfigFlags.isChromiumMode()) {
            launch();
            pump = launched && appleEventPumpRequested();
            webDisplays++;
        }
    }

    /**
     * Undoes {@link #init()} for a disposed Display: once the last browser-surface Display is gone,
     * NSApp gets its own class back and stops reporting {@code isRunning}, as native SWT's does.
     */
    static void release() {
        if (nativeLibraryOff() || ConfigFlags.isDesktopMode() || ConfigFlags.isChromiumMode() || webDisplays == 0)
            return;
        if (--webDisplays > 0 || applicationClass == 0)
            return;
        OS.object_setClass(NSApplication.sharedApplication().id, applicationClass);
        applicationClass = 0;
    }

    /**
     * Nothing to ready: {@code Program.launch} opens the system browser through NSWorkspace, and its
     * first Cocoa call waits for the library {@link #preload} is loading.
     */
    static void beforeBrowserLaunch() {
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
     *
     * <p>Where this process owns the OS menu bar, the first pass installs the application menu, so it
     * is not built inside Display creation, before the first frame; nothing can open the menu before
     * the event loop runs anyway. In a browser tab the bar is the browser's, so nothing is installed.
     */
    static void syncMenuBar() {
        if (nativeLibraryOff())
            return;
        if (Config.getConfigFlags().system_menu_bar)
            MacApplicationMenu.install();
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

    /** Where the OS pointer is, in screen points from the primary screen's top left; null without the native library. */
    static Point cursorLocation() {
        if (nativeLibraryOff())
            return null;
        NSPoint location = NSEvent.mouseLocation();
        NSRect primaryFrame = new NSScreen(NSScreen.screens().objectAtIndex(0)).frame();
        return new Point((int) location.x, (int) (primaryFrame.height - location.y));
    }

    /** Whether a mouse button is held; false without the native library. */
    static boolean pointerButtonDown() {
        if (nativeLibraryOff())
            return false;
        return OS.objc_msgSend(OS.objc_getClass("NSEvent"), OS.sel_registerName("pressedMouseButtons")) != 0;
    }

    /**
     * Moves the OS pointer to a point from the primary screen's top left, by posting a mouse move as
     * SWT does; whether it was posted.
     */
    static boolean moveCursor(int x, int y) {
        if (nativeLibraryOff())
            return false;
        long source = OS.CGEventSourceCreate(OS.kCGEventSourceStateHIDSystemState);
        if (source == 0)
            return false;
        CGPoint position = new CGPoint();
        position.x = x;
        position.y = y;
        long event = OS.CGEventCreateMouseEvent(source, OS.kCGEventMouseMoved, position, 0);
        if (event != 0) {
            OS.CGEventPost(OS.kCGHIDEventTap, event);
            OS.CFRelease(event);
        }
        OS.CFRelease(source);
        return event != 0;
    }

    /** Whether the native SWT library is switched off ({@code -Ddev.equo.swt.loadLibrary=false}). */
    private static boolean nativeLibraryOff() {
        return "false".equals(System.getProperty(LOAD_LIBRARY));
    }

    /**
     * Whether this run asks for the Cocoa pump that delivers NSApp's events
     * ({@code -Ddev.equo.swt.mac.appleEventPump=true}). Off by default: only an application that
     * blocks its startup on the open-application Apple Event needs it, and the pump costs every
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
     *
     * <p>AWT needs it too: loading AWT on the main thread with no NSApp running makes AWT run its own
     * event loop there, so the Display thread never returns.
     */
    private static void launch() {
        // NSApp may only be driven from the main thread (-XstartOnFirstThread).
        if (applicationClass != 0 || !NSThread.isMainThread())
            return;
        NSApplication application = NSApplication.sharedApplication();
        if (application.isRunning()) {
            launched = true;
            return;
        }
        // -finishLaunching is a one-time call: a Display made after the last one was disposed only
        // marks NSApp running again.
        if (!launched)
            application.finishLaunching();
        launched = true;
        markRunning(application);
    }

    /**
     * Makes {@code [NSApp isRunning]} answer YES while a browser-surface Display is alive, as native
     * SWT's Display does. AWT decides from it whether to embed in the host's application or to run
     * its own event loop on the main thread, and this NSApp is never {@code -run}.
     */
    private static void markRunning(NSApplication application) {
        long cls = OS.objc_lookUpClass(APPLICATION_CLASS);
        if (cls == 0) {
            isRunningCallback = new Callback(DisplayBridgePlatform.class, "isRunningProc", 2);
            long proc = isRunningCallback.getAddress();
            if (proc == 0)
                SWT.error(SWT.ERROR_NO_MORE_CALLBACKS);
            cls = OS.objc_allocateClassPair(OS.object_getClass(application.id), APPLICATION_CLASS, 0);
            OS.class_addMethod(cls, OS.sel_isRunning, proc, "c@:");
            OS.objc_registerClassPair(cls);
        }
        applicationClass = OS.object_getClass(application.id);
        OS.object_setClass(application.id, cls);
    }

    static long isRunningProc(long id, long sel) {
        return webDisplays > 0 ? 1 : 0;
    }

    /** NSEventMaskAny: matches every event type. Not declared by every release's OS. */
    private static final long NS_ANY_EVENT_MASK = -1L;

    /** Drains the Cocoa event queue of an NSApp {@link #launch()} started, when the run asks for it. */
    private static void pumpEvents() {
        if (!pump)
            return;
        NSApplication application = NSApplication.sharedApplication();
        NSEvent event;
        while ((event = application.nextEventMatchingMask(NS_ANY_EVENT_MASK, null, OS.NSDefaultRunLoopMode, true)) != null)
            application.sendEvent(event);
    }

    /**
     * The size native SWT creates a shell at when the application gives it none: Cocoa makes every
     * shell, dialog or not, 5/8 of the screen each way, and the window rounds it to whole points.
     */
    static Point defaultShellSize(Rectangle screen, DartShell shell) {
        return new Point((int) Math.round(screen.width * 0.625), (int) Math.round(screen.height * 0.625));
    }

    /** Starts the preload thread when first initialized: once per process, as the library is process-wide. */
    private static final class CocoaPreload {

        static {
            Thread thread = new Thread(() -> {
                try (BootTrace.Span span = BootTrace.span("load SWT Cocoa library")) {
                    Class.forName("org.eclipse.swt.internal.cocoa.OS", true, DisplayBridgePlatform.class.getClassLoader());
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException(e);
                }
            }, "SWT Cocoa preload");
            thread.setDaemon(true);
            thread.start();
        }

        static void start() {
        }
    }
}
