import 'dart:async';

import 'package:flutter/widgets.dart';

import '../../comm/comm_frame.dart';
import '../../gen/widgets.dart' show WireChannel;

/// Keeps a key typed behind a Tab behind the focus that Tab moves.
///
/// Java sends a key to its focus control, and its focus control changes when this client reports
/// the control a traversal focused. Natively the focus moves while the Tab's KeyDown is handled, so
/// the keys after it reach the control it focused. Here a traversal can wait for Java's Traverse
/// answer, and the focused control can report a frame later, so a key released meanwhile reached
/// the control the Tab left. A traversal is settled by the first FocusIn this client sends after its
/// move, the report of the focus it moved; at once when the move left the focus where it was;
/// otherwise at the end of the frames that would have carried the report, and never later than
/// [timeout] after the move.
class TraversalOrder {
  TraversalOrder._();

  /// The longest a key waits behind a traversal that has moved, should no report ever come.
  static const Duration timeout = Duration(milliseconds: 400);

  static final List<Traversal> _open = [];
  static int _begun = 0;
  static int _settled = 0;
  static final List<(int, void Function())> _waiting = [];
  static bool _listening = false;

  /// How many traversals have begun so far.
  static int get begun => _begun;

  /// Whether the first [count] traversals have settled.
  static bool settledUpTo(int count) => _settled >= count;

  /// Runs [action] once the first [count] traversals have settled, in the order actions were given.
  static void after(int count, void Function() action) {
    _waiting.add((count, action));
    _release();
  }

  /// A traversal begins: its Tab's action was invoked.
  static Traversal begin() {
    if (!_listening) {
      _listening = true;
      EquoCommBase.afterSend = _sent;
    }
    final traversal = Traversal._(FocusManager.instance.primaryFocus);
    _open.add(traversal);
    _begun++;
    return traversal;
  }

  static void _sent(int channel) {
    if (channel != WireChannel.focusFocusIn) return;
    for (final traversal in _open) {
      if (traversal._moved && !traversal._settled) {
        traversal._settle();
        return;
      }
    }
  }

  static void _settledOne() {
    while (_open.isNotEmpty && _open.first._settled) {
      _open.removeAt(0);
      _settled++;
    }
    _release();
  }

  static void _release() {
    final due = <void Function()>[];
    while (_waiting.isNotEmpty && _waiting.first.$1 <= _settled) {
      due.add(_waiting.removeAt(0).$2);
    }
    if (due.isEmpty) return;
    // After the FocusIn that settled it has left, never from inside its send.
    scheduleMicrotask(() {
      for (final action in due) {
        action();
      }
    });
  }

  /// Forgets every traversal, for tests.
  @visibleForTesting
  static void reset() {
    for (final traversal in _open) {
      traversal._timer?.cancel();
    }
    _open.clear();
    _waiting.clear();
    _begun = 0;
    _settled = 0;
  }
}

/// One Tab's traversal, from its action to the report of the focus it moved.
class Traversal {
  Traversal._(this._focusBefore);

  final FocusNode? _focusBefore;
  bool _moved = false;
  bool _settled = false;
  Timer? _timer;

  /// The traversal has asked for its focus change, or decided on none ([focusChanges] false: a
  /// veto, a proposal dropped unanswered). Called again, it does nothing.
  void moved({bool focusChanges = true}) {
    if (_moved || _settled) return;
    _moved = true;
    if (!focusChanges) {
      _settle();
      return;
    }
    _timer = Timer(TraversalOrder.timeout, _settle);
    // After the move's focus change is applied, a microtask on: a Tab with nowhere to go left the
    // focus where it was, and has nothing to report.
    scheduleMicrotask(() {
      if (FocusManager.instance.primaryFocus == _focusBefore) _settle();
    });
    // The frame after the move applies a focus a control takes in its own build; the one after
    // that has reported it.
    final binding = WidgetsBinding.instance;
    binding.addPostFrameCallback((_) {
      binding.addPostFrameCallback((_) => _settle());
      binding.ensureVisualUpdate();
    });
    binding.ensureVisualUpdate();
  }

  void _settle() {
    if (_settled) return;
    _settled = true;
    _timer?.cancel();
    _timer = null;
    TraversalOrder._settledOne();
  }
}
