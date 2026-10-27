// A dashed straight line on the desktop renderer is drawn as triangles that carry their own
// antialiasing: a core at full coverage inside a ring fading to none across a pixel. Without the
// ring a thin dot covering no pixel centre would vanish; with it every dot shows, and the ink laid
// down matches the dash's real area.

import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';

/// The dots of a one-wide dotted diagonal: endpoint pairs, one unit on, one off.
Float32List _dots() {
  final out = <double>[];
  const from = Offset(10, 10), to = Offset(110, 180);
  final len = (to - from).distance, ux = (to.dx - from.dx) / len, uy = (to.dy - from.dy) / len;
  for (var d = 0.0; d < len; d += 2) {
    out
      ..add(from.dx + ux * d)
      ..add(from.dy + uy * d)
      ..add(from.dx + ux * (d + 1))
      ..add(from.dy + uy * (d + 1));
  }
  return Float32List.fromList(out);
}

/// Ink per pixel (0 none, 255 full) of [paint] at [scale] times the resolution, averaged back down.
Future<List<int>> _ink(int scale, void Function(Canvas) paint) async {
  const w = 130, h = 200;
  final recorder = ui.PictureRecorder();
  final canvas = Canvas(recorder)
    ..drawRect(Rect.fromLTWH(0, 0, w * scale.toDouble(), h * scale.toDouble()), Paint()..color = Colors.white)
    ..scale(scale.toDouble());
  paint(canvas);
  final image = await recorder.endRecording().toImage(w * scale, h * scale);
  final rgba = (await image.toByteData(format: ui.ImageByteFormat.rawRgba))!.buffer.asUint8List();
  final out = List<int>.filled(w * h, 0);
  for (var y = 0; y < h; y++) {
    for (var x = 0; x < w; x++) {
      var sum = 0;
      for (var yy = 0; yy < scale; yy++) {
        for (var xx = 0; xx < scale; xx++) {
          sum += 255 - rgba[((y * scale + yy) * w * scale + x * scale + xx) * 4 + 1];
        }
      }
      out[y * w + x] = sum ~/ (scale * scale);
    }
  }
  return out;
}

void main() {
  test('every dot of a thin dotted diagonal shows, with the ink it really covers', () async {
    final dots = _dots();
    final shape = SegmentsShape(dots, Colors.black, 1, SWT.CAP_FLAT);
    final drawn = await _ink(1, shape.draw);
    // The same dots as plain rectangles at 8x the resolution: their true coverage, averaged down.
    final exact = await _ink(8, (c) {
      for (var i = 0; i + 3 < dots.length; i += 4) {
        final a = Offset(dots[i], dots[i + 1]), b = Offset(dots[i + 2], dots[i + 3]);
        final n = Offset(-(b.dy - a.dy), b.dx - a.dx) / (b - a).distance * 0.5;
        c.drawPath(Path()..addPolygon([a + n, b + n, b - n, a - n], true), Paint()..color = Colors.black);
      }
    });
    var visible = 0;
    for (var i = 0; i + 3 < dots.length; i += 4) {
      // The shape is drawn half a pixel shifted, as a one-wide stroke is aligned to the pixel grid.
      final cx = ((dots[i] + dots[i + 2]) / 2 + 0.5).floor(), cy = ((dots[i + 1] + dots[i + 3]) / 2 + 0.5).floor();
      var ink = 0;
      for (var dy = -1; dy <= 1; dy++) {
        for (var dx = -1; dx <= 1; dx++) {
          ink += drawn[(cy + dy) * 130 + cx + dx];
        }
      }
      if (ink > 40) visible++;
    }
    expect(visible, dots.length ~/ 4, reason: 'a dot covering no pixel centre must not vanish');
    final total = drawn.fold(0, (a, b) => a + b), real = exact.fold(0, (a, b) => a + b);
    expect((total - real).abs() / real, lessThan(0.1), reason: 'drawn $total, real $real');
  });
}
