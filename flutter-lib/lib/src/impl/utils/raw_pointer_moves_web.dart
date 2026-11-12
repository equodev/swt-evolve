import 'dart:js_interop';

import 'package:web/web.dart' as web;

/// Calls [onMove] with every pointer move the browser reports as it happens (`pointerrawupdate`),
/// ahead of the move it delivers with the next frame: position in the page's CSS pixels, and the
/// buttons held.
void listenRawPointerMoves(void Function(double x, double y, int buttons) onMove) {
  web.window.addEventListener(
    'pointerrawupdate',
    ((web.PointerEvent e) => onMove(e.clientX.toDouble(), e.clientY.toDouble(), e.buttons)).toJS,
    web.AddEventListenerOptions(capture: true, passive: true),
  );
}
