import Cocoa
import FlutterMacOS

class MainFlutterWindow: NSWindow {
    private var flutterViewController: FlutterViewController!
    private var flutterViewController2: FlutterViewController!
    private var containerView: NSView!
    private var nativeView: NSView!
    
    override func awakeFromNib() {
        let windowFrame = self.frame
        self.setFrame(windowFrame, display: true)
        
        // Create the main container view
        containerView = NSView(frame: contentView!.bounds)
        containerView.autoresizingMask = [.width, .height]
        contentView?.addSubview(containerView)
        
        // Create and setup the Flutter view controller
        flutterViewController = FlutterViewController()
        // Create the Flutter view with a specific frame
        // Adjust these values based on your layout needs
        let flutterViewFrame = NSRect(x: 0, y: 0, width: windowFrame.width * 0.7, height: windowFrame.height * 0.5)
        flutterViewController.view.frame = flutterViewFrame
//        flutterViewController.view.autoresizingMask = [.width, .height]
        
        // Create a native view for demonstration
        nativeView = NSView(frame: NSRect(x: windowFrame.width * 0.7,
                                        y: 0,
                                        width: windowFrame.width * 0.3,
                                      height: windowFrame.height * 0.7))
//        nativeView.autoresizingMask = [.width, .height]
        nativeView.wantsLayer = true
        nativeView.layer?.backgroundColor = NSColor.lightGray.cgColor

        // Create and setup the Flutter view controller
        flutterViewController2 = FlutterViewController()
        // Create the Flutter view with a specific frame
        // Adjust these values based on your layout needs
        let flutterViewFrame2 = NSRect(x: 10, y: 300, width: windowFrame.width * 0.5, height: windowFrame.height * 0.4)
        flutterViewController2.view.frame = flutterViewFrame2

        // Add both views to the container
        containerView.addSubview(flutterViewController.view)
        containerView.addSubview(nativeView)
        containerView.addSubview(flutterViewController2.view)

        // Important: We still need to add the Flutter view controller as a child
        // to handle lifecycle events properly
//        let rootViewController = NSViewController()
//        rootViewController.view = containerView
//        self.contentViewController = rootViewController
//        rootViewController.addChild(flutterViewController)
        
        RegisterGeneratedPlugins(registry: flutterViewController)

        super.awakeFromNib()
        
//        registerMethodChannel()
    }
    //  override func awakeFromNib() {
//    let flutterViewController = FlutterViewController()
//      flutterViewController.loadView()
//      flutterViewController.viewDidLoad()
//      flutterViewController.viewWillAppear()
//
//      flutterViewController.backgroundColor = NSColor.blue
//    let windowFrame = self.frame
//    self.contentViewController = flutterViewController
//      
//    let intermView = NSView(frame: windowFrame.insetBy(dx: -300, dy: -300))
//      intermView.wantsLayer = true
//      intermView.layer?.backgroundColor = NSColor.red.cgColor
////      self.contentView?.addSubview(intermView)
//      self.contentView = intermView
//
//      let engine = flutterViewController.engine
//      let flutterView = flutterViewController.view
////      flutterViewController.
//      
//
//
//      
//      print("engine:", engine, "view:", flutterView, "loaded:", flutterViewController.isViewLoaded)
//      print("nib:", flutterViewController.nibBundle, flutterViewController.nibName, "id:", flutterViewController.viewIdentifier)
////      flutterView.frame = NSRect(origin: CGPoint(x: 20, y: 20), size: CGSize(width: 400, height: 400))
//      flutterView.setFrameSize(CGSize(width: 400, height: 300))
//      self.contentView?.addSubview(flutterView)
////      self.contentView = flutterView
//      
////      flutterView.needsDisplay = true
//      
////      flutterViewController.viewWillAppear()
////      flutterViewController.awakeFromNib()
//
//      self.setFrame(windowFrame, display: true)
//
//    RegisterGeneratedPlugins(registry: flutterViewController)
//
//    super.awakeFromNib()
//  }
}
