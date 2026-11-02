// drawImage of an ImageData-built image in a GC(Image) paint: unlike a remoteRef blit it must
// decode PNG first, so it resolves over several event-loop turns.

import 'dart:convert';
import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/channel_header.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';
import 'package:swtflutter/src/impl/utils/image_utils.dart';

import 'support/gc_frames.dart';
import 'support/channels.dart' as channels;

// 10x10, red at (0,0) and (9,9), black elsewhere; PNG-encoded by SWT's ImageLoader.
const _redCornersPng =
    'iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAIAAAACUFjqAAAAEUlEQVR4nGP4zzAKSAPAEAMAOA0B/w2tcokAAAAASUVORK5CYII=';

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

  test('an image GC blits an application bitmap into the render it hands back', () async {
    const gcId = 9901;
    const ref = 9910;
    final comm = EquoCommService.commForTesting;

    GCDrawer.standalone(VGC()
      ..swt = 'GC'
      ..id = gcId);

    comm.receiveBinary(_frame('GC/$gcId/b', gcState(VGC.empty()
      ..alpha = 255
      ..clippingRects = []
      ..background = colourOf(0, 0, 255)
      ..foreground = colourOf(0, 0, 0))));
    comm.receiveBinary(_frame(
        'GC/$gcId/imageInit',
        imageInit(VImage()
          ..width = 10
          ..height = 10)));
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
    comm.receiveBinary(_frame('GC/$gcId/b', gcOp('fillRectangleintintintint', [0, 0, 10, 10])));
    comm.receiveBinary(_frame('GC/$gcId/b', gcOp('drawImageImageintint', [bitmap, 0, 0])));
    comm.receiveBinary(_frame('GC/$gcId/gcDispose',
        gcDisposeOf({'ref': ref, 'pixels': false, 'retain': false})));

    await _settle();

    final rendered = await ImageUtils.decodeVImageToUIImage(
        VImage()..remoteRef = ref);
    expect(rendered, isNotNull,
        reason: 'the cycle produced no render at all');

    expect(await _pixelAt(rendered!, 0, 0), const ui.Color(0xFFFF0000),
        reason: 'the blit never reached the render: (0,0) is still the fill');
    expect(await _pixelAt(rendered, 9, 9), const ui.Color(0xFFFF0000),
        reason: 'the far corner of the blit was lost');
    expect(await _pixelAt(rendered, 5, 5), const ui.Color(0xFF000000),
        reason: 'the bitmap covers the fill, so its black interior wins');
  });
}
