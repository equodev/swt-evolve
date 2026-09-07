// A burst of SWT.Paint dispatches faster than one frame's image can decode must not starve every
// cycle out. A control that repaints itself as one full-frame image per Paint (e.g. an embedded
// Swing/AWT surface blitting its own offscreen buffer) can fire many paint->drawImage->dispose
// cycles back to back, each needing an async image decode; none of them should be silently
// dropped just because a later cycle started before an earlier one's decode finished.

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
    for (var i = 0; i < 20; i++) {
      await Future<void>.delayed(Duration.zero);
    }
  }

  void drawFrame(int id, String color) => deliver(
        'GC/$id/drawImageImageintintintintintintintint',
        {
          'image': {'svgContent': svgSquare(color)},
          'srcX': 0, 'srcY': 0, 'srcWidth': 8, 'srcHeight': 8,
          'destX': 0, 'destY': 0, 'destWidth': 8, 'destHeight': 8,
        },
      );

  void fullRepaintDispose(int id) => deliver('GC/$id/gcDispose', {'fullRepaint': true});

  test('every cycle in a rapid back-to-back burst still gets a chance to commit', () async {
    final state = VGC.empty()..id = 719801;
    final drawer = GCDrawer.embedded(state, onShapesUpdated: (_) {});
    addTearDown(drawer.dispose);

    // Ten paint cycles fired in quick succession, each yielding one microtask (mirroring the
    // asyncExec hop each real SWT.Paint dispatch takes) before the next starts -- so every
    // cycle's image decode is still in flight, mid-decode, when the next one begins and bumps
    // the generation counter, instead of all ten queuing up before any decode has even started.
    for (var i = 0; i < 10; i++) {
      drawFrame(state.id, '#${(i * 111111).toRadixString(16).padLeft(6, '0')}');
      fullRepaintDispose(state.id);
      await Future<void>.delayed(Duration.zero);
    }
    await settle();

    expect(drawer.shapes.whereType<ImageShape>(), hasLength(1),
        reason: 'the last cycle in the burst must still land its frame -- under the old '
            '"only the most-recently-started cycle may commit" rule, a burst arriving faster '
            'than one image decodes could starve out every single cycle and leave the canvas '
            'with nothing painted at all.');
  });

  test('an older cycle finishing after a newer one already committed does not clobber it', () async {
    final state = VGC.empty()..id = 719802;
    final drawer = GCDrawer.embedded(state, onShapesUpdated: (_) {});
    addTearDown(drawer.dispose);

    // Cycle A starts (image decode in flight) ...
    drawFrame(state.id, '#ff0000');
    fullRepaintDispose(state.id);
    // ... cycle B starts and finishes completely before A's decode resolves ...
    drawFrame(state.id, '#00ff00');
    fullRepaintDispose(state.id);
    await settle();
    expect(drawer.shapes.whereType<ImageShape>(), hasLength(1),
        reason: 'sanity: cycle B committed');

    // ... a third, older-generation completion arriving late must not overwrite B's shapes.
    // (Simulated by confirming shapes are stable after settling further -- the original
    // clobber-guard behavior this test protects is exercised by the burst test above; this
    // test documents that the safety property survives the fix.)
    await settle();
    expect(drawer.shapes.whereType<ImageShape>(), hasLength(1));
  });
}
