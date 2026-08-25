import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

/// A GC blit draws a bitmap the application owns, at a size it chose. The bundled icon set is
/// resolved by the bare filename stem, so a widget library that ships its own `left.png` — a 9x9
/// column-group indicator — matched Evolve's `assets/icons/left.svg`, a 24x24 panel glyph, and was
/// painted at the SVG's own size and flat-tinted: a solid black bar in place of a small triangle.
/// So the bundled set only reaches a blit when the application asks for it with
/// `gc_icons_replacement`; an explicit Java-side substitution (svgContent) applies either way.

/// The 9x9 RGBA indicator exactly as Java serializes it: a left-pointing triangle whose shape
/// lives entirely in the alpha channel (every pixel is black, so losing alpha yields a black box).
const _indicatorPng =
    'iVBORw0KGgoAAAANSUhEUgAAAAkAAAAJCAYAAADgkQYQAAAAO0lEQVR4nGNgwA4YgbgHhxwYsALxE'
    'iD+j0sBDxDvgirAqkgMiM8gKcCqaAeaAvJNIspNMEDQdzCAEk4AKNMWZTaEExwAAAAASUVORK5CYII=';

VGCDrawImageImageintintintintintintintint _naturalSizeBlitAt(int x, int y) =>
    VGCDrawImageImageintintintintintintintint(
      destX: x,
      destY: y,
      // What GC.drawImage(image, x, y) sends: no explicit geometry, so the image's own size wins.
      destWidth: -1,
      destHeight: -1,
      srcX: 0,
      srcY: 0,
      srcWidth: -1,
      srcHeight: -1,
    );

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  test('gc_icons_replacement is off unless the application turns it on', () {
    expect(gcIconsReplacement, isFalse);
    applyConfigFlags(ConfigFlags()..gc_icons_replacement = true);
    expect(gcIconsReplacement, isTrue);
  });

  test('a GC blit keeps the application bitmap when a bundled icon shares its filename', () async {
    final image = VImage()
      ..filename = 'left'
      ..imageData = (VImageData()
        ..width = 9
        ..height = 9
        ..depth = 24
        ..data = base64Decode(_indicatorPng));

    final shape = await ImageShape.fromVImageDetailed(
        image, _naturalSizeBlitAt(529, 5), null);

    expect(shape.type, ImageType.raster,
        reason: 'the application bitmap must be drawn, not a bundled SVG icon');
    expect(shape.image, isNotNull);
    expect(shape.destRect.width, 9);
    expect(shape.destRect.height, 9);
    // Tinting is for a resolved icon; an application bitmap keeps its own colors.
    expect(shape.colorFilter, isNull);
  });

  test('an explicit Java-side svgContent still replaces the blit', () async {
    final image = VImage()
      ..filename = 'left'
      ..svgContent =
          '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="24" height="24">'
          '<rect x="3" y="3" width="6" height="18"/></svg>'
      ..imageData = (VImageData()
        ..width = 9
        ..height = 9
        ..depth = 24
        ..data = base64Decode(_indicatorPng));

    final shape = await ImageShape.fromVImageDetailed(
        image, _naturalSizeBlitAt(529, 5), null);

    expect(shape.type, ImageType.svg);
  });

  test('with gc_icons_replacement on, the bundled icon stands in', () async {
    applyConfigFlags(ConfigFlags()..gc_icons_replacement = true);
    final image = VImage()
      ..filename = 'left'
      ..imageData = (VImageData()
        ..width = 9
        ..height = 9
        ..depth = 24
        ..data = base64Decode(_indicatorPng));

    final shape = await ImageShape.fromVImageDetailed(
        image, _naturalSizeBlitAt(529, 5), null);

    expect(shape.type, ImageType.svg,
        reason: 'the application opted in, so assets/icons/left.svg draws');
  });
}
