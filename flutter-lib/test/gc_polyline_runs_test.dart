// A wide polyline joins a GC's triangle runs as triangles that never overlap: a translucent stroke
// lays its colour once, as the stroke does, and differs from it only along its antialiased edges. A
// polyline whose stroke would reach over itself is drawn on its own. `gc_approximate_batching` false
// keeps every shape a run would antialias by its own means drawn on its own, and the run exact.

import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/foundation.dart' show kIsWeb;
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart' show getConfigFlags;

class _Calls implements ui.Canvas {
  final List<String> log = [];

  @override
  void drawVertices(ui.Vertices vertices, BlendMode blendMode, Paint paint) => log.add('triangles');

  @override
  void drawPath(Path path, Paint paint) => log.add('path');

  @override
  void drawOval(Rect rect, Paint paint) => log.add('oval');

  @override
  void drawLine(Offset p1, Offset p2, Paint paint) => log.add('line');

  @override
  Float64List getTransform() => Float64List.fromList([1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1]);

  @override
  dynamic noSuchMethod(Invocation invocation) => null;
}

const _size = 120;

Future<Uint8List> _render(double scale, void Function(ui.Canvas) paint) async {
  final recorder = ui.PictureRecorder();
  final canvas = ui.Canvas(recorder)
    ..drawRect(Rect.fromLTWH(0, 0, _size * scale, _size * scale), Paint()..color = const Color(0xFFFFFFFF))
    ..scale(scale);
  paint(canvas);
  final image = await recorder.endRecording().toImage((_size * scale).round(), (_size * scale).round());
  return (await image.toByteData(format: ui.ImageByteFormat.rawRgba))!.buffer.asUint8List();
}

Future<(int, double)> _difference(List<Shape> shapes, double scale) async {
  final run = await _render(scale, (c) => drawShapeRun(c, shapes));
  final apart = await _render(scale, (c) {
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
  return (worst, total / run.length);
}

PolylineShape _polyline(List<int> points, Color color, double width, int cap, int join) =>
    PolylineShape(points, color, width, cap, join);

void main() {
  setUp(() => polylinesJoinRuns = true);
  tearDown(() => polylinesJoinRuns = kIsWeb);

  const blue = Color(0xA02050D0), red = Color(0xA0D03020), green = Color(0xFF10A040);

  // gc.routes: wide translucent routes across the canvas, turning both ways, crossing each other.
  List<Shape> routes(int cap, int join) => [
        _polyline([10, 20, 40, 50, 70, 30, 100, 60, 110, 100], blue, 6, cap, join),
        _polyline([10, 90, 35, 60, 60, 95, 90, 40, 112, 20], red, 6, cap, join),
        _polyline([15, 110, 50, 105, 100, 112], green, 3, cap, join),
        _polyline([20, 10, 100, 12], red, 5, cap, join),
      ];

  for (final (cap, join) in [
    (SWT.CAP_ROUND, SWT.JOIN_ROUND),
    (SWT.CAP_FLAT, SWT.JOIN_MITER),
    (SWT.CAP_SQUARE, SWT.JOIN_BEVEL),
  ]) {
    test('wide polylines with cap $cap and join $join of any paint share one triangle list', () {
      final calls = _Calls();
      drawShapeRun(calls, routes(cap, join));

      expect(calls.log, ['triangles']);
    });

    for (final scale in [1.0, 2.0]) {
      test('as a run they paint what their strokes do but along the edges, cap $cap, join $join, '
          'scale $scale', () async {
        final (worst, mean) = await _difference(routes(cap, join), scale);

        // A colour blended twice where triangles overlapped would differ by far more, along every join.
        expect(worst, lessThan(64), reason: 'mean difference $mean');
        expect(mean, lessThan(1.0), reason: 'worst channel difference $worst');
      });
    }
  }

  test('a polyline drawn again, as a GC repaints it, keeps the triangles it was built with', () async {
    final first = await _render(2, (c) => drawShapeRun(c, routes(SWT.CAP_ROUND, SWT.JOIN_ROUND)));
    final built = polylineStrokesBuilt;
    final again = await _render(2, (c) => drawShapeRun(c, routes(SWT.CAP_ROUND, SWT.JOIN_ROUND)));

    expect(polylineStrokesBuilt, built, reason: 'equal polylines are found, not built again');
    expect(again, first);

    drawShapeRun(_Calls(), [
      _polyline([10, 20, 40, 50, 70, 30, 100, 60, 110, 100], red, 6, SWT.CAP_ROUND, SWT.JOIN_ROUND),
      _polyline([10, 20, 40, 50, 70, 30, 100, 60, 110, 100], blue, 7, SWT.CAP_ROUND, SWT.JOIN_ROUND),
      _polyline([10, 20, 40, 50, 70, 30, 100, 60, 110, 101], blue, 6, SWT.CAP_ROUND, SWT.JOIN_ROUND),
      _polyline([10, 20, 40, 50, 70, 30, 100, 60, 110, 100], blue, 6, SWT.CAP_FLAT, SWT.JOIN_ROUND),
    ]);
    expect(polylineStrokesBuilt, built + 4, reason: 'another colour, width, point or cap builds its own');
  });

  test('a diagram of 300 connectors, drawn again, builds none of them again', () {
    // gc.diagram's connectors: three points each, every one different.
    List<Shape> connectors() => [
          for (var i = 0; i < 300; i++)
            _polyline([i % 50, i ~/ 50 * 15, i % 50 + 20, i ~/ 50 * 15, i % 50 + 20, i ~/ 50 * 15 + 10], blue, 2,
                SWT.CAP_FLAT, SWT.JOIN_MITER),
        ];
    drawShapeRun(_Calls(), connectors());
    final built = polylineStrokesBuilt;
    drawShapeRun(_Calls(), connectors());

    expect(polylineStrokesBuilt, built);
  });

  test('a polyline that turns back on itself is drawn on its own', () {
    final calls = _Calls();
    drawShapeRun(calls, [
      _polyline([10, 10, 100, 10, 12, 14], blue, 6, SWT.CAP_ROUND, SWT.JOIN_ROUND),
      _polyline([10, 50, 100, 60], blue, 6, SWT.CAP_ROUND, SWT.JOIN_ROUND),
    ]);

    expect(calls.log.first, 'path');
  });

  test('a polyline that crosses itself is drawn on its own', () {
    final calls = _Calls();
    drawShapeRun(calls, [
      _polyline([10, 10, 100, 100, 100, 10, 10, 100], blue, 6, SWT.CAP_FLAT, SWT.JOIN_MITER),
      _polyline([10, 50, 100, 60], blue, 6, SWT.CAP_FLAT, SWT.JOIN_MITER),
    ]);

    expect(calls.log.first, 'path');
  });

  test('a polyline whose next shape cannot join a run is drawn on its own', () {
    final calls = _Calls();
    drawShapeRun(calls, [
      _polyline([10, 10, 50, 40, 100, 20], blue, 6, SWT.CAP_FLAT, SWT.JOIN_MITER),
      OvalShape(const Rect.fromLTWH(10, 50, 20, 20), red, 0, isFilled: true),
    ]);

    expect(calls.log, ['path', 'oval']);
  });

  test('off the web, polylines are drawn as their strokes', () {
    polylinesJoinRuns = false;
    final calls = _Calls();
    drawShapeRun(calls, routes(SWT.CAP_ROUND, SWT.JOIN_ROUND));

    expect(calls.log, ['path', 'path', 'path', 'path']);
  });

  test('a polyline no wider than a device pixel is drawn on its own', () {
    final calls = _Calls();
    drawShapeRun(calls, [
      _polyline([10, 10, 50, 40, 100, 20], blue, 1, SWT.CAP_FLAT, SWT.JOIN_MITER),
      _polyline([10, 50, 100, 60], blue, 6, SWT.CAP_FLAT, SWT.JOIN_MITER),
    ]);

    expect(calls.log.first, 'path');
  });

  group('without approximate batching', () {
    Future<void> exactly(Future<void> Function() body) async {
      getConfigFlags().gc_approximate_batching = false;
      try {
        await body();
      } finally {
        getConfigFlags().gc_approximate_batching = null;
      }
    }

    List<Shape> mixed() => [
          LineShape(const Offset(0, 0), const Offset(90, 70), red, 1, SWT.CAP_FLAT, SWT.JOIN_MITER),
          PolygonShape([10, 10, 40, 10, 25, 40], blue, 0, 1, 1, isFilled: true),
          LineShape(const Offset(5, 0), const Offset(95, 70), green, 3, SWT.CAP_FLAT, SWT.JOIN_MITER),
          LineShape(const Offset(20, 10), const Offset(20, 90), blue, 2, SWT.CAP_FLAT, SWT.JOIN_MITER),
          LineShape(const Offset(10, 30), const Offset(90, 30), red, 2, SWT.CAP_FLAT, SWT.JOIN_MITER),
          SegmentsShape(Float32List.fromList([5, 95, 95, 5]), green, 0, SWT.CAP_FLAT),
          ...routes(SWT.CAP_ROUND, SWT.JOIN_ROUND),
        ];

    test('a diagonal line and a convex polygon are each drawn on their own', () => exactly(() async {
          final calls = _Calls();
          drawShapeRun(calls, mixed().sublist(0, 3));

          expect(calls.log, ['line', 'path', 'line']);
        }));

    test('on-grid lines and dashes still share one triangle list', () => exactly(() async {
          final calls = _Calls();
          drawShapeRun(calls, mixed().sublist(3, 6));

          expect(calls.log, ['triangles']);
        }));

    for (final scale in [1.0, 2.0]) {
      test('a run paints exactly what drawing each shape does, at scale $scale', () => exactly(() async {
            final (worst, mean) = await _difference(mixed(), scale);

            expect(worst, 0, reason: 'mean difference $mean');
          }));
    }
  });

  test('without approximate batching, wide polylines are each drawn on their own', () async {
    getConfigFlags().gc_approximate_batching = false;
    try {
      final calls = _Calls();
      drawShapeRun(calls, routes(SWT.CAP_ROUND, SWT.JOIN_ROUND));

      expect(calls.log, ['path', 'path', 'path', 'path']);
    } finally {
      getConfigFlags().gc_approximate_batching = null;
    }
  });
}
