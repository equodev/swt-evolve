import 'package:flutter/widgets.dart';

import '../control_evolve.dart' show ControlImpl;
import '../menu_evolve.dart' show MenuImpl;
import '../menuitem_evolve.dart' show MenuItemImpl;

/// A Composite's own focus stop, which an arrow key neither walks onto nor off: SWT moves the focus
/// between controls by Tab and by the application only, while Flutter's directional traversal, which
/// a menu navigates with, would move it with the arrows.
class NoDirectionalFocusNode extends FocusNode {
  NoDirectionalFocusNode({super.debugLabel});

  static bool _directional = false;

  @override
  bool get canRequestFocus => super.canRequestFocus && !_directional;

  /// Runs Flutter's directional traversal [move], which these stops sit out.
  static void directional(VoidCallback move) {
    if (FocusManager.instance.primaryFocus is NoDirectionalFocusNode) return;
    _directional = true;
    try {
      move();
    } finally {
      _directional = false;
    }
  }
}

/// Flutter's directional traversal, for a menu, with every [NoDirectionalFocusNode] left out of it.
/// An arrow in a control moves no focus here: Java traverses it where SWT does (a radio group, a
/// tool bar), and the control Flutter would find in that direction is seldom the one SWT names.
class ControlAwareDirectionalFocusAction extends DirectionalFocusAction {
  @override
  void invoke(DirectionalFocusIntent intent) {
    final context = FocusManager.instance.primaryFocus?.context;
    if (context != null && _inControl(context)) return;
    NoDirectionalFocusNode.directional(() => super.invoke(intent));
  }

  /// Whether the nearest menu or control around [context] is a control: a menu bar is drawn inside
  /// its Shell.
  static bool _inControl(BuildContext context) {
    var inControl = false;
    context.visitAncestorElements((element) {
      if (element is! StatefulElement) return true;
      final state = element.state;
      if (state is MenuImpl || state is MenuItemImpl) return false;
      if (state is ControlImpl) {
        inControl = true;
        return false;
      }
      return true;
    });
    return inControl;
  }
}
