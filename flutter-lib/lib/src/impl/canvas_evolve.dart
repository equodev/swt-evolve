import 'package:flutter/material.dart';
import '../gen/composite.dart';
import '../gen/control.dart';
import '../gen/gc.dart';
import '../gen/canvas.dart';
import '../gen/widgets.dart';
import 'composite_evolve.dart';
import '../custom/toolbar_composite.dart';
import 'color_utils.dart';
import 'utils/widget_utils.dart';
import '../theme/theme_extensions/canvas_theme_extension.dart';

abstract class Shape {
  void draw(Canvas c);
  @override
  String toString();

  Rect? get clipRect => null;
}

class CanvasImpl<T extends CanvasSwt, V extends VCanvas>
    extends CompositeImpl<T, V> {
  List<Shape> shapes = [];

  CanvasThemeExtension get _theme =>
      Theme.of(context).extension<CanvasThemeExtension>()!;

  Color get bg => colorFromVColor(state.background, defaultColor: _theme.backgroundColor);
  Color get fg => _theme.foregroundColor;
  Color gcBg = Colors.transparent;

  final int _alpha = 255;
  Rect? clipRect;

  Color applyAlpha(Color color) {
    if (_alpha == 255) return color;
    return color.withOpacity(_alpha / 255.0);
  }

  @override
  void initState() {
    super.initState();
    widget.sendPaintPaint(state, null);
  }

  @override
  void dispose() {
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final widgetTheme = _theme;
    final hasValidBounds = hasBounds(state.bounds);
    final constraints = getConstraintsFromBounds(state.bounds);

    // GC is the main content for Canvas - always create to receive draw operations
    final gc = gcOverlay ?? (VGC()..id = state.id);
    Widget child = GCSwt<VGC>(key: gcOverlayKey, value: gc);

    Widget content;
    if (hasValidBounds && constraints != null) {
      content = ConstrainedBox(
        constraints: constraints,
        child: child,
      );
    } else {
      content = SizedBox(
        width: widgetTheme.defaultWidth,
        height: widgetTheme.defaultHeight,
        child: child,
      );
    }

    return Listener(
      onPointerDown: (_) => widget.sendMouseMouseDown(state, null),
      onPointerUp: (_) => widget.sendMouseMouseUp(state, null),
      child: MouseRegion(
        onEnter: (_) => widget.sendMouseTrackMouseEnter(state, null),
        onExit: (_) => widget.sendMouseTrackMouseExit(state, null),
        onHover: (_) => widget.sendMouseTrackMouseHover(state, null),
        child: content,
      ),
    );
  }

  Size getBounds() {
    if (hasBounds(state.bounds)) {
      return Size(
          state.bounds!.width.toDouble(), state.bounds!.height.toDouble());
    }
    return Size(_theme.defaultWidth, _theme.defaultHeight);
  }

  Color rgbMapToColor(Map<String, dynamic> m) => Color.fromARGB(
        (m['alpha'] ?? 255) as int,
        (m['red'] ?? 0) as int,
        (m['green'] ?? 0) as int,
        (m['blue'] ?? 0) as int,
      );
}

/* ─────────────── ScenePainter ─────────────── */

class ScenePainter extends CustomPainter {
  ScenePainter(this.bg, this.shapes);
  final Color bg;
  final List<Shape> shapes;

  @override
  void paint(Canvas canvas, Size size) {
    canvas.drawRect(Offset.zero & size, Paint()..color = bg);
    for (final s in shapes) {
      s.draw(canvas);
    }
  }

  @override
  bool shouldRepaint(ScenePainter o) => o.bg != bg || o.shapes != shapes;
}
