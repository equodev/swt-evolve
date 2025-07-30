import 'package:fluent_ui/fluent_ui.dart';
import 'package:flutter/widgets.dart';
import '../comm/comm.dart';
import '../gen/canvas.dart';
import '../gen/widget.dart';
import '../impl/composite_evolve.dart';

// SHAPES
abstract class Shape {
  void draw(Canvas c);
  @override String toString();

// Each shape can have an associated clip
  Rect? get clipRect => null;
}

class RectShape extends Shape {
  RectShape(this.rect, this.color, this.strokeWidth, this.lineCap, this.lineJoin, [this.clipRect]);

  final Rect rect;
  final Color color;
  final double strokeWidth;
  final int lineCap;
  final int lineJoin;
  @override
  final Rect? clipRect;

  @override
  void draw(Canvas c) {
    if (clipRect != null) {
      c.save();
      c.clipRect(clipRect!);
    }

    c.drawRect(
        rect,
        Paint()
          ..color = color
          ..style = PaintingStyle.stroke
          ..strokeWidth = strokeWidth
          ..strokeCap = _getStrokeCap(lineCap)
          ..strokeJoin = _getStrokeJoin(lineJoin)
    );

    if (clipRect != null) {
      c.restore();
    }
  }

  @override
  String toString() => 'Rect $rect (width: $strokeWidth)${clipRect != null ? " [clipped]" : ""}';

  StrokeCap _getStrokeCap(int swtCap) {
    switch (swtCap) {
      case 1: return StrokeCap.butt;
      case 2: return StrokeCap.round;
      case 3: return StrokeCap.square;
      default: return StrokeCap.butt;
    }
  }

  StrokeJoin _getStrokeJoin(int swtJoin) {
    switch (swtJoin) {
      case 1: return StrokeJoin.miter;
      case 2: return StrokeJoin.round;
      case 3: return StrokeJoin.bevel;
      default: return StrokeJoin.miter;
    }
  }
}


class CanvasImpl<T extends CanvasSwt, V extends VCanvas>
    extends CompositeImpl<T, V> {
  List<Shape> shapes = [];
  Color bg = Colors.transparent;
  Color fg = Colors.black;
  Color gcBg = Colors.transparent;

  double _lineWidth = 1.0;
  int _lineCap = 1;
  int _lineJoin = 2;
  int _alpha = 255;
  Rect? clipRect;

  Color applyAlpha(Color color) {
    if (_alpha == 255) return color;
    return color.withOpacity(_alpha / 255.0);
  }

  @override
  void initState() {
    super.initState();
    registerListeners();
    widget.sendPaintPaint(state, null);
  }

  @override
  void dispose() {
    //_caretBlinkTimer?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext ctx) => CustomPaint(
    size: const Size(100, 100),
    painter: ScenePainter(bg, List.unmodifiable(shapes)),
  );

  // LISTENERS
  void registerListeners() {
    _bgListener();
    _gcBgColorListener();
    _fgColorListener();
  }

  Color rgbMapToColor(Map<String, dynamic> m) =>
      Color.fromARGB(
        (m['alpha'] ?? 255) as int,
        (m['red'] ?? 0) as int,
        (m['green'] ?? 0) as int,
        (m['blue'] ?? 0) as int,
      );

  void _bgListener() =>
      EquoCommService.onRaw("/Control/${state.id}/SetBackground",
              (p) => p is Color ? setState(() => bg = p) : null);

  void _gcBgColorListener() =>
      EquoCommService.onRaw("/GC/${state.id}/SetBackground", (p) {
        if (p is Color) {
          setState(() => gcBg = p);
        } else if (p is Map<String, dynamic>) {
          final r = (p['red'] ?? 0) as int;
          final g = (p['green'] ?? 0) as int;
          final b = (p['blue'] ?? 0) as int;
          final a = (p['alpha'] ?? 255) as int;
          setState(() => gcBg = Color.fromARGB(a, r, g, b));
        }
      });

  void _alphaListener() =>
      EquoCommService.onRaw("/GC/${state.id}/SetAlpha", (p) {
        if (p is Map<String, dynamic>) {
          final alpha = (p['alpha'] ?? 255) as int;
          setState(() => _alpha = alpha.clamp(0, 255));
        }
      });

  void _fgColorListener() =>
      EquoCommService.onRaw("/GC/${state.id}/SetForeground", (p) {
        if (p is Color) {
          setState(() => fg = p);
        } else if (p is Map<String, dynamic>) {
          setState(() => fg = rgbMapToColor(p));
        }
      });
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

