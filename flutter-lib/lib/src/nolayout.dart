import 'package:flutter/widgets.dart';
import 'gen/composite.dart';
import 'gen/control.dart';
import 'gen/widget.dart';
import 'gen/widgets.dart';

class NoLayout extends StatelessWidget {
  final VComposite composite;
  final List<VControl> children;

  const NoLayout({super.key, required this.children, required this.composite});

  // final List<VControl> sizes = [];

  @override
  Widget build(BuildContext context) {
    final visibleChildren =
        children.where((child) => child.visible == true).toList();

    return CustomMultiChildLayout(
        delegate: _AbsoluteLayoutDelegate(visibleChildren, composite),
        children: [
          for (var child in visibleChildren)
            LayoutId(id: child.id, child: mapWidgetFromValue(child))
        ]);
  }
}

class _AbsoluteLayoutDelegate extends MultiChildLayoutDelegate {
  List<VControl> children;
  VComposite composite;

  _AbsoluteLayoutDelegate(this.children, this.composite);

  @override
  Size getSize(BoxConstraints constraints) {
    final bounds = composite.bounds;
    if (bounds != null && bounds.width != 0 && bounds.height != 0) {
      return Size(bounds.width.toDouble(), bounds.height.toDouble());
    }
    return super.getSize(constraints);
  }

  @override
  void performLayout(Size size) {
    for (var child in children.whereType<VControl>()) {
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

      if (currentChild.id != oldChild.id) {
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
