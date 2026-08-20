import 'dart:async';

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

  final Map<Object, int> _activeDepths = {};
  final Map<Object, HoverChanged> _callbacks = {};
  Object? _winner;
  bool _resolveScheduled = false;

  void register(Object owner, HoverChanged onHoverChanged) {
    _callbacks[owner] = onHoverChanged;
  }

  void unregister(Object owner) {
    _callbacks.remove(owner);
    _activeDepths.remove(owner);
    if (_winner == owner) _winner = null;
  }

  void setActive(Object owner, int depth, bool active) {
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

  void _resolve() {
    _resolveScheduled = false;
    Object? newWinner;
    var bestDepth = -1;
    _activeDepths.forEach((owner, depth) {
      if (depth > bestDepth) {
        bestDepth = depth;
        newWinner = owner;
      }
    });
    if (newWinner == _winner) return;
    final oldWinner = _winner;
    _winner = newWinner;
    if (oldWinner != null) _callbacks[oldWinner]?.call(false);
    if (newWinner != null) _callbacks[newWinner]?.call(true);
  }
}
