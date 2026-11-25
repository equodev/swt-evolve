import 'dart:async';
import 'dart:ui';

import 'package:flutter/gestures.dart';
import 'package:flutter/semantics.dart';

import 'raw_pointer.dart';

/// Delivers, as the pointer events it was, a press the web engine turned into a semantics tap.
///
/// With the semantics tree on, the engine holds back a press that lands on a tappable semantics
/// node and, when the browser's click follows, sends a semantics tap in its place and drops the
/// press. A native control reports that press (MouseDown, MouseUp, its focus) before it selects,
/// so the tap of a click whose press the app never received is replaced by that press. A click
/// with no press before it, a screen reader's, stays a tap.
class PressReplay {
  PressReplay._();

  static bool _installed = false;
  static _Press? _pending;
  static bool _inClick = false;
  static final Map<PointerDeviceKind, int> _devices = {};
  static int _nextPointer = 1 << 30;

  /// Installs the replay once, on the web; elsewhere every press reaches the app.
  static void ensureInstalled() {
    if (_installed || !rawPointerPressesAvailable) return;
    _installed = true;
    listenRawPointerPresses(_onRaw);
    GestureBinding.instance.pointerRouter.addGlobalRoute(_onDelivered);
    final dispatcher = SemanticsBinding.instance.platformDispatcher;
    final perform = dispatcher.onSemanticsActionEvent;
    dispatcher.onSemanticsActionEvent = (action) {
      if (action.type == SemanticsAction.tap && _inClick && _replay()) return;
      perform?.call(action);
    };
  }

  static void _onRaw(String type, int pointerId, String pointerType, double x, double y, int buttons,
      Duration timeStamp) {
    switch (type) {
      case 'pointerdown':
        _pending = _Press(pointerId, _kindOf(pointerType), Offset(x, y), buttons, timeStamp);
      case 'pointerup':
        final press = _pending;
        if (press != null && press.pointerId == pointerId) {
          press.up = Offset(x, y);
          press.upStamp = timeStamp;
        }
      case 'pointercancel':
        _pending = null;
      case 'click':
        // Each click settles the press before it: replayed for its tap, or dropped with it.
        _inClick = true;
        Timer.run(() {
          _inClick = false;
          _pending = null;
        });
    }
  }

  static void _onDelivered(PointerEvent event) {
    _devices[event.kind] = event.device;
    if (event is PointerDownEvent) _pending = null;
  }

  static bool _replay() {
    final press = _pending;
    final up = press?.up;
    if (press == null || up == null) return false;
    _pending = null;
    final binding = GestureBinding.instance;
    final pointer = _nextPointer++;
    final device = _devices[press.kind] ?? 0;
    // Stamped as the engine stamps the pointer events it delivers: a click chains by its stamps.
    binding.handlePointerEvent(PointerDownEvent(
        timeStamp: press.downStamp,
        pointer: pointer,
        kind: press.kind,
        device: device,
        position: press.down,
        buttons: press.buttons));
    if (up != press.down) {
      binding.handlePointerEvent(PointerMoveEvent(
          timeStamp: press.upStamp,
          pointer: pointer,
          kind: press.kind,
          device: device,
          position: up,
          delta: up - press.down,
          buttons: press.buttons));
    }
    binding.handlePointerEvent(PointerUpEvent(
        timeStamp: press.upStamp, pointer: pointer, kind: press.kind, device: device, position: up));
    return true;
  }

  static PointerDeviceKind _kindOf(String pointerType) => switch (pointerType) {
        'touch' => PointerDeviceKind.touch,
        'pen' => PointerDeviceKind.stylus,
        _ => PointerDeviceKind.mouse,
      };
}

class _Press {
  _Press(this.pointerId, this.kind, this.down, this.buttons, this.downStamp) : upStamp = downStamp;

  final int pointerId;
  final PointerDeviceKind kind;
  final Offset down;
  final int buttons;
  final Duration downStamp;
  Offset? up;
  Duration upStamp;
}
