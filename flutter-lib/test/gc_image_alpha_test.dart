import 'dart:async';
import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/painting.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';

/// GC.setAlpha applies to every drawing primitive, images included: an application that blits an
/// icon at alpha 0 has asked for nothing to appear.

const _size = 8;

Future<ui.Image> _solidRed() {
  final pixels = Uint8List(_size * _size * 4);
  for (var i = 0; i < pixels.length; i += 4) {
    pixels[i] = 0xFF;
    pixels[i + 3] = 0xFF;
  }
  final done = Completer<ui.Image>();
  ui.decodeImageFromPixels(
      pixels, _size, _size, ui.PixelFormat.rgba8888, done.complete);
  return done.future;
}

/// The top-left pixel after painting [shape] over an opaque white ground.
Future<Color> _pixelUnder(ImageShape shape) async {
  final recorder = ui.PictureRecorder();
  final canvas = ui.Canvas(recorder);
  canvas.drawRect(const Rect.fromLTWH(0, 0, 8, 8),
      ui.Paint()..color = const Color(0xFFFFFFFF));
  shape.draw(canvas);
  final rendered =
      await recorder.endRecording().toImage(_size, _size);
  final data = await rendered.toByteData(format: ui.ImageByteFormat.rawRgba);
  final bytes = data!.buffer.asUint8List();
  return Color.fromARGB(bytes[3], bytes[0], bytes[1], bytes[2]);
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  const rect = Rect.fromLTWH(0, 0, 8, 8);

  test('an image blitted at alpha 0 paints nothing', () async {
    final image = await _solidRed();
    final shape = ImageShape.raster(image, rect, rect, alpha: 0);
    expect(await _pixelUnder(shape), const Color(0xFFFFFFFF),
        reason: 'a fully transparent blit must leave the ground untouched');
  });

  test('an image blitted at full alpha is unchanged', () async {
    final image = await _solidRed();
    final shape = ImageShape.raster(image, rect, rect, alpha: 255);
    expect(await _pixelUnder(shape), const Color(0xFFFF0000));
  });

  test('an image blitted at a partial alpha blends with the ground', () async {
    final image = await _solidRed();
    final shape = ImageShape.raster(image, rect, rect, alpha: 128);
    final blended = await _pixelUnder(shape);
    expect(blended.red, 255);
    expect(blended.green, greaterThan(100));
    expect(blended.green, lessThan(160));
  });

  test('the GC alpha in force at op time reaches the decoded blit', () async {
    final image = VImage()
      ..imageData = (VImageData()
        ..width = _size
        ..height = _size
        ..depth = 32
        ..data = await _pngBytes());

    final shape = await ImageShape.fromVImageDetailed(
        image,
        VGCDrawImageImageintintintintintintintint(
          destX: 0, destY: 0, destWidth: -1, destHeight: -1,
          srcX: 0, srcY: 0, srcWidth: -1, srcHeight: -1,
        ),
        null,
        alpha: 0);

    expect(shape.alpha, 0,
        reason: 'the decode is async, so the alpha has to be captured at op time');
  });
}

Future<Uint8List> _pngBytes() async {
  final image = await _solidRed();
  final data = await image.toByteData(format: ui.ImageByteFormat.png);
  return data!.buffer.asUint8List();
}
