// A GC paint is a list of shapes, and each canvas call is a draw the renderer has to issue. A run of
// shapes drawn together has to paint exactly what drawing them one by one would, in fewer calls:
// one clip for shapes that share it, and one triangle list for consecutive straight strokes and
// convex fills. They may differ in colour, width and opacity, because triangles in one list are
// blended in the order they are listed, exactly as separate draws are.

import 'dart:math' as math;
import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';

/// Records the canvas calls a run makes; anything else is a no-op.
class _Calls implements ui.Canvas {
  final List<String> log = [];

  @override
  void save() => log.add('save');

  @override
  void restore() => log.add('restore');

  @override
  void clipRect(Rect rect, {ui.ClipOp clipOp = ui.ClipOp.intersect, bool doAntiAlias = true}) =>
      log.add('clip $rect');

  @override
  void drawLine(Offset p1, Offset p2, Paint paint) => log.add('line');

  @override
  void drawVertices(ui.Vertices vertices, BlendMode blendMode, Paint paint) => log.add('triangles');

  @override
  Float64List getTransform() => Float64List.fromList([1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1]);

  @override
  void translate(double dx, double dy) {}

  @override
  void drawOval(Rect rect, Paint paint) => log.add('oval');

  @override
  void drawPath(Path path, Paint paint) => log.add('path');

  @override
  void clipPath(Path path, {bool doAntiAlias = true}) => log.add('clipPath');

  @override
  dynamic noSuchMethod(Invocation invocation) => null;
}

Future<Uint8List> _render(void Function(ui.Canvas) paint) async {
  final recorder = ui.PictureRecorder();
  final canvas = ui.Canvas(recorder)
    ..drawRect(const Rect.fromLTWH(0, 0, 60, 60), Paint()..color = const Color(0xFFFFFFFF));
  paint(canvas);
  final image = await recorder.endRecording().toImage(60, 60);
  return (await image.toByteData(format: ui.ImageByteFormat.rawRgba))!.buffer.asUint8List();
}

void main() {
  const black = Color(0xFF000000);
  const red = Color(0xFFFF0000);
  const clip = Rect.fromLTWH(0, 0, 50, 50);

  // Even widths: an odd one is shifted half a pixel inside its own save/restore when drawn alone,
  // which is not what these are about.
  LineShape line(double x, {Color color = black, double width = 2, Rect? clipRect, int cap = SWT.CAP_FLAT}) =>
      LineShape(Offset(x, 0), Offset(x, 10), color, width, cap, 1, clipRect);

  test('consecutive straight strokes are one call, whatever their colour and width', () {
    final calls = _Calls();
    drawShapeRun(calls, [line(1), line(2, color: red), line(3, width: 4), line(4, color: red.withAlpha(128))]);

    expect(calls.log, ['triangles']);
  });

  test('a run longer than one triangle list can index goes as several', () {
    final calls = _Calls();
    drawShapeRun(calls, [for (var i = 0; i < 9000; i++) line((i % 50).toDouble())]);

    expect(calls.log, ['triangles', 'triangles']);
  });

  test('a round-capped stroke is drawn as a stroke and ends the run', () {
    final calls = _Calls();
    drawShapeRun(calls, [line(1), line(2), line(3, cap: SWT.CAP_ROUND), line(4), line(5)]);

    expect(calls.log, ['triangles', 'line', 'triangles']);
  });

  test('a lone stroke is drawn as it always was', () {
    final calls = _Calls();
    drawShapeRun(calls, [line(1)]);

    expect(calls.log, ['line']);
  });

  test('shapes that share a clip are clipped once', () {
    final calls = _Calls();
    drawShapeRun(calls, [
      line(1, clipRect: clip),
      OvalShape(const Rect.fromLTWH(0, 0, 5, 5), black, 2, isFilled: false, clipRect: clip),
      line(2, color: red, clipRect: clip),
    ]);

    expect(calls.log, ['save', 'clip $clip', 'line', 'oval', 'line', 'restore']);
  });

  test('a shape without the clip closes it first', () {
    final calls = _Calls();
    drawShapeRun(calls, [line(1, clipRect: clip), line(2, color: red)]);

    expect(calls.log, ['save', 'clip $clip', 'line', 'restore', 'line']);
  });

  test('crossing translucent strokes in one run blend as drawn one by one', () async {
    final across = SegmentsShape(Float32List.fromList([5, 30, 55, 30]), red.withAlpha(128), 6, SWT.CAP_FLAT);
    final down = SegmentsShape(Float32List.fromList([30, 5, 30, 55]), const Color(0x800000FF), 6, SWT.CAP_FLAT);

    final run = await _render((c) => drawShapeRun(c, [across, down]));
    final apart = await _render((c) {
      across.draw(c);
      down.draw(c);
    });

    expect(run, apart);
  });

  test('a stroke drawn later stays on top where they cross', () async {
    final first = SegmentsShape(Float32List.fromList([5, 30, 55, 30]), red, 6, SWT.CAP_FLAT);
    final later = SegmentsShape(Float32List.fromList([30, 5, 30, 55]), const Color(0xFF0000FF), 6, SWT.CAP_FLAT);

    final pixels = await _render((c) => drawShapeRun(c, [first, later]));
    final centre = (30 * 60 + 30) * 4;

    expect(pixels.sublist(centre, centre + 3), [0, 0, 255]);
  });

  PolygonShape hexagon(double cx, double cy, Color color, {double r = 12}) => PolygonShape(
      [for (var k = 0; k < 6; k++) ...[
        (cx + r * math.cos(k * math.pi / 3)).round(),
        (cy + r * math.sin(k * math.pi / 3)).round()
      ]],
      color, 0, 1, 1,
      isFilled: true);

  test('convex fills and strokes of any paint share one triangle list', () {
    final calls = _Calls();
    drawShapeRun(calls, [hexagon(20, 20, red), line(5), hexagon(30, 30, black.withAlpha(120))]);

    expect(calls.log, ['triangles']);
  });

  test('a star is not convex, so it stays a path and ends the run', () {
    final star = PolygonShape([30, 5, 40, 50, 5, 20, 55, 20, 20, 50], red, 0, 1, 1, isFilled: true);
    final calls = _Calls();
    drawShapeRun(calls, [hexagon(20, 20, black), hexagon(40, 40, red), star]);

    expect(calls.log, ['triangles', 'path']);
  });

  test('overlapping translucent fills as a run paint as their paths do', () async {
    final shapes = [hexagon(25, 25, const Color(0x78FF0000), r: 18), hexagon(35, 32, const Color(0x780000FF), r: 18)];
    final run = await _render((c) => drawShapeRun(c, shapes));
    final apart = await _render((c) {
      for (final s in shapes) {
        s.draw(c);
      }
    });
    var worst = 0, total = 0;
    for (var i = 0; i < run.length; i++) {
      final d = (run[i] - apart[i]).abs();
      if (d > worst) worst = d;
      total += d;
    }
    // Edges are antialiased by different means, so a pixel may differ a little; nothing more.
    expect(worst, lessThan(48), reason: 'total difference $total');
    expect(total / run.length, lessThan(1.0));
  });

  test('consecutive shapes under the same path clip are clipped once, and drawn as a run', () {
    final clip = Path()..addOval(const Rect.fromLTWH(0, 0, 40, 40));
    final calls = _Calls();
    drawShapeRun(calls, [
      ClipPathShape(clip, [line(1)]),
      ClipPathShape(clip, [line(2, color: red)]),
      ClipPathShape(Path()..addOval(const Rect.fromLTWH(5, 5, 30, 30)), [line(3)]),
    ]);

    expect(calls.log, ['save', 'clipPath', 'triangles', 'restore', 'save', 'clipPath', 'line', 'restore']);
  });

  test('clipping consecutive shapes once paints what clipping each does', () async {
    final clip = Path()..addOval(const Rect.fromLTWH(5, 5, 50, 50));
    final shapes = [
      ClipPathShape(clip, [hexagon(25, 25, const Color(0x78FF0000), r: 18)]),
      ClipPathShape(clip, [OvalShape(const Rect.fromLTWH(10, 20, 40, 25), const Color(0x800000FF), 3, isFilled: false)]),
      ClipPathShape(clip, [line(30, color: black)]),
    ];
    final once = await _render((c) => drawShapeRun(c, shapes));
    final each = await _render((c) {
      for (final s in shapes) {
        for (final child in s.children) {
          c.save();
          c.clipPath(s.path);
          child.draw(c);
          c.restore();
        }
      }
    });

    expect(once, each);
  });
}
