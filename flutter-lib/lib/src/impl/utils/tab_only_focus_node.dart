import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

/// A focus stop reached by Tab only: a click, or the focus request Flutter Web sends with it, leaves
/// the focus where it was, as on a native tool item.
class TabOnlyFocusNode extends FocusNode {
  TabOnlyFocusNode({super.debugLabel});

  @override
  bool get canRequestFocus =>
      super.canRequestFocus && HardwareKeyboard.instance.isLogicalKeyPressed(LogicalKeyboardKey.tab);
}
