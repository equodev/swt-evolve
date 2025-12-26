import 'package:flutter/material.dart';
import '../gen/composite.dart';
import '../gen/control.dart';
import '../gen/gc.dart';
import '../gen/canvas.dart';
import '../gen/widgets.dart';
import 'composite_evolve.dart';
import '../custom/toolbar_composite.dart';

abstract class Shape {
  void draw(Canvas c);
  @override
  String toString();

  Rect? get clipRect => null;
}

class CanvasImpl<T extends CanvasSwt, V extends VCanvas>
    extends CompositeImpl<T, V> {
  List<Shape> shapes = [];
  Color bg = Colors.transparent;
  Color fg = Colors.black;
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
    final children = state.children;

    VGC gc = VGC.empty()..id = state.id;
    return GCSwt<VGC>(value: gc);
  }

  Size getBounds() {
    return Size((state.bounds?.width ?? 100).toDouble(),
        (state.bounds?.height ?? 100).toDouble());
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
