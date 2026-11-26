// Shapes drawn one after another under the same transform and clip share one transform: the canvas
// is moved once for them, and they paint what each under a transform of its own paints.

import 'dart:math' as math;
import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/tagged_values.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/transform.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';
import 'support/channels.dart' as channels;

const _size = 240;

/// [shapes] as a view of pixel ratio [ratio] shows them: the scene recorded, then scaled by the layer.
Future<Uint8List> _render(List<Shape> shapes, double ratio) async {
  final scene = ui.PictureRecorder();
  ScenePainter(const Color(0xFFFFFFFF), shapes, devicePixelRatio: ratio)
      .paint(ui.Canvas(scene), Size(_size / ratio, _size / ratio));
  final picture = scene.endRecording();
  final recorder = ui.PictureRecorder();
  ui.Canvas(recorder)
    ..scale(ratio)
    ..drawPicture(picture);
  final image = await recorder.endRecording().toImage(_size, _size);
  return (await image.toByteData(format: ui.ImageByteFormat.rawRgba))!.buffer.asUint8List();
}

/// Every shape a transform holds under a transform of its own, as each was staged before.
List<Shape> _oneEach(List<Shape> shapes) => [
      for (final s in shapes)
        if (s is TransformShape)
          for (final child in s.children) TransformShape(s.matrix, [child], s.clipRect)
        else
          s
    ];

VColor _color(int argb) => VColor.fromArgb(argb);

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  var nextId = 943001;

  Future<void> settle() async {
    for (var i = 0; i < 20; i++) {
      await Future<void>.delayed(const Duration(milliseconds: 5));
    }
  }

  // A diagram as draw2d paints one: each figure under a clip of its own bounds, then connections.
  Future<List<Shape>> diagram(List<double> transform) async {
    final id = nextId++;
    final state = VGC.empty()
      ..id = id
      ..transform = (VTransform()..elements = transform)
      ..lineWidth = 1;
    final d = GCDrawer.embedded(state);
    addTearDown(d.dispose);
    for (var f = 0; f < 12; f++) {
      final x = (f % 4) * 34 + 3, y = (f ~/ 4) * 30 + 3;
      state.clipping = VRectangle()
        ..x = x
        ..y = y
        ..width = 29
        ..height = 23;
      state.background = _color(0xFF000000 | (f * 0x1F3A57));
      d.onFillRectangleintintintint(VGCFillRectangleintintintint()
        ..x = x
        ..y = y
        ..width = 28
        ..height = 22);
      state.foreground = _color(0xFF102030);
      d.onDrawRectangleintintintint(VGCDrawRectangleintintintint()
        ..x = x
        ..y = y
        ..width = 28
        ..height = 22);
      d.onDrawLineintintintint(VGCDrawLineintintintint()
        ..x1 = x + 2
        ..y1 = y + 20
        ..x2 = x + 26
        ..y2 = y + 4);
      d.onDrawTextStringintintint(VGCDrawTextStringintintint()
        ..string = 'F$f'
        ..x = x + 3
        ..y = y + 3
        ..flags = 1);
    }
    state.clipping = null;
    state.foreground = _color(0xFFC03020);
    for (var f = 0; f + 1 < 12; f++) {
      final x = (f % 4) * 34 + 3, y = (f ~/ 4) * 30 + 3;
      d.onDrawPolylineint(VGCDrawPolylineint()..pointArray = [x + 28, y + 11, x + 31, y + 11, x + 34, y + 26]);
      d.onDrawLineintintintint(VGCDrawLineintintintint()
        ..x1 = x + 14
        ..y1 = y + 22
        ..x2 = x + 14
        ..y2 = y + 30);
      d.onFillOvalintintintint(VGCFillOvalintintintint()
        ..x = x + 12
        ..y = y + 20
        ..width = 4
        ..height = 4);
    }
    EquoCommService.commForTesting
        .receiveBinary(channels.frame('GC/$id/gcDispose', TaggedValues.encode({'fullRepaint': true})));
    await settle();
    return d.shapes;
  }

  final cos = math.cos(math.pi / 9), sin = math.sin(math.pi / 9);
  for (final (name, transform) in [
    ('zoomed to 150%', [1.5, 0.0, 0.0, 1.5, 0.0, 0.0]),
    ('zoomed to 200% and moved', [2.0, 0.0, 0.0, 2.0, 5.0, 3.0]),
    ('zoomed to 125% and moved by half a unit', [1.25, 0.0, 0.0, 1.25, 0.5, 0.5]),
    ('rotated', [cos, sin, -sin, cos, 60.0, 10.0]),
  ]) {
    test('$name: shapes under one transform paint what each under its own does', () async {
      final shapes = await diagram(transform);
      final separate = _oneEach(shapes);
      // A figure's shapes share its clip, the connections share none: one transform for each.
      expect(shapes.whereType<TransformShape>(), hasLength(13));
      expect(separate, hasLength(12 * 4 + 11 * 3));
      for (final ratio in [1.0, 2.0]) {
        expect(await _render(shapes, ratio), await _render(separate, ratio), reason: 'pixel ratio $ratio');
      }
    });
  }
}
