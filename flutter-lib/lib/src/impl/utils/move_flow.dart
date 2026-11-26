import 'package:flutter/foundation.dart';

import '../../comm/comm.dart';
import '../../gen/widgets.dart' show WireChannel;
import 'pending_move.dart';
import 'perf_marks.dart';

/// Keeps one pointer move in flight to Java, always the latest.
///
/// Java handles a move in a few milliseconds, but the updates one causes (a sash drag relayouts a
/// whole page) take this side far longer to apply. Sent as fast as the pointer reports them, moves
/// pile their updates up in front of the client, which then draws, one after another, where the
/// pointer was long ago: a drag that turns back draws the sash going back and forth. So after a
/// move is sent the next is held ([PendingMove] keeps replacing it with the latest) until Java says
/// the updates of the moves it handled have been sent, which it says after them, on the same
/// connection.
///
/// A Java that does not say so is never waited for: holding starts with its first answer. And no
/// move is held longer than [timeoutMicros], so an answer lost or never sent slows moves down,
/// never stops them.
class MoveFlow {
  MoveFlow._();

  /// The longest a move is held for an answer.
  static const int timeoutMicros = 100000;

  static bool _asked = false;
  static bool _answering = false;
  static bool _inFlight = false;
  static int _sentAt = 0;

  /// How long, from [now], a move must still be held for the one in flight; 0 when it can go.
  static int waitMicros(int now) {
    _ask();
    if (!_answering || !_inFlight) return 0;
    final left = _sentAt + timeoutMicros - now;
    return left > 0 ? left : 0;
  }

  /// A move was sent at [now].
  static void sent(int now) {
    _inFlight = true;
    _sentAt = now;
  }

  /// Java has sent the updates of the moves it handled: the move held since goes now.
  @visibleForTesting
  static void answered() {
    if (perfMarksEnabled) perfCount('Move.answered');
    _answering = true;
    _inFlight = false;
    PendingMove.flush();
  }

  /// Asks Java, once, to answer moves.
  static void _ask() {
    if (_asked) return;
    _asked = true;
    try {
      EquoCommService.on(0, 0, WireChannel.swtEvolveMoveHandled, (_) => answered());
      EquoCommService.sendOn(WireChannel.swtEvolveMoveHandled, const <String, Object>{});
    } catch (_) {
      // No connection to ask on (a test without one): moves are never held.
    }
  }

  @visibleForTesting
  static void reset({bool asked = false}) {
    _asked = asked;
    _answering = false;
    _inFlight = false;
    _sentAt = 0;
  }
}
