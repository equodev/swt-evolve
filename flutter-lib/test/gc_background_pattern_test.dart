// A Dart-backed Pattern has no native brush: what reaches the drawer is the image or the gradient
// end points, and the fill has to be painted from them. Every native backend lays both out in GC
// coordinates and repeats them past their extent.

import 'dart:convert';
import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/gen/path.dart';
import 'package:swtflutter/src/gen/pathdata.dart';
import 'package:swtflutter/src/gen/pattern.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  var nextId = 134401;

  void deliver(String actionId, Map<String, dynamic> json) {
    final action = utf8.encode(actionId);
    final body = utf8.encode(jsonEncode(json));
    final out = Uint8List(2 + action.length + body.length);
    out[0] = (action.length >> 8) & 0xFF;
    out[1] = action.length & 0xFF;
    out.setRange(2, 2 + action.length, action);
    out.setRange(2 + action.length, out.length, body);
    EquoCommService.commForTesting.receiveBinary(out);
  }

  Future<void> settle() async {
    for (var i = 0; i < 20; i++) {
      await Future<void>.delayed(const Duration(milliseconds: 5));
    }
  }

  (GCDrawer, int) drawerWith(VPattern pattern) {
    final id = nextId++;
    final state = VGC.empty()
      ..id = id
      ..backgroundPattern = pattern;
    final drawer = GCDrawer.embedded(state, onShapesUpdated: (_) {});
    addTearDown(drawer.dispose);
    return (drawer, id);
  }

  Future<List<Shape>> committed(GCDrawer drawer, int id) async {
    deliver('GC/$id/gcDispose', {'fullRepaint': true});
    await settle();
    return drawer.shapes;
  }

  VColor rgb(int r, int g, int b) => VColor()
    ..red = r
    ..green = g
    ..blue = b
    ..alpha = 255;

  /// Renders [shapes] over white and returns the RGBA bytes.
  Future<ByteData> pixels(List<Shape> shapes, int width, int height) async {
    final recorder = ui.PictureRecorder();
    final canvas = ui.Canvas(recorder);
    canvas.drawColor(Colors.white, BlendMode.src);
    for (final shape in shapes) {
      shape.draw(canvas);
    }
    final image = await recorder.endRecording().toImage(width, height);
    return (await image.toByteData(format: ui.ImageByteFormat.rawRgba))!;
  }

  Color at(ByteData data, int width, int x, int y) {
    final i = (y * width + x) * 4;
    return Color.fromARGB(data.getUint8(i + 3), data.getUint8(i),
        data.getUint8(i + 1), data.getUint8(i + 2));
  }

  /// A 2x2 PNG: red on the left column, blue on the right.
  Future<VImage> twoColumnImage() async {
    final recorder = ui.PictureRecorder();
    final canvas = ui.Canvas(recorder);
    canvas.drawRect(const Rect.fromLTWH(0, 0, 1, 2), Paint()..color = const Color(0xFFFF0000));
    canvas.drawRect(const Rect.fromLTWH(1, 0, 1, 2), Paint()..color = const Color(0xFF0000FF));
    final image = await recorder.endRecording().toImage(2, 2);
    final png = await image.toByteData(format: ui.ImageByteFormat.png);
    return VImage()
      ..width = 2
      ..height = 2
      ..imageData = (VImageData()..data = png!.buffer.asUint8List());
  }

  test('fillRectangle with an image pattern tiles the image across the rectangle', () async {
    final (drawer, id) = drawerWith(VPattern()..image = await twoColumnImage());

    drawer.onFillRectangleintintintint(
        VGCFillRectangleintintintint(x: 0, y: 0, width: 8, height: 4));

    final data = await pixels(await committed(drawer, id), 8, 4);
    expect(at(data, 8, 0, 0), const Color(0xFFFF0000));
    expect(at(data, 8, 1, 0), const Color(0xFF0000FF));
    expect(at(data, 8, 6, 3), const Color(0xFFFF0000), reason: 'the tile repeats');
    expect(at(data, 8, 7, 3), const Color(0xFF0000FF), reason: 'the tile repeats');
  });

  test('an image pattern tiles from the GC origin, not from the filled rectangle', () async {
    final (drawer, id) = drawerWith(VPattern()..image = await twoColumnImage());

    drawer.onFillRectangleintintintint(
        VGCFillRectangleintintintint(x: 1, y: 0, width: 3, height: 2));

    final data = await pixels(await committed(drawer, id), 4, 2);
    expect(at(data, 4, 0, 0), const Color(0xFFFFFFFF), reason: 'outside the fill');
    expect(at(data, 4, 1, 0), const Color(0xFF0000FF));
    expect(at(data, 4, 2, 0), const Color(0xFFFF0000));
  });

  test('fillPath honours a gradient pattern', () async {
    final (drawer, id) = drawerWith(VPattern()
      ..color1 = rgb(255, 0, 0)
      ..color2 = rgb(0, 0, 255)
      ..startX = 0
      ..startY = 0
      ..endX = 100
      ..endY = 0);

    drawer.onFillPathPath(VGCFillPathPath()
      ..path = (VPath()
        ..pathData = (VPathData()
          ..types = [SWT.PATH_MOVE_TO, SWT.PATH_LINE_TO, SWT.PATH_LINE_TO, SWT.PATH_LINE_TO, SWT.PATH_CLOSE]
          ..points = [0, 0, 100, 0, 100, 4, 0, 4])));

    final data = await pixels(await committed(drawer, id), 100, 4);
    final left = at(data, 100, 1, 1);
    final right = at(data, 100, 98, 1);
    expect(left.red, greaterThan(200));
    expect(left.blue, lessThan(50));
    expect(right.blue, greaterThan(200));
    expect(right.red, lessThan(50));
  });

  test('a gradient pattern repeats past its end point', () async {
    final (drawer, id) = drawerWith(VPattern()
      ..color1 = rgb(255, 0, 0)
      ..color2 = rgb(0, 0, 255)
      ..startX = 0
      ..startY = 0
      ..endX = 50
      ..endY = 0);

    drawer.onFillRectangleintintintint(
        VGCFillRectangleintintintint(x: 0, y: 0, width: 100, height: 4));

    final data = await pixels(await committed(drawer, id), 100, 4);
    final restart = at(data, 100, 51, 1);
    expect(restart.red, greaterThan(200), reason: 'past x=50 the gradient starts over at color1');
    expect(restart.blue, lessThan(50));
  });
}
