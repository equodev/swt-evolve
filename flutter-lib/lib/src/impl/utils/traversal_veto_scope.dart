import 'package:flutter/widgets.dart';

import '../focus_requests.dart';
import 'tab_only_focus_node.dart';
import 'veto_gate.dart';

/// Withholds Flutter's own focus traversal inside [child] while a `TraverseListener` could veto it.
///
/// Intercepts the traversal [Action]s, not the key: claiming the key from a `HardwareKeyboard`
/// handler does not stop the focus system, which reaches the same keystroke by its own path.
class TraversalVetoScope extends StatelessWidget {
  const TraversalVetoScope({super.key, required this.gate, required this.child});

  final VetoGate gate;
  final Widget child;

  /// Tab and Shift+Tab for the whole app, for when the client's focus sits above every Display --
  /// on the root or the route scope, where nothing inside [TraversalVetoScope] is asked. Ungated:
  /// no control the client focused is there to veto it.
  static final Map<Type, Action<Intent>> appActions = <Type, Action<Intent>>{
    NextFocusIntent: _GatedTraversal<NextFocusIntent>(null, forward: true),
    PreviousFocusIntent: _GatedTraversal<PreviousFocusIntent>(null, forward: false),
  };

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

  final VetoGate? gate;
  final bool forward;

  void _move() {
    final primary = FocusManager.instance.primaryFocus;
    final node = _traversalStart();
    // The traversal policy moves on from its scope's focused child, not from the node it is
    // called on, so the start has to be that child first.
    if (node != null && node != primary) node.requestFocus();
    // Java may answer after the Tab key is up; the Tab-only stops (tool items) still count as stops.
    TabOnlyFocusNode.keyMove(() {
      if (forward) {
        node?.nextFocus();
      } else {
        node?.previousFocus();
      }
    });
  }

  /// The node Tab moves on from. SWT traverses from its focus control, and Java does not move the
  /// client's keyboard focus when it focuses an ordinary control, so right after a `setFocus()` the
  /// client's own focus can be on no control at all, or on the shell around the focused one. A focus
  /// the client holds on some other control is left alone.
  static FocusNode? _traversalStart() {
    final primary = FocusManager.instance.primaryFocus;
    final holder = FocusRequests.instance.holder;
    if (holder == null) return primary;
    final primaryControl = _innermostControl(primary?.context);
    if (primaryControl != null && !_isWithin(holder, primaryControl)) return primary;
    if (primaryControl == holder) return primary;
    for (final node in FocusManager.instance.rootScope.traversalDescendants) {
      final context = node.context;
      if (context != null && _isWithin(context, holder)) return node;
    }
    return primary;
  }

  static BuildContext? _innermostControl(BuildContext? context) {
    if (context == null) return null;
    final controls = FocusRequests.instance.controls;
    if (controls.contains(context)) return context;
    BuildContext? found;
    context.visitAncestorElements((element) {
      if (controls.contains(element)) found = element;
      return found == null;
    });
    return found;
  }

  static bool _isWithin(BuildContext context, BuildContext ancestor) {
    if (context == ancestor) return true;
    var found = false;
    context.visitAncestorElements((element) {
      found = element == ancestor;
      return !found;
    });
    return found;
  }

  @override
  void invoke(T intent) {
    final gate = this.gate;
    if (gate == null || !gate.armed) {
      _move();
      return;
    }
    gate.propose((doit) {
      if (doit) _move();
    });
  }
}
