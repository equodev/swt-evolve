import 'dart:async';
import 'dart:ui' show Offset;

import 'package:flutter/scheduler.dart' show SchedulerBinding;

import 'dnd_session.dart' show ActiveDragTracker;

typedef HoverChanged = void Function(bool hovering);

/// Mirrors SwtDisplay's currentControl/checkEnterExit: native SWT only ever
/// considers the single deepest control under the pointer "hovered" -- an
/// ancestor never gets its own simultaneous MouseEnter while a descendant is
/// hovered. Flutter's MouseRegion has no such exclusivity (nested regions all
/// fire onEnter/onExit independently for their own geometric bounds), so each
/// control/composite reports its own hover state here and this class picks
/// the single deepest active one as the real SWT MouseEnter/MouseExit target.
class HoverExclusivityArbiter {
  HoverExclusivityArbiter._();
  static final HoverExclusivityArbiter instance = HoverExclusivityArbiter._();

  /// Where the pointer was, in global coordinates, on the latest crossing of the hover being
  /// resolved, and null once it is. A control that loses the hover to a descendant has not been
  /// crossed itself, so its own last crossing is stale: its MouseExit is placed from this instead, as
  /// natively it is placed at the pointer's current position.
  Offset? get pointerPosition => _pointerPosition;
  Offset? _pointerPosition;

  final Map<Object, int> _activeDepths = {};
  final Map<Object, HoverChanged> _callbacks = {};
  final Map<Object, void Function()> _leftWhileHeld = {};
  Object? _winner;
  bool _resolveScheduled = false;

  /// [onLeftWhileHeld] runs when the pointer leaves [owner] while a button is held, which defers
  /// its MouseExit to the release.
  void register(Object owner, HoverChanged onHoverChanged, {void Function()? onLeftWhileHeld}) {
    _callbacks[owner] = onHoverChanged;
    if (onLeftWhileHeld != null) _leftWhileHeld[owner] = onLeftWhileHeld;
  }

  void unregister(Object owner) {
    _callbacks.remove(owner);
    _leftWhileHeld.remove(owner);
    _activeDepths.remove(owner);
    if (_winner == owner) _winner = null;
  }

  /// [owner] at [depth] was crossed by the pointer, now at [globalPosition].
  void setActive(Object owner, int depth, bool active, Offset globalPosition) {
    _pointerPosition = globalPosition;
    if (active) {
      _activeDepths[owner] = depth;
    } else {
      _activeDepths.remove(owner);
    }
    // Batched via microtask so all onEnter/onExit callbacks Flutter fires
    // synchronously for one pointer move (all ancestors sharing the hit
    // point) are settled before picking a winner by depth.
    if (!_resolveScheduled) {
      _resolveScheduled = true;
      scheduleMicrotask(_resolve);
    }
  }

  void flushPending() {
    if (_resolveScheduled) _resolve();
  }

  /// Settles what the pointer is over once the frame in progress has been laid out and hit-tested
  /// again: the last step of a drag (a sash, a scroll) moves what is under the pointer only there.
  void settleAfterFrame() {
    final scheduler = SchedulerBinding.instance;
    scheduler.addPostFrameCallback((_) => settle());
    scheduler.scheduleFrame();
  }

  /// The releases whose answer from Java the hover waits for.
  int _settleHolds = 0;

  /// How long a release is waited for: past it, the hover settles on what the client shows.
  static const settleHoldLimit = Duration(seconds: 2);

  /// Keeps the pointer where it is, as while a button is held, until the returned function runs:
  /// a release whose answer from Java moves the controls around the pointer (a sash, and the
  /// siblings its listeners lay out) is settled once that answer is shown, not on what the client
  /// drew before it.
  void Function() holdSettle() {
    _settleHolds++;
    var released = false;
    late final Timer limit;
    void release() {
      if (released) return;
      released = true;
      limit.cancel();
      if (--_settleHolds == 0) settleAfterFrame();
    }

    limit = Timer(settleHoldLimit, release);
    return release;
  }

  /// Settles what the pointer is over now that nothing holds it.
  void settle() {
    if (!_resolveScheduled) {
      _resolveScheduled = true;
      scheduleMicrotask(_resolve);
    }
  }

  void _resolve() {
    _resolveScheduled = false;
    // A held button is SWT's implicit capture: nothing is entered or left until release, when
    // [settle] resolves what the pointer ended up over.
    final newWinner = _deepestActive();
    if (ActiveDragTracker.isSuppressingHover || _settleHolds > 0) {
      final winner = _winner;
      if (winner != null && newWinner != winner) _leftWhileHeld[winner]?.call();
      return;
    }
    if (newWinner != _winner) {
      final oldWinner = _winner;
      _winner = newWinner;
      if (oldWinner != null) _callbacks[oldWinner]?.call(false);
      if (newWinner != null) _callbacks[newWinner]?.call(true);
    }
    // Kept no longer than this hover: in another view, a later one has coordinates of its own.
    _pointerPosition = null;
  }

  Object? _deepestActive() {
    Object? deepest;
    var bestDepth = -1;
    _activeDepths.forEach((owner, depth) {
      if (depth > bestDepth) {
        bestDepth = depth;
        deepest = owner;
      }
    });
    return deepest;
  }
}
