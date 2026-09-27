// drawImage of an ImageData-built image in a GC(Image) paint: unlike a remoteRef blit it must
// decode PNG first, so it resolves over several event-loop turns.

import 'dart:convert';
import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';
import 'package:swtflutter/src/impl/utils/image_utils.dart';

// 10x10, red at (0,0) and (9,9), black elsewhere; PNG-encoded by SWT's ImageLoader.
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

List<int> _json(Object o) => utf8.encode(jsonEncode(o));

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

    comm.receiveBinary(_frame('GC/$gcId', _json({
      'id': gcId,
      'swt': 'GC',
      'alpha': 255,
      'clippingRects': <Object>[],
      'background': {'a': 255, 'r': 0, 'g': 0, 'b': 255},
      'foreground': {'a': 255, 'r': 0, 'g': 0, 'b': 0},
    })));
    comm.receiveBinary(
        _frame('GC/$gcId/imageInit', _json({'width': 10, 'height': 10})));
    comm.receiveBinary(_frame('GC/$gcId/fillRectangleintintintint',
        _json({'x': 0, 'y': 0, 'width': 10, 'height': 10})));
    comm.receiveBinary(_frame('GC/$gcId/drawImageImageintint', _json({
      'x': 0,
      'y': 0,
      'image': {
        'id': 4321,
        'swt': 'Image',
        'width': 10,
        'height': 10,
        'imageData': {
          'width': 10,
          'height': 10,
          'depth': 24,
          'data': _redCornersPng,
        },
      },
    })));
    comm.receiveBinary(_frame('GC/$gcId/gcDispose',
        _json({'ref': ref, 'pixels': false, 'retain': false})));

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
