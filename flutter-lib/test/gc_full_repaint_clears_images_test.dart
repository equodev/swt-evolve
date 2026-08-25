// A full repaint must replace everything the previous cycle committed, images included.
//
// An application that paints its content as GC images inside a real SWT.Paint (NatTable's sort and
// checkbox icons) restages exactly the images the new layout needs, so carrying the previous
// cycle's ImageShapes through leaves icons painted at positions nothing draws any more.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  const svgIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="8" height="8">'
      '<rect width="8" height="8" fill="#000000"/></svg>';

  /// Deliver one framed message, exactly as the wire transport would:
  /// [2-byte actionId length BE][actionId][body].
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

  /// Handlers dispatch on a microtask and a gcDispose cycle awaits its pending image decodes.
  Future<void> settle() async {
    for (var i = 0; i < 20; i++) {
      await Future<void>.delayed(Duration.zero);
    }
  }

  void drawIcon(int id, int x) => deliver(
        'GC/$id/drawImageImageintintintintintintintint',
        {
          'image': {'svgContent': svgIcon},
          'srcX': 0, 'srcY': 0, 'srcWidth': 8, 'srcHeight': 8,
          'destX': x, 'destY': 0, 'destWidth': 8, 'destHeight': 8,
        },
      );

  test('a full repaint drops the images the previous cycle committed', () async {
    final state = VGC.empty()..id = 719701;
    final drawer = GCDrawer.embedded(state, onShapesUpdated: (_) {});
    addTearDown(drawer.dispose);

    // Paint cycle 1: the grid draws a checkbox icon at the column's current position.
    drawIcon(state.id, 100);
    deliver('GC/${state.id}/gcDispose', {'fullRepaint': true});
    await settle();
    expect(drawer.shapes.whereType<ImageShape>(), hasLength(1),
        reason: 'sanity: the first paint cycle commits the icon it drew');

    // Paint cycle 2: the column moved away, so this cycle draws the icon somewhere else.
    drawIcon(state.id, 40);
    deliver('GC/${state.id}/gcDispose', {'fullRepaint': true});
    await settle();

    expect(drawer.shapes.whereType<ImageShape>(), hasLength(1),
        reason: 'a full repaint replaces the canvas; an icon the new cycle drew elsewhere must '
            'not leave a copy behind at the old position');
    expect(drawer.shapes.whereType<ImageShape>().single.destRect.left, 40);
  });

  test('a full repaint that draws nothing leaves nothing painted', () async {
    final state = VGC.empty()..id = 719702;
    final drawer = GCDrawer.embedded(state, onShapesUpdated: (_) {});
    addTearDown(drawer.dispose);

    drawIcon(state.id, 100);
    deliver('GC/${state.id}/gcDispose', {'fullRepaint': true});
    await settle();
    expect(drawer.shapes, isNotEmpty);

    // The grid was filtered empty / the column group collapsed: nothing is drawn this cycle.
    deliver('GC/${state.id}/gcDispose', {'fullRepaint': true});
    await settle();

    expect(drawer.shapes, isEmpty,
        reason: 'nothing the application still draws means nothing still painted');
  });

  test('a feedback-only draw still composites on top of what is already painted', () async {
    final state = VGC.empty()..id = 719703;
    final drawer = GCDrawer.embedded(state, onShapesUpdated: (_) {});
    addTearDown(drawer.dispose);

    drawIcon(state.id, 100);
    deliver('GC/${state.id}/gcDispose', {'fullRepaint': true});
    await settle();

    // A GC opened outside any Paint dispatch — draw2d/GEF drives its own painting from the
    // LightweightSystem — adds to the canvas instead of wiping it.
    drawIcon(state.id, 40);
    deliver('GC/${state.id}/gcDispose', {'fullRepaint': false});
    await settle();

    expect(drawer.shapes.whereType<ImageShape>(), hasLength(2),
        reason: 'a feedback draw composites; it must not clear what the last full repaint left');
  });
}
