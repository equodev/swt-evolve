import 'package:flutter/material.dart';

import '../widget_config.dart';

/// Draws the app at the factor SWT's zoom implies, and lays it out in the space it is drawn into
/// — `Transform.scale` changes what is painted, not the constraints the child is handed, and SWT
/// positions widgets across the area it was told about.
///
/// Mount it above the Navigator (`MaterialApp.builder`), so the overlay every popup mounts into is
/// inside the same transform. Material places a context menu at `anchorRect + localPosition`, where
/// the anchor is measured against the overlay and the position comes from the gesture: with the
/// overlay left outside, those two are in different spaces and the menu opens at a fraction of the
/// distance the pointer travelled. The same split would leave tooltips, toasts and dropdowns drawn
/// at the view's scale instead of the app's.
///
/// The chain is unconditional, identity factor included: dropping it at scale 1.0 would move every
/// widget's depth on the rebuild that first applies a zoom, and Flutter discards the State — and
/// with it each widget's update subscription — of everything below a depth change.
class SwtZoomScale extends StatelessWidget {
  const SwtZoomScale({super.key, required this.child});

  final Widget child;

  @override
  Widget build(BuildContext context) {
    return ValueListenableBuilder<double>(
      valueListenable: appScaleNotifier,
      builder: (context, scale, _) => LayoutBuilder(
        builder: (context, constraints) {
          final media = MediaQuery.of(context);
          final w = constraints.hasBoundedWidth
              ? constraints.maxWidth / scale
              : double.infinity;
          final h = constraints.hasBoundedHeight
              ? constraints.maxHeight / scale
              : double.infinity;
          return Transform.scale(
            scale: scale,
            alignment: Alignment.topLeft,
            child: OverflowBox(
              alignment: Alignment.topLeft,
              minWidth: 0,
              maxWidth: w,
              minHeight: 0,
              maxHeight: h,
              child: SizedBox(
                width: w.isFinite ? w : null,
                height: h.isFinite ? h : null,
                child: MediaQuery(
                  data: media.copyWith(
                    size: Size(w.isFinite ? w : media.size.width,
                        h.isFinite ? h : media.size.height),
                  ),
                  child: child,
                ),
              ),
            ),
          );
        },
      ),
    );
  }
}
