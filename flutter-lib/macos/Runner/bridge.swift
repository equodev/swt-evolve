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
    static let shared = FlutterBridgeController()
    
    private var flutterViewController: FlutterViewController?
    private var window: NSWindow? // Keep for standalone window case if needed
    
    private init() {
        print("FlutterBridgeController.init")
    }
    
    func initialize(parentView: NSView?, port: Int32, widgetId: Int64, widgetName: String) {
        print("FlutterBridgeController.initialize port:\(port) parent:\(String(describing: parentView)) id:\(widgetId) name:\(widgetName)")
        // Ensure we're on the main thread
        if !Thread.isMainThread {
            DispatchQueue.main.sync {
                self.initialize(parentView: parentView, port: port, widgetId: widgetId, widgetName: widgetName)
            }
            return
        }

        let arguments = [String(port), String(widgetId), widgetName]
        let project = FlutterDartProject(precompiledDartBundle: Bundle(path: "/Users/guillez/ws/equoswt/flutter-lib/build/macos/Build/Products/Release/swtflutter.app/Contents/Frameworks/App.framework"))
        project.dartEntrypointArguments = arguments
        // Create Flutter view controller
        flutterViewController = FlutterViewController.init(project: project)
//         print("FlutterBridgeController.initialize 2")

        // If parent view is provided, add Flutter view as a subview
        if let parent = parentView {
//             print("FlutterBridgeController.initialize 3")
            let frame = parent.frame

            if let flutterView = flutterViewController?.view {
//                let flutterViewFrame = NSRect(x: 0, y: 0, width: 400, height: 500)
                flutterView.frame = frame

//                flutterView.frame = parent.bounds
                flutterView.autoresizingMask = [.width, .height]
                print("FlutterBridgeController.initialize 4 ", parent, flutterView, flutterView.frame)
                parent.addSubview(flutterView)
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
    }

}

// JNI bridge function
@MainActor @_cdecl("Java_org_eclipse_swt_widgets_FlutterSwt_InitializeFlutterWindow")
public func InitializeFlutterWindow(env: UnsafeMutablePointer<JNIEnv?>, cls: jclass, port: jint, parent: jlong, widget_id: jlong, widget_name: jstring) {
    let parentView = parent != 0 ? unsafeBitCast(UInt(parent), to: NSView.self) : nil
   
    let cString = env.pointee!.pointee.GetStringUTFChars(env, widget_name, nil)
    let swiftString = String(cString: cString!)
    env.pointee?.pointee.ReleaseStringUTFChars(env, widget_name, cString)
    FlutterBridgeController.shared.initialize(parentView: parentView, port: port, widgetId: Int64(widget_id), widgetName: swiftString)
}
