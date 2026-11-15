import 'dart:js_interop';

import 'package:web/web.dart' as web;

/// Calls [onMove] with every pointer move the browser reports as it happens (`pointerrawupdate`),
/// ahead of the move it delivers with the next frame: position in the page's CSS pixels, and the
/// buttons held.
void listenRawPointerMoves(void Function(double x, double y, int buttons) onMove) {
  web.window.addEventListener(
    'pointerrawupdate',
    ((_PointerPosition e) => onMove(e.clientX, e.clientY, e.buttons)).toJS,
    web.AddEventListenerOptions(capture: true, passive: true),
  );
}

/// A pointer event's position is fractional, but package:web reads it as an int, which dart2wasm
/// refuses for a fraction.
extension type _PointerPosition(JSObject _) implements JSObject {
  external double get clientX;
  external double get clientY;
  external int get buttons;
}
