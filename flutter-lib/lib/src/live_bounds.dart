import 'package:flutter/foundation.dart';

import 'gen/rectangle.dart';

/// The bounds each control last reported for **itself**, keyed by its SWT id.
///
/// A Composite lays its children out from its own copy of them ([NoLayout] constrains each child to
/// the `bounds` it finds in `state.children`), and the same bounds also arrive on the child's own
/// state channel. When the parent's copy is stale it pins the child at that size for good, since an
/// update on the child's own channel cannot influence how its parent lays it out. At 0x0 that means
/// no paint and no semantics node — an invisible widget with healthy state.
///
/// Entries are owner-scoped: Flutter mounts a replacement element before unmounting the one it
/// replaces, so disposal must only erase an entry that is still the disposing State's own.
class LiveBounds {
  LiveBounds._();

  static final Map<int, VRectangle> _bounds = {};
  static final Map<int, Object> _owners = {};
  static final Map<int, _BoundsTick> _ticks = {};

  /// Records what [owner] (a control's State) currently believes its own bounds are.
  static void publish(int id, VRectangle? bounds, Object owner) {
    _owners[id] = owner;
    if (bounds == null) return;
    final previous = _bounds[id];
    if (previous != null && _sameRect(previous, bounds)) return;
    _bounds[id] = bounds;
    _ticks[id]?.tick();
  }

  /// Drops [id]'s entry, but only if [owner] is still the State that owns it.
  static void forget(int id, Object owner) {
    if (!identical(_owners[id], owner)) return;
    _owners.remove(id);
    _bounds.remove(id);
    _ticks.remove(id);
  }

  static VRectangle? of(int id) => _bounds[id];

  /// Notifies whenever any of [ids] reports new bounds for itself. A layout delegate passes this
  /// as its `relayout` so a child's own update re-runs its parent's layout — the parent itself
  /// never rebuilt, which is exactly the case this registry exists for.
  static Listenable? relayoutFor(Iterable<int> ids) {
    final ticks = <Listenable>[];
    for (final id in ids) {
      ticks.add(_ticks.putIfAbsent(id, () => _BoundsTick()));
    }
    return ticks.isEmpty ? null : Listenable.merge(ticks);
  }

  @visibleForTesting
  static void clear() {
    _bounds.clear();
    _owners.clear();
    _ticks.clear();
  }

  static bool _sameRect(VRectangle a, VRectangle b) =>
      a.x == b.x && a.y == b.y && a.width == b.width && a.height == b.height;
}

class _BoundsTick extends ChangeNotifier {
  void tick() => notifyListeners();
}
