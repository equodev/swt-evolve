import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:image/image.dart' as img;
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';
import 'package:swtflutter/src/impl/utils/image_utils.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

import 'support/icon_renders.dart';

/// preserve_icon_colors keeps an application's icons as drawn, but a monochrome glyph that would
/// not read on the theme's ground still takes the theme tint: a black toolbar glyph on a dark
/// theme would otherwise vanish. Color artwork, and a glyph that already reads, stay as drawn.

Uint8List _solidPng(int r, int g, int b, {int side = 16}) {
  final image = img.Image(width: side, height: side, numChannels: 4);
  img.fill(image, color: img.ColorRgba8(r, g, b, 255));
  return Uint8List.fromList(img.encodePng(image));
}

VImage _image(Uint8List png) => VImage()..imageData = (VImageData()..data = png);

void main() {
  setUp(() {
    resetConfigFlags();
    ImageUtils.clearCache();
    applyConfigFlags(ConfigFlags()
      ..force_theme = 'dark'
      ..preserve_icon_colors = true);
    setCurrentTheme(true);
  });
  tearDown(resetConfigFlags);

  testWidgets('a black glyph on a dark theme takes the theme tint', (tester) async {
    expect(preserveIconColors, isTrue);
    final center = await _centerPixel(tester, _solidPng(0, 0, 0));
    expect(center, AppColors.getColor(true),
        reason: 'a black glyph on a dark ground does not read and must be tinted');
  });

  testWidgets('a near-black glyph on a dark theme takes the theme tint', (tester) async {
    final center = await _centerPixel(tester, _solidPng(0x33, 0x33, 0x33));
    expect(center, AppColors.getColor(true));
  });

  testWidgets('a light glyph that reads on a dark theme keeps its own tone', (tester) async {
    final center = await _centerPixel(tester, _solidPng(0xE0, 0xE0, 0xE0));
    expect(center, const Color(0xFFE0E0E0));
  });

  testWidgets('color artwork on a dark theme keeps its colors', (tester) async {
    final center = await _centerPixel(tester, _solidPng(0x3F, 0x3E, 0x85));
    expect(center, const Color(0xFF3F3E85));
  });

  testWidgets('a black glyph rendered on the light theme takes the tint after a switch to dark',
      (tester) async {
    final png = _solidPng(0, 0, 0);
    setCurrentTheme(false);
    expect(await _asyncCenterPixel(tester, png), const Color(0xFF000000),
        reason: 'a black glyph reads on the light ground and stays as drawn');

    setCurrentTheme(true);
    expect(await _asyncCenterPixel(tester, png), AppColors.getColor(true),
        reason: 'the render made for the light ground must not be reused on the dark one');
  });

  group('a replacement SVG', () {
    const blackGlyph = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">'
        '<circle cx="12" cy="12" r="10" fill="none" stroke="#000000"/>'
        '<path d="M6 12h12" fill="#000000"/></svg>';
    const unpaintedGlyph = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">'
        '<path d="M6 12h12"/></svg>';
    const lightGlyph = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">'
        '<path d="M6 12h12" stroke="#E0E0E0"/></svg>';
    const colorArtwork = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">'
        '<path d="M6 12h12" stroke="#000000"/><circle r="4" fill="#3F3E85"/></svg>';

    testWidgets('drawn in black on a dark theme takes the theme tint', (tester) async {
      expect(await _svgFilter(tester, blackGlyph),
          ColorFilter.mode(AppColors.getColor(true), BlendMode.srcIn));
    });

    testWidgets('with no paint of its own draws black, so it takes the theme tint',
        (tester) async {
      expect(await _svgFilter(tester, unpaintedGlyph),
          ColorFilter.mode(AppColors.getColor(true), BlendMode.srcIn));
    });

    testWidgets('drawn in a light grey that reads on a dark theme stays as drawn', (tester) async {
      expect(await _svgFilter(tester, lightGlyph), isNull);
    });

    testWidgets('in color stays as drawn', (tester) async {
      expect(await _svgFilter(tester, colorArtwork), isNull);
    });

    testWidgets('drawn in black on a light theme stays as drawn', (tester) async {
      setCurrentTheme(false);
      expect(await _svgFilter(tester, blackGlyph), isNull);
    });

    testWidgets('drawn in black by a GC on a dark theme takes the theme tint', (tester) async {
      expect(await _gcSvgFilter(tester, blackGlyph),
          ColorFilter.mode(AppColors.getColor(true), BlendMode.srcIn));
    });

    testWidgets('drawn in a light grey by a GC on a dark theme stays as drawn', (tester) async {
      expect(await _gcSvgFilter(tester, lightGlyph), isNull);
    });
  });

  group('a named icon', () {
    testWidgets('whose icon theme color does not read on a dark theme takes the theme tint',
        (tester) async {
      expect(await _namedIconColor(tester, null), AppColors.getColor(true));
    });

    testWidgets('whose icon theme color reads on a dark theme keeps it', (tester) async {
      expect(await _namedIconColor(tester, const Color(0xFFE0E0E0)), const Color(0xFFE0E0E0));
    });
  });
}

/// The color filter the replacement SVG is painted with, null when it is drawn as authored.
Future<ColorFilter?> _svgFilter(WidgetTester tester, String svg) async {
  final widget = ImageUtils.buildVImage(VImage()..svgContent = svg, size: 16, renderAsIcon: true);
  expect(widget, isNotNull);
  await tester.pumpWidget(Directionality(textDirection: TextDirection.ltr, child: widget!));
  return tester.widget<SvgPicture>(find.byType(SvgPicture)).colorFilter;
}

/// The color filter a GC drawImage paints the replacement SVG with, null when drawn as authored.
Future<ColorFilter?> _gcSvgFilter(WidgetTester tester, String svg) async {
  final blit = VGCDrawImageImageintintintintintintintint(
    destX: 0,
    destY: 0,
    destWidth: 16,
    destHeight: 16,
    srcX: 0,
    srcY: 0,
    srcWidth: -1,
    srcHeight: -1,
  );
  final shape = await tester.runAsync(() => ImageShape.fromVImageDetailed(
      VImage()..svgContent = svg, blit, null, useThemeColors: true));
  expect(shape!.type, ImageType.svg);
  return shape.colorFilter;
}

/// The color a named icon's glyph is painted in, under an icon theme of [iconThemeColor] (the
/// framework's fallback, black, when null).
Future<Color?> _namedIconColor(WidgetTester tester, Color? iconThemeColor) async {
  final widget = ImageUtils.buildIconWidget('search', size: 16);
  expect(widget, isNotNull);
  Widget tree = widget!;
  if (iconThemeColor != null) {
    tree = IconTheme(data: IconThemeData(color: iconThemeColor), child: tree);
  }
  await tester.pumpWidget(Directionality(textDirection: TextDirection.ltr, child: tree));
  final glyph = tester.widget<RichText>(
      find.descendant(of: find.byType(Icon), matching: find.byType(RichText)));
  return glyph.text.style?.color;
}

/// Builds the image as a Label does and reads back the painted pixel at its center.
Future<Color> _centerPixel(WidgetTester tester, Uint8List png, {int side = 16}) async {
  final widget = ImageUtils.buildVImage(
    _image(png),
    width: side.toDouble(),
    height: side.toDouble(),
    size: side.toDouble(),
    renderAsIcon: true,
  );
  expect(widget, isNotNull);
  return (await paintedIconPixels(tester, widget!, side: side))(side ~/ 2, side ~/ 2);
}

/// Renders the image through the async path a CTabItem uses, with no color of its own, and reads
/// back the painted pixel at its center.
Future<Color> _asyncCenterPixel(WidgetTester tester, Uint8List png, {int side = 16}) async {
  final widget = await tester.runAsync(() => ImageUtils.buildVImageAsync(
        _image(png),
        width: side.toDouble(),
        height: side.toDouble(),
        size: side.toDouble(),
        color: null,
        renderAsIcon: true,
      ));
  expect(widget, isNotNull);
  return (await paintedIconPixels(tester, widget!, side: side))(side ~/ 2, side ~/ 2);
}
