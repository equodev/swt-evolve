import 'package:flutter/services.dart';

/// Desktop-native implementation of the Equo CSD window API.
///
/// On the web the host injects `window.equo.*` (see [equo_window_web.dart]); the
/// desktop-native embedder has no such host, so the equivalent operations are exposed
/// over a Flutter platform channel (`dev.equo.swt/window`) implemented by the native
/// runner (`flutter_bridge.swift` / `.cpp` / `.cc`). This is the desktop analogue of the
/// browser CSD API — it drives the same frameless top-level window the whole Display fills.
class EquoWindow {
  EquoWindow._();

  static const MethodChannel _channel = MethodChannel('dev.equo.swt/window');

  /// The native embedder always registers the channel for the Display window, so the
  /// desktop CSD path is available whenever this implementation is compiled in.
  static bool get available => true;

  /// Fire-and-forget: window controls must never throw into the gesture that triggered
  /// them (e.g. a channel not yet wired on the very first frame, or a non-Display client).
  static void _invoke(String method, [Map<String, Object?>? args]) {
    _channel.invokeMethod<void>(method, args).catchError((Object _) {});
  }

  static void minimize() => _invoke('minimize');
  static void maximize() => _invoke('maximize');
  static void restore() => _invoke('restore');
  static void close() => _invoke('close');

  /// Begins an OS-native window move. Coordinates are accepted for API parity with the web
  /// side; the native window manager drives the drag from the current pointer/event.
  static void beginMove(double screenX, double screenY) => _invoke('beginMove');

  /// Begins an OS-native window resize from [edge] (one of TOP_LEFT, TOP, TOP_RIGHT, RIGHT,
  /// BOTTOM_RIGHT, BOTTOM, BOTTOM_LEFT, LEFT). No-op on platforms whose native frame still
  /// owns the resize edges (macOS).
  static void beginResize(double screenX, double screenY, String edge) =>
      _invoke('beginResize', {'edge': edge});

  // Geometry getters exist for the web "bounds" maximize strategy; desktop uses the
  // "direct" strategy (the native window maximizes itself), so these stay 0.
  static double get screenOriginX => 0;
  static double get screenOriginY => 0;
  static double get viewportWidth => 0;
  static double get viewportHeight => 0;
  static double get screenAvailLeft => 0;
  static double get screenAvailTop => 0;
  static double get screenAvailWidth => 0;
  static double get screenAvailHeight => 0;

  /// Wires window focus changes so the macOS controls grey out when inactive. The native
  /// embedder pushes `active(bool)` calls over the same channel.
  static void installWindowStateListeners(void Function(bool active) onActive) {
    _channel.setMethodCallHandler((call) async {
      if (call.method == 'active') {
        onActive(call.arguments == true);
      }
      return null;
    });
  }
}
