// A GC clipped to a Path or a Region must confine everything drawn afterwards to that shape.
//
// The clip's bounding rectangle is not enough: an application that clips to a ring of circles and
// then fills the canvas expects the fill only inside them, and a backend that keeps just the box
// (or drops the clip) paints over everything — the whole canvas comes out one flat colour.

import 'dart:convert';
import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';

const Size canvasSize = Size(100, 100);
const Color canvasBg = Color(0xFFFFFFFF);
const Color fill = Color(0xFF0000FF);

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  var nextId = 960001;

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
      await Future<void>.delayed(Duration.zero);
    }
  }

  /// A drawer whose GC state is decoded from [clip], the way a state push arrives from Java.
  (GCDrawer, int) drawerClippedTo(Map<String, dynamic> clip) {
    final id = nextId++;
    final state = VGC.fromJson({
      'id': id,
      'swt': 'GC',
      'style': 0,
      'background': {'red': 0, 'green': 0, 'blue': 255, 'alpha': 255},
      ...clip,
    });
    final drawer = GCDrawer.embedded(state, onShapesUpdated: (_) {});
    addTearDown(drawer.dispose);
    return (drawer, id);
  }

  /// Fills the whole canvas, then rasterizes what the GC committed.
  Future<Uint8List> fillCanvasAndRasterize(GCDrawer drawer, int id) async {
    drawer.onFillRectangleintintintint(VGCFillRectangleintintintint()
      ..x = 0
      ..y = 0
      ..width = 100
      ..height = 100);
    deliver('GC/$id/gcDispose', {'fullRepaint': true});
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

  Color pixelAt(Uint8List rgba, int x, int y) {
    final i = (y * canvasSize.width.toInt() + x) * 4;
    return Color.fromARGB(rgba[i + 3], rgba[i], rgba[i + 1], rgba[i + 2]);
  }

  // The upper-left triangle, as SWT's PathData: move, two lines, close. Its bounding box is the
  // whole canvas, so only the geometry can keep the fill out of the lower-right half.
  const trianglePath = {
    'clippingPath': {'types': [1, 2, 2, 5], 'points': [0.0, 0.0, 100.0, 0.0, 0.0, 100.0]},
    'clipping': {'x': 0, 'y': 0, 'width': 100, 'height': 100},
  };

  test('a fill under a path clip stops at the path', () async {
    final (drawer, id) = drawerClippedTo(trianglePath);
    final pixels = await fillCanvasAndRasterize(drawer, id);

    expect(pixelAt(pixels, 10, 10), fill, reason: 'inside the clip path');
    expect(pixelAt(pixels, 90, 90), canvasBg,
        reason: 'outside the clip path — an ignored clip paints the whole canvas');
  });

  test('a fill under a region clip stops at every rectangle of the region', () async {
    // Two vertical bars with a gap between them: the gap is what a bounding box would lose.
    final (drawer, id) = drawerClippedTo(const {
      'clippingRects': [0, 0, 20, 100, 60, 0, 20, 100],
      'clipping': {'x': 0, 'y': 0, 'width': 80, 'height': 100},
    });
    final pixels = await fillCanvasAndRasterize(drawer, id);

    expect(pixelAt(pixels, 10, 50), fill, reason: 'first rectangle of the region');
    expect(pixelAt(pixels, 70, 50), fill, reason: 'second rectangle of the region');
    expect(pixelAt(pixels, 40, 50), canvasBg,
        reason: 'the gap between them — a region is not its bounding box');
  });

  test('overlapping region rectangles stay filled, whatever the GC fill rule', () async {
    // SWT starts a GC at FILL_EVEN_ODD and RegionHelper.add can emit overlapping rectangles, so a
    // region clip that honoured the GC rule would punch a hole where two of them meet.
    final (drawer, id) = drawerClippedTo(const {
      'fillRule': 1, // SWT.FILL_EVEN_ODD
      'clippingRects': [0, 0, 60, 100, 40, 0, 60, 100],
      'clipping': {'x': 0, 'y': 0, 'width': 100, 'height': 100},
    });
    final pixels = await fillCanvasAndRasterize(drawer, id);

    expect(pixelAt(pixels, 50, 50), fill,
        reason: 'the overlap of the two rectangles — a region is their union, not their symmetric '
            'difference');
    expect(pixelAt(pixels, 10, 50), fill);
    expect(pixelAt(pixels, 90, 50), fill);
  });

  test('a clip path follows the GC fill rule', () async {
    // Two concentric squares: under even-odd, SWT's default, the inner one falls outside the clip,
    // where winding fills straight through it.
    const concentric = {
      'clippingPath': {'types': [1, 2, 2, 2, 5, 1, 2, 2, 2, 5], 'points': [
        0.0, 0.0, 100.0, 0.0, 100.0, 100.0, 0.0, 100.0, //
        30.0, 30.0, 70.0, 30.0, 70.0, 70.0, 30.0, 70.0,
      ]},
      'clipping': {'x': 0, 'y': 0, 'width': 100, 'height': 100},
    };

    final (evenOdd, idEvenOdd) = drawerClippedTo({...concentric, 'fillRule': 1});
    final evenOddPixels = await fillCanvasAndRasterize(evenOdd, idEvenOdd);
    expect(pixelAt(evenOddPixels, 10, 50), fill, reason: 'between the two squares');
    expect(pixelAt(evenOddPixels, 50, 50), canvasBg,
        reason: 'the inner square is a hole under even-odd, SWT GCData\'s initial rule');

    final (winding, idWinding) = drawerClippedTo({...concentric, 'fillRule': 2});
    final windingPixels = await fillCanvasAndRasterize(winding, idWinding);
    expect(pixelAt(windingPixels, 50, 50), fill,
        reason: 'FILL_WINDING fills straight through the inner square');

    // A GC that never had its fill rule set carries GCData's initial FILL_EVEN_ODD.
    final (unset, idUnset) = drawerClippedTo(concentric);
    expect(pixelAt(await fillCanvasAndRasterize(unset, idUnset), 50, 50), canvasBg,
        reason: 'an absent fill rule is even-odd, not winding');
  });

  test('a curved clip path keeps its curve', () async {
    // The circle inscribed in the canvas, as the four cubics SWT reports for a full arc.
    final (drawer, id) = drawerClippedTo(const {
      'clippingPath': {'types': [1, 4, 4, 4, 4, 5], 'points': [
        100.0, 50.0, //
        100.0, 22.4, 77.6, 0.0, 50.0, 0.0, //
        22.4, 0.0, 0.0, 22.4, 0.0, 50.0, //
        0.0, 77.6, 22.4, 100.0, 50.0, 100.0, //
        77.6, 100.0, 100.0, 77.6, 100.0, 50.0,
      ]},
      'clipping': {'x': 0, 'y': 0, 'width': 100, 'height': 100},
    });
    final pixels = await fillCanvasAndRasterize(drawer, id);

    expect(pixelAt(pixels, 50, 50), fill, reason: 'the centre of the circle');
    expect(pixelAt(pixels, 2, 2), canvasBg,
        reason: 'the corner the circle does not reach — the clip is not its bounding box');
  });

  test('an unclipped GC still fills the whole canvas', () async {
    final (drawer, id) = drawerClippedTo(const {});
    final pixels = await fillCanvasAndRasterize(drawer, id);

    expect(pixelAt(pixels, 25, 50), fill);
    expect(pixelAt(pixels, 75, 50), fill);
  });
}
