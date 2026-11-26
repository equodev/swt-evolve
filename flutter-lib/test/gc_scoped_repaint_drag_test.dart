// A drag repaints a Canvas through scoped Paints whose damage slides by less than its own size, so
// no later damage covers an earlier one and every layer stays held. What a later damage erases of a
// held layer can never show again, so the drawer drops those ops: what a layer keeps is the band
// no later damage reaches, and a frame replays little more than one full scene.

import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';

import 'support/gc_frames.dart';

const _width = 400, _height = 300, _cell = 10;

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  Future<void> settle() async {
    for (var i = 0; i < 20; i++) {
      await Future<void>.delayed(Duration.zero);
    }
  }

  void clip(int id, Rect? r) => receive(
      'GC/$id/b',
      GcFrame()
          .change(VGC.empty()..clipping = r == null ? null : rectOf(r.left.toInt(), r.top.toInt(), r.width.toInt(), r.height.toInt()),
              {WireKey.k_GC_clipping})
          .bytes);

  void fill(int id, Rect r) => receive('GC/$id/b',
      gcOp('fillRectangleintintintint', [r.left.toInt(), r.top.toInt(), r.width.toInt(), r.height.toInt()]));

  Future<void> end(int id, {Rect? damage}) async {
    receive(
        'GC/$id/gcDispose',
        gcDisposeOf({
          'fullRepaint': true,
          if (damage != null)
            'damage': {
              'x': damage.left.toInt(),
              'y': damage.top.toInt(),
              'width': damage.width.toInt(),
              'height': damage.height.toInt()
            }
        }));
    await settle();
  }

  /// What a draw2d Paint draws inside [area]: the background, the grid cells it reaches, the figure.
  void paintScene(int id, Rect area, Rect figure) {
    fill(id, area);
    for (var y = 0; y < _height; y += _cell) {
      for (var x = 0; x < _width; x += _cell) {
        final cell = Rect.fromLTWH(x + 1.0, y + 1.0, _cell - 2.0, _cell - 2.0);
        if (cell.overlaps(area)) fill(id, cell);
      }
    }
    if (figure.overlaps(area)) fill(id, figure);
  }

  int heldOps(GCDrawer d) {
    var n = 0;
    for (final s in d.shapes) {
      n += s is RegionShape ? s.ops.length : 1;
    }
    return n;
  }

  Future<ui.Image> render(GCDrawer d) {
    final recorder = ui.PictureRecorder();
    final c = ui.Canvas(recorder);
    c.drawRect(const Rect.fromLTWH(0, 0, 400, 300), Paint()..color = Colors.white);
    drawShapeRun(c, d.shapes, background: Colors.white);
    return recorder.endRecording().toImage(_width, _height);
  }

  Future<List<int>> pixels(ui.Image image) async =>
      (await image.toByteData(format: ui.ImageByteFormat.rawRgba))!.buffer.asUint8List();

  test('a sustained drag keeps what each layer still shows, and it is what a full paint draws', () async {
    const id = 862001;
    var frame = 0;
    int? askedAt;
    final d = GCDrawer.embedded(VGC.empty()..id = id,
        onShapesUpdated: (_) {}, onFullRepaintNeeded: () => askedAt ??= frame);
    addTearDown(d.dispose);

    const everything = Rect.fromLTWH(0, 0, 400, 300);
    var figure = const Rect.fromLTWH(40, 100, 120, 80);
    paintScene(id, everything, figure);
    await end(id);
    final fullOps = heldOps(d);

    const frames = 120;
    for (; frame < frames; frame++) {
      final next = figure.shift(const Offset(2, 0));
      final damage = figure.expandToInclude(next);
      figure = next;
      clip(id, damage);
      paintScene(id, damage, figure);
      clip(id, null);
      await end(id, damage: damage);
    }
    final held = heldOps(d);
    final layers = d.shapes.whereType<RegionShape>().length;

    final reference = GCDrawer.embedded(VGC.empty()..id = id + 1, onShapesUpdated: (_) {});
    addTearDown(reference.dispose);
    paintScene(id + 1, everything, figure);
    await end(id + 1);

    final counts = '$frames frames, full scene $fullOps ops; held $held ops in $layers layers';
    expect(await pixels(await render(d)), await pixels(await render(reference)),
        reason: 'the layers held show what one full paint of the last frame does');
    expect(held, lessThan(fullOps * 2),
        reason: 'each layer keeps only the band no later damage erases ($counts)');
    expect(askedAt, isNull,
        reason: 'the layers replay less than two full scenes, so Java is not asked for one ($counts)');
  });
}
