// A copyArea must shift a picture-backed blit like every other shape, and bound it to the area it
// copied: the web renderers ignore a clip around a nested picture, so the shape has to carry pixels.

import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';

ui.Picture _picture() {
  final recorder = ui.PictureRecorder();
  ui.Canvas(recorder).drawRect(
      const Rect.fromLTWH(0, 0, 10, 10), ui.Paint()..color = const Color(0xFF0000FF));
  return recorder.endRecording();
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('copyArea shifts a picture-backed blit like any other shape', () {
    const src = Rect.fromLTWH(0, 0, 10, 10);
    final painted = <Shape>[
      ImageShape.picture(_picture(), src, src),
      RectShape(src, const Color(0xFF00FF00), 1, 0, 0),
    ];

    final copied = GCDrawer.copyAreaShapes(
      baseImage: null,
      painted: painted,
      srcRect: src,
      destOffset: const Offset(0, 20),
    );

    final picture = copied.whereType<ImageShape>().single;
    final rect = copied.whereType<RectShape>().single;
    expect(rect.rect.top, 20, reason: 'the ordinary shape moves');
    expect(picture.destRect.top, 20,
        reason: 'the picture-backed blit has to move with it');
    expect(picture.type, ImageType.raster,
        reason: 'and carry pixels, since a clip does not bound a nested picture');
    expect(picture.clipRect, const Rect.fromLTWH(0, 20, 10, 10),
        reason: 'bounded to the area the copy landed on');
  });
}
