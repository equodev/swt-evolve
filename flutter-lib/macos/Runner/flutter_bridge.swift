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

    func initialize(port: Int32, displayId: Int64, widgetName: String, theme: String, backgroundColor: Int32, width: Int32, height: Int32) {
        print("FlutterDisplayWindowController.initialize port:\(port) id:\(displayId) name:\(widgetName) \(width)x\(height)")

        let app = NSApplication.shared
        app.setActivationPolicy(.regular)
        setupMainMenuIfNeeded(app)
        // Report the app as "running" (see the note at the top) so embedded AWT/Swing coexists with
        // our hand-driven pump() instead of hijacking the main thread with its own [NSApp run].
        installIsRunningOverride()

        // Same Flutter bootstrap as the embedded path: load the precompiled Dart bundle next to the
        // dylib and pass [port, id, name, theme, bg, parentBg] so main() connects to the comm port.
        let bg = String(backgroundColor)
        let arguments = [String(port), String(displayId), widgetName, theme, bg, bg]
        let frameworkPath = bundleBase()! + "/Frameworks/App.framework"
        let project = FlutterDartProject(precompiledDartBundle: Bundle(path: frameworkPath))
        project.dartEntrypointArguments = arguments
        let fvc = FlutterViewController(project: project)
        self.flutterViewController = fvc

        let win = NSWindow(
            contentRect: NSRect(x: 0, y: 0, width: CGFloat(width), height: CGFloat(height)),
            styleMask: [.titled, .closable, .miniaturizable, .resizable],
            backing: .buffered,
            defer: false)
        win.title = widgetName
        // Match the window chrome to the Flutter background so there is no black flash before the
        // first frame is rendered.
        win.backgroundColor = NSColor(
            red: CGFloat((backgroundColor >> 16) & 0xFF) / 255.0,
            green: CGFloat((backgroundColor >> 8) & 0xFF) / 255.0,
            blue: CGFloat(backgroundColor & 0xFF) / 255.0,
            alpha: 1.0)
        win.contentViewController = fvc
        win.delegate = self
        win.center()
        win.isReleasedWhenClosed = false
        self.window = win

        RegisterGeneratedPlugins(registry: fvc)

        // Bootstrap the app without entering the modal run loop (so this JNI call returns to Java).
        // The SWT event loop then services Cocoa via pump().
        app.finishLaunching()
        // From here we service the AppKit loop ourselves via pump(); treat the app as running.
        deskAppConsideredRunning = true
        win.makeKeyAndOrderFront(nil)
        app.activate(ignoringOtherApps: true)
    }

    /// Drains all pending native events, then spins the run loop briefly. Returns a negative value
    /// once the window has been closed.
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
        return closed ? -1 : 0
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
        win.setContentSize(NSSize(width: CGFloat(w), height: CGFloat(h)))
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

    override func dispose() {
        flutterViewController?.engine.shutDownEngine()
        window?.delegate = nil
        window?.close()
        window = nil
        flutterViewController = nil
    }

    func windowWillClose(_ notification: Notification) {
        closed = true
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
public func FlutterNative_initialize(env: UnsafeMutablePointer<JNIEnv?>, cls: jclass, port: jint, parent: jlong, widget_id: jlong, widget_name: jstring, theme: jstring, background_color: jint, parent_background_color: jint, width: jint, height: jint) -> jlong {
    let name = jstringToSwift(env, widget_name)
    let themeString = jstringToSwift(env, theme)
    let surface: FlutterSurface
    if width > 0 && height > 0 {
        let c = FlutterDisplayWindowController()
        c.initialize(port: port, displayId: Int64(widget_id), widgetName: name, theme: themeString, backgroundColor: background_color, width: width, height: height)
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

@MainActor @_cdecl("Java_dev_equo_swt_FlutterNative_SetTitle")
public func FlutterNative_setTitle(env: UnsafeMutablePointer<JNIEnv?>, cls: jclass, context: jlong, title: jstring) {
    surfaceFrom(context)?.setTitle(jstringToSwift(env, title))
}

@MainActor @_cdecl("Java_dev_equo_swt_FlutterNative_SetState")
public func FlutterNative_setState(env: UnsafeMutablePointer<JNIEnv?>, cls: jclass, context: jlong, state: jint) {
    surfaceFrom(context)?.setState(state)
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
