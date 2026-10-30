// draw2d/GEF paints through BufferedGraphicsSource, not SWT.Paint. Its flushGraphics() is one
// synchronous Java method that emits, back to back and with nothing waited for in between:
//
//   imageGC.dispose()          -> GC/create + imageInit + ops + GC/<img>/gcDispose(ref N)
//   controlGC.drawImage(image) -> GC/<ctrl>/drawImageImage... carrying image:{remoteRef:N}
//   imageBuffer.dispose()      -> Image/releaseRemoteRef N
//   controlGC.dispose()        -> GC/<ctrl>/gcDispose {fullRepaint:false}
//
// (verified by javap -p -c on org.eclipse.draw2d.BufferedGraphicsSource 3.10.100.201606061308)
//
// The blit ships no pixels — only the ref — so the figure is painted if and only if the ref
// resolves to the ui.Image the off-screen cycle rendered. This drives that exact frame order and
// asserts the blit reaches the scene with real pixels.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/channel_header.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';
import 'package:swtflutter/src/impl/utils/image_utils.dart';

import 'support/gc_frames.dart';
import 'support/channels.dart' as channels;
import 'support/channels.dart' show NamedChannels;

Uint8List _frame(String actionId, List<int> body) {
  final out = channels.frame(actionId, Uint8List.fromList(body));
  return out;
}


/// The gcDispose body GCImageDrawer.endDrawCycle puts on the wire.
List<int> _gcDisposeBody(int ref, {bool wantPixels = false, bool retain = false}) =>
    gcDisposeOf({'ref': ref, 'pixels': wantPixels, 'retain': retain});

List<int> _refBody(int ref) =>
    (ByteData(8)..setInt64(0, ref, Endian.big)).buffer.asUint8List();

int _readInt64BE(ByteData v, int offset) =>
    v.getInt32(offset, Endian.big) * 0x100000000 + v.getUint32(offset + 4, Endian.big);

Future<void> _settle() async {
  for (var i = 0; i < 60; i++) {
    await Future<void>.delayed(Duration.zero);
  }
}

/// Every raster ImageShape reachable in [shapes], however deeply wrapped in transform/clip shapes.
List<ImageShape> _rasterShapes(List<Shape> shapes) {
  final found = <ImageShape>[];
  void walk(List<Shape> list) {
    for (final s in list) {
      if (s is ImageShape) found.add(s);
      if (s is RegionShape) walk(s.ops);
      if (s is TransformShape) walk(s.children);
      if (s is ClipPathShape) walk(s.children);
    }
  }

  walk(shapes);
  return found;
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  const imageGcId = 7701;
  const controlGcId = 7702;
  const bufferWidth = 40;
  const bufferHeight = 30;

  setUp(() {
    // What main.dart's _registerImageReleaseListener registers in production: the release takes its
    // turn in the apply queue like any other frame.
    EquoCommService.commForTesting.onBytesNamed('Image/releaseRemoteRef', (bytes) {
      ImageUtils.releaseRemoteImage(
          _readInt64BE(ByteData.sublistView(bytes), 0));
    });
  });

  test('a draw2d blit paints the off-screen render its remoteRef names', () async {
    const ref = 7710;
    final comm = EquoCommService.commForTesting;

    List<Shape> committed = const [];
    GCDrawer.embedded(
      VGC()
        ..swt = 'GC'
        ..id = controlGcId,
      onShapesUpdated: (shapes) => committed = List<Shape>.from(shapes),
    );

    // 1. imageGC.dispose(). GC/create builds the drawer before anything behind it applies, which
    //    is exactly what the handler does in production.
    GCDrawer.standalone(VGC()
      ..swt = 'GC'
      ..id = imageGcId);
    comm.receiveBinary(_frame('GC/$imageGcId/imageInit',
        imageInit(VImage()
          ..width = bufferWidth
          ..height = bufferHeight)));
    comm.receiveBinary(_frame('GC/$imageGcId/b',
        gcOp('fillRectangleintintintint', [0, 0, bufferWidth, bufferHeight])));
    comm.receiveBinary(
        _frame('GC/$imageGcId/gcDispose', _gcDisposeBody(ref)));

    // 2. controlGC.drawImage(image, 0, 0, w, h, x, y, w, h) — the ref, no pixels.
    final buffer = VImage()
      ..id = 7720
      ..remoteRef = ref
      ..width = bufferWidth
      ..height = bufferHeight;
    describeImage(buffer);
    comm.receiveBinary(_frame(
        'GC/$controlGcId/b',
        gcOp('drawImageImageintintintintintintintint',
            [buffer, 0, 0, bufferWidth, bufferHeight, 5, 9, bufferWidth, bufferHeight])));

    // 3. imageBuffer.dispose().
    comm.receiveBinary(_frame('Image/releaseRemoteRef', _refBody(ref)));

    // 4. controlGC.dispose() — opened outside any Paint, so this composites instead of replacing.
    comm.receiveBinary(
        _frame('GC/$controlGcId/gcDispose', gcDisposeOf({'fullRepaint': false})));

    await _settle();

    final rasters = _rasterShapes(committed);
    expect(rasters, isNotEmpty,
        reason: 'the blit must reach the scene as a raster shape');
    // An unread render stays a picture; either form paints the figure, neither is a blank canvas.
    final blit = rasters.single;
    expect(blit.image ?? blit.remotePicture, isNotNull,
        reason: 'the blit carries no pixels, so the figure is painted only if remoteRef '
            '$ref resolved to the off-screen render; carrying neither is a blank canvas');
    expect(blit.destRect, const Rect.fromLTWH(5, 9, 40, 30));
  });
}
