// draw2d and GEF draw every piece of transient feedback in XOR mode, and they draw it *white* —
// `MarqueeSelectionTool.MarqueeRectangleFigure.paintFigure` does `setXORMode(true)` then
// `setForegroundColor(ColorConstants.white)` (verified with javap on gef 3.11.0.201606061308).
// The inversion is what makes it visible; painted normally it is opaque white on a light canvas,
// which is nothing at all.
//
// `VGC.XORMode` already crossed the wire — Java set it and the generated deserializer read it —
// and the drawer ignored it, so a GEF marquee drag changed zero pixels on the reported app.

import 'dart:convert';
import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';

Uint8List _frame(String actionId, List<int> body) {
  final action = utf8.encode(actionId);
  final out = Uint8List(2 + action.length + body.length);
  out[0] = (action.length >> 8) & 0xFF;
  out[1] = action.length & 0xFF;
  out.setRange(2, 2 + action.length, action);
  out.setRange(2 + action.length, out.length, body);
  return out;
}

void _deliver(String actionId, Map<String, dynamic> json) =>
    EquoCommService.commForTesting
        .receiveBinary(_frame(actionId, utf8.encode(jsonEncode(json))));

Future<void> _settle() async {
  for (var i = 0; i < 40; i++) {
    await Future<void>.delayed(Duration.zero);
  }
}

/// Every shape in [shapes], flattened through the wrapper shapes.
List<Shape> _flatten(List<Shape> shapes) {
  final out = <Shape>[];
  void walk(List<Shape> list) {
    for (final s in list) {
      out.add(s);
      if (s is XorShape) walk(s.children);
      if (s is TransformShape) walk(s.children);
      if (s is ClipPathShape) walk(s.children);
      if (s is RegionShape) walk(s.ops);
    }
  }

  walk(shapes);
  return out;
}

/// Renders [shapes] over an opaque white backdrop and reads back the centre pixel.
Future<ui.Color> _paintAndSample(List<Shape> shapes, {int size = 12}) async {
  final recorder = ui.PictureRecorder();
  final canvas = ui.Canvas(recorder);
  canvas.drawRect(Rect.fromLTWH(0, 0, size.toDouble(), size.toDouble()),
      Paint()..color = const ui.Color(0xFFFFFFFF));
  for (final s in shapes) {
    s.draw(canvas);
  }
  final image = await recorder.endRecording().toImage(size, size);
  final data = await image.toByteData(format: ui.ImageByteFormat.rawRgba);
  final b = data!.buffer.asUint8List();
  final i = ((size ~/ 2) * size + (size ~/ 2)) * 4;
  final c = ui.Color.fromARGB(b[i + 3], b[i], b[i + 1], b[i + 2]);
  image.dispose();
  return c;
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  const gcId = 8801;

  /// A white horizontal line across the middle, exactly as the marquee draws it.
  void whiteLineAcross() => _deliver('GC/$gcId/drawLineintintintint',
      {'x1': 0, 'y1': 6, 'x2': 12, 'y2': 6});

  void setForegroundWhite() => _deliver('GC/$gcId/state', {
        'swt': 'GC',
        'id': gcId,
        'foreground': {'red': 255, 'green': 255, 'blue': 255, 'alpha': 255},
      });

  test('a white line drawn in XOR mode inverts the white backdrop to black', () async {
    late List<Shape> committed;
    GCDrawer.embedded(
      VGC()
        ..swt = 'GC'
        ..id = gcId
        ..XORMode = true
        ..lineWidth = 6
        ..foreground = (VColor()
          ..red = 255
          ..green = 255
          ..blue = 255
          ..alpha = 255),
      onShapesUpdated: (s) => committed = List<Shape>.from(s),
    );

    whiteLineAcross();
    _deliver('GC/$gcId/gcDispose', {'fullRepaint': false});
    await _settle();

    expect(_flatten(committed).whereType<XorShape>(), hasLength(1),
        reason: 'an op drawn while XORMode is on must be wrapped so it inverts');

    final sampled = await _paintAndSample(committed);
    expect(sampled.red, lessThan(64),
        reason: 'white XOR white is black — this is what makes the marquee visible; '
            'painted normally the sample stays 0xFF and the rubber band is invisible');
    expect(sampled.green, lessThan(64));
    expect(sampled.blue, lessThan(64));
  });

  test('the same line without XOR mode stays white, and is wrapped in nothing', () async {
    late List<Shape> committed;
    GCDrawer.embedded(
      VGC()
        ..swt = 'GC'
        ..id = gcId + 1
        ..lineWidth = 6
        ..foreground = (VColor()
          ..red = 255
          ..green = 255
          ..blue = 255
          ..alpha = 255),
      onShapesUpdated: (s) => committed = List<Shape>.from(s),
    );

    _deliver('GC/${gcId + 1}/drawLineintintintint',
        {'x1': 0, 'y1': 6, 'x2': 12, 'y2': 6});
    _deliver('GC/${gcId + 1}/gcDispose', {'fullRepaint': false});
    await _settle();

    expect(_flatten(committed).whereType<XorShape>(), isEmpty,
        reason: 'the wrapper is only for XOR mode; every other op keeps its plain blend');

    final sampled = await _paintAndSample(committed);
    expect(sampled.red, greaterThan(200),
        reason: 'without XOR the white line stays white — the behaviour every '
            'non-XOR GC op must keep');
  });

  test('XOR feedback never claims to cover what it paints over', () {
    final xor = XorShape([
      LineShape(const Offset(0, 0), const Offset(10, 10), const Color(0xFFFFFFFF),
          1, 0, 0),
    ]);

    expect(xor.opaqueCoverage, isNull,
        reason: 'an inverting overlay hides nothing, so a cycle carrying it must not '
            'drop the cycle underneath it');
    expect(xor.paintedBounds, isNull);
  });
}
