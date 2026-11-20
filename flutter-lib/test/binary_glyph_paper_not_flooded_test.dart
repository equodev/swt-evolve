import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:image/image.dart' as img;
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/utils/glyph_tone.dart';
import 'package:swtflutter/src/impl/utils/image_utils.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

import 'support/icon_renders.dart';

// A tinted control icon that is a multi-tone glyph takes the tint on its ink only, with alpha from
// darkness as GC.drawImage gives it: light tones drop out instead of being flooded with the tint.
// An opaque glyph drawn on a light paper would otherwise become a solid square with no glyph.

const _side = 32;
const _paper = 0xDC;

/// Opaque everywhere: light-grey paper with a black block of ink in the middle.
Uint8List _glyphOnPaperPng() {
  final image = img.Image(width: _side, height: _side, numChannels: 4);
  img.fill(image, color: img.ColorRgba8(_paper, _paper, _paper, 255));
  img.fillRect(image, x1: 10, y1: 10, x2: 21, y2: 21, color: img.ColorRgba8(0, 0, 0, 255));
  return Uint8List.fromList(img.encodePng(image));
}

/// Transparent ground with a black block of ink: one tone, whose alpha alone says where the ink is.
Uint8List _singleToneGlyphPng() {
  final image = img.Image(width: _side, height: _side, numChannels: 4);
  img.fillRect(image, x1: 10, y1: 10, x2: 21, y2: 21, color: img.ColorRgba8(0, 0, 0, 255));
  return Uint8List.fromList(img.encodePng(image));
}

/// Transparent ground with a black outline around a white fill.
Uint8List _outlinedFillPng() {
  final image = img.Image(width: _side, height: _side, numChannels: 4);
  img.fillRect(image, x1: 8, y1: 8, x2: 23, y2: 23, color: img.ColorRgba8(0, 0, 0, 255));
  img.fillRect(image, x1: 11, y1: 11, x2: 20, y2: 20, color: img.ColorRgba8(255, 255, 255, 255));
  return Uint8List.fromList(img.encodePng(image));
}

VImage _image(Uint8List png) => VImage()..imageData = (VImageData()..data = png);

void _theme({required bool dark, required bool preserve}) {
  resetConfigFlags();
  ImageUtils.clearCache();
  applyConfigFlags(ConfigFlags()
    ..force_theme = dark ? 'dark' : 'light'
    ..preserve_icon_colors = preserve);
  setCurrentTheme(dark);
}

/// Builds the image as a Button does and returns a reader of its painted pixels.
Future<Color Function(int x, int y)> _paint(WidgetTester tester, Uint8List png) {
  final widget = ImageUtils.buildVImage(
    _image(png),
    width: _side.toDouble(),
    height: _side.toDouble(),
    renderAsIcon: true,
  );
  expect(widget, isNotNull);
  return paintedIconPixels(tester, widget!, side: _side);
}

void main() {
  tearDown(resetConfigFlags);

  group('a glyph on an opaque paper', () {
    for (final (dark, preserve) in [(true, true), (true, false), (false, false)]) {
      testWidgets('dark=$dark preserve_icon_colors=$preserve: the ink takes the tint, the paper drops out',
          (tester) async {
        _theme(dark: dark, preserve: preserve);
        final pixels = await _paint(tester, _glyphOnPaperPng());
        expect(pixels(_side ~/ 2, _side ~/ 2), AppColors.getColor(true));
        final paper = pixels(2, 2);
        expect(paper.a, 0, reason: 'the paper was flooded with the tint: $paper');
      });
    }

    testWidgets('light preserve_icon_colors=true: kept as drawn, the ink reads on the light ground',
        (tester) async {
      _theme(dark: false, preserve: true);
      final pixels = await _paint(tester, _glyphOnPaperPng());
      expect(pixels(_side ~/ 2, _side ~/ 2), const Color(0xFF000000));
      expect(pixels(2, 2), const Color(0xFF000000 | _paper << 16 | _paper << 8 | _paper));
    });
  });

  testWidgets('a single-tone glyph takes no stencil and is tinted through its own alpha',
      (tester) async {
    _theme(dark: true, preserve: false);
    final png = _singleToneGlyphPng();
    final tone = GlyphTone.ofEncoded(png, GlyphTintLimits.fallback);
    expect(tone, isNotNull);
    expect(tone!.inkStencil(png), isNull);

    final pixels = await _paint(tester, png);
    expect(pixels(_side ~/ 2, _side ~/ 2), AppColors.getColor(true));
    expect(pixels(2, 2).a, 0);
  });

  testWidgets('a light fill inside an outline drops out even on a transparent ground',
      (tester) async {
    _theme(dark: true, preserve: false);
    final pixels = await _paint(tester, _outlinedFillPng());
    expect(pixels(9, _side ~/ 2), AppColors.getColor(true), reason: 'the outline is ink');
    final fill = pixels(_side ~/ 2, _side ~/ 2);
    expect(fill.a, 0, reason: 'the white fill is paper: $fill');
  });
}
