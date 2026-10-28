import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

/// A focus stop reached by Tab only: a click, or the focus request Flutter Web sends with it, leaves
/// the focus where it was, as on a native tool item.
class TabOnlyFocusNode extends FocusNode {
  TabOnlyFocusNode({super.debugLabel});

  static bool _keyMove = false;

  /// Runs [move] with these stops focusable as if Tab were down, for the other keys that move
  /// between them. Held until the focus change [move] requests has been applied.
  static void keyMove(void Function() move) {
    _keyMove = true;
    try {
      move();
    } finally {
      scheduleMicrotask(() => _keyMove = false);
    }
  }

  @override
  bool get canRequestFocus =>
      super.canRequestFocus &&
      (_keyMove || HardwareKeyboard.instance.isLogicalKeyPressed(LogicalKeyboardKey.tab));
}
