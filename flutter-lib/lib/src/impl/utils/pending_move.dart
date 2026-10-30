import 'dart:async';

import 'package:flutter/foundation.dart';

/// A control that can hold a move back: it keeps the move itself and sends it when asked.
abstract interface class HeldMove {
  void sendHeldMove();
}

/// The pointer move a control held back to keep its moves to one per throttle window.
///
/// Dropping it would leave Java a step behind the pointer whenever the pointer stops inside a
/// window: `Display.getCursorLocation()` and every hit test there would answer for where it was.
/// So it is sent when the window ends, or before any other event leaves, whichever comes first, and
/// Java hears the move and what follows it in the order they happened. The pointer is in one place,
/// so one move is held, whichever control it is for.
class PendingMove {
  PendingMove._();

  static HeldMove? _owner;
  static Timer? _timer;

  /// Holds [owner]'s move, to be sent once [afterMs] milliseconds have passed. It replaces a move
  /// [owner] held before; a move another control held is that control's last one, so it is sent first.
  static void hold(HeldMove owner, int afterMs) {
    if (_owner != null && !identical(_owner, owner)) flush();
    _owner = owner;
    _timer ??= Timer(Duration(milliseconds: afterMs), flush);
  }

  /// Sends the held move now, if there is one.
  static void flush() {
    final owner = _owner;
    if (owner == null) return;
    reset();
    owner.sendHeldMove();
  }

  /// Forgets the move [owner] holds: a control on its way out sends nothing.
  static void drop(Object owner) {
    if (identical(_owner, owner)) reset();
  }

  @visibleForTesting
  static void reset() {
    _owner = null;
    _timer?.cancel();
    _timer = null;
  }
}
