import 'dart:js_interop';

import 'package:web/web.dart' as web;

/// Calls [onMove] with every pointer move the browser reports as it happens (`pointerrawupdate`),
/// ahead of the move it delivers with the next frame: position in the page's CSS pixels, and the
/// buttons held.
void listenRawPointerMoves(void Function(double x, double y, int buttons) onMove) {
  web.window.addEventListener(
    'pointerrawupdate',
    ((_RawPointer e) => onMove(e.clientX, e.clientY, e.buttons)).toJS,
    web.AddEventListenerOptions(capture: true, passive: true),
  );
}

/// Whether the browser's own pointer presses can be read: only on the web.
const bool rawPointerPressesAvailable = true;

/// Calls [onEvent] with every `pointerdown`, `pointerup`, `pointercancel` and `click` the browser
/// dispatches, ahead of the engine's own listeners: the event's type, its pointer's id and type
/// (`mouse`, `touch`, `pen`; empty for a click), its position in the page's CSS pixels, the
/// buttons held and its time stamp, on the clock of the pointer events the engine delivers.
void listenRawPointerPresses(
    void Function(String type, int pointerId, String pointerType, double x, double y, int buttons,
            Duration timeStamp)
        onEvent) {
  final options = web.AddEventListenerOptions(capture: true, passive: true);
  for (final type in const ['pointerdown', 'pointerup', 'pointercancel']) {
    web.window.addEventListener(
      type,
      ((_RawPointer e) => onEvent(e.type, e.pointerId, e.pointerType, e.clientX, e.clientY,
          e.buttons, _durationOf(e.timeStamp))).toJS,
      options,
    );
  }
  web.window.addEventListener(
    'click',
    ((_RawPointer e) =>
        onEvent('click', -1, '', e.clientX, e.clientY, e.buttons, _durationOf(e.timeStamp))).toJS,
    options,
  );
}

/// The engine's conversion of an event's time stamp, in milliseconds.
Duration _durationOf(double milliseconds) {
  final ms = milliseconds.toInt();
  return Duration(
      milliseconds: ms,
      microseconds: ((milliseconds - ms) * Duration.microsecondsPerMillisecond).toInt());
}

/// A pointer event's position is fractional, but package:web reads it as an int, which dart2wasm
/// refuses for a fraction.
extension type _RawPointer(JSObject _) implements JSObject {
  external String get type;
  external int get pointerId;
  external String get pointerType;
  external double get clientX;
  external double get clientY;
  external int get buttons;
  external double get timeStamp;
}
