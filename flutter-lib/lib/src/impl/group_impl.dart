import 'package:flutter/material.dart';
import '../swt/group.dart';
import '../impl/composite_impl.dart';
import '../widgets.dart';
import 'widget_config.dart';
import 'styledgroup.dart';

class GroupImpl<T extends GroupSwt, V extends GroupValue>
    extends CompositeImpl<T, V> {
  final bool useDarkTheme = getCurrentTheme();

  @override
  Widget build(BuildContext context) {
    debugPrint("GroupImpl.build - texto: ${state.text}");
    final borderColor =
        useDarkTheme ? const Color(0xFF3F3F3F) : const Color(0xFFD1D1D1);
    final backgroundColor =
        useDarkTheme ? const Color(0xFF1E1E1E) : Colors.white;
    final textColor = useDarkTheme ? Colors.white : const Color(0xFF333333);

    Widget? child;
    if (state.children != null) {
      var groupChildren = state.children!;
      child = mapLayout(state, state.layout, groupChildren);
    }

    return StyledGroup(
      title: state.text ?? "",
      useDarkTheme: useDarkTheme,
      borderColor: borderColor,
      backgroundColor: backgroundColor,
      textColor: textColor,
      child: child,
    );
  }
}
