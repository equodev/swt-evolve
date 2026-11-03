import 'dart:convert';
import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/utils/image_utils.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

/// An application bitmap shown as a control's icon (a Label's, a Button's, an Item's) takes the
/// theme tint only while it reads as a monochrome glyph. Color artwork — an application logo in a
/// dialog — keeps its own colors; tinting it would flatten it into a solid silhouette.

/// 16x16, opaque #3F3E85: color artwork.
const _navyPng =
    'iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAGUlEQVR42mOwt2v9TwlmGDVg1IBRA4aLAQAFjwEfVHM4mwAAAABJRU5ErkJggg==';

/// 16x16, opaque #333333: a monochrome glyph.
const _greyPng =
    'iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAGUlEQVR42mMwNjb+TwlmGDVg1IBRA4aLAQDChZgQoP+T9QAAAABJRU5ErkJggg==';

/// 80x80, opaque #3F3E85: color artwork too large to have been decoded for the check.
const _largeNavyPng =
    'iVBORw0KGgoAAAANSUhEUgAAAFAAAABQCAYAAACOEfKtAAAAfklEQVR42u3QQREAAAQAMMlIJzcdnOceK7Co7OEuJAgUKFAgAgUKFIhAgQIFIlCgQIEIFChQIAIFChSIQIECBSJQoECBCBQoUCACBQoUiECBAgUKFCFQoECBCBQoUCACBQoUiECBAgUiUKBAgQgUKFAgAgUKFIhAgQIFIvDPAvY3G++aMRYMAAAAAElFTkSuQmCC';

VImage _image(String png) => VImage()..imageData = (VImageData()..data = base64Decode(png));

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  testWidgets('color artwork keeps its colors', (tester) async {
    expect(preserveIconColors, isFalse);
    final center = await _centerPixel(tester, _navyPng, 16);
    expect(center, const Color(0xFF3F3E85));
  });

  testWidgets('color artwork larger than a glyph keeps its colors', (tester) async {
    final center = await _centerPixel(tester, _largeNavyPng, 80);
    expect(center, const Color(0xFF3F3E85));
  });

  testWidgets('a monochrome glyph still takes the theme tint', (tester) async {
    final center = await _centerPixel(tester, _greyPng, 16);
    expect(center, isNot(const Color(0xFF333333)));
    expect(center, AppColors.getColor(true));
  });
}

/// Builds the image as a Label does and reads back the painted pixel at its center.
Future<Color> _centerPixel(WidgetTester tester, String png, int side) async {
  final boundaryKey = GlobalKey();
  final widget = ImageUtils.buildVImage(
    _image(png),
    width: side.toDouble(),
    height: side.toDouble(),
    size: side.toDouble(),
    renderAsIcon: true,
  );
  expect(widget, isNotNull);
  await tester.pumpWidget(Directionality(
    textDirection: TextDirection.ltr,
    child: Center(
      child: RepaintBoundary(
        key: boundaryKey,
        child: SizedBox(width: side.toDouble(), height: side.toDouble(), child: widget),
      ),
    ),
  ));
  await tester.runAsync(() async {
    await precacheImage(MemoryImage(base64Decode(png)), tester.element(find.byKey(boundaryKey)));
  });
  await tester.pumpAndSettle();

  // The widget decodes the bitmap on the real clock, which pumpAndSettle does not wait for: until
  // it has, nothing is painted. A wrong color is painted too, so it still fails.
  var center = await _readCenter(tester, boundaryKey, side);
  for (var i = 0; i < 100 && center.a == 0; i++) {
    await tester.runAsync(() => Future<void>.delayed(const Duration(milliseconds: 20)));
    await tester.pump();
    center = await _readCenter(tester, boundaryKey, side);
  }
  return center;
}

Future<Color> _readCenter(WidgetTester tester, GlobalKey boundaryKey, int side) async {
  late ByteData bytes;
  await tester.runAsync(() async {
    final boundary =
        tester.renderObject(find.byKey(boundaryKey)) as RenderRepaintBoundary;
    final image = await boundary.toImage();
    bytes = (await image.toByteData(format: ui.ImageByteFormat.rawRgba))!;
  });
  final offset = ((side ~/ 2) * side + side ~/ 2) * 4;
  return Color.fromARGB(bytes.getUint8(offset + 3), bytes.getUint8(offset),
      bytes.getUint8(offset + 1), bytes.getUint8(offset + 2));
}
