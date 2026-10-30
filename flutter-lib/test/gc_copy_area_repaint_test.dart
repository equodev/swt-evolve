// A control's GC copies what the control shows. Inside a Paint that is what the Paint erased its
// area to plus what it drew since, not the frame before it: a canvas painted twice copies the same
// pixels both times.

import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';

import 'support/gc_frames.dart';

const _bg = Color(0xFFFFFFFF);
const _width = 240;
const _height = 120;

void main() {
  late GCDrawer drawer;
  late ValueNotifier<List<Shape>> shown;
  final boundary = GlobalKey();

  Future<void> mount(WidgetTester tester, int id) async {
    shown = ValueNotifier(const []);
    drawer = GCDrawer.embedded(VGC.empty()..id = id, onShapesUpdated: (s) => shown.value = List.of(s));
    addTearDown(drawer.dispose);
    await tester.pumpWidget(Directionality(
      textDirection: TextDirection.ltr,
      child: Align(
        alignment: Alignment.topLeft,
        child: RepaintBoundary(
          key: boundary,
          child: ValueListenableBuilder<List<Shape>>(
            valueListenable: shown,
            builder: (context, shapes, _) => CustomPaint(
              size: Size(_width.toDouble(), _height.toDouble()),
              painter: ScenePainter(_bg, shapes),
            ),
          ),
        ),
      ),
    ));
    drawer.widgetContext = tester.element(find.byType(CustomPaint).first);
  }

  /// A line and an oval at the left, copied to the right: GCBasicSnippet's paint.
  Future<void> paint(WidgetTester tester, int id, {Map<String, int>? damage, bool inPaint = true}) async {
    receive('GC/$id/b', gcOp('drawLineintintintint', [10, 10, 110, 90]));
    receive('GC/$id/b', gcOp('drawOvalintintintint', [20, 20, 80, 60]));
    receive('GC/$id/b', gcOp('copyAreaintintintintintintboolean', [10, 10, 100, 80, 130, 10, false]));
    receive('GC/$id/gcDispose', tagged({'fullRepaint': inPaint, if (damage != null) 'damage': damage}));
    for (var i = 0; i < 20; i++) {
      await tester.pump();
    }
  }

  Future<Uint8List> pixels(WidgetTester tester) async {
    final box = boundary.currentContext!.findRenderObject()! as RenderRepaintBoundary;
    return (await tester.runAsync(() async {
      final image = await box.toImage();
      final data = await image.toByteData(format: ui.ImageByteFormat.rawRgba);
      image.dispose();
      return data!.buffer.asUint8List();
    }))!;
  }

  /// Pixels of the area at (10, 10) that differ from the same pixel of the copy at (130, 10).
  int differences(Uint8List rgba) {
    var n = 0;
    for (var y = 10; y < 90; y++) {
      for (var x = 10; x < 110; x++) {
        final a = (y * _width + x) * 4, b = (y * _width + x + 120) * 4;
        for (var k = 0; k < 4; k++) {
          if (rgba[a + k] != rgba[b + k]) {
            n++;
            break;
          }
        }
      }
    }
    return n;
  }

  /// Pixels of the area at (10, 10) that are not the background: what there is to copy.
  int drawn(Uint8List rgba) {
    var n = 0;
    for (var y = 10; y < 90; y++) {
      for (var x = 10; x < 110; x++) {
        if (rgba[(y * _width + x) * 4] != 0xFF) n++;
      }
    }
    return n;
  }

  testWidgets('a full Paint copies what it drew, once', (tester) async {
    const id = 851001;
    await mount(tester, id);
    await paint(tester, id);
    final first = await pixels(tester);
    expect(drawn(first), greaterThan(100));
    expect(differences(first), 0, reason: 'the first paint');

    await paint(tester, id);
    expect(differences(await pixels(tester)), 0, reason: 'a repaint copies what it drew, not that over the frame before');
  });

  testWidgets('a scoped Paint copies what it drew inside its damage, once', (tester) async {
    const id = 851002;
    await mount(tester, id);
    await paint(tester, id);
    await paint(tester, id, damage: {'x': 0, 'y': 0, 'width': _width, 'height': _height});
    expect(differences(await pixels(tester)), 0);
  });

  testWidgets('a GC outside a Paint copies what the control shows under what it drew', (tester) async {
    const id = 851003;
    await mount(tester, id);
    // Shown: a line and an oval at the left, and their copy at the right.
    await paint(tester, id);
    final before = await pixels(tester);
    // The same shapes again over what is shown, then copied over the copy: still the same pixels
    // at both places, now darker at the edges where two strokes overlap.
    await paint(tester, id, inPaint: false);
    final after = await pixels(tester);
    expect(differences(after), 0);
    expect(drawn(after), greaterThanOrEqualTo(drawn(before)));
  });
}
