// A GC translated by whole units draws a rectangle, an oval, a round rectangle, an arc, a path or text
// where drawing it at the translated coordinates would: the shape is held moved, under the transform's
// clip, rather than inside a transform of its own, so a rectangle joins the runs of its neighbours and
// the rest are drawn without a transform each. The pixels are the same either way.

import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/channel_header.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/tagged_values.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/gen/path.dart';
import 'package:swtflutter/src/gen/pathdata.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/transform.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';
import 'support/channels.dart' as channels;

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  var nextId = 941001;

  Future<void> settle() async {
    for (var i = 0; i < 20; i++) {
      await Future<void>.delayed(const Duration(milliseconds: 5));
    }
  }

  VRectangle rect(int x, int y, int w, int h) => VRectangle()
    ..x = x
    ..y = y
    ..width = w
    ..height = h;

  /// The shapes a paint of [draw] commits, under [elements] as the GC's transform.
  Future<List<Shape>> painted(List<double>? elements, void Function(GCDrawer d, int dx, int dy) draw,
      {required int dx, required int dy}) async {
    final id = nextId++;
    final state = VGC.empty()
      ..id = id
      ..lineWidth = 1
      ..clipping = rect(5, 5, 50, 50)
      ..transform = elements == null ? null : (VTransform()..elements = elements);
    final drawer = GCDrawer.embedded(state, onShapesUpdated: (_) {});
    addTearDown(drawer.dispose);
    draw(drawer, dx, dy);
    EquoCommService.commForTesting.receiveBinary(
        channels.frame('GC/$id/gcDispose', TaggedValues.encode({'fullRepaint': true})));
    await settle();
    return List.of(drawer.shapes);
  }

  void scene(GCDrawer d, int dx, int dy) {
    d.onFillRectangleintintintint(VGCFillRectangleintintintint()
      ..x = 2 + dx
      ..y = 3 + dy
      ..width = 20
      ..height = 12);
    d.onDrawRectangleintintintint(VGCDrawRectangleintintintint()
      ..x = 2 + dx
      ..y = 3 + dy
      ..width = 20
      ..height = 12);
    d.onDrawLineintintintint(VGCDrawLineintintintint()
      ..x1 = 1 + dx
      ..y1 = 20 + dy
      ..x2 = 30 + dx
      ..y2 = 28 + dy);
    d.onFillOvalintintintint(VGCFillOvalintintintint()
      ..x = 24 + dx
      ..y = 2 + dy
      ..width = 14
      ..height = 10);
    d.onFillPolygonint(VGCFillPolygonint()..pointArray = [2 + dx, 32 + dy, 14 + dx, 30 + dy, 8 + dx, 40 + dy]);
    d.onDrawTextStringintintint(VGCDrawTextStringintintint()
      ..string = 'Ag'
      ..x = 16 + dx
      ..y = 30 + dy
      ..flags = 1);
  }

  // gc.paths, gc.pie and gc.calendar: curved paths, pie slices and round rectangles under translations.
  void curves(GCDrawer d, int dx, int dy) {
    d.onFillRoundRectangleintintintintintint(VGCFillRoundRectangleintintintintintint()
      ..x = 2 + dx
      ..y = 2 + dy
      ..width = 24
      ..height = 10
      ..arcWidth = 6
      ..arcHeight = 6);
    d.onDrawRoundRectangleintintintintintint(VGCDrawRoundRectangleintintintintintint()
      ..x = 2 + dx
      ..y = 2 + dy
      ..width = 24
      ..height = 10
      ..arcWidth = 6
      ..arcHeight = 6);
    d.onFillArcintintintintintint(VGCFillArcintintintintintint()
      ..x = 28 + dx
      ..y = 2 + dy
      ..width = 18
      ..height = 18
      ..startAngle = 30
      ..arcAngle = 200);
    d.onDrawArcintintintintintint(VGCDrawArcintintintintintint()
      ..x = 28 + dx
      ..y = 2 + dy
      ..width = 18
      ..height = 18
      ..startAngle = 30
      ..arcAngle = 200);
    final path = VPath()
      ..pathData = (VPathData()
        ..types = [SWT.PATH_MOVE_TO, SWT.PATH_CUBIC_TO, SWT.PATH_QUAD_TO, SWT.PATH_CLOSE]
        ..points = [2.0 + dx, 30.0 + dy, 10.0 + dx, 18.0 + dy, 20.0 + dx, 40.0 + dy, 30.0 + dx, 28.0 + dy,
          22.0 + dx, 38.0 + dy, 8.0 + dx, 40.0 + dy]);
    d.onFillPathPath(VGCFillPathPath()..path = path);
    d.onDrawPathPath(VGCDrawPathPath()..path = path);
  }

  Future<Uint8List> render(List<Shape> shapes) async {
    final recorder = ui.PictureRecorder();
    final c = ui.Canvas(recorder)..drawRect(const Rect.fromLTWH(0, 0, 64, 64), Paint()..color = Colors.white);
    drawShapeRun(c, shapes);
    final image = await recorder.endRecording().toImage(64, 64);
    return (await image.toByteData(format: ui.ImageByteFormat.rawRgba))!.buffer.asUint8List();
  }

  test('shapes drawn under a whole-unit translation are held moved, and paint the same pixels', () async {
    const translate = [1.0, 0.0, 0.0, 1.0, 9.0, 6.0];
    final moved = await painted(translate, scene, dx: 0, dy: 0);
    final atTheCoordinates = await painted(null, scene, dx: 9, dy: 6);

    // Rectangles, ovals and text are held moved; the line and the polygon keep their transform.
    expect(moved.whereType<TransformShape>(), hasLength(2), reason: '$moved');
    expect(await render(moved), await render(atTheCoordinates));
  });

  test('round rectangles, arcs and paths under a whole-unit translation are held moved, and paint the same pixels',
      () async {
    const translate = [1.0, 0.0, 0.0, 1.0, 9.0, 6.0];
    final moved = await painted(translate, curves, dx: 0, dy: 0);
    final atTheCoordinates = await painted(null, curves, dx: 9, dy: 6);

    expect(moved.whereType<TransformShape>(), isEmpty, reason: '$moved');
    expect(await render(moved), await render(atTheCoordinates));
  });

  test('a path drawn under a whole-unit translation is built once, where it lands, when it is painted', () async {
    void fill(GCDrawer d, int dx, int dy) => d.onFillPathPath(VGCFillPathPath()
      ..path = (VPath()
        ..pathData = (VPathData()
          ..types = [SWT.PATH_MOVE_TO, SWT.PATH_LINE_TO, SWT.PATH_LINE_TO, SWT.PATH_CLOSE]
          ..points = [2.0, 2.0, 30.0, 4.0, 12.0, 26.0])));
    final before = pathsBuilt;
    final moved = await painted(const [1.0, 0.0, 0.0, 1.0, 9.0, 6.0], fill, dx: 0, dy: 0);
    expect(pathsBuilt - before, 0, reason: 'nothing needed the path before it was painted');

    await render(moved);
    expect(pathsBuilt - before, 1);
    expect((moved.single as PathShape).path.getBounds(), const Rect.fromLTRB(11, 8, 39, 32));
  });

  test('a fractional translation keeps its transform', () async {
    final shapes = await painted([1.0, 0.0, 0.0, 1.0, 9.5, 6.0], scene, dx: 0, dy: 0);

    expect(shapes.whereType<TransformShape>(), isNotEmpty);
  });
}
