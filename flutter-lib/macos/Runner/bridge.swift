//
//  bridge.swift
//  FlutterBridge
//
//  Created by Guillermo Zunino on 13/02/2025.
//

import Foundation
import Cocoa
import FlutterMacOS

// Class to hold our Flutter instances
@MainActor
class FlutterBridgeController {

    private var flutterViewController: FlutterViewController?
    private var window: NSWindow? // Keep for standalone window case if needed
    private var view: NSView?

    init() {
        print("FlutterBridgeController.init")
    }

    func initialize(parentView: NSView?, port: Int32, widgetId: Int64, widgetName: String) -> NSView? {
        print("FlutterBridgeController.initialize port:\(port) parent:\(String(describing: parentView)) id:\(widgetId) name:\(widgetName)")

        let arguments = [String(port), String(widgetId), widgetName]
        let frameworkPath = getDylibDirectory()! + "/swtflutter.app/Contents/Frameworks/App.framework"
        let project = FlutterDartProject(precompiledDartBundle: Bundle(path: frameworkPath))
        project.dartEntrypointArguments = arguments
        // Create Flutter view controller
        flutterViewController = FlutterViewController.init(project: project)
//         print("FlutterBridgeController.initialize 2")

        // If parent view is provided, add Flutter view as a subview
        if let parent = parentView {
//             print("FlutterBridgeController.initialize 3")
            let frame = parent.frame

            let container = FlippedView()
            container.frame = frame
//             container.wantsLayer = true
//             container.layer?.backgroundColor = NSColor.red.cgColor
            parent.addSubview(container, positioned: .below, relativeTo: nil) // add it last, otherwise appears first to swt
            self.view = container

            if let flutterView = flutterViewController?.view {
// //                let flutterViewFrame = NSRect(x: 0, y: 0, width: 400, height: 500)
                flutterView.frame = container.frame
//                 flutterView.frame = NSRect(x: frame.origin.x, y: frame.origin.y, width: frame.width, height: frame.height)
//                 flutterView.autoresizingMask = [.width]
//                flutterView.frame = parent.bounds
                flutterView.autoresizingMask = [.width, .height]
                print("FlutterBridgeController.initialize 4 ", parent, flutterView, flutterView.frame)
                container.addSubview(flutterView)
                return flutterView
            }
        } else {
            print("FlutterBridgeController.initialize 5")
            // Create a new window if no parent (fallback case)
            let window = NSWindow(
                contentRect: NSRect(x: 0, y: 0, width: 800, height: 600),
                styleMask: [.titled, .closable, .miniaturizable, .resizable],
                backing: .buffered,
                defer: false)
            
            window.contentViewController = flutterViewController
            window.center()
            window.makeKeyAndOrderFront(nil)
            
            self.window = window
        }
        RegisterGeneratedPlugins(registry: flutterViewController!)
        return nil
    }

    func getView() -> NSView? {
//         return flutterViewController?.view
        return view
    }

    func setFrame(x: Int32, y: Int32, w: Int32, h: Int32, vx: Int32, vy: Int32, vw: Int32, vh: Int32) {
        if let v = view {
            v.frame = NSRect(x: CGFloat(x), y: CGFloat(y), width: CGFloat(w), height: CGFloat(h))
            v.autoresizingMask = []
        }
        if (x == vx && y == vy && w == vw && h == vh) {
            return
        }
        if let flutterView = flutterViewController?.view {
            flutterView.frame = NSRect(x: CGFloat(vx), y: CGFloat(vy), width: CGFloat(vw), height: CGFloat(vh))
            flutterView.autoresizingMask = []
        }
    }

    func destroy() {
        flutterViewController?.view.removeFromSuperview()
        flutterViewController?.engine.shutDownEngine()
        flutterViewController = nil
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

// JNI bridge function
@MainActor @_cdecl("Java_org_eclipse_swt_widgets_SwtFlutterBridge_InitializeFlutterWindow")
public func InitializeFlutterWindow(env: UnsafeMutablePointer<JNIEnv?>, cls: jclass, port: jint, parent: jlong, widget_id: jlong, widget_name: jstring) -> jlong {
    let parentView = parent != 0 ? unsafeBitCast(UInt(parent), to: NSView.self) : nil
    let cString = env.pointee!.pointee.GetStringUTFChars(env, widget_name, nil)
    let swiftString = String(cString: cString!)
    env.pointee?.pointee.ReleaseStringUTFChars(env, widget_name, cString)
    let controller = FlutterBridgeController()
    let _ = controller.initialize(parentView: parentView, port: port, widgetId: Int64(widget_id), widgetName: swiftString)
    let controllerPtr = Unmanaged.passRetained(controller).toOpaque() // ToDo: return this
//     let viewPtr = Unmanaged.passRetained(view!).toOpaque()
    return jlong(Int(bitPattern: controllerPtr))
//     return jlong(Int(bitPattern: viewPtr))
//     FlutterBridgeController.shared.initialize(parentView: parentView, port: port, widgetId: Int64(widget_id), widgetName: swiftString)
}

@MainActor @_cdecl("Java_org_eclipse_swt_widgets_SwtFlutterBridge_GetView")
public func GetView(env: UnsafeMutablePointer<JNIEnv?>, cls: jclass, context: jlong) -> jlong {
print("Java_org_eclipse_swt_widgets_SwtFlutterBridge_GetView")
    let controller = context != 0 ? unsafeBitCast(UInt(context), to: FlutterBridgeController.self) : nil
    if let view = controller?.getView() {
        let viewPtr = Unmanaged.passRetained(view).toOpaque()
        return jlong(Int(bitPattern: viewPtr))
    }
    return 0
}

@MainActor @_cdecl("Java_org_eclipse_swt_widgets_SwtFlutterBridge_Dispose")
public func Dispose(env: UnsafeMutablePointer<JNIEnv?>, cls: jclass, context: jlong) {
print("Java_org_eclipse_swt_widgets_SwtFlutterBridge_Dispose")
    let controller = context != 0 ? unsafeBitCast(UInt(context), to: FlutterBridgeController.self) : nil
    controller!.destroy()
}

@MainActor @_cdecl("Java_org_eclipse_swt_widgets_SwtFlutterBridge_SetBounds")
public func SetBounds(env: UnsafeMutablePointer<JNIEnv?>, cls: jclass, context: jlong, x: jint, y: jint, width: jint, height: jint, vx: jint, vy: jint, vwidth: jint, vheight: jint) {
print("Java_org_eclipse_swt_widgets_SwtFlutterBridge_SetBounds")
    let controller = context != 0 ? unsafeBitCast(UInt(context), to: FlutterBridgeController.self) : nil
    controller!.setFrame(x: x, y: y, w: width, h: height, vx: vx, vy: vy, vw: vwidth, vh: vheight)
}

class FlippedView: NSView {
    override var isFlipped: Bool {
        return true
    }
}