import 'package:flutter/rendering.dart';

/// The part of [box]'s own paint that reaches the screen: every ancestor's paint clip, intersected.
Rect visibleRegion(RenderBox box) {
  var visible = Rect.largest;
  RenderObject child = box;
  for (var parent = child.parent; parent != null; parent = parent.parent) {
    final clip = parent.describeApproximatePaintClip(child);
    if (clip != null && parent is RenderBox) {
      visible = visible.intersect(MatrixUtils.transformRect(parent.getTransformTo(null), clip));
    }
    child = parent;
  }
  return visible;
}
