import 'package:flutter/widgets.dart';

import 'veto_gate.dart';

/// Withholds Flutter's own focus traversal inside [child] while a `TraverseListener` could veto it.
///
/// Intercepts the traversal [Action]s, not the key: claiming the key from a `HardwareKeyboard`
/// handler does not stop the focus system, which reaches the same keystroke by its own path.
class TraversalVetoScope extends StatelessWidget {
  const TraversalVetoScope({super.key, required this.gate, required this.child});

  final VetoGate gate;
  final Widget child;

  @override
  Widget build(BuildContext context) {
    return Actions(
      actions: <Type, Action<Intent>>{
        NextFocusIntent: _GatedTraversal<NextFocusIntent>(gate, forward: true),
        PreviousFocusIntent: _GatedTraversal<PreviousFocusIntent>(gate, forward: false),
      },
      child: child,
    );
  }
}

class _GatedTraversal<T extends Intent> extends Action<T> {
  _GatedTraversal(this.gate, {required this.forward});

  final VetoGate gate;
  final bool forward;

  void _move() {
    final node = FocusManager.instance.primaryFocus;
    if (forward) {
      node?.nextFocus();
    } else {
      node?.previousFocus();
    }
  }

  @override
  void invoke(T intent) {
    if (!gate.armed) {
      _move();
      return;
    }
    gate.propose((doit) {
      if (doit) _move();
    });
  }
}
