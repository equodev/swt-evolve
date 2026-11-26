// A polygon or polyline drawn for the first time is built in one shared path, reset for each shape: a
// picture keeps the geometry the path had when it was drawn, so later shapes rebuilding it change
// nothing drawn.

import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';

/// Records the paths a shape draws.
class _Paths implements ui.Canvas {
  final List<Path> drawn = [];

  @override
  void drawPath(Path path, Paint paint) => drawn.add(path);

  @override
  dynamic noSuchMethod(Invocation invocation) => null;
}

Future<Uint8List> _pixels(ui.Picture picture) async {
  final image = await picture.toImage(120, 120);
  return (await image.toByteData(format: ui.ImageByteFormat.rawRgba))!.buffer.asUint8List();
}

ui.Picture _record(void Function(ui.Canvas) paint) {
  final recorder = ui.PictureRecorder();
  final canvas = ui.Canvas(recorder)
    ..drawRect(const Rect.fromLTWH(0, 0, 120, 120), Paint()..color = const Color(0xFFFFFFFF));
  paint(canvas);
  return recorder.endRecording();
}

/// Each shape as it was drawn before, from a path of its own.
void _reference(ui.Canvas c, List<int> points, double width, bool filled, bool closed, PathFillType fillType) {
  final shift = !filled && (width.round().isOdd || width.round() == 0) ? 0.5 : 0.0;
  final path = Path()
    ..fillType = fillType
    ..moveTo(points[0] + shift, points[1] + shift);
  for (var i = 2; i < points.length; i += 2) {
    path.lineTo(points[i] + shift, points[i + 1] + shift);
  }
  if (closed) path.close();
  c.drawPath(
      path,
      Paint()
        ..color = const Color(0xFF203040)
        ..style = filled ? PaintingStyle.fill : PaintingStyle.stroke
        ..strokeWidth = width
        ..strokeCap = getStrokeCap(SWT.CAP_FLAT)
        ..strokeJoin = getStrokeJoin(SWT.JOIN_MITER));
}

void main() {
  const color = Color(0xFF203040);
  // A self-crossing star, so the even-odd rule shows, then shapes elsewhere on the canvas.
  const star = [60, 5, 75, 110, 10, 40, 110, 40, 45, 110];
  const zigzag = [5, 100, 30, 70, 55, 100, 80, 70, 105, 100];
  const box = [8, 8, 40, 8, 40, 30, 8, 30];

  List<Shape> shapes() => [
        PolygonShape(star, color, 0, SWT.CAP_FLAT, SWT.JOIN_MITER, isFilled: true),
        PolylineShape(zigzag, color, 3, SWT.CAP_FLAT, SWT.JOIN_MITER),
        PolygonShape(box, color, 1, SWT.CAP_FLAT, SWT.JOIN_MITER),
        PolygonShape(star, color, 0, SWT.CAP_FLAT, SWT.JOIN_MITER,
            isFilled: true, fillType: PathFillType.nonZero),
        PolylineShape(box, color, 0, SWT.CAP_FLAT, SWT.JOIN_MITER),
      ];

  void reference(ui.Canvas c, int i) {
    switch (i) {
      case 0:
        _reference(c, star, 0, true, true, PathFillType.evenOdd);
      case 1:
        _reference(c, zigzag, 3, false, false, PathFillType.nonZero);
      case 2:
        _reference(c, box, 1, false, true, PathFillType.evenOdd);
      case 3:
        _reference(c, star, 0, true, true, PathFillType.nonZero);
      case 4:
        _reference(c, box, 0, false, false, PathFillType.nonZero);
    }
  }

  test('shapes drawn one after another paint what each drawn from its own path does', () async {
    final all = shapes();
    final drawn = _record((c) {
      for (final s in all) {
        s.draw(c);
      }
    });
    final before = _record((c) {
      for (var i = 0; i < all.length; i++) {
        reference(c, i);
      }
    });
    expect(await _pixels(drawn), await _pixels(before));
  });

  test('a picture keeps its shape when the next shape rebuilds the path', () async {
    final all = shapes();
    for (var i = 0; i < all.length; i++) {
      final picture = _record(all[i].draw);
      // The next shape rebuilds the shared path before this picture is rasterized.
      _record(all[(i + 1) % all.length].draw);
      final before = _record((c) => reference(c, i));
      expect(await _pixels(picture), await _pixels(before), reason: 'shape $i');
    }
  });

  test('every polygon and polyline drawn for the first time draws from the same path', () {
    // Geometry no other test draws: drawn again, a shape gets a path of its own (gc_kept_paths_test).
    List<int> moved(List<int> points) => [for (final v in points) v + 3];
    final canvas = _Paths();
    for (final s in [
      PolygonShape(moved(star), color, 0, SWT.CAP_FLAT, SWT.JOIN_MITER, isFilled: true),
      PolylineShape(moved(zigzag), color, 3, SWT.CAP_FLAT, SWT.JOIN_MITER),
      PolygonShape(moved(box), color, 1, SWT.CAP_FLAT, SWT.JOIN_MITER),
      PolygonShape(moved(star), color, 0, SWT.CAP_FLAT, SWT.JOIN_MITER,
          isFilled: true, fillType: PathFillType.nonZero),
      PolylineShape(moved(box), color, 0, SWT.CAP_FLAT, SWT.JOIN_MITER),
    ]) {
      s.draw(canvas);
    }
    expect(canvas.drawn, hasLength(5));
    expect(canvas.drawn.every((p) => identical(p, canvas.drawn.first)), isTrue);
  });
}
