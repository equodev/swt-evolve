import 'dart:math' as math;

import 'package:flutter/foundation.dart' show listEquals;
import 'package:flutter/material.dart';
import '../gen/cbanner.dart';
import '../impl/composite_evolve.dart';
import '../theme/theme_extensions/composite_theme_extension.dart';

/// CBanner needs no build() of its own: DartCBannerLayout does the left/right/bottom arrangement
/// in Java, so every child reaches Dart with its final bounds and the plain Composite build lays
/// them out unchanged. Declaring one would be actively harmful — overriding build() bypasses
/// CompositeImpl.buildComposite(), which is what renders `children` at all, so the banner's
/// content (in an Eclipse 3.x workbench, the whole top CoolBar) disappears.
class CBannerImpl<T extends CBannerSwt, V extends VCBanner> extends CompositeImpl<T, V> {
  /// The lines take the theme's border colour, where SWT draws them in COLOR_WIDGET_HIGHLIGHT_SHADOW;
  /// a translucent one is laid over the background first, since the shades are worked out from it.
  @override
  Widget paintOverGround(Widget ground, Color background) => CustomPaint(
    foregroundPainter: CBannerPainter(
      curveLine: state.curveLine,
      bottomLine: state.bottomLine ?? -1,
      borderColor: Color.alphaBlend(Theme.of(context).extension<CompositeThemeExtension>()!.borderColor, background),
      background: background,
    ),
    child: ground,
  );
}

/// What CBanner.onPaint draws: the line above the bottom control, and the curve between the left
/// and the right with the stripe that fades into it along the bottom edge.
class CBannerPainter extends CustomPainter {
  /// The points onPaint joins for the curve, x and y in turn; null when there is no curve.
  final List<int>? curveLine;
  final int bottomLine;
  final Color borderColor;
  final Color background;

  const CBannerPainter({
    required this.curveLine,
    required this.bottomLine,
    required this.borderColor,
    required this.background,
  });

  /// The tail fades over this many points to the left of the curve: CBanner.CURVE_TAIL.
  static const int curveTail = 200;

  @override
  void paint(Canvas canvas, Size size) {
    if (bottomLine >= 0) {
      canvas.drawRect(Rect.fromLTWH(0, bottomLine.toDouble(), size.width, 1), Paint()..color = borderColor);
    }
    final line = curveLine;
    if (line == null || line.length < 4) return;
    // Evolve's display is 24 bits deep, so onPaint always takes its anti-aliased branch: the curve
    // shifted a point either way in a colour three quarters of the way to the background, then the
    // tail, then the curve itself.
    final shade = Paint()
      ..color = Color.fromARGB(
        255,
        _towards(borderColor.r, background.r),
        _towards(borderColor.g, background.g),
        _towards(borderColor.b, background.b),
      );
    _polyline(canvas, line, -1, shade);
    _polyline(canvas, line, 1, shade);
    // The curve starts one point left of its first vertex.
    final curveStart = line[0] - 1;
    final x1 = math.max(0, curveStart - curveTail);
    final tail = Rect.fromLTWH(x1.toDouble(), size.height - 1, (curveStart - x1 + 1).toDouble(), 1);
    canvas.drawRect(tail, Paint()..shader = LinearGradient(colors: [background, borderColor]).createShader(tail));
    _polyline(canvas, line, 0, Paint()..color = borderColor);
  }

  /// from + 3 * (to - from) / 4 in Java's integer arithmetic, on 0-255 channels.
  static int _towards(double fromChannel, double toChannel) {
    final from = (fromChannel * 255).round(), to = (toChannel * 255).round();
    return from + (3 * (to - from)) ~/ 4;
  }

  /// A one-point line through the pixel centres of [points], moved [dx] points sideways.
  static void _polyline(Canvas canvas, List<int> points, int dx, Paint paint) {
    final path = Path()..moveTo(points[0] + dx + 0.5, points[1] + 0.5);
    for (var i = 2; i + 1 < points.length; i += 2) {
      path.lineTo(points[i] + dx + 0.5, points[i + 1] + 0.5);
    }
    canvas.drawPath(
      path,
      paint
        ..style = PaintingStyle.stroke
        ..strokeWidth = 1
        ..strokeCap = StrokeCap.butt
        ..strokeJoin = StrokeJoin.miter,
    );
  }

  @override
  bool shouldRepaint(CBannerPainter old) =>
      !listEquals(old.curveLine, curveLine) ||
      old.bottomLine != bottomLine ||
      old.borderColor != borderColor ||
      old.background != background;
}
