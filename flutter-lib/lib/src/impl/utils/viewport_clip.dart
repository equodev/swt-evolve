import 'package:flutter/rendering.dart';
import 'package:flutter/widgets.dart';

/// What the scroll viewports around [context] leave visible, in [box]'s own coordinates, or null
/// when [context] is in no laid-out viewport. A copy drawn in the [Overlay] is clipped to it so that
/// it does not paint over what a viewport has scrolled away.
Rect? visibleViewportRect(BuildContext context, RenderBox box) {
  Rect? visible;
  var scrollable = context.findAncestorStateOfType<ScrollableState>();
  while (scrollable != null) {
    final viewport = scrollable.context.findRenderObject();
    final toBox = viewport is RenderBox && viewport.hasSize
        ? Matrix4.tryInvert(box.getTransformTo(viewport))
        : null;
    if (toBox != null) {
      final rect = MatrixUtils.transformRect(toBox, Offset.zero & (viewport as RenderBox).size);
      visible = visible == null ? rect : visible.intersect(rect);
    }
    scrollable = scrollable.context.findAncestorStateOfType<ScrollableState>();
  }
  return visible;
}

/// Clips to [rect], given in the clipped child's own coordinates.
class FixedRectClipper extends CustomClipper<Rect> {
  const FixedRectClipper(this.rect);

  final Rect rect;

  @override
  Rect getClip(Size size) => rect;

  @override
  Rect getApproximateClipRect(Size size) => rect;

  @override
  bool shouldReclip(FixedRectClipper oldClipper) => oldClipper.rect != rect;
}
