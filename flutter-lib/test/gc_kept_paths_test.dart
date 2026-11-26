// A GC repaints the same shapes frame after frame. A polygon, polyline or pie slice drawn once is
// built in the shared path; drawn again, it gets a path of its own, kept and drawn from then on. A
// Path's data finds the path built for the same data. Every way paints the same pixels.

import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';

class _Paths implements ui.Canvas {
  final List<Path> drawn = [];

  @override
  void drawPath(Path path, Paint paint) => drawn.add(path);

  @override
  dynamic noSuchMethod(Invocation invocation) => null;
}

Future<Uint8List> _render(void Function(ui.Canvas) paint) async {
  final recorder = ui.PictureRecorder();
  final canvas = ui.Canvas(recorder)..drawRect(const Rect.fromLTWH(0, 0, 100, 60), Paint()..color = Colors.white);
  paint(canvas);
  final image = await recorder.endRecording().toImage(100, 60);
  return (await image.toByteData(format: ui.ImageByteFormat.rawRgba))!.buffer.asUint8List();
}

void main() {
  // Geometry no other test draws, so what this test counts is its own.
  List<Shape> shapes() => [
        PolygonShape([3, 3, 41, 7, 29, 37, 7, 31], const Color(0xFFCC3322), 0, SWT.CAP_FLAT, SWT.JOIN_MITER,
            isFilled: true),
        PolylineShape([51, 5, 63, 29, 77, 9, 93, 41], const Color(0xFF2255CC), 2, SWT.CAP_FLAT, SWT.JOIN_MITER),
        ArcShape(const Rect.fromLTWH(47, 21, 33, 33), 0.4, 2.3, const Color(0xFF22AA44), 0, SWT.CAP_FLAT,
            SWT.JOIN_MITER, isFilled: true),
      ];

  void paint(ui.Canvas c, List<Shape> s) {
    for (final shape in s) {
      shape.paintBody(c);
    }
  }

  test('shapes drawn once build no path to keep; drawn again, one each, and none after', () async {
    final before = keptPathsBuilt;
    final once = await _render((c) => paint(c, shapes()));
    expect(keptPathsBuilt - before, 0);

    final again = await _render((c) => paint(c, shapes()));
    expect(keptPathsBuilt - before, 3);

    final third = await _render((c) => paint(c, shapes()));
    expect(keptPathsBuilt - before, 3);
    expect(again, once);
    expect(third, once);
  });

  test('a shape drawn again is drawn from its kept path, the same one every time', () {
    final first = _Paths(), second = _Paths();
    final s = [
      PolygonShape([13, 17, 61, 19, 37, 53], const Color(0xFF103050), 0, SWT.CAP_FLAT, SWT.JOIN_MITER,
          isFilled: true),
    ];
    paint(_Paths(), s);
    paint(first, s);
    paint(second, s);

    expect(identical(first.drawn.single, second.drawn.single), isTrue);
  });
}
