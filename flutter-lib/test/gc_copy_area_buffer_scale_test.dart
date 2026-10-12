// A copyArea on a GC over an Image held at the raster scale copies the Image's own pixels, at their size.
//
// JFace's LineNumberRulerColumn scrolls its buffer Image this way: a new GC on the buffer, a copyArea of
// the band it keeps, the exposed band drawn fresh. The buffer is denser than its size, so the band copied
// from it has to come back at the size it was drawn at, not at the buffer's density.

import 'dart:convert';
import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';
import 'package:swtflutter/src/impl/utils/image_utils.dart';

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

const _red = ui.Color(0xFFFF0000);
const _white = ui.Color(0xFFFFFFFF);

/// Opens a GC on a 10x15 Image held at twice its size (20x30 pixels).
void _openGc(EquoCommBase comm, int gcId, Map<String, Object> imageInit, {int background = 0xFFFFFF}) {
  GCDrawer.standalone(VGC()
    ..swt = 'GC'
    ..id = gcId);
  comm.receiveBinary(_frame('GC/$gcId', _json({
    'id': gcId,
    'swt': 'GC',
    'alpha': 255,
    'bufferScale': 2.0,
    'clippingRects': <Object>[],
    'background': {'a': 255, 'r': (background >> 16) & 0xFF, 'g': (background >> 8) & 0xFF, 'b': background & 0xFF},
    'foreground': {'a': 255, 'r': 0, 'g': 0, 'b': 0},
  })));
  comm.receiveBinary(_frame('GC/$gcId/imageInit', _json(imageInit)));
}

void _fill(EquoCommBase comm, int gcId, int x, int y, int w, int h) =>
    comm.receiveBinary(_frame('GC/$gcId/fillRectangleintintintint',
        _json({'x': x, 'y': y, 'width': w, 'height': h})));

void _dispose(EquoCommBase comm, int gcId, int ref) =>
    comm.receiveBinary(_frame('GC/$gcId/gcDispose',
        _json({'ref': ref, 'pixels': false, 'retain': false})));

/// The first paint of the buffer: white, with a 5x5 red square at (0, 5).
Future<void> _paintBuffer(EquoCommBase comm, int gcId, int ref) async {
  _openGc(comm, gcId, {'width': 20, 'height': 30});
  _fill(comm, gcId, 0, 0, 10, 15);
  comm.receiveBinary(_frame('GC/$gcId',
      _json({'id': gcId, 'swt': 'GC', 'alpha': 255, 'bufferScale': 2.0, 'clippingRects': <Object>[],
        'background': {'a': 255, 'r': 255, 'g': 0, 'b': 0},
        'foreground': {'a': 255, 'r': 0, 'g': 0, 'b': 0}})));
  _fill(comm, gcId, 0, 5, 5, 5);
  _dispose(comm, gcId, ref);
  await _settle();
}

/// The scroll: a GC on the painted buffer copies rows 5..15 up to rows 0..10.
Future<ui.Image> _scrollBuffer(EquoCommBase comm, int gcId, int baseRef, int ref) async {
  _openGc(comm, gcId, {'width': 20, 'height': 30, 'remoteRef': baseRef});
  comm.receiveBinary(_frame('GC/$gcId/copyAreaintintintintintintboolean',
      _json({'srcX': 0, 'srcY': 5, 'width': 10, 'height': 10, 'destX': 0, 'destY': 0, 'paint': true})));
  _dispose(comm, gcId, ref);
  await _settle();
  final rendered = await ImageUtils.decodeVImageToUIImage(VImage()..remoteRef = ref);
  expect(rendered, isNotNull, reason: 'the scroll produced no render at all');
  return rendered!;
}

Future<void> _expectScrolled(ui.Image scrolled) async {
  expect(scrolled.width, 20);
  expect(await _pixelAt(scrolled, 5, 5), _red,
      reason: 'the red square, 5x5 at (0, 5), moves up to (0, 0): 10x10 pixels of the buffer');
  expect(await _pixelAt(scrolled, 15, 5), _white,
      reason: 'and stays 5 wide, not twice that');
  expect(await _pixelAt(scrolled, 5, 15), _white,
      reason: 'and 5 tall: what lands below it comes from below the square');
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('the band copied out of a buffer drawn as a display list keeps its size', () async {
    final comm = EquoCommService.commForTesting;
    await _paintBuffer(comm, 9951, 9960);

    await _expectScrolled(await _scrollBuffer(comm, 9952, 9960, 9961));
  });

  test('the band copied out of a buffer already turned into pixels keeps its size', () async {
    final comm = EquoCommService.commForTesting;
    await _paintBuffer(comm, 9953, 9962);
    // Reading the render turns it into pixels, so the next GC starts from a raster base.
    final painted = await ImageUtils.decodeVImageToUIImage(VImage()..remoteRef = 9962);
    expect(await _pixelAt(painted!, 5, 15), _red, reason: 'the buffer as painted: the square at (0, 5)');

    await _expectScrolled(await _scrollBuffer(comm, 9954, 9962, 9963));
  });
}
