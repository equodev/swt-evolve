import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import '../gen/control.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../styles.dart';

abstract class ControlImpl<T extends ControlSwt, V extends VControl>
    extends WidgetSwtState<T, V> {
  @override
  Widget build(BuildContext context) {
    return const Text("Control");
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
