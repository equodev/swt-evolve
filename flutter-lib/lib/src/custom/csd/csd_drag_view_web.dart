// Web drag surface: a transparent DOM div whose mousedown starts the native window move
// via window.equo.beginMove with the real screen coordinates from the DOM MouseEvent.
import 'dart:js_interop';
import 'dart:ui_web' as ui_web;

import 'package:flutter/widgets.dart';
import 'package:web/web.dart' as web;

@JS('window.equo')
external JSObject? get _equo;

extension type _EquoApi._(JSObject _) implements JSObject {
  external void beginMove(JSNumber screenX, JSNumber screenY);
}

const String _viewType = 'csd-window-drag';
bool _registered = false;

void _ensureRegistered() {
  if (_registered) return;
  _registered = true;
  ui_web.platformViewRegistry.registerViewFactory(_viewType, (int viewId) {
    final el = web.HTMLDivElement()
      ..style.width = '100%'
      ..style.height = '100%'
      ..style.cursor = 'default';
    el.addEventListener(
      'mousedown',
      (web.Event event) {
        final e = event as web.MouseEvent;
        if (e.button != 0) return;
        (_equo as _EquoApi?)?.beginMove(e.screenX.toJS, e.screenY.toJS);
      }.toJS,
    );
    return el;
  });
}

/// A draggable window region. Place it where nothing else needs the pointer (a title strip
/// or a dedicated grip); it occupies the given space and moves the window on press-drag.
class CsdDragView extends StatelessWidget {
  const CsdDragView({super.key});

  @override
  Widget build(BuildContext context) {
    _ensureRegistered();
    return const HtmlElementView(viewType: _viewType);
  }
}
