// A control repainted through a GC opened outside a Paint event — JFace's LineNumberRulerColumn
// blits its whole gutter on every viewport change — must not make the display list grow without
// end. The retained frame exists so a partial repaint does not blank the rest; a cycle that
// repaints the same ground is replacing it, not adding to it.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';
import 'package:swtflutter/src/impl/utils/image_utils.dart';

const gutter = Rect.fromLTWH(0, 0, 40, 400);

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

  test('a gutter reblitted outside a Paint does not grow the display list', () async {
    const id = 918273;
    final drawer = GCDrawer.embedded(VGC()
      ..swt = 'GC'
      ..id = id);

    // What LineNumberRulerColumn.redraw() emits: one blit of the buffer it just rendered, covering
    // the whole gutter. The ref names a picture this side rendered, so it is known opaque.
    Future<void> gutterTick(int ref) async {
      final rendered = await createTestImage(
          width: gutter.width.toInt(), height: gutter.height.toInt());
      ImageUtils.registerRemoteImage(ref, rendered);
      deliver('GC/$id/drawImageImageintint', {
        'image': {
          'remoteRef': ref,
          'width': gutter.width.toInt(),
          'height': gutter.height.toInt(),
        },
        'x': 0,
        'y': 0,
      });
      // fullRepaint false: the GC was opened outside a Paint dispatch. No damage rectangle,
      // because nothing scoped it.
      deliver('GC/$id/gcDispose', {'fullRepaint': false});
      await settle();
    }

    await gutterTick(7001);
    final afterFirst = drawer.shapes.length;
    expect(afterFirst, 1, reason: 'one blit, one shape');

    for (var tick = 0; tick < 20; tick++) {
      await gutterTick(7002 + tick);
    }

    expect(drawer.shapes.length, afterFirst,
        reason: 'twenty more repaints of the same ground left ${drawer.shapes.length} shapes where '
            'the first cycle left $afterFirst');
  });

  test('a cycle that covers only part of the control keeps what it does not cover', () async {
    const id = 918274;
    final drawer = GCDrawer.embedded(VGC()
      ..swt = 'GC'
      ..id = id);

    // A full-width band across the top, then a later cycle repainting only the top half of it.
    deliver('GC/$id/setBackgroundColor', {'color': {'red': 0, 'green': 0, 'blue': 255}});
    deliver('GC/$id/fillRectangleintintintint', {'x': 0, 'y': 0, 'width': 40, 'height': 400});
    deliver('GC/$id/gcDispose', {'fullRepaint': false});
    await settle();
    expect(drawer.shapes.length, 1);

    deliver('GC/$id/setBackgroundColor', {'color': {'red': 255, 'green': 0, 'blue': 0}});
    deliver('GC/$id/fillRectangleintintintint', {'x': 0, 'y': 0, 'width': 40, 'height': 200});
    deliver('GC/$id/gcDispose', {'fullRepaint': false});
    await settle();

    expect(drawer.shapes.length, 2,
        reason: 'the first fill is still visible below y=200 and must not be dropped');
  });

  // Only a shape that definitely paints every pixel of a known rectangle may claim to hide what is
  // under it. Everything else keeps what it is drawn over, which costs retention and never
  // correctness.
  group('what counts as covering', () {
    test('an opaque filled rectangle covers its own rectangle', () {
      final fill = RectShape(gutter, const Color(0xFF0000FF), 0, 0, 0, isFilled: true);
      expect(fill.opaqueCoverage, gutter);
    });

    test('a translucent fill covers nothing, however large', () {
      final fill = RectShape(gutter, const Color(0x800000FF), 0, 0, 0, isFilled: true);
      expect(fill.opaqueCoverage, isNull);
    });

    test('an outlined rectangle covers nothing — it paints only its edge', () {
      final outline = RectShape(gutter, const Color(0xFF0000FF), 1, 0, 0, isFilled: false);
      expect(outline.opaqueCoverage, isNull);
    });

    test('a fill clipped smaller than itself covers nothing', () {
      final clipped = RectShape(gutter, const Color(0xFF0000FF), 0, 0, 0,
          isFilled: true, clipRect: const Rect.fromLTWH(0, 0, 40, 10));
      expect(clipped.opaqueCoverage, isNull);
    });

    test('an application bitmap covers nothing — its alpha is unknown here', () async {
      final bitmap = await createTestImage(width: 40, height: 400);
      final blit = ImageShape.raster(bitmap, gutter, gutter);
      expect(blit.opaqueCoverage, isNull);
      expect(blit.paintedBounds, gutter,
          reason: 'it can still be dropped by something that does cover it');
    });

    test('a picture this side rendered covers its destination', () async {
      final rendered = await createTestImage(width: 40, height: 400);
      final blit = ImageShape.raster(rendered, gutter, gutter, opaqueSource: true);
      expect(blit.opaqueCoverage, gutter);
    });

    test('a rendered picture drawn faded covers nothing', () async {
      final rendered = await createTestImage(width: 40, height: 400);
      final blit =
          ImageShape.raster(rendered, gutter, gutter, opaqueSource: true, alpha: 128);
      expect(blit.opaqueCoverage, isNull);
    });
  });
}
