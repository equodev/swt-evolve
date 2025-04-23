import 'package:fluent_ui/fluent_ui.dart';
import 'package:flutter/widgets.dart';
import 'package:swtflutter/src/swt/composite.dart';
import 'package:swtflutter/src/swt/control.dart';
import 'package:swtflutter/src/swt/widget.dart';
import 'package:swtflutter/src/widgets.dart';

class NoLayout extends StatelessWidget {
  final CompositeValue composite;
  final List<WidgetValue> children;

  NoLayout({super.key, required this.children, required this.composite});

  final List<ControlValue> sizes = [];

  @override
  Widget build(BuildContext context) {
    return CustomMultiChildLayout(
        delegate: _AbsoluteLayoutDelegate(children, composite),
        children: [
          for (var child in children)
            LayoutId(id: child.id, child: mapWidgetFromValue(child))
        ]);
  }
}

class _AbsoluteLayoutDelegate extends MultiChildLayoutDelegate {
  List<WidgetValue> children;
  CompositeValue composite;

  _AbsoluteLayoutDelegate(this.children, this.composite);

  @override
  Size getSize(BoxConstraints constraints) {
    final bounds = composite.bounds;
    if (bounds != null) {
      return Size(bounds.width.toDouble(), bounds.height.toDouble());
    }
    return super.getSize(constraints);
  }

  @override
  void performLayout(Size size) {
    for (var child in children.whereType<ControlValue>()) {
      var w = child.bounds?.width.toDouble();
      var h = child.bounds?.height.toDouble();
      var x = child.bounds?.x ?? 0;
      var y = child.bounds?.y ?? 0;
      layoutChild(child.id, BoxConstraints.tightFor(width: w, height: h));
      positionChild(child.id, Offset(x.toDouble(), y.toDouble()));
    }
  }

  @override
  bool shouldRelayout(covariant _AbsoluteLayoutDelegate oldDelegate) {
    if (children.length != oldDelegate.children.length) {
      return true;
    }

    for (int i = 0; i < children.length; i++) {
      final currentChild = children[i];
      final oldChild = oldDelegate.children[i];

      if (currentChild.id != oldChild.id ||
          currentChild is! ControlValue ||
          oldChild is! ControlValue) {
        return true;
      }

      final currentBounds = currentChild.bounds;
      final oldBounds = oldChild.bounds;

      if (currentBounds == null && oldBounds != null ||
          currentBounds != null && oldBounds == null) {
        return true;
      }

      if (currentBounds != null && oldBounds != null) {
        if (currentBounds.x != oldBounds.x ||
            currentBounds.y != oldBounds.y ||
            currentBounds.width != oldBounds.width ||
            currentBounds.height != oldBounds.height) {
          return true;
        }
      }
    }

    return false;
  }
}
