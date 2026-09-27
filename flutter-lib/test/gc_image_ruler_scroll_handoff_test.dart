// A ruler scroll step copies with one GC and repaints the exposed band with a second on the same
// Image: the second drawer must start from the render the first one left.

import 'dart:convert';
import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';
import 'package:swtflutter/src/impl/utils/image_utils.dart';

const int _w = 29;
const int _h = 120;
const int _step = 18; // one line

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
  for (var i = 0; i < 200; i++) {
    await Future<void>.delayed(Duration.zero);
  }
}

Map<String, Object> _rgb(int r, int g, int b) =>
    {'a': 255, 'r': r, 'g': g, 'b': b};

/// Opens a drawer for one GC on the buffer Image, whose current content is [baseRef].
void _openGc(dynamic comm, int gcId, int? baseRef, Map<String, Object> background) {
  // What the GC/create handler does in production.
  GCDrawer.standalone(VGC()
    ..swt = 'GC'
    ..id = gcId);
  comm.receiveBinary(_frame('GC/$gcId', _json({
    'id': gcId,
    'swt': 'GC',
    'alpha': 255,
    'clippingRects': <Object>[],
    'background': background,
  })));
  comm.receiveBinary(_frame('GC/$gcId/imageInit', _json({
    'id': 77,
    'swt': 'Image',
    'width': _w,
    'height': _h,
    if (baseRef != null) 'remoteRef': baseRef,
  })));
}

void _closeGc(dynamic comm, int gcId, int ref) {
  comm.receiveBinary(_frame('GC/$gcId/gcDispose',
      _json({'ref': ref, 'pixels': false, 'retain': false})));
}

Future<ui.Color> _pixelAt(ui.Image image, int x, int y) async {
  final data = await image.toByteData(format: ui.ImageByteFormat.rawRgba);
  final bytes = data!.buffer.asUint8List();
  final i = (y * image.width + x) * 4;
  return ui.Color.fromARGB(bytes[i + 3], bytes[i], bytes[i + 1], bytes[i + 2]);
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('a ruler scrolled by copyArea keeps the rows it retained', () async {
    final comm = EquoCommService.commForTesting;
    const red = ui.Color(0xFFFF0000);
    const green = ui.Color(0xFF00FF00);

    // Cycle 0: the gutter as it stands — red everywhere, with one green row at y=[18,36).
    _openGc(comm, 6001, null, _rgb(255, 0, 0));
    comm.receiveBinary(_frame('GC/6001/fillRectangleintintintint',
        _json({'x': 0, 'y': 0, 'width': _w, 'height': _h})));
    comm.receiveBinary(_frame('GC/6001', _json({
      'id': 6001, 'swt': 'GC', 'alpha': 255,
      'clippingRects': <Object>[], 'background': _rgb(0, 255, 0),
    })));
    comm.receiveBinary(_frame('GC/6001/fillRectangleintintintint',
        _json({'x': 0, 'y': _step, 'width': _w, 'height': _step})));
    _closeGc(comm, 6001, 6100);
    await _settle();

    final before = await ImageUtils.decodeVImageToUIImage(VImage()..remoteRef = 6100);
    expect(before, isNotNull, reason: 'the gutter never rendered at all');
    expect(await _pixelAt(before!, 2, _step + 4), green,
        reason: 'the marked row should be where it was drawn');

    // Cycle 1: one scroll step. The first GC copies rows [18, 120) up to y=0.
    _openGc(comm, 6002, 6100, _rgb(255, 0, 0));
    comm.receiveBinary(_frame('GC/6002/copyAreaintintintintintint', _json({
      'srcX': 0, 'srcY': _step, 'width': _w, 'height': _h - _step,
      'destX': 0, 'destY': 0,
    })));
    _closeGc(comm, 6002, 6101);
    await _settle();
    // The copy cycle on its own: its render is what the next GC starts from.
    final copied = await ImageUtils.decodeVImageToUIImage(VImage()..remoteRef = 6101);
    expect(copied, isNotNull, reason: 'the copyArea cycle produced no render');
    expect(await _pixelAt(copied!, 2, 4), green,
        reason: 'copyArea drew nothing — it ran before the base image it copies out of existed');

    // Cycle 2: A second GC on the same Image fills only the newly exposed band at the bottom.
    _openGc(comm, 6003, 6101, _rgb(255, 0, 0));
    comm.receiveBinary(_frame('GC/6003/fillRectangleintintintint',
        _json({'x': 0, 'y': _h - _step, 'width': _w, 'height': _step})));
    _closeGc(comm, 6003, 6102);
    await _settle();

    final after = await ImageUtils.decodeVImageToUIImage(VImage()..remoteRef = 6102);
    expect(after, isNotNull, reason: 'the scrolled gutter produced no render');

    // The green row started at y=18 and the gutter scrolled up by one row, so it belongs at y=0.
    expect(await _pixelAt(after!, 2, 4), green,
        reason: 'the retained rows were lost in the hand-off between the two GCs, so the gutter '
            'keeps the numbers it had before the scroll');
    expect(await _pixelAt(after, 2, _step + 4), red,
        reason: 'the row the marked one vacated should have moved on');
  });

  test('a scrolled ruler is handed from GC to GC as a picture, never read back', () async {
    final comm = EquoCommService.commForTesting;
    const red = ui.Color(0xFFFF0000);
    const green = ui.Color(0xFF00FF00);

    _openGc(comm, 6201, null, _rgb(255, 0, 0));
    comm.receiveBinary(_frame('GC/6201/fillRectangleintintintint',
        _json({'x': 0, 'y': 0, 'width': _w, 'height': _h})));
    comm.receiveBinary(_frame('GC/6201', _json({
      'id': 6201, 'swt': 'GC', 'alpha': 255,
      'clippingRects': <Object>[], 'background': _rgb(0, 255, 0),
    })));
    comm.receiveBinary(_frame('GC/6201/fillRectangleintintintint',
        _json({'x': 0, 'y': _step, 'width': _w, 'height': _step})));
    _closeGc(comm, 6201, 6300);
    await _settle();

    _openGc(comm, 6202, 6300, _rgb(255, 0, 0));
    comm.receiveBinary(_frame('GC/6202/copyAreaintintintintintint', _json({
      'srcX': 0, 'srcY': _step, 'width': _w, 'height': _h - _step,
      'destX': 0, 'destY': 0,
    })));
    _closeGc(comm, 6202, 6301);
    await _settle();

    _openGc(comm, 6203, 6301, _rgb(255, 0, 0));
    comm.receiveBinary(_frame('GC/6203/fillRectangleintintintint',
        _json({'x': 0, 'y': _h - _step, 'width': _w, 'height': _step})));
    _closeGc(comm, 6203, 6302);
    await _settle();

    for (final ref in [6300, 6301, 6302]) {
      expect(ImageUtils.remoteRender(ref)!.hasPixels, isFalse,
          reason: 'render $ref was made into pixels only to be drawn again: on the web that is an '
              'off-screen render and a GPU readback per ruler repaint');
    }
    final after = await ImageUtils.decodeVImageToUIImage(VImage()..remoteRef = 6302);
    expect(await _pixelAt(after!, 2, 4), green, reason: 'the retained rows moved up with the copy');
    expect(await _pixelAt(after, 2, _step + 4), red);
  });

  test('a long chain of partial repaints is flattened rather than nested without end', () async {
    final comm = EquoCommService.commForTesting;
    const blue = ui.Color(0xFF0000FF);
    _openGc(comm, 6400, null, _rgb(0, 0, 255));
    comm.receiveBinary(_frame('GC/6400/fillRectangleintintintint',
        _json({'x': 0, 'y': 0, 'width': _w, 'height': _h})));
    _closeGc(comm, 6400, 6500);
    await _settle();
    var base = 6500;
    for (var i = 1; i <= 20; i++) {
      // Each cycle draws only one pixel row, so every render has to start from the one before.
      _openGc(comm, 6400 + i, base, _rgb(0, 0, 255));
      comm.receiveBinary(_frame('GC/${6400 + i}/fillRectangleintintintint',
          _json({'x': 0, 'y': i, 'width': 1, 'height': 1})));
      _closeGc(comm, 6400 + i, 6500 + i);
      await _settle();
      base = 6500 + i;
      expect(ImageUtils.remoteRender(base)!.depth, lessThanOrEqualTo(8),
          reason: 'every render drawing the whole chain before it costs more with every scroll');
    }
    final last = await ImageUtils.decodeVImageToUIImage(VImage()..remoteRef = base);
    expect(await _pixelAt(last!, 10, 60), blue, reason: 'the first render is still underneath');
  });

  test('a repaint that covers the whole buffer does not draw the one before it', () async {
    final comm = EquoCommService.commForTesting;
    _openGc(comm, 6600, null, _rgb(255, 0, 0));
    comm.receiveBinary(_frame('GC/6600/fillRectangleintintintint',
        _json({'x': 0, 'y': 0, 'width': _w, 'height': _h})));
    _closeGc(comm, 6600, 6700);
    await _settle();
    _openGc(comm, 6601, 6700, _rgb(0, 255, 0));
    comm.receiveBinary(_frame('GC/6601/fillRectangleintintintint',
        _json({'x': 0, 'y': 0, 'width': _w, 'height': _h})));
    _closeGc(comm, 6601, 6701);
    await _settle();

    expect(ImageUtils.remoteRender(6701)!.depth, 0,
        reason: 'what is painted over completely is not part of the render');
    final img = await ImageUtils.decodeVImageToUIImage(VImage()..remoteRef = 6701);
    expect(await _pixelAt(img!, 3, 3), const ui.Color(0xFF00FF00));
  });
}
