// A GC attribute only reaches the canvas if Java puts it on the wire and the drawer reads it back:
// lineStyle and fillRule are both set once and then govern every op that follows, so losing either
// leaves a drawing that is entirely plausible and entirely wrong - dashes stroked solid, a
// self-overlapping polygon filled through.

import 'dart:convert';
import 'dart:math' as math;
import 'dart:typed_data';
import 'dart:ui' as ui;
import 'dart:ui' show Rect;

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  var nextId = 134301;

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

  (GCDrawer, int) drawerWith(
      {int? lineStyle, int? lineWidth, List<int>? lineDash, int? fillRule}) {
    final id = nextId++;
    final state = VGC.empty()
      ..id = id
      ..lineStyle = lineStyle
      ..lineWidth = lineWidth
      ..lineDash = lineDash
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

  /// Lines > Line Styles draws each style as one horizontal rule across the canvas.
  Future<List<Shape>> horizontalRule(
      {int? lineStyle, int? lineWidth, List<int>? lineDash}) async {
    final (drawer, id) = drawerWith(
        lineStyle: lineStyle, lineWidth: lineWidth, lineDash: lineDash);
    drawer.onDrawLineintintintint(VGCDrawLineintintintint()
      ..x1 = 0
      ..y1 = 10
      ..x2 = 100
      ..y2 = 10);
    return committed(drawer, id);
  }

  /// How much of the stroke is actually drawn, along the whole shape.
  double inkedLength(PathShape shape) => shape.path
      .computeMetrics()
      .fold(0.0, (total, metric) => total + metric.length);

  group('line style', () {
    test('a solid line is one unbroken stroke', () async {
      final shapes = await horizontalRule(lineStyle: SWT.LINE_SOLID);

      expect(shapes.single, isA<LineShape>());
    });

    test('an unset line style draws solid, as SWT starts out', () async {
      final shapes = await horizontalRule();

      expect(shapes.single, isA<LineShape>());
    });

    test('LINE_DASH leaves gaps along the line it spans', () async {
      final shapes = await horizontalRule(lineStyle: SWT.LINE_DASH, lineWidth: 4);

      final dashed = shapes.single as PathShape;
      expect(dashed.path.getBounds().left, 0);
      expect(dashed.path.getBounds().right, 100);
      // 12 on, 4 off in line-width units: three quarters of the run is ink.
      expect(inkedLength(dashed), closeTo(75, 12));
    });

    test('LINE_DOT lays down less ink than LINE_DASH over the same run',
        () async {
      final dash = (await horizontalRule(lineStyle: SWT.LINE_DASH, lineWidth: 4))
          .single as PathShape;
      final dot = (await horizontalRule(lineStyle: SWT.LINE_DOT, lineWidth: 4))
          .single as PathShape;

      expect(inkedLength(dot), lessThan(inkedLength(dash)));
    });

    test('every dashed style is distinguishable from the others', () async {
      final inked = <int, double>{};
      for (final style in [
        SWT.LINE_DASH,
        SWT.LINE_DOT,
        SWT.LINE_DASHDOT,
        SWT.LINE_DASHDOTDOT,
      ]) {
        final shapes = await horizontalRule(lineStyle: style, lineWidth: 4);
        inked[style] = inkedLength(shapes.single as PathShape);
      }

      expect(inked.values.toSet(), hasLength(inked.length));
      // Solid is the line itself; every dashed style draws less than the whole run.
      expect(inked.values.every((length) => length < 100), isTrue);
    });

    test('a hairline dashes too, at the width-independent pattern', () async {
      final shapes =
          await horizontalRule(lineStyle: SWT.LINE_DASH, lineWidth: 0);

      // 18 on, 6 off: a quarter of the run is gap.
      expect(inkedLength(shapes.single as PathShape), closeTo(75, 12));
    });

    test('LINE_CUSTOM follows the dash lengths the application set', () async {
      final shapes = await horizontalRule(
          lineStyle: SWT.LINE_CUSTOM, lineWidth: 1, lineDash: [20, 20]);

      // Taken as given rather than scaled by the width: 20 on, 20 off, three times over the run.
      expect(inkedLength(shapes.single as PathShape), closeTo(60, 1));
    });

    test('LINE_CUSTOM with no dashes falls back to solid, as SWT does',
        () async {
      final shapes =
          await horizontalRule(lineStyle: SWT.LINE_CUSTOM, lineWidth: 1);

      expect(shapes.single, isA<LineShape>());
    });

    test('a dashed rectangle outline dashes as well', () async {
      final (drawer, id) = drawerWith(lineStyle: SWT.LINE_DASH, lineWidth: 2);
      drawer.onDrawRectangleintintintint(VGCDrawRectangleintintintint()
        ..x = 0
        ..y = 0
        ..width = 40
        ..height = 40);

      final shapes = await committed(drawer, id);
      final outline = shapes.single as PathShape;
      expect(outline.path.getBounds(), const Rect.fromLTRB(0, 0, 40, 40));
      expect(inkedLength(outline), lessThan(160));
    });

    test('a filled shape is untouched by the line style', () async {
      final (drawer, id) = drawerWith(lineStyle: SWT.LINE_DASH, lineWidth: 2);
      drawer.onFillRectangleintintintint(VGCFillRectangleintintintint()
        ..x = 0
        ..y = 0
        ..width = 40
        ..height = 40);

      expect((await committed(drawer, id)).single, isA<RectShape>());
    });
  });

  group('fill rule', () {
    // Polygons > Star Polygon: a five-pointed star drawn as one self-crossing outline, whose
    // pentagonal middle is covered twice and so falls to the fill rule in force.
    final star = <int>[];
    for (var i = 0; i < 5; i++) {
      final angle = -math.pi / 2 + i * 4 * math.pi / 5;
      star.add((50 + 40 * math.cos(angle)).round());
      star.add((50 + 40 * math.sin(angle)).round());
    }

    Future<bool> starCentreIsFilled({int? fillRule}) async {
      final (drawer, id) = drawerWith(fillRule: fillRule);
      drawer.onFillPolygonint(VGCFillPolygonint()..pointArray = star);
      final shapes = await committed(drawer, id);

      final recorder = ui.PictureRecorder();
      shapes.single.draw(ui.Canvas(recorder));
      final image = await recorder.endRecording().toImage(100, 100);
      final pixels = await image.toByteData();
      // Alpha of the centre pixel, which the star's arms enclose twice over.
      return pixels!.getUint8((50 * 100 + 50) * 4 + 3) != 0;
    }

    test('the default rule leaves the star centre unfilled, as SWT does',
        () async {
      expect(await starCentreIsFilled(), isFalse);
    });

    test('FILL_EVEN_ODD leaves the star centre unfilled', () async {
      expect(await starCentreIsFilled(fillRule: SWT.FILL_EVEN_ODD), isFalse);
    });

    test('FILL_WINDING fills the star through', () async {
      expect(await starCentreIsFilled(fillRule: SWT.FILL_WINDING), isTrue);
    });

    test('an arm of the star is filled under either rule', () async {
      for (final rule in [SWT.FILL_EVEN_ODD, SWT.FILL_WINDING]) {
        final (drawer, id) = drawerWith(fillRule: rule);
        drawer.onFillPolygonint(VGCFillPolygonint()..pointArray = star);
        final shapes = await committed(drawer, id);

        final recorder = ui.PictureRecorder();
        shapes.single.draw(ui.Canvas(recorder));
        final image = await recorder.endRecording().toImage(100, 100);
        final pixels = await image.toByteData();
        // Just below the top point, inside the arm and outside the middle pentagon.
        expect(pixels!.getUint8((18 * 100 + 50) * 4 + 3), isNot(0),
            reason: 'fill rule $rule');
      }
    });
  });
}
