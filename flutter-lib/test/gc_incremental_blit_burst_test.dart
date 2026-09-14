// draw2d/GEF paints through BufferedGraphicsSource, not SWT.Paint: it renders the damaged region
// into an off-screen Image and blits that Image onto the control with a GC opened outside any
// Paint dispatch, so the cycle arrives as fullRepaint:false with no damage rectangle.
//
// The buffer is sized to the damaged region alone (BufferedGraphicsSource.getGraphics intersects
// the control bounds with the damage), so each incremental cycle carries content no later cycle
// resends. Adding a figure to a diagram produces one such blit, and GEF fires several back to back
// for the surrounding feedback; if a cycle is discarded because a newer one started before its
// image decoded, that figure is gone until an unrelated full repaint redraws the whole scene.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  String svgSquare(String color) =>
      '<svg xmlns="http://www.w3.org/2000/svg" width="8" height="8">'
      '<rect width="8" height="8" fill="$color"/></svg>';

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
    for (var i = 0; i < 40; i++) {
      await Future<void>.delayed(Duration.zero);
    }
  }

  /// One BufferedGraphicsSource.flushGraphics: the damage-sized buffer blitted to its position.
  void blit(int id, String color, int x, int y) => deliver(
        'GC/$id/drawImageImageintintintintintintintint',
        {
          'image': {'svgContent': svgSquare(color)},
          'srcX': 0, 'srcY': 0, 'srcWidth': 8, 'srcHeight': 8,
          'destX': x, 'destY': y, 'destWidth': 8, 'destHeight': 8,
        },
      );

  /// controlGC.dispose() on a GC opened outside a Paint dispatch.
  void incrementalDispose(int id) =>
      deliver('GC/$id/gcDispose', {'fullRepaint': false});

  test('every incremental blit in a burst reaches the scene', () async {
    final state = VGC.empty()..id = 719901;
    final drawer = GCDrawer.embedded(state, onShapesUpdated: (_) {});
    addTearDown(drawer.dispose);

    // Four repairDamage cycles fired back to back, so each cycle's image decode is still in
    // flight when the next one starts and bumps the generation counter.
    const colors = ['#ff0000', '#00ff00', '#0000ff', '#ffff00'];
    for (var i = 0; i < colors.length; i++) {
      blit(state.id, colors[i], i * 20, 0);
      incrementalDispose(state.id);
    }
    await settle();

    expect(drawer.shapes.whereType<ImageShape>(), hasLength(colors.length),
        reason: 'an incremental cycle composites on top of the previous frame instead of '
            'replacing it, so its content is never resent -- dropping one because a newer '
            'cycle started loses that region until an unrelated full repaint redraws it');
  });

  test('an incremental blit survives a later cycle superseding it', () async {
    final state = VGC.empty()..id = 719902;
    final drawer = GCDrawer.embedded(state, onShapesUpdated: (_) {});
    addTearDown(drawer.dispose);

    // The figure the user just added, blitted alone ...
    blit(state.id, '#ff0000', 40, 40);
    incrementalDispose(state.id);
    // ... and GEF's selection feedback for it, blitted before the first decode resolves.
    blit(state.id, '#00ff00', 0, 0);
    incrementalDispose(state.id);
    await settle();

    expect(drawer.shapes.whereType<ImageShape>(), hasLength(2),
        reason: 'the added figure and the feedback cover different regions; neither replaces '
            'the other');
  });

  test('a full repaint still replaces everything an incremental burst left', () async {
    final state = VGC.empty()..id = 719903;
    final drawer = GCDrawer.embedded(state, onShapesUpdated: (_) {});
    addTearDown(drawer.dispose);

    blit(state.id, '#ff0000', 0, 0);
    incrementalDispose(state.id);
    blit(state.id, '#00ff00', 20, 0);
    incrementalDispose(state.id);
    await settle();
    expect(drawer.shapes.whereType<ImageShape>(), hasLength(2), reason: 'sanity');

    // An SWT.Paint arrives: DeferredUpdateManager paints the whole root figure onto the paint GC.
    blit(state.id, '#0000ff', 0, 0);
    deliver('GC/${state.id}/gcDispose', {'fullRepaint': true});
    await settle();

    expect(drawer.shapes.whereType<ImageShape>(), hasLength(1),
        reason: 'a full repaint repaints the whole client area, so nothing the incremental '
            'cycles left survives it');
  });
}
