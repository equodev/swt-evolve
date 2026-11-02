// What drawing a GC's shapes costs on the Dart side: drawShapeRun onto a recording canvas, as the
// canvas's painter does on every frame that repaints it. Time, and bytes allocated, per shape.
//
// A measurement, printed rather than asserted, for comparing the drawing code before and after a
// change. Bytes need the VM service: run with
//   flutter test --enable-vmservice --tags bench test/gc_draw_bench_test.dart

@Tags(['bench'])
library;

import 'dart:ui' as ui;

import 'package:flutter/painting.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';

import 'support/heap_bytes.dart';

const int _draws = 20000;
const int _allocDraws = 1000;
const int _rounds = 5;

/// Draws [shapes] [count] times, a fresh recording every [burst] draws.
void _drawMany(List<Shape> shapes, int count) {
  var recorder = ui.PictureRecorder();
  var canvas = ui.Canvas(recorder);
  for (var i = 0; i < count; i++) {
    drawShapeRun(canvas, shapes);
    if (i % burst == burst - 1) {
      recorder.endRecording().dispose();
      recorder = ui.PictureRecorder();
      canvas = ui.Canvas(recorder);
    }
  }
  recorder.endRecording().dispose();
}

Future<void> _report(String name, List<Shape> shapes, NewSpace? heap) async {
  _drawMany(shapes, 2000);
  final times = <double>[];
  for (var round = 0; round < _rounds; round++) {
    final sw = Stopwatch()..start();
    _drawMany(shapes, _draws);
    times.add(sw.elapsedMicroseconds * 1000 / (_draws * shapes.length));
  }
  double? bytes;
  if (heap != null) {
    final samples = <double>[];
    for (var round = 0; round < _rounds; round++) {
      final quiet = await heap.bytesOf(() async => _drawMany(const [], _allocDraws));
      final run = await heap.bytesOf(() async => _drawMany(shapes, _allocDraws));
      if (quiet != null && run != null) samples.add((run - quiet) / (_allocDraws * shapes.length));
    }
    if (samples.isNotEmpty) bytes = median(samples);
  }
  // ignore: avoid_print
  print(measureLine('$name, per shape', median(times), bytes));
}

Color _shade(int k) => Color(0xFF000000 | (k * 0x1F3D5B) & 0xFFFFFF);

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('MEASURE: drawing a GC\'s shapes', () async {
    final heap = await NewSpace.connect();

    // A paint as the decode benchmark's: a fill and a line in turn.
    final mixed = <Shape>[
      for (var k = 0; k < 8; k++)
        k.isEven
            ? RectShape(Rect.fromLTWH(k * 1.0, 3, 20, 14), _shade(k), 0, 1, 1, isFilled: true)
            : LineShape(Offset(0, k * 1.0), Offset(40, k * 1.0), _shade(k), 1, 1, 1),
    ];
    // A ruler or a grid: lines alone, which draw as one triangle list.
    final lines = <Shape>[
      for (var k = 0; k < 20; k++) LineShape(Offset(0, k * 14.0), Offset(300, k * 14.0), _shade(1), 1, 1, 1),
    ];
    // Cell backgrounds: filled rectangles alone.
    final fills = <Shape>[
      for (var k = 0; k < 20; k++)
        RectShape(Rect.fromLTWH(0, k * 14.0, 300, 14), _shade(k % 3), 0, 1, 1, isFilled: true),
    ];

    await _report('draw: 4 fills and 4 lines', mixed, heap);
    await _report('draw: 20 lines', lines, heap);
    await _report('draw: 20 fills', fills, heap);

    if (heap == null) {
      // ignore: avoid_print
      print('MEASURE bytes need --enable-vmservice');
    }
    await heap?.dispose();
  });
}
