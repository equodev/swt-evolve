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
import 'package:swtflutter/src/impl/widget_config.dart' show appScaleNotifier;

/// Records the canvas calls a run makes; anything else is a no-op.
class _Calls implements ui.Canvas {
  _Calls([this.factor = 1, this.dx = 0]);
  final double factor, dx;
  final List<String> log = [];

  @override
  void drawRect(Rect rect, Paint paint) => log.add('rect');

  @override
  void drawParagraph(ui.Paragraph paragraph, Offset offset) => log.add('text');

  // Text recorded once per layout is drawn as that picture.
  @override
  void drawPicture(ui.Picture picture) => log.add('text');

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
  Float64List getTransform() =>
      Float64List.fromList([factor, 0, 0, 0, 0, factor, 0, 0, 0, 0, 1, 0, dx, 0, 0, 1]);

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

  // gc.rects' frame: fills and outlines in turn, overlapping, in translucent and opaque colours, down
  // to a pixel wide or high.
  List<Shape> rects(double width) => [
        for (var i = 0; i < 14; i++) ...[
          RectShape(
              Rect.fromLTWH(3.0 + i * 3, 4.0 + (i * 7) % 30, i < 2 ? 1 : 6.0 + i * 2,
                  i == 1 ? 1 : 5.0 + (i * 5) % 17),
              i.isEven ? red : const Color(0x800000FF), 0, SWT.CAP_FLAT, SWT.JOIN_MITER,
              isFilled: true),
          RectShape(
              Rect.fromLTWH(3.0 + i * 3, 4.0 + (i * 7) % 30, i < 2 ? 1 : 6.0 + i * 2,
                  i == 1 ? 2 : 5.0 + (i * 5) % 17),
              i.isEven ? black : const Color(0x8000FF00), width, SWT.CAP_FLAT, SWT.JOIN_MITER),
        ]
      ];

  Future<void> expectSamePixels(List<Shape> shapes, void Function(ui.Canvas) setUp) async {
    final run = await _render((c) {
      setUp(c);
      drawShapeRun(c, shapes);
    });
    final apart = await _render((c) {
      setUp(c);
      for (final s in shapes) {
        s.draw(c);
      }
    });
    var differing = 0, worst = 0;
    for (var i = 0; i < run.length; i++) {
      final d = (run[i] - apart[i]).abs();
      if (d != 0) differing++;
      if (d > worst) worst = d;
    }
    expect(differing, 0, reason: 'worst channel difference $worst');
  }

  for (final width in [0.0, 1.0, 2.0, 3.0]) {
    for (final scale in [1.0, 2.0]) {
      // A hairline is one device pixel: at a device scale of 2 that is half a unit, off the grid.
      final batched = width > 0 || scale == 1;
      test(
          'fills and outlines of width $width at scale $scale are '
          '${batched ? "one triangle list" : "drawn one by one"}, and paint the same pixels', () async {
        final shapes = rects(width);
        final calls = _Calls(scale);
        drawShapeRun(calls, shapes);
        // Off the grid, a hairline is drawn on its own; fills it does not touch may still join a run past it.
        if (batched) {
          expect(calls.log.toSet(), {'triangles'});
        } else {
          expect(calls.log, contains('rect'));
        }

        await expectSamePixels(shapes, (c) => c.scale(scale));
      });
    }
  }

  test('rectangles off the device grid are drawn one by one', () async {
    for (final calls in [_Calls(1.25), _Calls(1, 0.5)]) {
      drawShapeRun(calls, rects(2));
      expect(calls.log.toSet(), {'rect'}, reason: 'scale ${calls.factor}, offset ${calls.dx}');
    }
    await expectSamePixels(rects(2), (c) => c.scale(1.25));
    await expectSamePixels(rects(0), (c) => c.translate(0.5, 0));
  });

  test('without antialiasing, fills and wide outlines still paint the same pixels as a run', () async {
    final calls = _Calls();
    RenderHintsShape(false, null, rects(2)).draw(calls);
    expect(calls.log.toSet(), {'triangles'});
    await expectSamePixels([RenderHintsShape(false, null, rects(2))], (c) {});
  });

  // On screen the canvas transform leaves out the view's pixel ratio and the app's zoom.
  test('on screen, the view ratio and the app zoom decide which rectangles are on the device grid', () {
    final fills = [for (final s in rects(2)) if ((s as RectShape).isFilled) s];
    List<String> painted({required double ratio, double zoom = 1, List<Shape>? shapes}) {
      final before = appScaleNotifier.value;
      appScaleNotifier.value = zoom;
      try {
        final calls = _Calls();
        ScenePainter(const Color(0xFFFFFFFF), shapes ?? fills, devicePixelRatio: ratio)
            .paint(calls, const Size(60, 60));
        return calls.log.where((call) => call == 'triangles' || call == 'rect').skip(1).toList();
      } finally {
        appScaleNotifier.value = before;
      }
    }

    expect(painted(ratio: 2).toSet(), {'triangles'});
    expect(painted(ratio: 1.5).toSet(), {'rect'});
    expect(painted(ratio: 1, zoom: 1.25).toSet(), {'rect'});
    expect(painted(ratio: 1, shapes: rects(0)).toSet(), {'triangles'});
    expect(painted(ratio: 2, shapes: rects(0)), contains('rect'),
        reason: 'a hairline is half a unit at a device scale of 2');
    expect(painted(ratio: 1, zoom: 2, shapes: rects(0)), contains('rect'));
  });

  test('without antialiasing, a hairline is drawn on its own', () {
    final calls = _Calls();
    RenderHintsShape(false, null, rects(0)).draw(calls);
    expect(calls.log, contains('rect'));
  });

  test('a clipped focus rectangle leaves the run its clip, and every save its restore', () {
    final calls = _Calls();
    drawShapeRun(calls, [
      FocusRectShape(const Rect.fromLTWH(2, 2, 20, 10), black, clip),
      line(30, clipRect: clip),
    ]);

    expect(calls.log, ['save', 'clip $clip', 'path', 'line', 'restore']);
  });

  // A bar chart's frame: each bar, then its label, in turn.
  List<Shape> bars({required bool labelsOnBars}) => [
        for (var i = 0; i < 6; i++) ...[
          RectShape(Rect.fromLTWH(2.0 + i * 9, 30, 6, 20), i.isEven ? red : black, 0, SWT.CAP_FLAT,
              SWT.JOIN_MITER, isFilled: true),
          TextShape('$i', Offset(2.0 + i * 9, labelsOnBars ? 34 : 2),
              const TextStyle(fontSize: 8, color: Color(0xFF0000FF))),
        ]
      ];

  test('bars whose labels touch none of them are one triangle list, the labels drawn after it', () async {
    final calls = _Calls();
    drawShapeRun(calls, bars(labelsOnBars: false));
    final drawn = calls.log.where((call) => call == 'triangles' || call == 'text' || call == 'rect').toList();

    expect(drawn, ['triangles', for (var i = 0; i < 6; i++) 'text']);
    await expectSamePixels(bars(labelsOnBars: false), (c) {});
  });

  test('a bar does not move before a label it would paint over', () async {
    final calls = _Calls();
    drawShapeRun(calls, bars(labelsOnBars: true));

    expect(calls.log.where((call) => call == 'triangles'), isEmpty,
        reason: 'each label overlaps the bar after it in the list: ${calls.log}');
    await expectSamePixels(bars(labelsOnBars: true), (c) {});
  });

  // A diagram's frame as draw2d paints it: each figure under a clip of its own bounds, filled,
  // outlined and labelled.
  List<Shape> figures({double inset = 0}) => [
        for (var i = 0; i < 4; i++)
          for (final shape in [
            RectShape(Rect.fromLTWH(2.0 + i * 14, 6, 11, 20), i.isEven ? red : const Color(0x8000FF00), 0,
                SWT.CAP_FLAT, SWT.JOIN_MITER,
                isFilled: true, clipRect: Rect.fromLTWH(2.0 + i * 14 + inset, 6, 12, 21)),
            RectShape(Rect.fromLTWH(2.0 + i * 14, 6, 11, 20), black, 1, SWT.CAP_FLAT, SWT.JOIN_MITER,
                clipRect: Rect.fromLTWH(2.0 + i * 14 + inset, 6, 12, 21)),
            TextShape('$i', Offset(4.0 + i * 14, 34), const TextStyle(fontSize: 8, color: Color(0xFF0000FF)),
                Rect.fromLTWH(2.0 + i * 14, 30, 12, 14)),
          ])
            shape
      ];

  test('figures each wholly inside their own clip are one triangle list, their labels after it', () async {
    final calls = _Calls();
    drawShapeRun(calls, figures());
    final drawn = calls.log.where((call) => call == 'triangles' || call == 'text' || call == 'rect').toList();

    expect(drawn, ['triangles', for (var i = 0; i < 4; i++) 'text']);
    await expectSamePixels(figures(), (c) {});
  });

  test('a run of a few rectangles draws them, a longer one builds triangles; both as each alone', () async {
    for (final count in [2, 3, 4, 8]) {
      final run = rects(2).take(count).toList();
      final calls = _Calls();
      drawShapeRun(calls, run);
      final few = count < 4;
      expect(calls.log.where((call) => call == 'triangles'), hasLength(few ? 0 : 1), reason: '$count: ${calls.log}');
      expect(calls.log.where((call) => call == 'rect'), hasLength(few ? count : 0), reason: '$count: ${calls.log}');
      await expectSamePixels(run, (c) {});
    }
  });

  test('a figure its clip cuts into keeps the clip and is drawn under it', () async {
    final calls = _Calls();
    drawShapeRun(calls, figures(inset: 2));

    // Each figure's fill and outline still share its clip: drawn under it, as two rectangles, which
    // is cheaper than a triangle list of two.
    expect(calls.log.where((call) => call == 'triangles'), isEmpty, reason: '${calls.log}');
    expect(calls.log.where((call) => call == 'rect'), hasLength(8), reason: '${calls.log}');
    expect(calls.log.where((call) => call.startsWith('clip ')), hasLength(8), reason: '${calls.log}');
    await expectSamePixels(figures(inset: 2), (c) {});
  });

  // On screen the view's pixel ratio is applied by the layer, after the scene is recorded: a run's
  // lines are antialiased over one device pixel only if it knows how big that is.
  test('on a view of pixel ratio 2, a run of lines paints what drawing each line does', () async {
    final lines = <Shape>[
      for (var i = 0; i < 6; i++) ...[
        LineShape(Offset(2.0 + i * 4, 1), Offset(2.0 + i * 4, 28), black, 1, SWT.CAP_FLAT, 1),
        LineShape(Offset(1, 3.0 + i * 4), Offset(28, 3.0 + i * 4), const Color(0xFF2050C0), 1, SWT.CAP_FLAT, 1),
      ]
    ];
    final recorder = ui.PictureRecorder();
    ScenePainter(const Color(0xFFFFFFFF), lines, devicePixelRatio: 2)
        .paint(ui.Canvas(recorder), const Size(30, 30));
    final scene = recorder.endRecording();

    final shown = await _render((c) {
      c.scale(2);
      c.drawPicture(scene);
    });
    final eachDrawn = await _render((c) {
      c.scale(2);
      c.drawRect(const Rect.fromLTWH(0, 0, 30, 30), Paint()..color = const Color(0xFFFFFFFF));
      for (final s in lines) {
        s.draw(c);
      }
    });

    expect(shown, eachDrawn);
  });

  // A chart's grid or a table's rules: horizontal and vertical strokes that cross, translucent ones
  // included, whose ends land on whole pixels at both scales (an odd width with flat caps ends half a
  // pixel in, and keeps its antialiased edges).
  List<Shape> rules() => [
        for (final (width, cap) in [
          (1.0, SWT.CAP_SQUARE),
          (2.0, SWT.CAP_FLAT),
          (2.0, SWT.CAP_SQUARE),
          (3.0, SWT.CAP_SQUARE),
        ]) ...[
            LineShape(Offset(3.0 + width * 9 + cap, 2), Offset(3.0 + width * 9 + cap, 50), const Color(0x800000FF),
                width, cap, 1),
            LineShape(Offset(2, 4.0 + width * 7 + cap * 2), Offset(54, 4.0 + width * 7 + cap * 2), red, width, cap, 1),
          ],
        LineShape(const Offset(5, 55), const Offset(50, 40), black, 2, SWT.CAP_FLAT, 1),
      ];

  for (final scale in [1.0, 2.0]) {
    test('horizontal and vertical strokes as a run paint what drawing each one does, at scale $scale', () async {
      final shapes = rules();
      final run = await _render((c) {
        c.scale(scale);
        drawShapeRun(c, shapes.sublist(0, shapes.length - 1));
      });
      final apart = await _render((c) {
        c.scale(scale);
        for (final s in shapes.sublist(0, shapes.length - 1)) {
          s.draw(c);
        }
      });
      expect(run, apart);
    });
  }
}
