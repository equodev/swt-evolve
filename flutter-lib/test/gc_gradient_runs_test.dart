// fillGradientRectangle's gradients, on whole device pixels, join a GC's triangle runs as quads whose
// colours run from edge to edge: a gradient between two colours along one axis is linear in it, which
// the quad reproduces at every pixel. What they paint differs from the gradient drawn on its own only
// by the renderer's rounding of each colour.

import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart' show getConfigFlags;

class _Calls implements ui.Canvas {
  _Calls([this.factor = 1]);
  final double factor;
  final List<String> log = [];

  @override
  void drawVertices(ui.Vertices vertices, BlendMode blendMode, Paint paint) => log.add('triangles');

  @override
  void drawPaint(Paint paint) => log.add('gradient');

  @override
  Float64List getTransform() => Float64List.fromList([factor, 0, 0, 0, 0, factor, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1]);

  @override
  dynamic noSuchMethod(Invocation invocation) => null;
}

const _size = 100;

Future<Uint8List> _render(double scale, void Function(ui.Canvas) paint) async {
  final recorder = ui.PictureRecorder();
  final canvas = ui.Canvas(recorder)
    ..drawRect(Rect.fromLTWH(0, 0, _size * scale, _size * scale), Paint()..color = const Color(0xFFFFFFFF))
    ..scale(scale);
  paint(canvas);
  final image = await recorder.endRecording().toImage((_size * scale).round(), (_size * scale).round());
  return (await image.toByteData(format: ui.ImageByteFormat.rawRgba))!.buffer.asUint8List();
}

Future<int> _worst(List<Shape> shapes, double scale) async {
  final run = await _render(scale, (c) => drawShapeRun(c, shapes));
  final apart = await _render(scale, (c) {
    for (final s in shapes) {
      s.draw(c);
    }
  });
  var worst = 0;
  for (var i = 0; i < run.length; i++) {
    final d = (run[i] - apart[i]).abs();
    if (d > worst) worst = d;
  }
  return worst;
}

void main() {
  const colors = [
    Color(0xFFD02020), Color(0xFF2050D0), Color(0xFF10A040), Color(0xFFF0C010),
    Color(0xFF8020C0), Color(0xFF000000), Color(0xFFFFFFFF), Color(0xFF808080),
  ];

  // gc.gradient's: overlapping gradients, top to bottom and left to right in turn, and one drawn with
  // a negative width and one with a negative height, which swap the colours' ends.
  List<Shape> gradients() => [
        for (var i = 0; i < 12; i++)
          GradientRectShape(Rect.fromLTWH((i * 37) % 70, (i * 23) % 60, 30, 20), colors[i % 8],
              colors[(i + 4) % 8], i.isEven),
        GradientRectShape(const Rect.fromLTWH(90, 70, -40, 25), colors[0], colors[1], false),
        GradientRectShape(const Rect.fromLTWH(10, 95, 30, -20), colors[2], colors[3], true),
      ];

  test('gradients along an axis are one triangle list', () {
    final calls = _Calls();
    drawShapeRun(calls, gradients());

    expect(calls.log, ['triangles']);
  });

  for (final scale in [1.0, 2.0]) {
    test('as one triangle list they paint what drawing each does, at scale $scale', () async {
      expect(await _worst(gradients(), scale), lessThanOrEqualTo(2));
    });
  }

  test('off the device grid a gradient is drawn on its own', () {
    final calls = _Calls(1.25);
    drawShapeRun(calls, gradients().sublist(0, 2));

    expect(calls.log, ['gradient', 'gradient']);
  });

  test('a diagonal gradient is drawn on its own', () {
    final calls = _Calls();
    drawShapeRun(calls, [
      ...gradients().sublist(0, 2),
      GradientRectShape.aligned(
          const Rect.fromLTWH(0, 0, 20, 20), colors[0], colors[1], Alignment.topLeft, Alignment.bottomRight),
    ]);

    expect(calls.log, ['triangles', 'gradient']);
  });

  test('without approximate batching, gradients are each drawn on their own', () {
    getConfigFlags().gc_approximate_batching = false;
    try {
      final calls = _Calls();
      drawShapeRun(calls, gradients().sublist(0, 3));

      expect(calls.log, ['gradient', 'gradient', 'gradient']);
    } finally {
      getConfigFlags().gc_approximate_batching = null;
    }
  });
}
