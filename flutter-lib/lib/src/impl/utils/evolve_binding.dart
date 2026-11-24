import 'package:flutter/semantics.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

/// The binding Evolve runs on. It differs from [WidgetsFlutterBinding] in one thing: when
/// [followPlatformSemantics] is false it declines the engine's request to build the semantics tree.
///
/// The Linux embedder asks for that tree on every start, since GTK gives it no way to tell whether
/// an assistive technology is listening, and keeping the tree costs every frame a third or more of
/// its build and layout. The Windows embedder asks whenever any UI Automation client queries the
/// window, screen reader or not. Each runner answers that question itself (the accessibility bus,
/// the system's screen reader flag) and passes it in, then reports an assistive technology that
/// starts or stops later on [accessibilityChannel]. An explicit [ensureSemantics] (the test
/// semantics) still turns the tree on.
class EvolveWidgetsBinding extends WidgetsFlutterBinding {
  EvolveWidgetsBinding._(this.followPlatformSemantics);

  /// Installs the binding, unless one is installed already.
  static void ensureInitialized({required bool followPlatformSemantics}) {
    if (!followPlatformSemantics) EvolveWidgetsBinding._(false);
    WidgetsFlutterBinding.ensureInitialized();
  }

  /// Where the runner reports whether an assistive technology listens (`assistiveTechnology`, a
  /// bool), when that changes after the application started.
  static const MethodChannel accessibilityChannel = MethodChannel('dev.equo.swt/accessibility');

  /// Whether the semantics tree follows the engine's request for it.
  final bool followPlatformSemantics;

  bool _declining = false;
  SemanticsHandle? _assistiveTechnologyHandle;

  @override
  void initInstances() {
    _declining = !followPlatformSemantics;
    try {
      super.initInstances();
    } finally {
      _declining = false;
    }
    if (!followPlatformSemantics) {
      // Only SemanticsBinding answers this callback, by asking for the tree again.
      platformDispatcher.onSemanticsEnabledChanged = () {};
      accessibilityChannel.setMethodCallHandler(_handleAccessibilityCall);
    }
  }

  @override
  SemanticsHandle ensureSemantics() {
    final handle = super.ensureSemantics();
    // The engine's request, made while the binding initializes: released at once, so the tree stays
    // off until something else asks for it. The framework keeps this handle and never disposes it
    // again, since the callback that would is replaced above.
    if (_declining) handle.dispose();
    return handle;
  }

  Future<void> _handleAccessibilityCall(MethodCall call) async {
    if (call.method != 'assistiveTechnology') return;
    if (call.arguments == true) {
      _assistiveTechnologyHandle ??= ensureSemantics();
    } else {
      _assistiveTechnologyHandle?.dispose();
      _assistiveTechnologyHandle = null;
    }
  }
}
