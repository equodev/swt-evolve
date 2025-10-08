import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import '../gen/control.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../styles.dart';
import 'widget_config.dart';

abstract class ControlImpl<T extends ControlSwt, V extends VControl>
    extends WidgetSwtState<T, V> {
  @override
  Widget build(BuildContext context) {
    return const Text("Control");
  }

  /// Get the SWT background color, with fallback to parent background color
  Color? getSwtBackgroundColor(BuildContext context) {
    var swtBackground = state.background;
    if (swtBackground != null) {
      return Color.fromARGB(
        swtBackground.alpha,
        swtBackground.red,
        swtBackground.green,
        swtBackground.blue,
      );
    }

    // If it has no color of its own, use the parent color
    int? parentColor = getCurrentParentBackgroundColor();
    if (parentColor != null) {
      return Color(0xFF000000 | parentColor);
    }

    return null;
  }

  /// Get the SWT foreground color (no parent fallback)
  Color? getSwtForegroundColor(BuildContext context) {
    var swtForeground = state.foreground;
    if (swtForeground != null) {
      return Color.fromARGB(
        swtForeground.alpha,
        swtForeground.red,
        swtForeground.green,
        swtForeground.blue,
      );
    }

    return null;
  }

  Widget wrap(Widget widget) {
    if (state.style.has(SWT.BORDER)) {
      widget = Container(
          decoration:
              BoxDecoration(border: Border.all(width: 0.5, color: Colors.grey)),
          child: widget);
    }

    if (state.visible != null && !state.visible!) {
      return Visibility(
        visible: false,
        child: widget,
      );
    }
    if (state.enabled != null && !state.enabled!) {
      return Opacity(opacity: 0.35, child: widget);
    }

    // if (state.menu != null) {
    //   return applyMenu(widget);
    // }

    return widget;
  }
}
