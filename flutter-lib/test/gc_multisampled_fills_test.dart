// Where the canvas multisamples, as Impeller's does, a filled path is its triangles with no fringe of
// its own, so a convex fill in a run is the same triangles: one triangle list, even with
// gc_approximate_batching off, covering exactly the polygon.

import 'dart:typed_data';
import 'dart:ui' as ui;

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
  Float64List getTransform() => Float64List.fromList([1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1]);

  @override
  dynamic noSuchMethod(Invocation invocation) => null;
}

const _size = 80;

Future<Uint8List> _render(void Function(ui.Canvas) paint) async {
  final recorder = ui.PictureRecorder();
  final canvas = ui.Canvas(recorder)
    ..drawRect(const Rect.fromLTWH(0, 0, _size + 0.0, _size + 0.0), Paint()..color = const Color(0xFFFFFFFF));
  paint(canvas);
  final image = await recorder.endRecording().toImage(_size, _size);
  return (await image.toByteData(format: ui.ImageByteFormat.rawRgba))!.buffer.asUint8List();
}

void main() {
  const blue = Color(0xC00030C0), red = Color(0xFFD02020);
  // A map's regions: convex, of 3 to 8 corners, and one concave.
  final convex = [
    [3, 4, 30, 9, 12, 33],
    [40, 3, 70, 6, 76, 25, 61, 38, 44, 30],
    [8, 44, 20, 40, 34, 46, 38, 60, 30, 74, 14, 76, 5, 68, 4, 54],
  ];
  const concave = [44, 44, 76, 44, 76, 76, 60, 56, 44, 76];
  List<Shape> shapes(List<List<int>> polygons) => [
        for (var i = 0; i < polygons.length; i++)
          PolygonShape(polygons[i], i.isEven ? blue : red, 0, SWT.CAP_FLAT, SWT.JOIN_MITER, isFilled: true),
      ];

  tearDown(() {
    debugMultisampled = null;
    getConfigFlags().gc_approximate_batching = null;
  });

  test('convex fills are one triangle list, with approximate batching off too', () {
    for (final approximate in [null, false]) {
      debugMultisampled = true;
      getConfigFlags().gc_approximate_batching = approximate;
      final calls = _Calls();
      drawShapeRun(calls, shapes(convex));
      expect(calls.log, ['triangles'], reason: 'gc_approximate_batching $approximate');
    }
  });

  test('without multisampling and with approximate batching off, each fill is its path', () {
    debugMultisampled = false;
    getConfigFlags().gc_approximate_batching = false;
    final calls = _Calls();
    drawShapeRun(calls, shapes(convex));
    expect(calls.log, ['path', 'path', 'path']);
  });

  test('a concave fill is drawn as its path', () {
    debugMultisampled = true;
    final calls = _Calls();
    drawShapeRun(calls, shapes([...convex.take(2), concave]));
    expect(calls.log, ['triangles', 'path']);
  });

  test('the triangles cover exactly the polygon: each pixel centre inside it, none outside', () async {
    // The software canvas draws triangles without antialiasing, so they are held to the same
    // polygons filled without it: what a multisampled canvas samples is the same coverage.
    debugMultisampled = true;
    final run = await _render((c) => drawShapeRun(c, shapes(convex)));
    final paths = await _render((c) {
      for (var i = 0; i < convex.length; i++) {
        final p = convex[i];
        final path = Path()..moveTo(p[0].toDouble(), p[1].toDouble());
        for (var k = 2; k + 1 < p.length; k += 2) {
          path.lineTo(p[k].toDouble(), p[k + 1].toDouble());
        }
        c.drawPath(path..close(), Paint()..color = i.isEven ? blue : red..isAntiAlias = false);
      }
    });
    var differ = 0;
    for (var i = 0; i < run.length; i++) {
      if ((run[i] - paths[i]).abs() > 1) differ++;
    }
    expect(differ, 0);
  });
}
