// A GC under a transform keeps its rectangle clip in device space: an owner-drawn row's GC is
// translated to the row and clipped to one cell, and text drawn wider than the cell must stop at
// the cell's edge rather than run over the next columns.

import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/tagged_values.dart';
import 'package:swtflutter/src/comm/widget_wire.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/transform.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';
import 'support/channels.dart' as channels;

const Size canvasSize = Size(100, 40);
const Color canvasBg = Color(0xFFFFFFFF);

/// Where the cell ends on the canvas: the clip's right edge, in device space.
const int cellRight = 40;

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  var nextId = 961001;

  Future<void> settle() async {
    for (var i = 0; i < 20; i++) {
      await Future<void>.delayed(Duration.zero);
    }
  }

  /// A drawer translated 10 pixels left and clipped to the first [cellRight] device pixels.
  (GCDrawer, int) cellDrawer() {
    final id = nextId++;
    final gc = VGC.empty()
      ..foreground = (VColor()
        ..red = 0
        ..green = 0
        ..blue = 0
        ..alpha = 255)
      ..background = (VColor()
        ..red = 0
        ..green = 0
        ..blue = 255
        ..alpha = 255)
      ..transform = (VTransform()..elements = [1.0, 0.0, 0.0, 1.0, -10.0, 0.0])
      ..clipping = (VRectangle()
        ..x = 0
        ..y = 0
        ..width = cellRight
        ..height = 40);
    final pushed = Uint8List.fromList(WidgetWireWriter.encodeWith((w) {
      gc.writeWire(w);
      w.u8(0);
    }));
    final state = WidgetWireReader(pushed).fields(VGC.empty()
      ..id = id
      ..swt = 'GC');
    final drawer = GCDrawer.embedded(state, onShapesUpdated: (_) {});
    addTearDown(drawer.dispose);
    return (drawer, id);
  }

  Future<Uint8List> rasterize(GCDrawer drawer, int id) async {
    final body = TaggedValues.encode({'fullRepaint': true});
    EquoCommService.commForTesting.receiveBinary(channels.frame('GC/$id/gcDispose', body));
    await settle();
    final recorder = ui.PictureRecorder();
    ScenePainter(canvasBg, drawer.shapes).paint(Canvas(recorder), canvasSize);
    final image = await recorder
        .endRecording()
        .toImage(canvasSize.width.toInt(), canvasSize.height.toInt());
    final bytes = await image.toByteData(format: ui.ImageByteFormat.rawRgba);
    image.dispose();
    return bytes!.buffer.asUint8List();
  }

  bool inkedColumn(Uint8List rgba, int x) {
    for (var y = 0; y < canvasSize.height.toInt(); y++) {
      final i = (y * canvasSize.width.toInt() + x) * 4;
      if (Color.fromARGB(rgba[i + 3], rgba[i], rgba[i + 1], rgba[i + 2]) != canvasBg) return true;
    }
    return false;
  }

  test('text wider than the cell stops at the clip, under the row transform', () async {
    final (drawer, id) = cellDrawer();
    drawer.onDrawTextStringintintint(VGCDrawTextStringintintint()
      ..string = 'a text much wider than its cell'
      ..x = 12
      ..y = 4
      ..flags = 1); // SWT.DRAW_TRANSPARENT
    final pixels = await rasterize(drawer, id);

    expect(inkedColumn(pixels, 10), isTrue, reason: 'the text inside the cell');
    for (var x = cellRight; x < canvasSize.width.toInt(); x++) {
      expect(inkedColumn(pixels, x), isFalse, reason: 'column $x is past the cell\'s edge');
    }
  });

  test('a fill wider than the cell stops at the clip, under the row transform', () async {
    final (drawer, id) = cellDrawer();
    drawer.onFillRectangleintintintint(VGCFillRectangleintintintint()
      ..x = 10
      ..y = 0
      ..width = 100
      ..height = 40);
    final pixels = await rasterize(drawer, id);

    expect(inkedColumn(pixels, cellRight - 1), isTrue, reason: 'the fill inside the cell');
    expect(inkedColumn(pixels, cellRight), isFalse, reason: 'the fill past the cell\'s edge');
  });
}
