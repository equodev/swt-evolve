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
    
    func initialize(parentView: NSView?) {
        print("FlutterBridgeController.initialize ", parentView)
        // Ensure we're on the main thread
        if !Thread.isMainThread {
            DispatchQueue.main.sync {
                self.initialize(parentView: parentView)
            }
            return
        }
        
        let project = FlutterDartProject.init(precompiledDartBundle: Bundle(path: "swtflutter.app/Contents/Frameworks/App.framework"))
        // Create Flutter view controller
        flutterViewController = FlutterViewController.init(project: project)
        print("FlutterBridgeController.initialize 2")

        // If parent view is provided, add Flutter view as a subview
        if let parent = parentView {
            print("FlutterBridgeController.initialize 3")
            let frame = parent.frame

            if let flutterView = flutterViewController?.view {
//                let flutterViewFrame = NSRect(x: 0, y: 0, width: 400, height: 500)
                flutterView.frame = frame

//                flutterView.frame = parent.bounds
                flutterView.autoresizingMask = [.width, .height]
                print("FlutterBridgeController.initialize 4 ", parent,  flutterView, flutterView.frame)
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
@MainActor @_cdecl("Java_dev_equo_Main_InitializeFlutterWindow")
public func InitializeFlutterWindow(env: UnsafeMutablePointer<JNIEnv?>, cls: jclass, hwnd: jlong) {
    print("swift InitializeFlutterWindow\n")
    // Convert jlong to NSView
    
    let parentView = hwnd != 0 ? unsafeBitCast(UInt(hwnd), to: NSView.self) : nil
   
    print("parentView:", parentView)
    
    FlutterBridgeController.shared.initialize(parentView: parentView)
}
