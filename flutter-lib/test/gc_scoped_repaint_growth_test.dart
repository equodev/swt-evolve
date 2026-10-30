// A scoped cycle erases its rectangle before painting it, so the display list drops whatever lay
// inside it; disjoint layers are the frame and need no full repaint from Java.

import 'dart:convert';
import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/channel_header.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/tagged_values.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';
import 'support/gc_frames.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  void deliver(String actionId, Map<String, dynamic> payload) =>
      receive(actionId, actionId.endsWith('/gcDispose') ? gcDisposeOf(payload) : tagged(payload));

  Future<void> settle() async {
    for (var i = 0; i < 20; i++) {
      await Future<void>.delayed(Duration.zero);
    }
  }

  void fill(int id, int x, int y, int width, int height) {
    receive('GC/$id/b', gcOp('fillRectangleintintintint', [x, y, width, height]));
  }

  Future<void> paint(int id, {Map<String, int>? damage}) async {
    deliver('GC/$id/gcDispose', {'fullRepaint': true, if (damage != null) 'damage': damage});
    await settle();
  }

  Map<String, int> cell(int x, int y) => {'x': x, 'y': y, 'width': 20, 'height': 20};

  GCDrawer drawer(int id, void Function() onFullRepaintNeeded) {
    final d = GCDrawer.embedded(VGC.empty()..id = id,
        onShapesUpdated: (_) {}, onFullRepaintNeeded: onFullRepaintNeeded);
    addTearDown(d.dispose);
    return d;
  }

  test('disjoint scoped repaints are the frame, not a reason to ask Java for one', () async {
    const id = 840101;
    var requests = 0;
    final d = drawer(id, () => requests++);
    fill(id, 0, 0, 400, 20);
    await paint(id);
    // As many cells as the layer limit, so only their disjointness keeps the request away.
    for (var i = 0; i < 16; i++) {
      fill(id, i * 20, 0, 20, 20);
      await paint(id, damage: cell(i * 20, 0));
    }

    expect(requests, 0);
    expect(d.shapes.whereType<RegionShape>(), hasLength(16));
  });

  test('a cell repainted again and again is held once', () async {
    const id = 840102;
    final d = drawer(id, () {});
    fill(id, 0, 0, 400, 20);
    await paint(id);
    for (var i = 0; i < 50; i++) {
      fill(id, 40, 0, 20, 20);
      await paint(id, damage: cell(40, 0));
    }

    expect(d.shapes.whereType<RegionShape>(), hasLength(1));
  });

  test('what a full paint drew inside the damage goes with the scoped cycle that erases it', () async {
    const id = 840103;
    final d = drawer(id, () {});
    fill(id, 42, 2, 10, 10); // inside the cell repainted below
    fill(id, 200, 2, 10, 10); // elsewhere
    await paint(id);
    fill(id, 40, 0, 20, 20);
    await paint(id, damage: cell(40, 0));

    final outside = d.shapes.where((s) => s is! RegionShape).map((s) => s.paintedBounds).toList();
    expect(outside, [const Rect.fromLTWH(200, 2, 10, 10)],
        reason: 'the fill at 42,2 is erased by the scoped cycle; the one at 200,2 is untouched');
  });

  test('a hairline outline stays when the damage is its rectangle: its far edges lie outside',
      () async {
    const id = 840104;
    final d = drawer(id, () {});
    await paint(id);
    final outline = RectShape(const Rect.fromLTWH(40, 0, 20, 20), Colors.black, 0, 0, 0);
    d.shapes.add(outline);
    fill(id, 40, 0, 20, 20);
    await paint(id, damage: cell(40, 0));

    expect(d.shapes, contains(outline),
        reason: 'a hairline is drawn on pixel centres, so it paints column 60 and row 20');
  });

  test('dropping a shape leaves the image a surviving copy of it still draws', () async {
    const id = 840105;
    final d = drawer(id, () {});
    await paint(id);
    final recorder = ui.PictureRecorder();
    ui.Canvas(recorder).drawRect(const Rect.fromLTWH(0, 0, 10, 10), Paint()..color = Colors.red);
    final image = await recorder.endRecording().toImage(10, 10);
    const src = Rect.fromLTWH(0, 0, 10, 10);
    // copyArea reuses the original's image for the copy.
    final original = ImageShape.raster(image, src, const Rect.fromLTWH(42, 2, 10, 10));
    final copy = ImageShape.raster(image, src, const Rect.fromLTWH(200, 2, 10, 10));
    d.shapes.addAll([original, copy]);
    fill(id, 40, 0, 20, 20);
    await paint(id, damage: cell(40, 0));

    expect(d.shapes, isNot(contains(original)));
    expect(d.shapes, contains(copy));
    expect(copy.image?.debugDisposed, isFalse);
  });

  test('a damage that slides a pixel per frame never covers the last one, so the backstop bounds it',
      () async {
    const id = 840106;
    var requests = 0;
    final d = drawer(id, () => requests++);
    fill(id, 0, 0, 400, 20);
    await paint(id);
    for (var i = 0; i < 300; i++) {
      fill(id, i, 0, 20, 20);
      await paint(id, damage: cell(i, 0));
    }

    expect(d.shapes.whereType<RegionShape>(), hasLength(300),
        reason: 'each layer still shows a one-pixel band no later damage covers');
    expect(requests, 1, reason: 'asked once; Java has not answered yet');

    fill(id, 0, 0, 400, 20);
    await paint(id);
    for (var i = 0; i < 17; i++) {
      fill(id, i, 0, 20, 20);
      await paint(id, damage: cell(i, 0));
    }
    expect(requests, 2, reason: 'the full Paint that answered re-arms the backstop');
  });
}
