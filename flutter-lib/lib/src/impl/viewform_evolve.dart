import 'package:flutter/material.dart';
import '../gen/viewform.dart';
import '../impl/composite_evolve.dart';
import '../theme/theme_extensions/composite_theme_extension.dart';

class ViewFormImpl<T extends ViewFormSwt, V extends VViewForm> extends CompositeImpl<T, V> {
  @override
  Widget build(BuildContext context) => buildComposite();

  /// The lines take the theme's border colour and the band its focused-border colour, where
  /// SWT draws them in COLOR_WIDGET_NORMAL_SHADOW and COLOR_LIST_SELECTION; a translucent one is
  /// laid over the form's background first, as SWT's colours are opaque.
  @override
  Widget paintOverGround(Widget ground, Color background) {
    final theme = Theme.of(context).extension<CompositeThemeExtension>()!;
    return CustomPaint(
      foregroundPainter: ViewFormPainter(
        border: state.borderVisible ?? false,
        highlight: state.highlight ?? 0,
        separator: state.separator ?? -1,
        borderColor: Color.alphaBlend(theme.borderColor, background),
        highlightColor: Color.alphaBlend(theme.focusedBorderColor, background),
      ),
      child: ground,
    );
  }
}

/// What ViewForm.onPaint draws: a one-point border round the form, the highlight band inside it,
/// and the separator line between the top row and the content.
class ViewFormPainter extends CustomPainter {
  final bool border;
  final int highlight;
  final int separator;
  final Color borderColor;
  final Color highlightColor;

  const ViewFormPainter({
    required this.border,
    required this.highlight,
    required this.separator,
    required this.borderColor,
    required this.highlightColor,
  });

  @override
  void paint(Canvas canvas, Size size) {
    final w = size.width, h = size.height;
    final line = Paint()..color = borderColor;
    if (border) {
      // drawRectangle(0, 0, w - 1, h - 1): the outermost ring of pixels.
      canvas
        ..drawRect(Rect.fromLTWH(0, 0, w, 1), line)
        ..drawRect(Rect.fromLTWH(0, h - 1, w, 1), line)
        ..drawRect(Rect.fromLTWH(0, 0, 1, h), line)
        ..drawRect(Rect.fromLTWH(w - 1, 0, 1, h), line);
      if (highlight > 0) {
        // The polygon from (1, 1) to (w - 1, h - 1) with its middle cut out, [highlight] wide.
        final band = Path()
          ..fillType = PathFillType.evenOdd
          ..addRect(Rect.fromLTRB(1, 1, w - 1, h - 1))
          ..addRect(Rect.fromLTRB(1.0 + highlight, 1.0 + highlight, w - 1 - highlight, h - 1 - highlight));
        canvas.drawPath(band, Paint()..color = highlightColor);
      }
    }
    if (separator > -1) {
      // drawLine(borderLeft + highlight, separator, w - borderLeft - borderRight - highlight, separator).
      final inset = border ? 1 : 0;
      final x1 = (inset + highlight).toDouble();
      final x2 = w - 2 * inset - highlight;
      canvas.drawRect(Rect.fromLTRB(x1, separator.toDouble(), x2 + 1, separator + 1.0), line);
    }
  }

  @override
  bool shouldRepaint(ViewFormPainter old) =>
      old.border != border ||
      old.highlight != highlight ||
      old.separator != separator ||
      old.borderColor != borderColor ||
      old.highlightColor != highlightColor;
}
