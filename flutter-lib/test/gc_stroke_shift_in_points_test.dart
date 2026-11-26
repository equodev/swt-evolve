// An odd-width stroke sits on pixel centres. A polyline or polygon moves its own points by the half
// pixel instead of moving the canvas, which paints the same pixels in fewer canvas calls.

import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';

/// Records the canvas calls a shape makes.
class _Calls implements ui.Canvas {
  final List<String> log = [];

  @override
  void save() => log.add('save');

  @override
  void restore() => log.add('restore');

  @override
  void translate(double dx, double dy) => log.add('translate');

  @override
  void drawPath(Path path, Paint paint) => log.add('path');

  @override
  dynamic noSuchMethod(Invocation invocation) => null;
}

Future<Uint8List> _render(double scale, void Function(ui.Canvas) paint) async {
  final recorder = ui.PictureRecorder();
  final canvas = ui.Canvas(recorder)
    ..drawRect(const Rect.fromLTWH(0, 0, 120, 120), Paint()..color = const Color(0xFFFFFFFF))
    ..scale(scale);
  paint(canvas);
  final image = await recorder.endRecording().toImage(120, 120);
  return (await image.toByteData(format: ui.ImageByteFormat.rawRgba))!.buffer.asUint8List();
}

/// The stroke as it was drawn before: the canvas moved by the half pixel around a path of the points.
void _reference(ui.Canvas c, List<int> points, double width, bool filled, bool closed) {
  final path = Path()
    ..fillType = PathFillType.evenOdd
    ..moveTo(points[0].toDouble(), points[1].toDouble());
  for (var i = 2; i < points.length; i += 2) {
    path.lineTo(points[i].toDouble(), points[i + 1].toDouble());
  }
  if (closed) path.close();
  final odd = !filled && (width.round().isOdd || width.round() == 0);
  if (odd) {
    c.save();
    c.translate(0.5, 0.5);
  }
  c.drawPath(
      path,
      Paint()
        ..color = const Color(0xFF203040)
        ..style = filled ? PaintingStyle.fill : PaintingStyle.stroke
        ..strokeWidth = width
        ..strokeCap = getStrokeCap(SWT.CAP_FLAT)
        ..strokeJoin = getStrokeJoin(SWT.JOIN_MITER));
  if (odd) c.restore();
}

void main() {
  const points = [5, 7, 40, 7, 52, 33, 18, 50, 9, 21];
  const color = Color(0xFF203040);

  for (final closed in [false, true]) {
    final kind = closed ? 'polygon' : 'polyline';
    Shape shape(double width, bool filled) => closed
        ? PolygonShape(points, color, width, SWT.CAP_FLAT, SWT.JOIN_MITER, isFilled: filled)
        : PolylineShape(points, color, width, SWT.CAP_FLAT, SWT.JOIN_MITER, isFilled: filled);

    for (final scale in [1.0, 2.0]) {
      for (final width in [0.0, 1.0, 2.0, 3.0]) {
        for (final filled in closed ? [false, true] : [false]) {
          test('a $kind of width $width${filled ? ', filled,' : ''} at scale $scale paints as before', () async {
            final drawn = await _render(scale, shape(width, filled).draw);
            final before = await _render(scale, (c) => _reference(c, points, width, filled, closed));
            expect(drawn, before);
          });
        }
      }
    }

    test('a $kind of odd width does not move the canvas', () {
      final calls = _Calls();
      shape(1, false).draw(calls);
      expect(calls.log, ['path']);
    });
  }
}
