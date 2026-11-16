import 'dart:math' as math;

import 'package:flutter/rendering.dart';

/// Places a drop-down (a Combo's list, a DateTime's calendar) under its field, and above it when
/// it does not fit underneath, as native SWT does: a field on the window's bottom edge would
/// otherwise open its drop-down off-screen. It is kept inside the window's edges too.
class DropDownLayout extends SingleChildLayoutDelegate {
  DropDownLayout(this.field, {this.shrinkToFit = true});

  /// The field's rect, in the coordinates of the overlay the drop-down is shown in.
  final Rect field;

  /// Whether the drop-down is cut to the taller side of the field, as a list that scrolls is. One
  /// that cannot scroll, as a calendar, keeps its size: when it fits on neither side it opens on
  /// the side with more room and is moved over the field until it is inside the window.
  final bool shrinkToFit;

  @override
  BoxConstraints getConstraintsForChild(BoxConstraints constraints) {
    if (!shrinkToFit) return constraints.loosen();
    final double below = constraints.maxHeight - field.bottom;
    return constraints.loosen().copyWith(maxHeight: math.max(0.0, math.max(below, field.top)));
  }

  @override
  Offset getPositionForChild(Size size, Size childSize) {
    final double below = size.height - field.bottom;
    final bool fitsBelow = childSize.height <= below;
    final bool above = !fitsBelow && (childSize.height <= field.top || field.top > below);
    final double y = above ? field.top - childSize.height : field.bottom;
    final double x = math.max(0.0, math.min(field.left, size.width - childSize.width));
    return Offset(x, y.clamp(0.0, math.max(0.0, size.height - childSize.height)));
  }

  @override
  bool shouldRelayout(DropDownLayout oldDelegate) =>
      oldDelegate.field != field || oldDelegate.shrinkToFit != shrinkToFit;
}
