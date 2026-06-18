// Web implementation of the Equo CSD window API, talking to `window.equo.*`.
//
// `window.equo` is injected by the host only when the standalone window was created
// with `csd=true`, and may be absent (CSD off, older host, or injected slightly after
// Flutter first paints). Every accessor therefore re-reads `window.equo` lazily and
// no-ops when it is missing, so a control tapped before injection fails safe.
import 'dart:js_interop';

import 'package:web/web.dart' as web;

@JS('window.equo')
external JSObject? get _equo;

@JS('window.screenX')
external JSNumber? get _screenX;

@JS('window.screenY')
external JSNumber? get _screenY;

@JS('window.innerWidth')
external JSNumber? get _innerWidth;

@JS('window.innerHeight')
external JSNumber? get _innerHeight;

@JS('window.screen')
external JSObject? get _screen;

extension type _Screen._(JSObject _) implements JSObject {
  external JSNumber get availWidth;
  external JSNumber get availHeight;
  external JSNumber? get availLeft;
  external JSNumber? get availTop;
}

extension type _EquoApi._(JSObject _) implements JSObject {
  external void minimize();
  external void maximize();
  external void restore();
  external void close();
  external void beginMove(JSNumber screenX, JSNumber screenY);
  external void beginResize(JSNumber screenX, JSNumber screenY, JSString edge);
}

/// Static gateway to the host window. Mirrors [equo_window_stub.dart]'s API.
class EquoWindow {
  EquoWindow._();

  static _EquoApi? get _api => _equo as _EquoApi?;

  /// Whether the host CSD API is present (re-checked every call — never cached).
  static bool get available => _equo != null;

  static void minimize() => _api?.minimize();
  static void maximize() => _api?.maximize();
  static void restore() => _api?.restore();
  static void close() => _api?.close();

  /// Begins an OS-native window move. [screenX]/[screenY] must be absolute screen
  /// coordinates (see [screenOriginX]/[screenOriginY]).
  static void beginMove(double screenX, double screenY) =>
      _api?.beginMove(screenX.toJS, screenY.toJS);

  /// Begins an OS-native window resize from [edge] (one of TOP_LEFT, TOP,
  /// TOP_RIGHT, RIGHT, BOTTOM_RIGHT, BOTTOM, BOTTOM_LEFT, LEFT).
  static void beginResize(double screenX, double screenY, String edge) =>
      _api?.beginResize(screenX.toJS, screenY.toJS, edge.toJS);

  /// The window's top-left position on the screen. The frameless Flutter view fills
  /// the window, so adding a view-local logical (== CSS) pixel coordinate yields the
  /// screen coordinate the move/resize API expects.
  static double get screenOriginX => _screenX?.toDartDouble ?? 0;
  static double get screenOriginY => _screenY?.toDartDouble ?? 0;

  /// Current window content size (the frameless view fills the window).
  static double get viewportWidth => _innerWidth?.toDartDouble ?? 0;
  static double get viewportHeight => _innerHeight?.toDartDouble ?? 0;

  /// The screen work area (excludes the menu bar/dock), in CSS pixels — the target a
  /// "maximize" should fill. availLeft/availTop are non-standard; default to 0.
  static _Screen? get _scr => _screen as _Screen?;
  static double get screenAvailLeft => _scr?.availLeft?.toDartDouble ?? 0;
  static double get screenAvailTop => _scr?.availTop?.toDartDouble ?? 0;
  static double get screenAvailWidth => _scr?.availWidth.toDartDouble ?? 0;
  static double get screenAvailHeight => _scr?.availHeight.toDartDouble ?? 0;

  /// Wires window focus/blur to [onActive] so the macOS controls can grey out when the window
  /// is inactive. We don't seed from document.hasFocus() — at main() time the page isn't
  /// focused yet, so it returns false and (since focus never changes afterward) the window
  /// stays wrongly "inactive". The notifier defaults to active; blur/focus refine it.
  static void installWindowStateListeners(void Function(bool active) onActive) {
    web.window.addEventListener(
      'focus',
      ((web.Event _) => onActive(true)).toJS,
    );
    web.window.addEventListener(
      'blur',
      ((web.Event _) => onActive(false)).toJS,
    );
  }
}
