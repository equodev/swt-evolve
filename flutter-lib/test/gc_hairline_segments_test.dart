// A hairline segment's antialiased core has no width, so it goes as six vertices instead of the
// eight a wider one takes: what it paints is the same.

import 'dart:math' as math;
import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';

const _w = 90, _h = 40;

Future<Uint8List> _render(void Function(ui.Canvas) paint, double scale) async {
  final recorder = ui.PictureRecorder();
  final canvas = ui.Canvas(recorder)
    ..drawRect(Rect.fromLTWH(0, 0, _w * scale, _h * scale), Paint()..color = const Color(0xFFFFFFFF))
    ..scale(scale);
  paint(canvas);
  final image = await recorder.endRecording().toImage((_w * scale).round(), (_h * scale).round());
  return (await image.toByteData(format: ui.ImageByteFormat.rawRgba))!.buffer.asUint8List();
}

/// The segments as the eight-vertex ring draws every one of them: a core of four corners at full
/// coverage inside a ring fading to none across one device pixel.
ui.Vertices _ring(Float32List points, double strokeWidth, Color color, double px, int lineCap) {
  final positions = <double>[], colors = <int>[], indices = <int>[];
  final rgb = color.toARGB32() & 0xFFFFFF;
  for (var i = 0; i + 3 < points.length; i += 4) {
    final x1 = points[i], y1 = points[i + 1], x2 = points[i + 2], y2 = points[i + 3];
    final segment = math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    if (segment == 0) continue;
    final f = px / 2;
    final half = (strokeWidth > 0 ? strokeWidth : px) / 2;
    final cap = lineCap == SWT.CAP_SQUARE ? half : 0.0;
    final width = strokeWidth.round();
    final shift = width.isEven && width != 0 ? 0.0 : 0.5;
    final hi = math.max(half - f, 0.0), ho = half + f;
    final ux = (x2 - x1) / segment, uy = (y2 - y1) / segment;
    final along = segment / 2 + cap;
    final li = math.max(along - f, 0.0), lo = along + f;
    final innerArea = 4 * hi * li, outerArea = 4 * ho * lo;
    final laid = (innerArea + outerArea + math.sqrt(innerArea * outerArea)) / 3;
    final coverage = laid > 0 ? math.min(1.0, 4 * half * along / laid) : 1.0;
    final core = ((color.a * coverage * 255).round() << 24) | rgb;
    final mx = (x1 + x2) / 2 + shift, my = (y1 + y2) / 2 + shift;
    final iax = ux * li, iay = uy * li, ibx = -uy * hi, iby = ux * hi;
    final oax = ux * lo, oay = uy * lo, obx = -uy * ho, oby = ux * ho;
    final base = positions.length ~/ 2;
    positions.addAll([
      mx - iax - ibx, my - iay - iby, mx + iax - ibx, my + iay - iby, //
      mx + iax + ibx, my + iay + iby, mx - iax + ibx, my - iay + iby,
      mx - oax - obx, my - oay - oby, mx + oax - obx, my + oay - oby,
      mx + oax + obx, my + oay + oby, mx - oax + obx, my - oay + oby,
    ]);
    colors.addAll([core, core, core, core, rgb, rgb, rgb, rgb]);
    indices.addAll([base, base + 1, base + 2, base, base + 2, base + 3]);
    for (var k = 0; k < 4; k++) {
      final q = (k + 1) % 4;
      indices.addAll([base + k, base + 4 + k, base + 4 + q, base + k, base + 4 + q, base + q]);
    }
  }
  return ui.Vertices.raw(ui.VertexMode.triangles, Float32List.fromList(positions),
      colors: Int32List.fromList(colors), indices: Uint16List.fromList(indices));
}

void main() {
  const color = Color(0xFF203040);

  Float32List dotted(double y) => Float32List.fromList([
        for (var x = 1.0; x + 1 <= 86; x += 2) ...[x, y, x + 1, y],
      ]);
  Float32List vertical(double x) => Float32List.fromList([
        for (var y = 1.0; y + 1 <= 38; y += 2) ...[x, y, x, y + 1],
      ]);
  Float32List diagonal() => Float32List.fromList([
        for (var t = 0.0; t + 3 <= 60; t += 5) ...[4 + t, 3 + t * 0.5, 7 + t, 4.5 + t * 0.5],
      ]);
  Float32List tiny() => Float32List.fromList([
        for (var x = 2.0; x + 0.3 <= 80; x += 3) ...[x, 20.25, x + 0.3, 20.25],
      ]);

  for (final scale in [1.0, 2.0]) {
    final px = 1 / scale;
    for (final (name, points) in [
      ('dotted', dotted(9)),
      ('vertical', vertical(30)),
      ('diagonal', diagonal()),
      ('shorter than a pixel', tiny()),
    ]) {
      for (final width in {0.0, px}) {
        for (final cap in [SWT.CAP_FLAT, SWT.CAP_SQUARE]) {
          test('a hairline $name at scale $scale, width $width, cap $cap paints what its ring did', () async {
            final drawn = await _render((c) => SegmentsShape(points, color, width, cap).draw(c), scale);
            final ring = await _render(
                (c) => c.drawVertices(_ring(points, width, color, px, cap), BlendMode.dst, Paint()), scale);
            expect(drawn, ring);
          });
        }
      }
    }
  }

  test('a segment wider than a pixel keeps its core', () async {
    final points = dotted(12);
    final drawn = await _render((c) => SegmentsShape(points, color, 2, SWT.CAP_FLAT).draw(c), 2);
    final ring =
        await _render((c) => c.drawVertices(_ring(points, 2, color, 0.5, SWT.CAP_FLAT), BlendMode.dst, Paint()), 2);
    expect(drawn, ring);
  });
}
