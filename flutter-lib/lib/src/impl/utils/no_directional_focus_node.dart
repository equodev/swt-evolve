import 'package:flutter/widgets.dart';

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

/// Flutter's directional traversal, with every [NoDirectionalFocusNode] left out of it.
class ControlAwareDirectionalFocusAction extends DirectionalFocusAction {
  @override
  void invoke(DirectionalFocusIntent intent) {
    NoDirectionalFocusNode.directional(() => super.invoke(intent));
  }
}
