// A Paint scoped to a rectangle must land on screen exactly as a full repaint of the same state
// would, and must leave every pixel outside that rectangle alone.
//
// That pixel identity is what makes honouring the damage rectangle safe, and it rests on the
// erase: a 1px border drawn in both the retained frame and the scoped one composites darker
// without it.

import 'dart:convert';
import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';

const Size canvasSize = Size(200, 80);
const Color canvasBg = Color(0xFFFFFFFF);
// The full bounds of the right-hand cell, which is what a grid invalidates to repaint it — one
// pixel wider and taller than the interior its painter fills, the border being the grid's.
const Rect cellBounds = Rect.fromLTWH(100, 0, 100, 40);

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

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

  /// The ops a grid emits for [hover]: border lines first, then the cell interiors at w-1 x h-1.
  void paintGrid(int id, {required bool hover}) {
    for (final line in const [
      [99, 0, 99, 40],
      [199, 0, 199, 40],
      [0, 39, 200, 39],
    ]) {
      deliver('GC/$id/drawLineintintintint',
          {'x1': line[0], 'y1': line[1], 'x2': line[2], 'y2': line[3]});
    }
    deliver('GC/$id/setBackgroundColor', {'color': {'red': 221, 'green': 221, 'blue': 221}});
    deliver('GC/$id/fillRectangleintintintint',
        {'x': 0, 'y': 0, 'width': 99, 'height': 39});
    if (hover) {
      deliver('GC/$id/setBackgroundColor', {'color': {'red': 51, 'green': 153, 'blue': 255}});
    }
    deliver('GC/$id/fillRectangleintintintint',
        {'x': 100, 'y': 0, 'width': 99, 'height': 39});
  }

  Future<Uint8List> rasterize(List<Shape> shapes) async {
    final recorder = ui.PictureRecorder();
    ScenePainter(canvasBg, shapes)
        .paint(Canvas(recorder), canvasSize);
    final image = await recorder
        .endRecording()
        .toImage(canvasSize.width.toInt(), canvasSize.height.toInt());
    final bytes = await image.toByteData(format: ui.ImageByteFormat.rawRgba);
    image.dispose();
    return bytes!.buffer.asUint8List();
  }

  int differingPixels(Uint8List a, Uint8List b) {
    var count = 0;
    for (var i = 0; i < a.length; i += 4) {
      if (a[i] != b[i] || a[i + 1] != b[i + 1] || a[i + 2] != b[i + 2]) count++;
    }
    return count;
  }

  /// The shapes a canvas holds after [cycles] paints, the last of them scoped to [damage].
  Future<List<Shape>> paintCycles(int id, List<bool> hoverPerCycle, {Rect? damage}) async {
    final state = VGC.empty()..id = id;
    final drawer = GCDrawer.embedded(state, onShapesUpdated: (_) {});
    addTearDown(drawer.dispose);
    for (var i = 0; i < hoverPerCycle.length; i++) {
      final last = i == hoverPerCycle.length - 1;
      paintGrid(id, hover: hoverPerCycle[i]);
      deliver('GC/$id/gcDispose', {
        'fullRepaint': true,
        if (last && damage != null)
          'damage': {
            'x': damage.left.toInt(),
            'y': damage.top.toInt(),
            'width': damage.width.toInt(),
            'height': damage.height.toInt(),
          },
      });
      await settle();
    }
    return List<Shape>.from(drawer.shapes);
  }

  test('a scoped repaint renders exactly what a full repaint of the same state would', () async {
    final reference = await rasterize(await paintCycles(830001, [true]));
    final scoped = await rasterize(
        await paintCycles(830002, [false, true], damage: cellBounds));

    expect(differingPixels(reference, scoped), 0,
        reason: 'compositing the hover as a layer over the previous frame must be '
            'indistinguishable from repainting the whole canvas with the hover on');
  });

  test('a scoped repaint leaves the pixels outside its rectangle untouched', () async {
    final before = await rasterize(await paintCycles(830003, [false]));
    final after = await rasterize(
        await paintCycles(830004, [false, true], damage: cellBounds));

    final width = canvasSize.width.toInt();
    for (var i = 0; i < before.length; i += 4) {
      final pixel = i ~/ 4;
      final point = Offset((pixel % width).toDouble(), (pixel ~/ width).toDouble());
      if (cellBounds.contains(point)) continue;
      expect(after.sublist(i, i + 3), before.sublist(i, i + 3),
          reason: 'pixel at $point is outside the damaged rectangle');
    }
  });

  test('a scoped repaint keeps the previous frame instead of replacing it', () async {
    final shapes = await paintCycles(830005, [false, true], damage: cellBounds);

    expect(shapes.whereType<RegionShape>(), hasLength(1),
        reason: 'the scoped cycle becomes one layer');
    expect(shapes.length, greaterThan(1),
        reason: 'the frame it composites over has to still be there');
    expect(shapes.whereType<RegionShape>().single.rect, cellBounds);
  });

  test('a layer supersedes one it covers, so a hover sweep does not stack them', () async {
    final state = VGC.empty()..id = 830006;
    final drawer = GCDrawer.embedded(state, onShapesUpdated: (_) {});
    addTearDown(drawer.dispose);

    paintGrid(state.id, hover: false);
    deliver('GC/${state.id}/gcDispose', {'fullRepaint': true});
    await settle();

    // Hovering the cell, then leaving it, repaints the same bounds twice.
    for (var i = 0; i < 4; i++) {
      paintGrid(state.id, hover: i.isEven);
      deliver('GC/${state.id}/gcDispose', {
        'fullRepaint': true,
        'damage': {'x': 100, 'y': 0, 'width': 100, 'height': 40},
      });
      await settle();
    }

    expect(drawer.shapes.whereType<RegionShape>(), hasLength(1),
        reason: 'four repaints of the same rectangle leave one layer, not four');
  });

  test('a full repaint after a scoped one drops the layer', () async {
    final state = VGC.empty()..id = 830007;
    final drawer = GCDrawer.embedded(state, onShapesUpdated: (_) {});
    addTearDown(drawer.dispose);

    paintGrid(state.id, hover: false);
    deliver('GC/${state.id}/gcDispose', {'fullRepaint': true});
    await settle();
    paintGrid(state.id, hover: true);
    deliver('GC/${state.id}/gcDispose', {
      'fullRepaint': true,
      'damage': {'x': 100, 'y': 0, 'width': 100, 'height': 40},
    });
    await settle();
    expect(drawer.shapes.whereType<RegionShape>(), hasLength(1), reason: 'sanity');

    paintGrid(state.id, hover: true);
    deliver('GC/${state.id}/gcDispose', {'fullRepaint': true});
    await settle();

    expect(drawer.shapes.whereType<RegionShape>(), isEmpty,
        reason: 'a full-area Paint repaints everything, so no layer survives it');
  });
}
