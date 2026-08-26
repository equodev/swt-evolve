import 'dart:async';
import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';

/// Regression for the line-number gutter that stops following the text while scrolling.
///
/// JFace's `LineNumberRulerColumn` keeps one buffer `Image` across paints: on each scroll it
/// opens a fresh `GC` on that buffer, `copyArea`s the retained band by the scroll delta and
/// redraws only the newly exposed band. A GC opened over an existing Image has an empty shape
/// list — its content is the image — so a copyArea that only translates shapes moves nothing
/// and the retained band keeps its pre-scroll line numbers.
void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  /// A [height]x1 image whose row `y` is the grey `y`.
  Future<ui.Image> rowRamp(int height) {
    final pixels = Uint8List(height * 4);
    for (int y = 0; y < height; y++) {
      pixels[y * 4] = y;
      pixels[y * 4 + 1] = y;
      pixels[y * 4 + 2] = y;
      pixels[y * 4 + 3] = 255;
    }
    final done = Completer<ui.Image>();
    ui.decodeImageFromPixels(pixels, 1, height, ui.PixelFormat.rgba8888, done.complete);
    return done.future;
  }

  Future<List<int>> rowsOf(ui.Image base, List<Shape> shapes, int height) async {
    final recorder = ui.PictureRecorder();
    final canvas = ui.Canvas(recorder);
    canvas.drawImage(base, Offset.zero, ui.Paint());
    for (final shape in shapes) {
      shape.draw(canvas);
    }
    final rendered = await recorder.endRecording().toImage(1, height);
    final bytes = await rendered.toByteData(format: ui.ImageByteFormat.rawRgba);
    return [for (int y = 0; y < height; y++) bytes!.getUint8(y * 4)];
  }

  test('copyArea shifts the pixels the GC drawable already shows', () async {
    const height = 8;
    final base = await rowRamp(height);

    // Scroll up by 3: rows 0..4 move down to rows 3..7, exactly as LineNumberRulerColumn
    // asks when the viewport moves and it keeps the band it can re-use.
    final shapes = GCDrawer.copyAreaShapes(
      baseImage: base,
      painted: const [],
      srcRect: const Rect.fromLTWH(0, 0, 1, height - 3),
      destOffset: const Offset(0, 3),
    );

    expect(await rowsOf(base, shapes, height), [0, 1, 2, 0, 1, 2, 3, 4],
        reason: 'rows 0..4 must be copied down by 3; the source rows stay put, as SWT '
            'copyArea copies rather than moves');
  });

  test('copyArea still carries the shapes drawn through the GC', () async {
    const height = 8;
    final base = await rowRamp(height);
    final white = RectShape(const Rect.fromLTWH(0, 0, 1, 2), const Color(0xFFFFFFFF), 0, 0, 0,
        isFilled: true);

    final shapes = GCDrawer.copyAreaShapes(
      baseImage: base,
      painted: [white],
      srcRect: const Rect.fromLTWH(0, 0, 1, height - 3),
      destOffset: const Offset(0, 3),
    );

    expect(await rowsOf(base, [white, ...shapes], height),
        [255, 255, 2, 255, 255, 2, 3, 4],
        reason: 'the copy must reproduce the GC content — base pixels and the shapes drawn '
            'over them — not one or the other');
  });
}
