import 'package:flutter/widgets.dart';

import 'veto_gate.dart';

/// Holds a context menu until Java's `MenuDetectListener`s have had their say.
///
/// No arming leg: a right-click needs no focus, so Java has no event on which to announce that a
/// veto is possible, and every menu pays one round trip. Affordable for a single gesture, and
/// bounded by the fail-open timer.
class MenuDetectGate {
  MenuDetectGate._();

  static final Map<String, VetoGate> _gates = {};

  /// Call before sending the MenuDetect so queue order matches Java's answers.
  static void withhold(String swt, int id, VoidCallback open) {
    final gate = _gates.putIfAbsent('$swt/$id', () {
      final created = VetoGate.optimistic('menu/verdict');
      created.attach(swt, id);
      return created;
    });
    gate.propose((doit) {
      if (doit) open();
    });
  }
}
