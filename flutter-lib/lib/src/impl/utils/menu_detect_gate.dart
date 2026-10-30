import 'package:flutter/widgets.dart';

import 'veto_gate.dart';
import '../../gen/widgets.dart' show WireChannel;

/// Holds a context menu until Java's `MenuDetectListener`s have had their say.
///
/// No arming leg: a right-click needs no focus, so Java has no event on which to announce that a
/// veto is possible, and every menu pays one round trip. Affordable for a single gesture, and
/// bounded by the fail-open timer.
class MenuDetectGate {
  MenuDetectGate._();

  /// One gate per control whose menu it holds, by id, from its first right-click until it goes away.
  static final Map<int, VetoGate> _gates = {};

  /// Call before sending the MenuDetect so queue order matches Java's answers.
  static void withhold(int type, int id, VoidCallback open) {
    final gate = _gates.putIfAbsent(id, () {
      final created = VetoGate.optimistic(WireChannel.menuVerdict);
      created.attach(type, id);
      return created;
    });
    gate.propose((doit) {
      if (doit) open();
    });
  }

  /// Drops the gate of the control [id], when it goes away.
  static void forget(int id) => _gates.remove(id)?.detach();

  @visibleForTesting
  static int get gates => _gates.length;
}
