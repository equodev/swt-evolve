import 'package:flutter/foundation.dart';
import 'package:flutter/widgets.dart';

import '../../gen/region.dart';

/// Clips a control to the SWT `Region` it was set to.
///
/// A region is a set of disjoint rectangles rather than one, so its bounds are not its shape: an
/// application draws an outline by adding a rectangle and subtracting the middle, and clipping to
/// the bounds instead would fill in exactly the hole that makes it an outline. The Eclipse
/// workbench frames its drag-and-drop feedback that way — a shell as large as the whole window
/// whose region is a few thin bars — so ignoring the region paints over the application.
///
/// Coordinates are the control's own, which is what `Region#add` was given.
class RegionClip extends StatelessWidget {
  const RegionClip({super.key, required this.rects, required this.child});

  final List<int> rects;
  final Widget child;

  /// Wraps [child] only when there is a region to honour. A control with no region set has none.
  static Widget maybe(VRegion? region, Widget child) {
    final rects = region?.rects;
    if (rects == null) return child;
    return RegionClip(rects: rects, child: child);
  }

  @override
  Widget build(BuildContext context) =>
      ClipPath(clipper: _RegionClipper(rects), child: child);
}

class _RegionClipper extends CustomClipper<Path> {
  const _RegionClipper(this.rects);

  final List<int> rects;

  @override
  Path getClip(Size size) {
    final path = Path();
    // A region with no rectangles covers nothing, and an empty path clips everything away — which
    // is what SWT does with one, rather than leaving the control whole.
    for (int at = 0; at + 3 < rects.length; at += 4) {
      path.addRect(Rect.fromLTWH(
        rects[at].toDouble(),
        rects[at + 1].toDouble(),
        rects[at + 2].toDouble(),
        rects[at + 3].toDouble(),
      ));
    }
    return path;
  }

  @override
  bool shouldReclip(_RegionClipper oldClipper) =>
      !listEquals(rects, oldClipper.rects);
}
