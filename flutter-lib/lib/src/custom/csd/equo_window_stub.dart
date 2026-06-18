/// Native/desktop stub for the Equo CSD window API. There is no host `window.equo`
/// off the web, so every method is a no-op and [available] is always false. Keeps the
/// CSD widgets compiling on non-web targets without `dart:js_interop`.
class EquoWindow {
  EquoWindow._();

  static bool get available => false;

  static void minimize() {}
  static void maximize() {}
  static void restore() {}
  static void close() {}
  static void beginMove(double screenX, double screenY) {}
  static void beginResize(double screenX, double screenY, String edge) {}

  static double get screenOriginX => 0;
  static double get screenOriginY => 0;
  static double get viewportWidth => 0;
  static double get viewportHeight => 0;
  static double get screenAvailLeft => 0;
  static double get screenAvailTop => 0;
  static double get screenAvailWidth => 0;
  static double get screenAvailHeight => 0;
  static void installWindowStateListeners(
    void Function(bool active) onActive,
  ) {}
}
