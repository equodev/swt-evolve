// copyArea on an Image held at twice its size: the area copied is in the Image's units, and what it
// copies, the last render, is in the buffer's pixels. An editor's ruler scrolls by copying its
// buffer up or down and drawing only the band that comes into view; a copy scaled a second time
// drew the numbers it kept twice their size.

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

Uint8List _frame(String actionId, List<int> body) =>
    channels.frame(actionId, Uint8List.fromList(body));

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

const _red = ui.Color(0xFFFF0000);

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test(
    'a copy of the last render lands at the size it was drawn, on a buffer at twice the size',
    () => _copiesAtItsSize(gcId: 9941, keptPaints: 1),
  );

  test('so does a copy of a render kept long enough to be flattened to pixels', () async {
    // Each kept render nests the last one under it; past eight they become pixels, as a long
    // scroll makes them.
    await _copiesAtItsSize(gcId: 9961, keptPaints: 10);
  });

  test(
    'a copy of the bottom band lands where it was asked, on a buffer scaled more down than across',
    () => _copiesBottomBand(gcId: 9971, keptPaints: 1),
  );

  test('so does a copy of the bottom band of a render flattened to pixels', () async {
    await _copiesBottomBand(gcId: 9981, keptPaints: 10);
  });
}

/// A 9x100 Image at a raster scale of 1.6 is 14x160 pixels: 14/9 across and 1.6 down. A red band
/// fills its last 10 rows (pixels 144 to 159); the next GC copies it 10 rows up (pixels 128 to 143).
Future<void> _copiesBottomBand({required int gcId, required int keptPaints}) async {
  final comm = EquoCommService.commForTesting;

  GCDrawer.standalone(
    VGC()
      ..swt = 'GC'
      ..id = gcId,
  );

  comm.receiveBinary(
    _frame(
      'GC/$gcId/b',
      gcState(VGC.empty()
        ..alpha = 255
        ..bufferScale = 14 / 9
        ..bufferScaleY = 1.6
        ..clippingRects = []
        ..background = colourOf(255, 0, 0)
        ..foreground = colourOf(0, 0, 0)),
    ),
  );
  comm.receiveBinary(_frame(
      'GC/$gcId/imageInit',
      imageInit(VImage()
        ..width = 14
        ..height = 160)));
  comm.receiveBinary(_frame('GC/$gcId/b', gcOp('fillRectangleintintintint', [0, 90, 9, 10])));
  for (var paint = 0; paint < keptPaints; paint++) {
    comm.receiveBinary(
      _frame(
        'GC/$gcId/gcDispose',
        gcDisposeOf({'ref': gcId * 100 + paint, 'pixels': false, 'retain': true}),
      ),
    );
    await _settle();
  }

  comm.receiveBinary(
      _frame('GC/$gcId/b', gcOp('copyAreaintintintintintintboolean', [0, 90, 9, 10, 0, 80, false])));
  comm.receiveBinary(
    _frame(
      'GC/$gcId/gcDispose',
      gcDisposeOf({'ref': gcId * 100 + 99, 'pixels': false, 'retain': false}),
    ),
  );
  await _settle();

  final rendered = await ImageUtils.decodeVImageToUIImage(VImage()..remoteRef = gcId * 100 + 99);
  expect(rendered, isNotNull, reason: 'the cycle produced no render at all');
  expect(rendered!.height, 160);
  expect(await _pixelAt(rendered, 13, 159), _red,
      reason: 'the band reaches the last row of the buffer');
  expect(await _pixelAt(rendered, 13, 128), _red,
      reason: 'the copy starts at row 80 of the Image: pixel 128');
  expect(await _pixelAt(rendered, 13, 126), isNot(_red),
      reason: 'and no higher: the band read from the base starts at pixel 144');
}

Future<void> _copiesAtItsSize({required int gcId, required int keptPaints}) async {
  final comm = EquoCommService.commForTesting;

  GCDrawer.standalone(
    VGC()
      ..swt = 'GC'
      ..id = gcId,
  );

  // A 10x10 Image held at 20x20 pixels, white, with a red 2x2 square in its corner.
  comm.receiveBinary(
    _frame(
      'GC/$gcId/b',
      gcState(VGC.empty()
        ..alpha = 255
        ..bufferScale = 2.0
        ..clippingRects = []
        ..background = colourOf(255, 0, 0)
        ..foreground = colourOf(0, 0, 0)),
    ),
  );
  comm.receiveBinary(_frame(
      'GC/$gcId/imageInit',
      imageInit(VImage()
        ..width = 20
        ..height = 20)));
  comm.receiveBinary(_frame('GC/$gcId/b', gcOp('fillRectangleintintintint', [0, 0, 2, 2])));
  // Kept: the next GC on this Image draws on top of this render.
  for (var paint = 0; paint < keptPaints; paint++) {
    comm.receiveBinary(
      _frame(
        'GC/$gcId/gcDispose',
        gcDisposeOf({'ref': gcId * 100 + paint, 'pixels': false, 'retain': true}),
      ),
    );
    await _settle();
  }

  // The next GC copies the top left quarter to the bottom right, as a ruler shifts its buffer.
  comm.receiveBinary(
      _frame('GC/$gcId/b', gcOp('copyAreaintintintintintintboolean', [0, 0, 5, 5, 5, 5, false])));
  comm.receiveBinary(
    _frame(
      'GC/$gcId/gcDispose',
      gcDisposeOf({'ref': gcId * 100 + 99, 'pixels': false, 'retain': false}),
    ),
  );
  await _settle();

  final rendered = await ImageUtils.decodeVImageToUIImage(VImage()..remoteRef = gcId * 100 + 99);
  expect(rendered, isNotNull, reason: 'the cycle produced no render at all');
  expect(
    await _pixelAt(rendered!, 1, 1),
    _red,
    reason: 'sanity: the square is still in the corner',
  );
  expect(
    await _pixelAt(rendered, 12, 12),
    _red,
    reason: 'the copy of the square, 2x2 at (5,5): pixels 10 to 13',
  );
  expect(
    await _pixelAt(rendered, 16, 16),
    isNot(_red),
    reason: 'and no bigger: a copy scaled twice covers pixels 10 to 17',
  );
}
