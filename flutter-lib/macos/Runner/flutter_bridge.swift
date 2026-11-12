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
import OpenGL.GL

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

/// Steps of the native side's start, in microseconds since the epoch (the clock the Java and Dart
/// sides of the boot trace read), answered on the window channel's `bootTrace`. Only the first
/// window's start is recorded, so the list stays bounded however many windows the app opens.
private var bootSteps: [[String: Any]] = []
private var bootRecording = true

private func bootNowMicros() -> Int64 {
    return Int64(Date().timeIntervalSince1970 * 1_000_000)
}

@MainActor @discardableResult
private func bootStep<T>(_ name: String, _ body: @MainActor () -> T) -> T {
    let start = bootNowMicros()
    let value = body()
    if bootRecording {
        bootSteps.append(["name": name, "depth": 1, "startUs": start, "endUs": bootNowMicros()])
    }
    return value
}

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

// =================================================================================================
// Lookups the Flutter engine repeats for every paragraph it builds.
//
// Each paragraph builder starts from the engine's default text style, which reads its font family
// from +[NSFont systemFontOfSize:] behind an OS version check. AppKit works both answers out anew on
// every call -- the version check parses the system version string each time -- and with many
// labels changing at once that is a measurable share of the frame. Both answers are fixed for the
// life of the process, so the first one is kept and handed back to every caller.
// =================================================================================================
private var engineLookupsCached = false

@MainActor
private func cacheEngineLookups() {
    if engineLookupsCached { return }
    engineLookupsCached = true
    cacheOperatingSystemVersionCheck()
    cacheSystemFonts()
}

/// Answers kept by key, safe to read from any thread. The original is called outside the lock, as it
/// may itself call back into the cached method.
private final class KeptAnswers<Key: Hashable, Value> {
    private var answers = [Key: Value]()
    private let lock = NSLock()

    func answer(_ key: Key, _ compute: () -> Value) -> Value {
        lock.lock()
        let kept = answers[key]
        lock.unlock()
        if let kept { return kept }
        let value = compute()
        lock.lock()
        answers[key] = value
        lock.unlock()
        return value
    }
}

private func cacheOperatingSystemVersionCheck() {
    let info = ProcessInfo.processInfo
    let sel = #selector(ProcessInfo.isOperatingSystemAtLeast(_:))
    // The method of the class the shared instance actually is, which overrides NSProcessInfo's.
    guard let cls = object_getClass(info), let method = class_getInstanceMethod(cls, sel) else { return }
    typealias CheckFn = @convention(c) (AnyObject, Selector, OperatingSystemVersion) -> Bool
    let original = unsafeBitCast(method_getImplementation(method), to: CheckFn.self)
    let kept = KeptAnswers<Int, Bool>()
    let override: @convention(block) (AnyObject, OperatingSystemVersion) -> Bool = { receiver, version in
        kept.answer(version.majorVersion << 40 | version.minorVersion << 20 | version.patchVersion) {
            original(receiver, sel, version)
        }
    }
    method_setImplementation(method, imp_implementationWithBlock(override))
}

private func cacheSystemFonts() {
    let sel = #selector(NSFont.systemFont(ofSize:))
    guard let method = class_getClassMethod(NSFont.self, sel) else { return }
    typealias FontFn = @convention(c) (AnyObject, Selector, CGFloat) -> NSFont
    let original = unsafeBitCast(method_getImplementation(method), to: FontFn.self)
    let kept = KeptAnswers<CGFloat, NSFont>()
    let override: @convention(block) (AnyObject, CGFloat) -> NSFont = { receiver, size in
        kept.answer(size) { original(receiver, sel, size) }
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
    func setOwner(_ owner: FlutterSurface?) {}
    func origin() -> (Int32, Int32)? { return nil }
    func isActive() -> Bool { return false }
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
        let fvc = LifecycleFlutterViewController(project: project)
        flutterViewController = fvc
        RegisterGeneratedPlugins(registry: fvc)

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
                fvc.completeLifecycle()
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
            fvc.completeLifecycle()

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

    /// The window this one is a child of, as native SWT makes a child shell's window a child window
    /// of its parent's: it moves with it, stays above it and is miniaturized with it.
    private weak var ownerWindow: NSWindow?

    func initialize(port: Int32, owner: NSWindow?, displayId: Int64, widgetName: String, theme: String, backgroundColor: Int32, width: Int32, height: Int32, csdEnabled: Bool) {
        print("FlutterDisplayWindowController.initialize port:\(port) id:\(displayId) name:\(widgetName) \(width)x\(height)")

        let app = bootStep("NSApplication.shared") { NSApplication.shared }
        // Application-level bootstrap, once per process rather than once per window. A second window
        // must not repeat it: -finishLaunching is documented as a one-time call, and running it again
        // re-posts the launch notifications and re-runs AppKit's launch bookkeeping. Doing that while
        // AppKit is mid-dispatch -- which is exactly where a window opened from a click is created --
        // left the new window's engine unattached, so the in-flight pointer event was delivered to an
        // invalid engine handle and the window never came up. Opening the same shell from a timer
        // never showed it, because there is no event being dispatched then.
        let firstWindow = !deskAppConsideredRunning
        if firstWindow {
            bootStep("NSApp activation policy") { app.setActivationPolicy(.regular) }
            bootStep("main menu") { setupMainMenuIfNeeded(app) }
            // Report the app as "running" (see the note at the top) so embedded AWT/Swing coexists
            // with our hand-driven pump() instead of hijacking the main thread with its own [NSApp run].
            installIsRunningOverride()
        }

        // Same Flutter bootstrap as the embedded path: load the precompiled Dart bundle next to the
        // dylib and pass [port, id, name, theme, bg, parentBg] so main() connects to the comm port.
        let bg = String(backgroundColor)
        let arguments = [String(port), String(displayId), widgetName, theme, bg, bg]
        let frameworkPath = bundleBase()! + "/Frameworks/App.framework"
        let project = bootStep("FlutterDartProject") { FlutterDartProject(precompiledDartBundle: Bundle(path: frameworkPath)) }
        project.dartEntrypointArguments = arguments
        let fvc = bootStep("FlutterViewController (engine start)") { LifecycleFlutterViewController(project: project) }
        self.flutterViewController = fvc
        let windowStart = bootNowMicros()

        // .fullSizeContentView only when Flutter draws the title bar: it extends the content under
        // the title bar area, which is what CSD needs and what would otherwise leave the system
        // title bar covering the top of the app.
        var styleMask: NSWindow.StyleMask = [.titled, .closable, .miniaturizable, .resizable]
        if csdEnabled {
            styleMask.insert(.fullSizeContentView)
        }
        let win = FlutterDisplayWindow(
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
        NotificationCenter.default.addObserver(self, selector: #selector(screenParametersChanged(_:)),
                                               name: NSApplication.didChangeScreenParametersNotification, object: nil)
        win.center()
        win.isReleasedWhenClosed = false
        self.window = win
        self.ownerWindow = owner
        if bootRecording {
            bootSteps.append(["name": "NSWindow", "depth": 1, "startUs": windowStart, "endUs": bootNowMicros()])
        }

        bootStep("RegisterGeneratedPlugins") { RegisterGeneratedPlugins(registry: fvc) }
        setupWindowChannel(fvc)

        if firstWindow {
            // Bootstrap the app without entering the modal run loop (so this JNI call returns to
            // Java). The SWT event loop then services Cocoa via pump().
            bootStep("finishLaunching") { app.finishLaunching() }
            // From here we service the AppKit loop ourselves via pump(); treat the app as running.
            deskAppConsideredRunning = true
            bootStep("makeKeyAndOrderFront") { win.makeKeyAndOrderFront(nil) }
            // Only the first window takes the application forward. Repeating it for every window
            // pulls activation out from under whatever is being dispatched at the time.
            bootStep("activate") { app.activate(ignoringOtherApps: true) }
        } else {
            win.makeKeyAndOrderFront(nil)
        }
        bootRecording = false
        fvc.completeLifecycle()
        attachToOwner()
    }

    /// Cocoa only keeps a child window while both are on screen, so, as native SWT's
    /// Shell.updateParent does, it is attached when either one is shown and detached when either one
    /// is hidden; a hidden owner leaves the window unattached.
    private func attachToOwner() {
        guard let win = window, win.isVisible, let owner = ownerWindow, owner.isVisible else { return }
        owner.addChildWindow(win, ordered: .above)
    }

    private func detachFromOwner() {
        guard let win = window, let owner = win.parent else { return }
        owner.removeChildWindow(win)
    }

    /// The windows this one owns, found through their delegate rather than `childWindows`, which
    /// leaves out the ones not attached while this one was hidden.
    private func ownedControllers() -> [FlutterDisplayWindowController] {
        guard let win = window else { return [] }
        return NSApp.windows.compactMap { $0.delegate as? FlutterDisplayWindowController }
            .filter { $0.ownerWindow === win }
    }

    override func setOwner(_ owner: FlutterSurface?) {
        let ownerWin = owner?.getView()?.window
        if ownerWin === ownerWindow { return }
        detachFromOwner()
        ownerWindow = ownerWin
        attachToOwner()
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
            // A wake has done its job by ending the wait. Dispatching it would open a Core Animation
            // transaction that the run loop then commits to the window server, for nothing.
            if isWakeEvent(event) { continue }
            NSApp.sendEvent(event)
        }
        // Under Flutter 3.35's merged platform/UI thread the engine's UI work (frame scheduling,
        // post-frame callbacks — including the one that sends ClientReady) runs on this main run
        // loop. The non-blocking nextEvent drain above doesn't give it time, so a busy SWT
        // readAndDispatch loop would never let a frame render. One pass over the run loop runs
        // whatever the engine has ready; waiting in it would hold every paint that follows the pump,
        // and an idle SWT loop already waits inside the OS loop (waitForEvent).
        CFRunLoopRunInMode(CFRunLoopMode.defaultMode, 0, false)
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
        // Size first: the origin below assumes the final content height, and AppKit clamps a frame
        // that would overflow the screen, so placing the still-large initial window first leaves it
        // pinned under the menu bar instead of at (x, y). A caller with no size yet passes 0: move
        // the window, leave its size alone. Resizing to a pixel here is what left the application
        // with an invisible window it could never grow out of, because a window that small reports
        // no viewport for the handshake to correct.
        if w > 0 && h > 0 {
            win.setContentSize(NSSize(width: CGFloat(w), height: CGFloat(h)))
        }
        // SWT uses a top-left origin on the primary screen; Cocoa a bottom-left one on that same
        // screen, whatever screen the window ends up on. Flipping against any other screen (the
        // window's, or the key window's) lands the frame off-screen and AppKit clamps it under the
        // menu bar.
        if let primary = NSScreen.screens.first {
            let origin = NSPoint(x: CGFloat(x), y: primary.frame.height - CGFloat(y) - CGFloat(h))
            win.setFrameOrigin(origin)
        }
    }


    /// Whether this window is key, i.e. the one AppKit sends keyboard input to. The same fact the
    /// traffic lights mirror through `windowDidBecomeKey`, asked for directly.
    override func isActive() -> Bool {
        return window?.isKeyWindow ?? false
    }

    /// Where the window's CONTENT starts on screen, in the same top-left space setBounds takes.
    /// Content rather than frame, so the title bar is already accounted for and a caller converting
    /// a window-local point needs no further correction. Flips against the primary screen, the one
    /// setBounds places against, so the two round-trip; both share that call's multi-monitor caveat.
    ///
    /// The SWT loop asks on every pump, so the answer is kept until the window moves, resizes or
    /// changes screen, or the screens are rearranged.
    override func origin() -> (Int32, Int32)? {
        if let kept = keptOrigin { return kept }
        guard let win = window, let screen = NSScreen.screens.first else { return nil }
        let content = win.contentRect(forFrameRect: win.frame)
        let top = screen.frame.height - (content.origin.y + content.height)
        let origin = (Int32(content.origin.x.rounded()), Int32(top.rounded()))
        keptOrigin = origin
        return origin
    }

    private var keptOrigin: (Int32, Int32)?

    func windowDidMove(_ notification: Notification) {
        keptOrigin = nil
    }

    func windowDidResize(_ notification: Notification) {
        keptOrigin = nil
    }

    func windowDidChangeScreen(_ notification: Notification) {
        keptOrigin = nil
    }

    @objc private func screenParametersChanged(_ notification: Notification) {
        keptOrigin = nil
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
            attachToOwner()
            ownedControllers().forEach { $0.attachToOwner() }
        } else {
            ownedControllers().forEach { $0.detachFromOwner() }
            detachFromOwner()
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
                if call.method == "bootTrace" {
                    result(bootSteps)
                    return
                }
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
        detachFromOwner()
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

    /// A window shown while this one was miniaturized could not be attached then.
    func windowDidDeminiaturize(_ notification: Notification) {
        ownedControllers().forEach { $0.attachToOwner() }
    }

    // Key state drives two things in Dart: the traffic lights, which grey out when the window is not
    // key (csdWindowActive, see equo_window_stub.installWindowStateListeners), and the engine's app
    // lifecycle, see deliverActive.
    func windowDidBecomeKey(_ notification: Notification) {
        notifyActive(true)
    }

    func windowDidResignKey(_ notification: Notification) {
        notifyActive(false)
    }

    // Full screen can be entered or left without SWT asking (the OS, accessibility tools), so the
    // window reports every change and SWT's Shell.getFullScreen() follows it.
    func windowDidEnterFullScreen(_ notification: Notification) {
        windowChannel?.invokeMethod("fullScreen", arguments: true)
    }

    func windowDidExitFullScreen(_ notification: Notification) {
        windowChannel?.invokeMethod("fullScreen", arguments: false)
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
        guard windowChannel != nil, hasPumped else {
            pendingActive = active
            return
        }
        pendingActive = nil
        deliverActive(active)
    }

    /// The engine learns the app lifecycle only from an `NSApp.delegate` that is a
    /// `FlutterAppLifecycleProvider`, and nothing installs one here: this process bootstraps NSApp
    /// itself, and an embedding host (AWT) may own the delegate. Without it the lifecycle stays
    /// `resumed`, the FocusManager never parks the focus, and the shell never sends Deactivate.
    /// Each window has its own engine, so its key state is forwarded to that engine directly.
    private func deliverActive(_ active: Bool) {
        windowChannel?.invokeMethod("active", arguments: active)
        guard let engine = flutterViewController?.engine as FlutterAppLifecycleDelegate? else { return }
        let notification = Notification(name: active ? NSApplication.willBecomeActiveNotification
                                                      : NSApplication.willResignActiveNotification)
        if active {
            engine.handleWillBecomeActive?(notification)
        } else {
            engine.handleWillResignActive?(notification)
        }
    }

    /// Whether this window's engine has had a turn of the run loop, which is when it starts. There
    /// is no engine flag to read for this on macOS, and the first `pump()` is exactly the moment.
    private var hasPumped = false

    /// Sends whatever `notifyActive` had to hold back, once the engine is up.
    private func flushPendingActive() {
        guard let active = pendingActive, windowChannel != nil else { return }
        pendingActive = nil
        deliverActive(active)
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
public func FlutterNative_initialize(env: UnsafeMutablePointer<JNIEnv?>, cls: jclass, port: jint, parent: jlong, widget_id: jlong, widget_name: jstring, theme: jstring, background_color: jint, parent_background_color: jint, width: jint, height: jint, csd_enabled: jboolean, impeller_enabled: jboolean) -> jlong {
    // impeller_enabled is the Windows engine's renderer choice; the macOS engine always renders with Impeller.
    cacheEngineLookups()
    let name = jstringToSwift(env, widget_name)
    let themeString = jstringToSwift(env, theme)
    let surface: FlutterSurface
    if width > 0 && height > 0 {
        let c = FlutterDisplayWindowController()
        // For a window, |parent| is the surface of the window that owns this one, or 0 for none.
        let owner = surfaceFrom(parent)?.getView()?.window
        c.initialize(port: port, owner: owner, displayId: Int64(widget_id), widgetName: name, theme: themeString, backgroundColor: background_color, width: width, height: height, csdEnabled: csd_enabled == 1)
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

/// The subtype that marks the events `FlutterNative_wake` posts, so the pump can drop them.
private let wakeEventSubtype: Int16 = 0x5741

private func isWakeEvent(_ event: NSEvent) -> Bool {
    return event.type == .applicationDefined && event.subtype.rawValue == wakeEventSubtype
        && event.windowNumber == 0
}

/// Wakes the main run loop so a `waitForEvent` in progress returns now. Called from a non-UI thread
/// (the comm thread posting an SWT `asyncExec`), hence no `@MainActor`: it only hops to the main
/// queue, which is what breaks the wait. `nextEvent` needs an event to hand back, so the hop posts
/// one; a pump that already consumed the wake dispatches it harmlessly to no window.
@_cdecl("Java_dev_equo_swt_FlutterNative_Wake")
public func FlutterNative_wake(env: UnsafeMutablePointer<JNIEnv?>, cls: jclass, context: jlong) {
    DispatchQueue.main.async {
        // No application yet, no event loop to wake.
        guard let app = NSApp,
              let wakeEvent = NSEvent.otherEvent(with: .applicationDefined,
                                                 location: .zero,
                                                 modifierFlags: [],
                                                 timestamp: ProcessInfo.processInfo.systemUptime,
                                                 windowNumber: 0,
                                                 context: nil,
                                                 subtype: wakeEventSubtype,
                                                 data1: 0,
                                                 data2: 0) else { return }
        app.postEvent(wakeEvent, atStart: true)
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

@MainActor @_cdecl("Java_dev_equo_swt_FlutterNative_IsActive")
public func FlutterNative_isActive(env: UnsafeMutablePointer<JNIEnv?>, cls: jclass, context: jlong) -> jboolean {
    return (surfaceFrom(context)?.isActive() ?? false) ? jboolean(1) : jboolean(0)
}

// The work area, and reading or moving the pointer, are asked of the Windows runner only; here they
// answer FlutterNative's "unknown".
@_cdecl("Java_dev_equo_swt_FlutterNative_GetWorkAreaTop")
public func FlutterNative_getWorkAreaTop(env: UnsafeMutablePointer<JNIEnv?>, cls: jclass, context: jlong, x: jint, y: jint, width: jint, height: jint) -> jint {
    return jint(Int32.min)
}

@_cdecl("Java_dev_equo_swt_FlutterNative_GetCursorLocation")
public func FlutterNative_getCursorLocation(env: UnsafeMutablePointer<JNIEnv?>, cls: jclass) -> jlong {
    return jlong(Int64.min)
}

@_cdecl("Java_dev_equo_swt_FlutterNative_SetCursorLocation")
public func FlutterNative_setCursorLocation(env: UnsafeMutablePointer<JNIEnv?>, cls: jclass, x: jint, y: jint) -> jboolean {
    return jboolean(0)
}

@MainActor @_cdecl("Java_dev_equo_swt_FlutterNative_SetVisible")
public func FlutterNative_setVisible(env: UnsafeMutablePointer<JNIEnv?>, cls: jclass, context: jlong, visible: jboolean) {
    surfaceFrom(context)?.setVisible(visible != 0)
}

@MainActor @_cdecl("Java_dev_equo_swt_FlutterNative_SetOwner")
public func FlutterNative_setOwner(env: UnsafeMutablePointer<JNIEnv?>, cls: jclass, context: jlong, owner: jlong) {
    surfaceFrom(context)?.setOwner(surfaceFrom(owner))
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

/// A FlutterViewController whose viewDidLoad and viewWillAppear run under any host. AppKit sends
/// them only to apps linked against the macOS 10.10 SDK or later, and FlutterViewController sets up
/// its vsync source in the first and starts its engine in the second: under a host built against an
/// older SDK (Java 8's launcher is) the window would stay black, or never repaint after its first
/// frame, and every message to the Dart side would be dropped.
final class LifecycleFlutterViewController: FlutterViewController {

    private var didLoad = false
    private var willAppear = false

    /// Whether the host never sent viewDidLoad: it was linked against an SDK older than 10.10.
    private(set) var hostSkipsLifecycle = false

    override func viewDidLoad() {
        super.viewDidLoad()
        didLoad = true
    }

    override func viewWillAppear() {
        super.viewWillAppear()
        willAppear = true
    }

    /// Runs what AppKit did not, once the view is in its window; FlutterViewController's
    /// viewWillAppear starts an engine that is not running. Such a host does not put a view
    /// controller in its view's responder chain either, and FlutterViewController takes the view's
    /// key events as its next responder, so it is put there.
    func completeLifecycle() {
        if !isViewLoaded { _ = view }
        if !didLoad { hostSkipsLifecycle = true }
        if view.nextResponder !== self {
            nextResponder = view.nextResponder
            view.nextResponder = self
        }
        if !didLoad { viewDidLoad() }
        if !willAppear { viewWillAppear() }
    }
}

/// The window of a desktop-mode Flutter view. A Command key-down nothing handled comes back up the
/// responder chain to the window's keyDown:, which offers it as a key equivalent to the content
/// view again. An engine that answers at once instead of asynchronously (one that is not running)
/// sends it straight back up, and that cycle overflows the stack. Offering an event once is enough,
/// so a keyDown: re-entered with the event it is already handling returns.
final class FlutterDisplayWindow: NSWindow {

    private var keyDownInFlight: NSEvent?

    override func keyDown(with event: NSEvent) {
        if let inFlight = keyDownInFlight, inFlight === event || (inFlight.timestamp == event.timestamp
            && inFlight.keyCode == event.keyCode && inFlight.type == event.type) {
            return
        }
        let outer = keyDownInFlight
        keyDownInFlight = event
        defer { keyDownInFlight = outer }
        super.keyDown(with: event)
    }

    /// Under a host linked against an old enough SDK, a view's default rightMouseDown: takes the
    /// press as a contextual-menu request when the window carries a menu (an application can give
    /// it one through the native window it reaches), so the press and its release never reach the
    /// Flutter view controller and the engine sees a right drag as plain moves. Context menus are
    /// SWT's own (MenuDetect), so right and other-button presses on the engine's views go to the
    /// controller directly, and so do the drags and the release of a press sent there, wherever
    /// the pointer is by then.
    override func sendEvent(_ event: NSEvent) {
        guard let controller = contentViewController as? LifecycleFlutterViewController,
              controller.hostSkipsLifecycle else {
            super.sendEvent(event)
            return
        }
        switch event.type {
        case .rightMouseDown, .otherMouseDown:
            guard let content = contentView else { break }
            let point = content.superview?.convert(event.locationInWindow, from: nil) ?? event.locationInWindow
            guard isEngineView(content.hitTest(point), under: controller.view) else { break }
            routedButtons.insert(event.buttonNumber)
            if event.type == .rightMouseDown {
                controller.rightMouseDown(with: event)
            } else {
                controller.otherMouseDown(with: event)
            }
            return
        case .rightMouseDragged, .otherMouseDragged:
            guard routedButtons.contains(event.buttonNumber) else { break }
            if event.type == .rightMouseDragged {
                controller.rightMouseDragged(with: event)
            } else {
                controller.otherMouseDragged(with: event)
            }
            return
        case .rightMouseUp, .otherMouseUp:
            guard routedButtons.remove(event.buttonNumber) != nil else { break }
            if event.type == .rightMouseUp {
                controller.rightMouseUp(with: event)
            } else {
                controller.otherMouseUp(with: event)
            }
            return
        default:
            break
        }
        super.sendEvent(event)
    }

    /// The buttons whose press `sendEvent` sent to the controller and that are still down.
    private var routedButtons = Set<Int>()

    /// Whether `view` is the controller's view or one of the engine's own views inside it, rather
    /// than a native view placed over the Flutter content.
    private func isEngineView(_ view: NSView?, under root: NSView) -> Bool {
        var current = view
        while let v = current {
            if v === root { return true }
            if !NSStringFromClass(type(of: v)).hasPrefix("Flutter") { return false }
            current = v.superview
        }
        return false
    }
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

// =================================================================================================
// OpenGL for GLCanvas (dev.equo.swt.GLNative). The client draws the canvas, so its context renders
// into a view that is never shown, and every swap reads the frame back for the client to paint.
// =================================================================================================

private final class GLSurface {
    let context: NSOpenGLContext
    let window: NSWindow
    let doubleBuffer: Bool

    @MainActor
    init(context: NSOpenGLContext, width: Int, height: Int, doubleBuffer: Bool) {
        self.context = context
        self.doubleBuffer = doubleBuffer
        // A window that is never ordered in still gets a drawable, so the context has a default framebuffer.
        window = NSWindow(contentRect: NSRect(x: 0, y: 0, width: max(1, width), height: max(1, height)),
                          styleMask: .borderless, backing: .buffered, defer: false)
        window.isReleasedWhenClosed = false
        let view = NSView(frame: NSRect(x: 0, y: 0, width: max(1, width), height: max(1, height)))
        view.wantsBestResolutionOpenGLSurface = false
        window.contentView = view
        context.view = view
    }
}

private final class MainThreadHop<T>: @unchecked Sendable {
    let lock = NSLock()
    let done = DispatchSemaphore(value: 0)
    var result: T?
    var abandoned = false
}

/// AppKit creates, resizes and frees drawables only on the main thread; the GL calls stay on the caller's.
/// Assumes the launcher's main thread runs its run loop; returns nil, with |body| never run, when it doesn't
/// answer within a few seconds.
private func onMainThread<T>(_ body: @escaping @MainActor () -> T) -> T? {
    if Thread.isMainThread {
        return assumingMainActor(body)
    }
    let hop = MainThreadHop<T>()
    DispatchQueue.main.async {
        hop.lock.lock()
        defer { hop.lock.unlock() }
        guard !hop.abandoned else { return }
        hop.result = assumingMainActor(body)
        hop.done.signal()
    }
    if hop.done.wait(timeout: .now() + 5) == .success {
        return hop.result
    }
    hop.lock.lock()
    defer { hop.lock.unlock() }
    hop.abandoned = hop.result == nil
    return hop.result
}

private func glSurfaceFrom(_ handle: jlong) -> GLSurface? {
    return handle != 0 ? Unmanaged<GLSurface>.fromOpaque(UnsafeMutableRawPointer(bitPattern: UInt(handle))!).takeUnretainedValue() : nil
}

/// The GLData fields in dev.equo.swt.GLNative's order, mapped as SWT's Cocoa GLCanvas maps them.
private func glPixelFormatAttributes(_ d: [jint]) -> [NSOpenGLPixelFormatAttribute] {
    var a: [Int] = []
    if d[0] != 0 { a.append(NSOpenGLPFADoubleBuffer) }
    if d[1] != 0 { a.append(NSOpenGLPFAStereo) }
    let color = Int(d[2] + d[3] + d[4])
    if color > 0 { a += [NSOpenGLPFAColorSize, color] }
    if d[5] > 0 { a += [NSOpenGLPFAAlphaSize, Int(d[5])] }
    if d[6] > 0 { a += [NSOpenGLPFADepthSize, Int(d[6])] }
    if d[7] > 0 { a += [NSOpenGLPFAStencilSize, Int(d[7])] }
    if d[8] + d[9] + d[10] > 0 { a += [NSOpenGLPFAAccumSize, Int(d[8] + d[9] + d[10] + d[11])] }
    if d[12] > 0 { a += [NSOpenGLPFASampleBuffers, Int(d[12])] }
    if d[13] > 0 { a += [NSOpenGLPFASamples, Int(d[13])] }
    return a.map { NSOpenGLPixelFormatAttribute($0) } + [0]
}

/// Returns 0 when no pixel format matches |glData|, -1 when the main thread didn't create the drawable in time.
@_cdecl("Java_dev_equo_swt_GLNative_Create")
public func GLNative_create(env: UnsafeMutablePointer<JNIEnv?>, cls: jclass, glData: jintArray, share: jlong, width: jint, height: jint) -> jlong {
    let jni = env.pointee!.pointee
    var data = [jint](repeating: 0, count: 14)
    jni.GetIntArrayRegion(env, glData, 0, min(jsize(data.count), jni.GetArrayLength(env, glData)), &data)
    guard let format = NSOpenGLPixelFormat(attributes: glPixelFormatAttributes(data)),
          let context = NSOpenGLContext(format: format, share: glSurfaceFrom(share)?.context) else { return 0 }
    guard let surface = onMainThread({
        GLSurface(context: context, width: Int(width), height: Int(height), doubleBuffer: data[0] != 0)
    }) else { return -1 }
    return jlong(Int(bitPattern: Unmanaged.passRetained(surface).toOpaque()))
}

/// Returns false when the main thread didn't resize the drawable in time.
@_cdecl("Java_dev_equo_swt_GLNative_SetSize")
public func GLNative_setSize(env: UnsafeMutablePointer<JNIEnv?>, cls: jclass, handle: jlong, width: jint, height: jint) -> jboolean {
    guard let surface = glSurfaceFrom(handle) else { return 0 }
    let resized: Bool? = onMainThread {
        surface.window.setContentSize(NSSize(width: max(1, Int(width)), height: max(1, Int(height))))
        surface.context.update()
        return true
    }
    return resized != nil ? 1 : 0
}

/// Makes the surface's context current on the calling thread; 0 clears the thread's context.
@_cdecl("Java_dev_equo_swt_GLNative_MakeCurrent")
public func GLNative_makeCurrent(env: UnsafeMutablePointer<JNIEnv?>, cls: jclass, handle: jlong) {
    if let surface = glSurfaceFrom(handle) {
        surface.context.makeCurrentContext()
    } else {
        NSOpenGLContext.clearCurrentContext()
    }
}

@_cdecl("Java_dev_equo_swt_GLNative_IsCurrent")
public func GLNative_isCurrent(env: UnsafeMutablePointer<JNIEnv?>, cls: jclass, handle: jlong) -> jboolean {
    guard let surface = glSurfaceFrom(handle) else { return 0 }
    return NSOpenGLContext.current === surface.context ? 1 : 0
}

/// Reads the frame about to be swapped into |pixels| (width * height 0xAARRGGBB, top row first),
/// then swaps. The application's read framebuffer, read buffer and pixel-pack state are left as they were.
@_cdecl("Java_dev_equo_swt_GLNative_Swap")
public func GLNative_swap(env: UnsafeMutablePointer<JNIEnv?>, cls: jclass, handle: jlong, pixels: jintArray?, width: jint, height: jint) -> jboolean {
    guard let surface = glSurfaceFrom(handle) else { return 0 }
    var read = false
    let jni = env.pointee!.pointee
    if let pixels = pixels, width > 0, height > 0, jni.GetArrayLength(env, pixels) >= width * height {
        let previous = NSOpenGLContext.current
        if previous !== surface.context { surface.context.makeCurrentContext() }
        var readFramebuffer: GLint = 0, readBuffer: GLint = 0, packBuffer: GLint = 0, alignment: GLint = 0
        var rowLength: GLint = 0, skipRows: GLint = 0, skipPixels: GLint = 0
        glGetIntegerv(GLenum(GL_READ_FRAMEBUFFER_BINDING_EXT), &readFramebuffer)
        glBindFramebufferEXT(GLenum(GL_READ_FRAMEBUFFER_EXT), 0)
        glGetIntegerv(GLenum(GL_READ_BUFFER), &readBuffer)
        glGetIntegerv(GLenum(GL_PIXEL_PACK_BUFFER_BINDING), &packBuffer)
        glGetIntegerv(GLenum(GL_PACK_ALIGNMENT), &alignment)
        glGetIntegerv(GLenum(GL_PACK_ROW_LENGTH), &rowLength)
        glGetIntegerv(GLenum(GL_PACK_SKIP_ROWS), &skipRows)
        glGetIntegerv(GLenum(GL_PACK_SKIP_PIXELS), &skipPixels)
        glBindBuffer(GLenum(GL_PIXEL_PACK_BUFFER), 0)
        glPixelStorei(GLenum(GL_PACK_ALIGNMENT), 4)
        glPixelStorei(GLenum(GL_PACK_ROW_LENGTH), 0)
        glPixelStorei(GLenum(GL_PACK_SKIP_ROWS), 0)
        glPixelStorei(GLenum(GL_PACK_SKIP_PIXELS), 0)
        glReadBuffer(GLenum(surface.doubleBuffer ? GL_BACK : GL_FRONT))
        let w = Int(width), h = Int(height)
        var frame = [jint](repeating: 0, count: w * h)
        frame.withUnsafeMutableBytes { bytes in
            glReadPixels(0, 0, width, height, GLenum(GL_BGRA), GLenum(GL_UNSIGNED_INT_8_8_8_8_REV), bytes.baseAddress)
        }
        glReadBuffer(GLenum(readBuffer))
        glPixelStorei(GLenum(GL_PACK_ALIGNMENT), alignment)
        glPixelStorei(GLenum(GL_PACK_ROW_LENGTH), rowLength)
        glPixelStorei(GLenum(GL_PACK_SKIP_ROWS), skipRows)
        glPixelStorei(GLenum(GL_PACK_SKIP_PIXELS), skipPixels)
        glBindBuffer(GLenum(GL_PIXEL_PACK_BUFFER), GLuint(packBuffer))
        glBindFramebufferEXT(GLenum(GL_READ_FRAMEBUFFER_EXT), GLuint(readFramebuffer))
        // GL rows run bottom-up.
        frame.withUnsafeBufferPointer { buffer in
            for row in 0..<h {
                jni.SetIntArrayRegion(env, pixels, jsize(row * w), width, buffer.baseAddress! + (h - 1 - row) * w)
            }
        }
        if previous !== surface.context {
            if let previous = previous { previous.makeCurrentContext() } else { NSOpenGLContext.clearCurrentContext() }
        }
        read = true
    }
    surface.context.flushBuffer()
    return read ? 1 : 0
}

@_cdecl("Java_dev_equo_swt_GLNative_Destroy")
public func GLNative_destroy(env: UnsafeMutablePointer<JNIEnv?>, cls: jclass, handle: jlong) {
    guard handle != 0 else { return }
    let unmanaged = Unmanaged<GLSurface>.fromOpaque(UnsafeMutableRawPointer(bitPattern: UInt(handle))!)
    if unmanaged._withUnsafeGuaranteedRef({ NSOpenGLContext.current === $0.context }) {
        NSOpenGLContext.clearCurrentContext()
    }
    // The last reference goes on the main thread, which frees the window and view; nothing waits for it.
    let free: @MainActor () -> Void = {
        let surface = unmanaged.takeUnretainedValue()
        surface.context.clearDrawable()
        surface.window.close()
        unmanaged.release()
    }
    if Thread.isMainThread {
        assumingMainActor(free)
    } else {
        DispatchQueue.main.async { assumingMainActor(free) }
    }
}
