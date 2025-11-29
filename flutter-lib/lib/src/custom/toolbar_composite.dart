import 'package:flutter/material.dart';
import 'package:swtflutter/src/gen/canvas.dart';

import '../gen/composite.dart';
import '../gen/control.dart';
import '../gen/widgets.dart';
import '../impl/composite_evolve.dart';

class ToolbarComposite extends CompositeSwt<VComposite> {
  const ToolbarComposite({super.key, required super.value});

  @override
  State<StatefulWidget> createState() => ToolbarCompositeImpl();
}

class ToolbarCompositeImpl extends CompositeImpl<ToolbarComposite, VComposite> {
  @override
  Widget buildComposite() {
    final children = state.children;

    if (children == null || children.isEmpty) {
      return wrap(const SizedBox.shrink());
    }

    return Row(
        mainAxisSize: MainAxisSize.min,
        children:
            children.map((child) => buildMapWidgetFromValue(child)).toList()
        //.reversed
        //.toList()
        );
  }

  Widget buildMapWidgetFromValue(VControl child) {
    if (child is VComposite && (child.swt == "Composite")) {
      return ToolbarComposite(value: child);
    } else if (child is VCanvas) {
      return ToolbarComposite(value: child);
    }
    return mapWidgetFromValue(child);
  }
}
