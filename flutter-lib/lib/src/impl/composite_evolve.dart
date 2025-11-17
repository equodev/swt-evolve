import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import '../gen/composite.dart';
import '../gen/control.dart';
import '../gen/toolbar.dart';
import '../gen/toolitem.dart';
import '../gen/canvas.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/scrollable_evolve.dart';

class CompositeImpl<T extends CompositeSwt, V extends VComposite>
    extends ScrollableImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    return buildComposite();
  }

  Widget buildComposite() {
    final children = state.children;

    if (children == null || children.isEmpty) {
      return wrap(const Spacer());
    }

    return Row(
      mainAxisSize: MainAxisSize.min,
      children: children
          .map((child) => mapWidgetFromValue(child))
          .toList()
          //.reversed
          //.toList()
    );

  }
}
