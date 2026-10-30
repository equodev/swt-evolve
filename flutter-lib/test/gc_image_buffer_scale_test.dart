// A GC on an Image held at the raster scale draws in the Image's own units; the render scales.

import 'dart:convert';
import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/channel_header.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';
import 'package:swtflutter/src/impl/utils/image_utils.dart';

import 'support/gc_frames.dart';
import 'support/channels.dart' as channels;

Uint8List _frame(String actionId, List<int> body) {
  final out = channels.frame(actionId, Uint8List.fromList(body));
  return out;
}


Future<void> _settle() async {
  for (var i = 0; i < 120; i++) {
    await Future<void>.delayed(Duration.zero);
  }
}

Future<ui.Color> _pixelAt(ui.Image image, int x, int y) async {
  final data = await image.toByteData(format: ui.ImageByteFormat.rawRgba);
  final bytes = data!.buffer.asUint8List();
  final i = (y * image.width + x) * 4;
  return ui.Color.fromARGB(bytes[i + 3], bytes[i], bytes[i + 1], bytes[i + 2]);
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('ops on an Image whose buffer is denser than its size land in the size the application gave it',
      () async {
    const gcId = 9921;
    const ref = 9930;
    final comm = EquoCommService.commForTesting;

    GCDrawer.standalone(VGC()
      ..swt = 'GC'
      ..id = gcId);

    // A 10x10 Image held at twice its size: 20x20 pixels.
    comm.receiveBinary(_frame('GC/$gcId/b', gcState(VGC.empty()
      ..alpha = 255
      ..bufferScale = 2.0
      ..clippingRects = []
      ..background = colourOf(255, 0, 0)
      ..foreground = colourOf(0, 0, 0))));
    comm.receiveBinary(_frame(
        'GC/$gcId/imageInit',
        imageInit(VImage()
          ..width = 20
          ..height = 20)));
    comm.receiveBinary(_frame('GC/$gcId/b', gcOp('fillRectangleintintintint', [0, 0, 5, 5])));
    comm.receiveBinary(_frame('GC/$gcId/gcDispose',
        gcDisposeOf({'ref': ref, 'pixels': false, 'retain': false})));

    await _settle();

    final rendered = await ImageUtils.decodeVImageToUIImage(VImage()..remoteRef = ref);
    expect(rendered, isNotNull, reason: 'the cycle produced no render at all');
    expect(rendered!.width, 20);
    expect(await _pixelAt(rendered, 9, 9), const ui.Color(0xFFFF0000),
        reason: 'a 5x5 fill covers 10x10 pixels of a buffer at twice the size');
    expect(await _pixelAt(rendered, 11, 11), const ui.Color(0xFFFFFFFF),
        reason: 'and no more');
  });
}
