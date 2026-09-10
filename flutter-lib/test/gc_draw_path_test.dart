// A Dart-backed Path has no OS handle, so the PathData a Java Path recorded is the whole shape:
// if drawPath/fillPath do not replay points+types, the canvas stays empty however much the
// application drew. SWT's default fill rule is even-odd, which is what makes nested sub paths
// read as rings rather than one solid blob.

import 'dart:convert';
import 'dart:typed_data';
import 'dart:ui' show Offset, PathFillType, Rect;

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/gen/path.dart';
import 'package:swtflutter/src/gen/pathdata.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  var nextId = 134101;

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

  (GCDrawer, int) drawerWith({int? fillRule}) {
    final id = nextId++;
    final state = VGC.empty()
      ..id = id
      ..fillRule = fillRule;
    final drawer = GCDrawer.embedded(state, onShapesUpdated: (_) {});
    addTearDown(drawer.dispose);
    return (drawer, id);
  }

  Future<List<Shape>> committed(GCDrawer drawer, int id) async {
    deliver('GC/$id/gcDispose', {'fullRepaint': true});
    await settle();
    return drawer.shapes;
  }

  VPath path(List<int> types, List<double> points) => VPath()
    ..pathData = (VPathData()
      ..types = types
      ..points = points);

  test('drawPath replays the geometry the application built', () async {
    final (drawer, id) = drawerWith();

    // The 4-point outline of Lines > Line Join: moveTo + lineTo x3 + close.
    drawer.onDrawPathPath(VGCDrawPathPath()
      ..path = path([
        SWT.PATH_MOVE_TO,
        SWT.PATH_LINE_TO,
        SWT.PATH_LINE_TO,
        SWT.PATH_LINE_TO,
        SWT.PATH_CLOSE,
      ], [
        50, 10, //
        90, 50, //
        50, 90, //
        10, 50,
      ]));

    final shapes = await committed(drawer, id);
    expect(shapes, hasLength(1));
    final shape = shapes.single as PathShape;
    expect(shape.isFilled, isFalse);
    expect(shape.path.getBounds(), const Rect.fromLTRB(10, 10, 90, 90));
  });

  test('a curve is replayed as a curve, not as its control points', () async {
    final (drawer, id) = drawerWith();

    // Path > Path Operations draws a cubic on a fresh path: the recorded moveTo(0,0) opens it.
    drawer.onDrawPathPath(VGCDrawPathPath()
      ..path = path([SWT.PATH_MOVE_TO, SWT.PATH_CUBIC_TO],
          [0, 0, -150, 100, 150, 200, 0, 300]));

    final metric =
        ((await committed(drawer, id)).single as PathShape).path.computeMetrics().single;
    expect(metric.getTangentForOffset(metric.length)!.position, const Offset(0, 300));
    expect(metric.length, greaterThan(340),
        reason: 'a cubic run through those control points is far longer than the 300 chord');
  });

  test('quadTo travels as a quadratic', () async {
    final (drawer, id) = drawerWith();

    drawer.onDrawPathPath(VGCDrawPathPath()
      ..path = path([SWT.PATH_MOVE_TO, SWT.PATH_QUAD_TO], [0, 0, 50, 100, 100, 0]));

    final metric =
        ((await committed(drawer, id)).single as PathShape).path.computeMetrics().single;
    expect(metric.getTangentForOffset(metric.length)!.position, const Offset(100, 0));
    expect(metric.getTangentForOffset(metric.length / 2)!.position.dy, 50,
        reason: 'the apex of that quadratic sits halfway up its control point');
  });

  test('fillPath fills, and follows SWT default even-odd rule', () async {
    final (drawer, id) = drawerWith();

    drawer.onFillPathPath(VGCFillPathPath()
      ..path = path([SWT.PATH_MOVE_TO, SWT.PATH_LINE_TO, SWT.PATH_LINE_TO, SWT.PATH_CLOSE],
          [0, 0, 10, 0, 10, 10]));

    final shape = (await committed(drawer, id)).single as PathShape;
    expect(shape.isFilled, isTrue);
    expect(shape.path.fillType, PathFillType.evenOdd,
        reason: 'nested sub paths only read as rings under even-odd');
  });

  test('an explicit winding fill rule overrides the default', () async {
    final (drawer, id) = drawerWith(fillRule: SWT.FILL_WINDING);

    drawer.onFillPathPath(VGCFillPathPath()
      ..path = path([SWT.PATH_MOVE_TO, SWT.PATH_LINE_TO], [0, 0, 10, 10]));

    final shape = (await committed(drawer, id)).single as PathShape;
    expect(shape.path.fillType, PathFillType.nonZero);
  });

  test('a path with no geometry draws nothing rather than an empty shape', () async {
    final (drawer, id) = drawerWith();

    drawer.onDrawPathPath(VGCDrawPathPath()..path = VPath());
    drawer.onFillPathPath(VGCFillPathPath());

    expect(await committed(drawer, id), isEmpty);
  });

  test('the types array survives the base64 encoding dsl-json gives a Java byte[]', () {
    final decoded = VPathData.fromJson({
      'types': base64Encode([SWT.PATH_MOVE_TO, SWT.PATH_LINE_TO, SWT.PATH_CLOSE]),
      'points': [1.0, 2.0, 3.0, 4.0],
    });
    expect(decoded.types, [SWT.PATH_MOVE_TO, SWT.PATH_LINE_TO, SWT.PATH_CLOSE]);
    expect(decoded.points, [1.0, 2.0, 3.0, 4.0]);
  });
}
