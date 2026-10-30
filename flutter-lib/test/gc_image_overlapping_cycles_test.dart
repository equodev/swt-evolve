// With two render cycles on one Image in flight at once, the drawer must still register a render
// for every remoteRef it is given: an unregistered ref blits nothing.

import 'dart:convert';
import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';
import 'package:swtflutter/src/impl/utils/image_utils.dart';

import 'support/gc_frames.dart';

// 10x10, red at (0,0) and (9,9): decoding it makes its cycle span several turns, so a later one can overtake it.
const _redCornersPng =
    'iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAIAAAACUFjqAAAAEUlEQVR4nGP4zzAKSAPAEAMAOA0B/w2tcokAAAAASUVORK5CYII=';

Uint8List _frame(String actionId, List<int> body) {
  final action = utf8.encode(actionId);
  final out = Uint8List(2 + action.length + body.length);
  out[0] = (action.length >> 8) & 0xFF;
  out[1] = action.length & 0xFF;
  out.setRange(2, 2 + action.length, action);
  out.setRange(2 + action.length, out.length, body);
  return out;
}


Future<void> _settle() async {
  for (var i = 0; i < 200; i++) {
    await Future<void>.delayed(Duration.zero);
  }
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('every ref a paint cycle is given gets a render, even when cycles overlap', () async {
    const gcId = 5501;
    const firstRef = 5510;
    const secondRef = 5511;
    final comm = EquoCommService.commForTesting;

    GCDrawer.standalone(VGC()
      ..swt = 'GC'
      ..id = gcId);

    comm.receiveBinary(_frame('GC/$gcId/b', gcState(VGC.empty()
      ..alpha = 255
      ..clippingRects = []
      ..background = colourOf(0, 0, 255))));
    comm.receiveBinary(_frame(
        'GC/$gcId/imageInit',
        imageInit(VImage()
          ..width = 10
          ..height = 10)));

    // Cycle one: carries a bitmap, so its commit waits on a decode.
    comm.receiveBinary(_frame('GC/$gcId/b', gcOp('fillRectangleintintintint', [0, 0, 10, 10])));
    final bitmap = VImage()
      ..id = 4321
      ..width = 10
      ..height = 10
      ..imageData = (VImageData()
        ..width = 10
        ..height = 10
        ..depth = 24
        ..data = base64Decode(_redCornersPng));
    describeImage(bitmap);
    comm.receiveBinary(_frame('GC/$gcId/b', gcOp('drawImageImageintint', [bitmap, 0, 0])));
    comm.receiveBinary(_frame('GC/$gcId/gcDispose',
        tagged({'ref': firstRef, 'pixels': false, 'retain': true})));

    // Cycle two starts before the first has committed — a second scroll step.
    comm.receiveBinary(_frame('GC/$gcId/b', gcOp('fillRectangleintintintint', [0, 0, 10, 10])));
    comm.receiveBinary(_frame('GC/$gcId/gcDispose',
        tagged({'ref': secondRef, 'pixels': false, 'retain': true})));

    await _settle();

    final second = await ImageUtils.decodeVImageToUIImage(VImage()..remoteRef = secondRef);
    expect(second, isNotNull, reason: 'the newest cycle produced no render at all');

    final first = await ImageUtils.decodeVImageToUIImage(VImage()..remoteRef = firstRef);
    expect(first, isNotNull,
        reason: 'the overtaken cycle was dropped, so the ref Java minted for it resolves to '
            'nothing — the canvas blits a blank and the gutter keeps its old numbers');
  });
}
