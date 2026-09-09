import 'package:flutter/gestures.dart';
import 'package:flutter/widgets.dart';

import '../../impl/utils/double_tap_detector.dart';
import 'csd_state.dart';
import 'equo_window.dart';

/// Desktop-native window-drag surface. Unlike the web build (which needs a real DOM
/// `mousedown` for CEF to honor the move), the native window manager begins the drag from
/// the current pointer, so a plain [Listener] that calls [EquoWindow.beginMove] is enough.
///
/// Double-click toggles maximize. It goes through [DoubleTapDetector] rather than a
/// `GestureDetector`, for the reason that class exists: the drag has to start on
/// pointer-down, and a double-tap recogniser would hold the arena and delay it.
class CsdDragView extends StatefulWidget {
  const CsdDragView({super.key});

  @override
  State<CsdDragView> createState() => _CsdDragViewState();
}

class _CsdDragViewState extends State<CsdDragView> {
  /// Wider than the shared window on purpose: on Windows and macOS the first click hands off
  /// to a modal OS drag loop, so the second one is only delivered once that loop returns.
  static const Duration _titleBarDoubleClick = Duration(milliseconds: 500);

  final DoubleTapDetector _detector =
      DoubleTapDetector(timeout: _titleBarDoubleClick, slop: 8.0);

  /// How far the pointer travels before the press counts as a drag rather than a click.
  static const double _dragSlop = 4.0;

  Offset? _pressOrigin;
  bool _moving = false;
  bool _pendingToggle = false;

  void _onPointerDown(PointerDownEvent event) {
    if (event.buttons != kPrimaryButton) return;
    if (_detector.registerTap(position: event.position) >= 2) {
      _detector.reset();
      _pressOrigin = null;
      // Deferred to the release: the host ignores a maximize/restore request made while a mouse
      // button is still down, which is why the same command works from a toolbar button (a tap,
      // so already released) and is dropped here.
      _pendingToggle = true;
      return;
    }
    // Only armed here. Handing off to the OS move loop on the press would block this layer --
    // it sits over the whole bar, so every button click would enter that loop and lose its
    // release, and the second click of a double-click would not be delivered until it ended.
    _pressOrigin = event.position;
    _moving = false;
  }

  void _onPointerMove(PointerMoveEvent event) {
    final origin = _pressOrigin;
    if (origin == null || _moving) return;
    if ((event.position - origin).distance < _dragSlop) return;
    _moving = true;
    EquoWindow.beginMove(event.position.dx, event.position.dy);
  }

  void _onPointerUp(PointerUpEvent event) {
    _pressOrigin = null;
    _moving = false;
    if (_pendingToggle) {
      _pendingToggle = false;
      csdToggleMaximize();
    }
  }

  @override
  Widget build(BuildContext context) {
    return Listener(
      behavior: HitTestBehavior.translucent,
      onPointerDown: _onPointerDown,
      onPointerMove: _onPointerMove,
      onPointerUp: _onPointerUp,
      child: const SizedBox.expand(),
    );
  }
}
