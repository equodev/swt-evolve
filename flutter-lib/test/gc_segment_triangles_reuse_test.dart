// A dashed line drawn frame after frame is the same thousands of dashes: their triangles are built
// once and drawn again while the segments, the stroke and the device pixel stay the same.

import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';

/// Keeps the triangles drawn; reports the transform it was made with.
class _Calls implements ui.Canvas {
  _Calls([this.factor = 1]);
  final double factor;
  final List<ui.Vertices> drawn = [];

  @override
  void drawVertices(ui.Vertices vertices, BlendMode blendMode, Paint paint) => drawn.add(vertices);

  @override
  Float64List getTransform() => Float64List.fromList([factor, 0, 0, 0, 0, factor, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1]);

  @override
  dynamic noSuchMethod(Invocation invocation) => null;
}

Future<Uint8List> _render(Shape shape) async {
  final recorder = ui.PictureRecorder();
  final canvas = ui.Canvas(recorder)
    ..drawRect(const Rect.fromLTWH(0, 0, 80, 20), Paint()..color = const Color(0xFFFFFFFF));
  shape.draw(canvas);
  final image = await recorder.endRecording().toImage(80, 20);
  return (await image.toByteData(format: ui.ImageByteFormat.rawRgba))!.buffer.asUint8List();
}

void main() {
  const color = Color(0xFF203040);

  // A dotted line as a GC's dashing makes it.
  Float32List dots({double y = 9, double start = 0}) => Float32List.fromList([
        for (var x = start; x + 3 <= 76; x += 6) ...[x, y, x + 3, y],
      ]);

  List<ui.Vertices> drawn(Shape shape, [double scale = 1]) {
    final calls = _Calls(scale);
    shape.draw(calls);
    return calls.drawn;
  }

  test('equal segments in a new list draw the same triangles', () {
    final first = drawn(SegmentsShape(dots(), color, 0, SWT.CAP_FLAT));
    final again = drawn(SegmentsShape(dots(), color, 0, SWT.CAP_FLAT));
    expect(first, isNotEmpty);
    expect(again.length, first.length);
    for (var i = 0; i < first.length; i++) {
      expect(identical(again[i], first[i]), isTrue);
    }
  });

  test('a change of segment, colour, width, device pixel or antialiasing builds them anew', () {
    final base = drawn(SegmentsShape(dots(), color, 0, SWT.CAP_FLAT)).single;
    for (final (name, triangles) in [
      ('moved', drawn(SegmentsShape(dots(y: 10), color, 0, SWT.CAP_FLAT)).single),
      ('colour', drawn(SegmentsShape(dots(), const Color(0xFF203041), 0, SWT.CAP_FLAT)).single),
      ('width', drawn(SegmentsShape(dots(), color, 1, SWT.CAP_FLAT)).single),
      ('cap', drawn(SegmentsShape(dots(), color, 0, SWT.CAP_SQUARE)).single),
      ('scale', drawn(SegmentsShape(dots(), color, 0, SWT.CAP_FLAT), 2).single),
      ('antialias', drawn(RenderHintsShape(false, null, [SegmentsShape(dots(), color, 0, SWT.CAP_FLAT)])).single),
    ]) {
      expect(identical(triangles, base), isFalse, reason: name);
    }
  });

  test('a list changed after it was drawn is not taken for what it held', () async {
    final reused = dots(start: 1);
    final shape = SegmentsShape(reused, color, 1, SWT.CAP_FLAT);
    final before = drawn(shape).single;
    reused[0] = 2;
    expect(identical(drawn(SegmentsShape(reused, color, 1, SWT.CAP_FLAT)).single, before), isFalse);
    expect(await _render(SegmentsShape(reused, color, 1, SWT.CAP_FLAT)),
        await _render(SegmentsShape(Float32List.fromList(reused), color, 1, SWT.CAP_FLAT)));
  });

  test('a list drawn again, then changed, is not taken for what it held', () {
    final reused = dots(start: 2);
    final before = drawn(SegmentsShape(reused, color, 1, SWT.CAP_FLAT)).single;
    expect(identical(drawn(SegmentsShape(reused, color, 1, SWT.CAP_FLAT)).single, before), isTrue);
    reused[0] = 3;
    expect(identical(drawn(SegmentsShape(reused, color, 1, SWT.CAP_FLAT)).single, before), isFalse);
    expect(identical(drawn(SegmentsShape(dots(start: 2), color, 1, SWT.CAP_FLAT)).single, before), isTrue);
  });

  test('a list that never changes keeps its triangles while its stroke stays the same', () async {
    final points = dots(start: 4);
    SegmentsShape fixed(Color color, double width, int cap) => SegmentsShape(points, color, width, cap, null, true);
    final base = drawn(fixed(color, 0, SWT.CAP_FLAT)).single;
    expect(identical(drawn(fixed(color, 0, SWT.CAP_FLAT)).single, base), isTrue);
    for (final (name, triangles) in [
      ('colour', drawn(fixed(const Color(0xFF203041), 0, SWT.CAP_FLAT)).single),
      ('width', drawn(fixed(color, 1, SWT.CAP_FLAT)).single),
      ('cap', drawn(fixed(color, 0, SWT.CAP_SQUARE)).single),
      ('scale', drawn(fixed(color, 0, SWT.CAP_FLAT), 2).single),
      ('antialias', drawn(RenderHintsShape(false, null, [fixed(color, 0, SWT.CAP_FLAT)])).single),
    ]) {
      expect(identical(triangles, base), isFalse, reason: name);
    }
    for (final width in [0.0, 1.0, 2.0]) {
      expect(await _render(fixed(color, width, SWT.CAP_FLAT)),
          await _render(SegmentsShape(Float32List.fromList(points), color, width, SWT.CAP_FLAT)),
          reason: 'width $width');
    }
  });

  test('triangles drawn again paint what they painted when built', () async {
    for (final width in [0.0, 1.0, 2.0]) {
      final fresh = await _render(SegmentsShape(dots(start: width), color, width, SWT.CAP_FLAT));
      final reused = await _render(SegmentsShape(dots(start: width), color, width, SWT.CAP_FLAT));
      expect(reused, fresh, reason: 'width $width');
    }
  });
}
