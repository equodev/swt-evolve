//
//  flutter_bridge.swift
//  FlutterBridge
//
//  Created by Guillermo Zunino on 13/02/2025.
//

import Foundation
import Cocoa
import FlutterMacOS
import ObjectiveC

// =================================================================================================
// Embedded-AWT coexistence (SWT_AWT / Swing embedding).
//
// The desk window drives the AppKit run loop itself via pump()/waitForEvent() and deliberately
// never calls [NSApp run] (so the JNI Initialize call can return to Java). That leaves
// -[NSApplication isRunning] == false. When an app then embeds AWT/Swing (Evolve's SWT_AWT bridge),
// AWT inspects that flag and, seeing a not-running app, starts its OWN modal loop
// (+[NSApplicationAWT runAWTLoopWithApp:] → -[NSApplication run]) on the main thread — which hijacks
// it from pump() and deadlocks the whole SWT/Flutter event loop. Native SWT never hits this because
// its event loop already has the app "running", so AWT embeds instead of taking over.
//
// Once we've finishLaunching'd we ARE effectively running (we just pump the loop by hand), so report
// isRunning = true. AWT then treats the app as an already-running host and embeds. The override only
// flips the return value once the desk window has launched; it changes nothing else about the loop.
// =================================================================================================
private var deskAppConsideredRunning = false
private var isRunningOverrideInstalled = false

@MainActor
private func installIsRunningOverride() {
    if isRunningOverrideInstalled { return }
    isRunningOverrideInstalled = true
    let sel = #selector(getter: NSApplication.isRunning)
    guard let method = class_getInstanceMethod(NSApplication.self, sel) else { return }
    typealias IsRunningFn = @convention(c) (AnyObject, Selector) -> Bool
    let original = unsafeBitCast(method_getImplementation(method), to: IsRunningFn.self)
    let override: @convention(block) (AnyObject) -> Bool = { app in
        deskAppConsideredRunning || original(app, sel)
    }
    method_setImplementation(method, imp_implementationWithBlock(override))
}

/// A Flutter "surface" hosted by the native bridge. Two kinds exist — an embedded view inside a
/// native SWT parent ({@code FlutterBridgeController}) and a standalone top-level window hosting the
/// whole Display ({@code FlutterDisplayWindowController}). The JNI entry points operate on this base
/// type so one set of functions dispatches to whichever kind the context points at.
@MainActor
class FlutterSurface: NSObject {
    func getView() -> NSView? { return nil }
    func setBounds(x: Int32, y: Int32, w: Int32, h: Int32, vx: Int32, vy: Int32, vw: Int32, vh: Int32) {}
    func pump() -> Int32 { return 0 }
    func waitForEvent(millis: Int32) {}
    func setTitle(_ title: String) {}
    func setState(_ state: Int32) {}
    func setVisible(_ visible: Bool) {}
    func origin() -> (Int32, Int32)? { return nil }
    func dispose() {}
}

// =================================================================================================
// Embedded surface: one Flutter view per native SWT widget, added as a subview of a native parent.
// =================================================================================================
@MainActor
class FlutterBridgeController: FlutterSurface {

    private var flutterViewController: FlutterViewController?
    private var window: NSWindow? // Keep for the offscreen/headless case
    private var view: NSView?

    func initialize(parentView: NSView?, port: Int32, widgetId: Int64, widgetName: String, theme: String, backgroundColor: Int32, parentBackgroundColor: Int32) -> NSView? {
        print("FlutterBridgeController.initialize port:\(port) parent:\(String(describing: parentView)) id:\(widgetId) name:\(widgetName)")

        let arguments = [String(port), String(widgetId), widgetName, theme, String(backgroundColor), String(parentBackgroundColor)]
        let frameworkPath = bundleBase()! + "/Frameworks/App.framework"
        let project = FlutterDartProject(precompiledDartBundle: Bundle(path: frameworkPath))
        project.dartEntrypointArguments = arguments
        flutterViewController = FlutterViewController.init(project: project)
        RegisterGeneratedPlugins(registry: flutterViewController!)

        // If parent view is provided, add Flutter view as a subview
        if let parent = parentView {
            let frame = parent.bounds

            let container = FlippedView()
            container.frame = frame
            container.wantsLayer = true
            parent.addSubview(container, positioned: .below, relativeTo: nil) // add it last, otherwise appears first to swt
            self.view = container

            if let flutterView = flutterViewController?.view {
                flutterView.frame = parent.bounds
                flutterView.autoresizingMask = [.width, .height]
                container.addSubview(flutterView)
                return flutterView
            }
        } else {
            print("FlutterBridgeController.initialize - Headless mode (offscreen window)")
            // Create an offscreen window to trigger Flutter engine
            let window = NSWindow(
                contentRect: NSRect(x: 0, y: 0, width: 1280, height: 720),
                styleMask: [.borderless],
                backing: .buffered,
                defer: false)

            window.contentViewController = flutterViewController

            // Try to trigger the engine by ordering the window offscreen
            window.orderBack(nil)

            if let flutterView = flutterViewController?.view {
                let explicitFrame = NSRect(x: 0, y: 0, width: 1280, height: 720)
                flutterView.frame = explicitFrame
                self.view = flutterView
                print("Headless mode: Actual frame is \(flutterView.frame)")
            } else {
                print("ERROR: flutterViewController.view is nil!")
            }

            self.window = window
        }
        return nil
    }

    override func getView() -> NSView? {
        return view
    }

    override func setBounds(x: Int32, y: Int32, w: Int32, h: Int32, vx: Int32, vy: Int32, vw: Int32, vh: Int32) {
        if let v = view {
            v.frame = NSRect(x: CGFloat(x), y: CGFloat(y), width: CGFloat(w), height: CGFloat(h))
            v.autoresizingMask = []
        }
        if let flutterView = flutterViewController?.view {
            flutterView.frame = NSRect(x: CGFloat(vx), y: CGFloat(vy), width: CGFloat(vw), height: CGFloat(vh))
        }
    }

    override func dispose() {
        flutterViewController?.view.removeFromSuperview()
        flutterViewController?.engine.shutDownEngine()
        flutterViewController = nil
    }
}

/// Runs `body` as if already on the main actor, without hopping.
///
/// `MainActor.assumeIsolated` is macOS 14+ and the runner deploys back to 11.5, so calling it
/// unguarded fails the build. Flutter always delivers platform-channel calls on the main thread —
/// the very precondition `assumeIsolated` checks — so on older systems we assert that ourselves and
/// cross into the actor the same way. Staying synchronous is the point: `beginMove` hands the
/// in-flight mouse-down to the OS drag loop and `NSApp.currentEvent` is already gone by the next
/// turn of the run loop.
@inline(__always)
private func assumingMainActor<T>(_ body: @MainActor () -> T) -> T {
    if #available(macOS 14.0, *) {
        return MainActor.assumeIsolated(body)
    }
    precondition(Thread.isMainThread, "window channel call delivered off the main thread")
    return withoutActuallyEscaping(body) { unsafeBitCast($0, to: (() -> T).self)() }
}

// =================================================================================================
// Window surface (desktop-native, 100% Flutter): the ENTIRE Dart-backed SWT tree in one top-level
// window. A single FlutterViewController fills an NSWindow and connects back over the comm port,
// exactly like the web client does in a browser. There is no native SWT here, so nothing else
// creates or runs the NSApplication — this controller bootstraps NSApp (regular activation policy +
// a minimal menu + finishLaunching) and the SWT event loop drives it by calling pump() per readAndDispatch.
// =================================================================================================
@MainActor
class FlutterDisplayWindowController: FlutterSurface, NSWindowDelegate {

    private var flutterViewController: FlutterViewController?
    private var window: NSWindow?
    private var closed = false
    private var closeRequested = false

    /// The CSD window channel; held so it outlives initialize() and can push focus changes.
    private var windowChannel: FlutterMethodChannel?

    func initialize(port: Int32, displayId: Int64, widgetName: String, theme: String, backgroundColor: Int32, width: Int32, height: Int32, csdEnabled: Bool) {
        print("FlutterDisplayWindowController.initialize port:\(port) id:\(displayId) name:\(widgetName) \(width)x\(height)")

        let app = NSApplication.shared
        // Application-level bootstrap, once per process rather than once per window. A second window
        // must not repeat it: -finishLaunching is documented as a one-time call, and running it again
        // re-posts the launch notifications and re-runs AppKit's launch bookkeeping. Doing that while
        // AppKit is mid-dispatch -- which is exactly where a window opened from a click is created --
        // left the new window's engine unattached, so the in-flight pointer event was delivered to an
        // invalid engine handle and the window never came up. Opening the same shell from a timer
        // never showed it, because there is no event being dispatched then.
        let firstWindow = !deskAppConsideredRunning
        if firstWindow {
            app.setActivationPolicy(.regular)
            setupMainMenuIfNeeded(app)
            // Report the app as "running" (see the note at the top) so embedded AWT/Swing coexists
            // with our hand-driven pump() instead of hijacking the main thread with its own [NSApp run].
            installIsRunningOverride()
        }

        // Same Flutter bootstrap as the embedded path: load the precompiled Dart bundle next to the
        // dylib and pass [port, id, name, theme, bg, parentBg] so main() connects to the comm port.
        let bg = String(backgroundColor)
        let arguments = [String(port), String(displayId), widgetName, theme, bg, bg]
        let frameworkPath = bundleBase()! + "/Frameworks/App.framework"
        let project = FlutterDartProject(precompiledDartBundle: Bundle(path: frameworkPath))
        project.dartEntrypointArguments = arguments
        let fvc = FlutterViewController(project: project)
        self.flutterViewController = fvc

        // .fullSizeContentView only when Flutter draws the title bar: it extends the content under
        // the title bar area, which is what CSD needs and what would otherwise leave the system
        // title bar covering the top of the app.
        var styleMask: NSWindow.StyleMask = [.titled, .closable, .miniaturizable, .resizable]
        if csdEnabled {
            styleMask.insert(.fullSizeContentView)
        }
        let win = NSWindow(
            contentRect: NSRect(x: 0, y: 0, width: CGFloat(width), height: CGFloat(height)),
            styleMask: styleMask,
            backing: .buffered,
            defer: false)
        win.title = widgetName
        // Client-Side Decorations: when Flutter draws the title bar itself (csd_scaffold.dart) the
        // system one is hidden rather than removed — the window stays .titled/.resizable, which
        // keeps the native resize edges, zoom semantics and window management a .borderless window
        // would lose. .fullSizeContentView lets Flutter paint up to the top edge. With CSD off
        // nothing would draw a replacement, so the system title bar and its buttons stay.
        if csdEnabled {
            win.titlebarAppearsTransparent = true
            win.titleVisibility = .hidden
            win.standardWindowButton(.closeButton)?.isHidden = true
            win.standardWindowButton(.miniaturizeButton)?.isHidden = true
            win.standardWindowButton(.zoomButton)?.isHidden = true
        }
        // Match the window chrome to the Flutter background so there is no black flash before the
        // first frame is rendered.
        win.backgroundColor = NSColor(
            red: CGFloat((backgroundColor >> 16) & 0xFF) / 255.0,
            green: CGFloat((backgroundColor >> 8) & 0xFF) / 255.0,
            blue: CGFloat(backgroundColor & 0xFF) / 255.0,
            alpha: 1.0)
        win.contentViewController = fvc
        // Adopting a content view controller makes the window take that controller's preferred size,
        // and a Flutter view has none until it has rendered -- so the window collapses to a pixel
        // here, and a window that small never reports a viewport worth having, which is the
        // handshake that would otherwise have corrected it. Restore the size that was asked for.
        win.setContentSize(NSSize(width: CGFloat(width), height: CGFloat(height)))
        win.delegate = self
        win.center()
        win.isReleasedWhenClosed = false
        self.window = win

        RegisterGeneratedPlugins(registry: fvc)
        setupWindowChannel(fvc)

        if firstWindow {
            // Bootstrap the app without entering the modal run loop (so this JNI call returns to
            // Java). The SWT event loop then services Cocoa via pump().
            app.finishLaunching()
            // From here we service the AppKit loop ourselves via pump(); treat the app as running.
            deskAppConsideredRunning = true
            win.makeKeyAndOrderFront(nil)
            // Only the first window takes the application forward. Repeating it for every window
            // pulls activation out from under whatever is being dispatched at the time.
            app.activate(ignoringOtherApps: true)
        } else {
            win.makeKeyAndOrderFront(nil)
        }
    }

    /// Drains all pending native events, then spins the run loop briefly. Returns -2 once per user
    /// close gesture (the window is still up — `windowShouldClose` vetoed it) and -1 once the window
    /// is really gone. Both are FlutterNative's pump contract; -2 is `PUMP_CLOSE_REQUESTED` there.
    override func pump() -> Int32 {
        if closed { return -1 }
        while let event = NSApp.nextEvent(matching: NSEvent.EventTypeMask.any,
                                          until: Date.distantPast,
                                          inMode: .default,
                                          dequeue: true) {
            NSApp.sendEvent(event)
        }
        // Under Flutter 3.35's merged platform/UI thread the engine's UI work (frame scheduling,
        // post-frame callbacks — including the one that sends ClientReady) runs on this main run
        // loop. The non-blocking nextEvent drain above doesn't give it time, so a busy SWT
        // readAndDispatch loop would never let a frame render. Spin the run loop ~2ms so it does —
        // the same fix applied to PumpMessages for the size-test harness.
        RunLoop.current.run(mode: .default, before: Date(timeIntervalSinceNow: 0.002))
        // The engine is up by now if it is going to be, so anything held back for it can go.
        hasPumped = true
        flushPendingActive()
        if closed { return -1 }
        if closeRequested {
            closeRequested = false
            return -2
        }
        return 0
    }

    /// Blocks until an event is available or up to `millis` ms, WITHOUT dequeuing it (the next
    /// pump() dispatches it). Peeking in default mode also services the run loop, so Flutter's
    /// main-thread tasks progress while idle — unlike a plain Thread.sleep on the Java side.
    override func waitForEvent(millis: Int32) {
        if closed { return }
        let deadline = Date(timeIntervalSinceNow: Double(millis) / 1000.0)
        _ = NSApp.nextEvent(matching: NSEvent.EventTypeMask.any,
                            until: deadline,
                            inMode: .default,
                            dequeue: false)
    }

    /// The window's content view. There is no native SWT here to give a Shell a view of its own, and
    /// an application that reaches for one gets nil and silently does nothing -- Eclipse's macOS
    /// Minimize, Zoom and Bring All to Front all go through `Shell.view.window()`. Handing out the
    /// content view gives them the real NSWindow to act on.
    override func getView() -> NSView? {
        return window?.contentView
    }

    override func setTitle(_ title: String) {
        window?.title = title
    }

    override func setBounds(x: Int32, y: Int32, w: Int32, h: Int32, vx: Int32, vy: Int32, vw: Int32, vh: Int32) {
        guard let win = window else { return }
        if let screen = win.screen ?? NSScreen.main {
            // SWT uses a top-left screen origin; Cocoa uses bottom-left. Flip the y of the frame.
            let origin = NSPoint(x: CGFloat(x), y: screen.frame.height - CGFloat(y) - CGFloat(h))
            win.setFrameOrigin(origin)
        }
        // A caller with no size yet passes 0: move the window, leave its size alone. Resizing to a
        // pixel here is what left the application with an invisible window it could never grow out
        // of, because a window that small reports no viewport for the handshake to correct.
        if w > 0 && h > 0 {
            win.setContentSize(NSSize(width: CGFloat(w), height: CGFloat(h)))
        }
    }


    /// Where the window's CONTENT starts on screen, in the same top-left space setBounds takes.
    /// Content rather than frame, so the title bar is already accounted for and a caller converting
    /// a window-local point needs no further correction. Uses the same screen as setBounds so the
    /// two round-trip; both share that call's multi-monitor caveat.
    override func origin() -> (Int32, Int32)? {
        guard let win = window, let screen = win.screen ?? NSScreen.main else { return nil }
        let content = win.contentRect(forFrameRect: win.frame)
        let top = screen.frame.height - (content.origin.y + content.height)
        return (Int32(content.origin.x.rounded()), Int32(top.rounded()))
    }

    override func setState(_ state: Int32) {
        guard let win = window else { return }
        switch state {
        case 1: // maximized
            if !win.isZoomed { win.zoom(nil) }
        case 2: // minimized
            win.miniaturize(nil)
        case 3: // fullscreen
            if !win.styleMask.contains(.fullScreen) { win.toggleFullScreen(nil) }
        default: // restore / normal
            if win.styleMask.contains(.fullScreen) {
                win.toggleFullScreen(nil)
            } else if win.isMiniaturized {
                win.deminiaturize(nil)
            } else if win.isZoomed {
                win.zoom(nil)
            }
        }
    }

    override func setVisible(_ visible: Bool) {
        guard let win = window else { return }
        // orderOut, not close: the window and its engine stay, so showing it again costs an
        // ordering call rather than a rebuild.
        if visible {
            win.makeKeyAndOrderFront(nil)
        } else {
            win.orderOut(nil)
        }
    }

    /// Registers the Client-Side-Decorations window channel the Flutter title bar drives. This is
    /// the desktop counterpart of the browser's injected `window.equo.*` host API: the Dart side
    /// (equo_window_stub.dart) calls in here when a window control is hit or the strip is dragged.
    private func setupWindowChannel(_ fvc: FlutterViewController) {
        let channel = FlutterMethodChannel(
            name: "dev.equo.swt/window",
            binaryMessenger: fvc.engine.binaryMessenger)
        channel.setMethodCallHandler { [weak self] call, result in
            assumingMainActor {
                guard let win = self?.window else {
                    result(nil)
                    return
                }
                switch call.method {
                case "minimize":
                    win.miniaturize(nil)
                case "maximize":
                    if !win.isZoomed { win.zoom(nil) }
                case "restore":
                    if win.isZoomed { win.zoom(nil) }
                case "close":
                    // Goes through the delegate, so windowShouldClose vetoes it and the next pump()
                    // reports -2 — the CSD button asks SWT to close exactly as the OS title bar does,
                    // and a doit = false listener keeps the window.
                    win.performClose(nil)
                case "beginMove":
                    // Hand the in-flight mouse-down to the OS drag loop, exactly as the system
                    // title bar would. Native SWT blocks in this same loop while a shell is
                    // dragged, so the paused pump matches platform behaviour.
                    if let event = NSApp.currentEvent { win.performDrag(with: event) }
                case "beginResize":
                    // The window keeps its native resizable edges, so the Flutter resize handles
                    // are not mounted on macOS (see CsdShell) and this never arrives.
                    break
                default:
                    result(FlutterMethodNotImplemented)
                    return
                }
                result(nil)
            }
        }
        self.windowChannel = channel
    }

    override func dispose() {
        windowChannel?.setMethodCallHandler(nil)
        windowChannel = nil
        pendingActive = nil

        // Take the view out of the window before shutting the engine down, not after. While the
        // Flutter view is still on screen it is still in the responder chain and still driving the
        // vsync waiter, so anything already in flight over a just-shut-down engine — a scheduled
        // vsync, a pointer event — reaches the embedder with a handle that no longer resolves, which
        // is what it reports as 'FlutterEngineOnVsync'/'FlutterEngineSendPointerEvent' returning
        // kInvalidArguments. Detaching first closes that window entirely.
        window?.delegate = nil
        window?.orderOut(nil)
        window?.contentViewController = nil

        flutterViewController?.engine.shutDownEngine()
        window?.close()
        window = nil
        flutterViewController = nil
    }

    /// The user asked to close (red button, Cmd+W). Refusing here keeps the window up so SWT.Close can
    /// run against a live window — a doit = false veto has something to keep, and an exit confirmation
    /// something to render into. The window is closed later, from the shell-dispose path via dispose().
    func windowShouldClose(_ sender: NSWindow) -> Bool {
        closeRequested = true
        return false
    }

    func windowWillClose(_ notification: Notification) {
        closed = true
    }

    // The macOS traffic lights grey out when the window is not key; the Dart controls mirror that
    // through csdWindowActive (see equo_window_stub.installWindowStateListeners).
    func windowDidBecomeKey(_ notification: Notification) {
        notifyActive(true)
    }

    func windowDidResignKey(_ notification: Notification) {
        notifyActive(false)
    }

    /// The active state this window has not been able to tell Dart about yet, or nil when it has.
    private var pendingActive: Bool?

    /// Tells Dart whether this window is key, once there is an engine that can be told.
    ///
    /// A window is made key inside `initialize`, before its engine has run — the engine starts on
    /// the first turn of the run loop, which only happens once the SWT loop calls `pump()`. Sending
    /// then reaches no engine ("Invalid engine handle"), and with a second window the message is
    /// lost outright rather than merely early: the first window's own state change is what ends up
    /// reported, and the new window's controls stay greyed out. Held and sent from `pump()` instead.
    private func notifyActive(_ active: Bool) {
        guard let channel = windowChannel, hasPumped else {
            pendingActive = active
            return
        }
        pendingActive = nil
        channel.invokeMethod("active", arguments: active)
    }

    /// Whether this window's engine has had a turn of the run loop, which is when it starts. There
    /// is no engine flag to read for this on macOS, and the first `pump()` is exactly the moment.
    private var hasPumped = false

    /// Sends whatever `notifyActive` had to hold back, once the engine is up.
    private func flushPendingActive() {
        guard let active = pendingActive, let channel = windowChannel else { return }
        pendingActive = nil
        channel.invokeMethod("active", arguments: active)
    }

    private func setupMainMenuIfNeeded(_ app: NSApplication) {
        if app.mainMenu != nil { return }
        let mainMenu = NSMenu()
        let appMenuItem = NSMenuItem()
        mainMenu.addItem(appMenuItem)
        let appMenu = NSMenu()
        let appName = ProcessInfo.processInfo.processName
        appMenu.addItem(withTitle: "Hide \(appName)", action: #selector(NSApplication.hide(_:)), keyEquivalent: "h")
        appMenu.addItem(NSMenuItem.separator())
        appMenu.addItem(withTitle: "Quit \(appName)", action: #selector(NSApplication.terminate(_:)), keyEquivalent: "q")
        appMenuItem.submenu = appMenu
        app.mainMenu = mainMenu
    }
}

private func jstringToSwift(_ env: UnsafeMutablePointer<JNIEnv?>, _ str: jstring) -> String {
    let cString = env.pointee!.pointee.GetStringUTFChars(env, str, nil)
    let result = String(cString: cString!)
    env.pointee?.pointee.ReleaseStringUTFChars(env, str, cString)
    return result
}

@MainActor
private func surfaceFrom(_ context: jlong) -> FlutterSurface? {
    return context != 0 ? unsafeBitCast(UInt(context), to: FlutterSurface.self) : nil
}

// =================================================================================================
// JNI entry points (dev.equo.swt.FlutterNative). One set of functions for both surface kinds.
// =================================================================================================

@MainActor @_cdecl("Java_dev_equo_swt_FlutterNative_Initialize")
public func FlutterNative_initialize(env: UnsafeMutablePointer<JNIEnv?>, cls: jclass, port: jint, parent: jlong, widget_id: jlong, widget_name: jstring, theme: jstring, background_color: jint, parent_background_color: jint, width: jint, height: jint, csd_enabled: jboolean) -> jlong {
    let name = jstringToSwift(env, widget_name)
    let themeString = jstringToSwift(env, theme)
    let surface: FlutterSurface
    if width > 0 && height > 0 {
        let c = FlutterDisplayWindowController()
        c.initialize(port: port, displayId: Int64(widget_id), widgetName: name, theme: themeString, backgroundColor: background_color, width: width, height: height, csdEnabled: csd_enabled == 1)
        surface = c
    } else {
        let parentView = parent != 0 ? unsafeBitCast(UInt(parent), to: NSView.self) : nil
        let c = FlutterBridgeController()
        _ = c.initialize(parentView: parentView, port: port, widgetId: Int64(widget_id), widgetName: name, theme: themeString, backgroundColor: background_color, parentBackgroundColor: parent_background_color)
        surface = c
    }
    return jlong(Int(bitPattern: Unmanaged.passRetained(surface).toOpaque()))
}

@MainActor @_cdecl("Java_dev_equo_swt_FlutterNative_GetView")
public func FlutterNative_getView(env: UnsafeMutablePointer<JNIEnv?>, cls: jclass, context: jlong) -> jlong {
    if let view = surfaceFrom(context)?.getView() {
        return jlong(Int(bitPattern: Unmanaged.passRetained(view).toOpaque()))
    }
    return 0
}

@MainActor @_cdecl("Java_dev_equo_swt_FlutterNative_Dispose")
public func FlutterNative_dispose(env: UnsafeMutablePointer<JNIEnv?>, cls: jclass, context: jlong) {
    guard context != 0 else { return }
    surfaceFrom(context)?.dispose()
    Unmanaged<FlutterSurface>.fromOpaque(UnsafeMutableRawPointer(bitPattern: UInt(context))!).release()
}

@MainActor @_cdecl("Java_dev_equo_swt_FlutterNative_SetBounds")
public func FlutterNative_setBounds(env: UnsafeMutablePointer<JNIEnv?>, cls: jclass, context: jlong, x: jint, y: jint, width: jint, height: jint, vx: jint, vy: jint, vwidth: jint, vheight: jint) {
    surfaceFrom(context)?.setBounds(x: x, y: y, w: width, h: height, vx: vx, vy: vy, vw: vwidth, vh: vheight)
}

@MainActor @_cdecl("Java_dev_equo_swt_FlutterNative_Pump")
public func FlutterNative_pump(env: UnsafeMutablePointer<JNIEnv?>, cls: jclass, context: jlong) -> jint {
    return jint(surfaceFrom(context)?.pump() ?? 0)
}

@MainActor @_cdecl("Java_dev_equo_swt_FlutterNative_WaitEvents")
public func FlutterNative_waitEvents(env: UnsafeMutablePointer<JNIEnv?>, cls: jclass, context: jlong, millis: jint) {
    surfaceFrom(context)?.waitForEvent(millis: millis)
}

/// Wakes the main run loop so a `waitForEvent` in progress returns now. Called from a non-UI thread
/// (the comm thread posting an SWT `asyncExec`), hence no `@MainActor`: it only hops to the main
/// queue, which is what breaks the wait. `nextEvent` needs an event to hand back, so the hop posts
/// one; a pump that already consumed the wake dispatches it harmlessly to no window.
@_cdecl("Java_dev_equo_swt_FlutterNative_Wake")
public func FlutterNative_wake(env: UnsafeMutablePointer<JNIEnv?>, cls: jclass, context: jlong) {
    DispatchQueue.main.async {
        guard let wakeEvent = NSEvent.otherEvent(with: .applicationDefined,
                                                 location: .zero,
                                                 modifierFlags: [],
                                                 timestamp: ProcessInfo.processInfo.systemUptime,
                                                 windowNumber: 0,
                                                 context: nil,
                                                 subtype: 0,
                                                 data1: 0,
                                                 data2: 0) else { return }
        NSApp.postEvent(wakeEvent, atStart: true)
    }
}

@MainActor @_cdecl("Java_dev_equo_swt_FlutterNative_SetTitle")
public func FlutterNative_setTitle(env: UnsafeMutablePointer<JNIEnv?>, cls: jclass, context: jlong, title: jstring) {
    surfaceFrom(context)?.setTitle(jstringToSwift(env, title))
}

@MainActor @_cdecl("Java_dev_equo_swt_FlutterNative_SetState")
public func FlutterNative_setState(env: UnsafeMutablePointer<JNIEnv?>, cls: jclass, context: jlong, state: jint) {
    surfaceFrom(context)?.setState(state)
}

@MainActor @_cdecl("Java_dev_equo_swt_FlutterNative_GetOrigin")
public func FlutterNative_getOrigin(env: UnsafeMutablePointer<JNIEnv?>, cls: jclass, context: jlong) -> jlong {
    guard let o = surfaceFrom(context)?.origin() else { return jlong(Int64.min) }
    return (jlong(o.0) << 32) | jlong(UInt32(bitPattern: o.1))
}

@MainActor @_cdecl("Java_dev_equo_swt_FlutterNative_SetVisible")
public func FlutterNative_setVisible(env: UnsafeMutablePointer<JNIEnv?>, cls: jclass, context: jlong, visible: jboolean) {
    surfaceFrom(context)?.setVisible(visible != 0)
}

// Stores the external bundle base so bundleBase() points the engine at it.
@_cdecl("Java_dev_equo_swt_FlutterNative_SetBundleDir")
public func FlutterNative_setBundleDir(env: UnsafeMutablePointer<JNIEnv?>, cls: jclass, dir: jstring?) {
    guard let dir = dir else { gBundleOverride = nil; return }
    let s = jstringToSwift(env, dir)
    gBundleOverride = s.isEmpty ? nil : s
}

// Spin the main run loop briefly so the Flutter engine can make progress while a caller busy-waits
// for a response (e.g. the size-test harness). Under Flutter 3.35's merged platform/UI thread the
// engine's UI work runs on the platform (main) run loop, so a thread that blocks without servicing
// that run loop never lets a frame render. In the real product the SWT event loop already drives the
// run loop; this matters only for callers that block the main thread themselves.
@_cdecl("Java_dev_equo_swt_FlutterNative_PumpMessages")
public func FlutterNative_pumpMessages(env: UnsafeMutablePointer<JNIEnv?>, cls: jclass, maxMessages: jint) -> jint {
    RunLoop.current.run(mode: .default, before: Date(timeIntervalSinceNow: 0.002))
    return 0
}

// External app-bundle base set from Java (dev.equo.ewt.bundleDir). When set it overrides
// getDylibDirectory() so the bridge boots an App.framework it does not sit beside. Callers
// append "/Frameworks/App.framework", so this must be the ".../swtflutter.app/Contents" dir.
// nil selects getDylibDirectory().
private var gBundleOverride: String? = nil

// Bundle base for the App.framework: the external override when set, else the dylib's own dir.
func bundleBase() -> String? {
    return gBundleOverride ?? getDylibDirectory()
}

func getDylibDirectory() -> String? {
    var info = Dl_info()
    guard dladdr(#dsohandle, &info) != 0,
          let path = info.dli_fname else {
        return nil
    }
    let dylibPath = String(cString: path)
    return URL(fileURLWithPath: dylibPath).deletingLastPathComponent().path
}

class FlippedView: NSView {
    private static var mouseMonitor: Any?
    private static var activeViews = NSHashTable<FlippedView>.weakObjects()

    override var isFlipped: Bool {
        return true
    }

    override func viewDidMoveToWindow() {
        super.viewDidMoveToWindow()
        if self.window != nil {
            FlippedView.activeViews.add(self)
            FlippedView.setupMonitorIfNeeded()
        } else {
            FlippedView.activeViews.remove(self)
            FlippedView.teardownMonitorIfEmpty()
        }
    }

    private static func setupMonitorIfNeeded() {
        guard mouseMonitor == nil else { return }
        mouseMonitor = NSEvent.addLocalMonitorForEvents(matching: .leftMouseDown) { event in
            guard let window = event.window else { return event }
            for flippedView in FlippedView.activeViews.allObjects {
                guard flippedView.window === window,
                      !flippedView.isHiddenOrHasHiddenAncestor,
                      let flutterView = flippedView.subviews.first,
                      !flutterView.isHidden else { continue }
                let locationInView = flippedView.convert(event.locationInWindow, from: nil)
                if flippedView.bounds.contains(locationInView) {
                    window.makeFirstResponder(flutterView)
                    break
                }
            }
            return event
        }
    }

    private static func teardownMonitorIfEmpty() {
        guard activeViews.allObjects.isEmpty, let monitor = mouseMonitor else { return }
        NSEvent.removeMonitor(monitor)
        mouseMonitor = nil
    }

    deinit {
        FlippedView.activeViews.remove(self)
        FlippedView.teardownMonitorIfEmpty()
    }
}
