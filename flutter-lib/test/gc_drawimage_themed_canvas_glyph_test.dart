import 'dart:convert';
import 'dart:ui' as ui;

import 'package:flutter/painting.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

/// `disable_swt_canvas_colors` re-grounds a Canvas in the theme's colors: every stroke, fill and
/// text role the GC paints follows, and so does a monochrome glyph the application blits — a grid's
/// checkbox, a sort indicator. Such a glyph has no color of the application's to keep, and on a
/// re-grounded canvas its own would be the wrong one. Color artwork does have one, and keeps it;
/// nothing changes at all while the canvas keeps the application's own colors.

/// 16x16 RGBA: a 1px #333333 box outline over a transparent interior — an unchecked checkbox cell
/// exactly as a grid library's painter blits it.
const _greyBoxPng =
    'iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAI0lEQVR42mNgGDTA2Nj4'
    'PykYqwGkWDZqwKgBtDOAoqQ8YAAASMpGIT+n1+AAAAAASUVORK5CYII=';

/// 16x16 RGBA: the same #333333 box outline around an opaque white interior — a checkbox whose
/// artwork carries its own light fill.
const _filledGreyBoxPng =
    'iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAIElEQVR42mNgGF7A2Nj4PzEYrwGEwKgBI8MAihLS0AQAgS44cNGTIhMAAAAASUVORK5CYII=';

/// The same footprint in gold — stands for the colored artwork alongside it in the same grid.
const _goldBoxPng =
    'iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAI0lEQVR42mNgGDTgxRav'
    '/6RgrAaQYtmoAaMG0M4AipLygAEAyTx/XQYGlqEAAAAASUVORK5CYII=';

const _tint = Color(0xFFF2F2F2);

VImage _blittedImage(String png) => VImage()
  ..filename = 'unchecked'
  ..imageData = (VImageData()
    ..width = 16
    ..height = 16
    ..data = base64Decode(png));

VGCDrawImageImageintintintintintintintint get _naturalSizeBlit =>
    VGCDrawImageImageintintintintintintintint(
      destX: 681,
      destY: 61,
      destWidth: -1,
      destHeight: -1,
      srcX: 0,
      srcY: 0,
      srcWidth: -1,
      srcHeight: -1,
    );

Future<ImageShape> _blit(String png, {GlyphTintLimits? limits}) =>
    ImageShape.fromVImageDetailed(_blittedImage(png), _naturalSizeBlit, null,
        tint: _tint, glyphLimits: limits);

const _darkCanvas = Color(0xFF202020);

/// [shape] blitted onto a 16x16 [_darkCanvas], as each pixel's ARGB.
Future<List<int>> _paintOnDarkCanvas(ImageShape shape) async {
  final recorder = ui.PictureRecorder();
  final canvas = ui.Canvas(recorder);
  canvas.drawRect(const Rect.fromLTWH(0, 0, 16, 16), Paint()..color = _darkCanvas);
  shape.draw(canvas);
  final image = await recorder.endRecording().toImage(16, 16);
  final bytes = (await image.toByteData(format: ui.ImageByteFormat.rawRgba))!;
  return [
    for (var i = 0; i < 16 * 16; i++)
      bytes.getUint8(i * 4 + 3) << 24 |
          bytes.getUint8(i * 4) << 16 |
          bytes.getUint8(i * 4 + 1) << 8 |
          bytes.getUint8(i * 4 + 2)
  ];
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  test('a monochrome glyph keeps its own colors while the canvas keeps the application\'s',
      () async {
    final shape = await _blit(_greyBoxPng);

    expect(shape.type, ImageType.raster);
    expect(shape.colorFilter, isNull);
  });

  test('a monochrome glyph takes the theme tint once the canvas is themed', () async {
    applyConfigFlags(ConfigFlags()..disable_swt_canvas_colors = true);

    final shape = await _blit(_greyBoxPng);

    expect(shape.type, ImageType.raster,
        reason: 'the application bitmap still draws — only its color moves');
    expect(shape.colorFilter, const ColorFilter.mode(_tint, BlendMode.srcIn));
  });

  test('a glyph\'s own light fill stays paper, and only its dark strokes take the tint', () async {
    applyConfigFlags(ConfigFlags()..disable_swt_canvas_colors = true);

    final shape = await ImageShape.fromVImageDetailed(
        _blittedImage(_filledGreyBoxPng),
        VGCDrawImageImageintintintintintintintint(
            destX: 0, destY: 0, destWidth: -1, destHeight: -1,
            srcX: 0, srcY: 0, srcWidth: -1, srcHeight: -1),
        null,
        tint: _tint);
    final pixels = await _paintOnDarkCanvas(shape);

    expect(pixels[3 * 16 + 3], _tint.toARGB32(), reason: 'the outline takes the tint');
    expect(pixels[8 * 16 + 8], _darkCanvas.toARGB32(),
        reason: 'the white interior is paper: tinting it would fill the box solid');
    expect(pixels[0], _darkCanvas.toARGB32(), reason: 'outside the glyph is untouched');
  });

  test('preserve_icon_colors does not hold the tint back', () async {
    // It keeps the *application's* icon colors, and a themed canvas is the more specific
    // instruction — an application may well set both.
    applyConfigFlags(ConfigFlags()
      ..disable_swt_canvas_colors = true
      ..preserve_icon_colors = true);

    final shape = await _blit(_greyBoxPng);

    expect(shape.colorFilter, const ColorFilter.mode(_tint, BlendMode.srcIn));
  });

  test('color artwork is never re-tinted', () async {
    applyConfigFlags(ConfigFlags()..disable_swt_canvas_colors = true);

    final shape = await _blit(_goldBoxPng);

    expect(shape.colorFilter, isNull);
  });

  test('the theme decides how big a glyph may be', () async {
    applyConfigFlags(ConfigFlags()..disable_swt_canvas_colors = true);

    final shape = await _blit(_greyBoxPng,
        limits: const GlyphTintLimits(maxSide: 8, channelTolerance: 4));

    expect(shape.colorFilter, isNull,
        reason: '16x16 is past a theme that admits 8px glyphs');
  });

  test('use_swt_colors keeps the application in charge of the whole canvas', () async {
    applyConfigFlags(ConfigFlags()
      ..disable_swt_canvas_colors = true
      ..use_swt_colors = true);

    final shape = await _blit(_greyBoxPng);

    expect(shape.colorFilter, isNull);
  });
}
