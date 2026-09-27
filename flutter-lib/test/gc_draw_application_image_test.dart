import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';
import 'package:swtflutter/src/impl/utils/image_utils.dart';

// 10x10 PNGs encoded by SWT's ImageLoader, as Java sends them for `new Image(display, imageData)`.
const _png8bpp =
    'iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAIAAAACUFjqAAAAEUlEQVR4nGM4wDAKSAMHGBgA6zYBgWnoDYMAAAAASUVORK5CYII=';
const _png24bpp =
    'iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAIAAAACUFjqAAAAEUlEQVR4nGP4zzAKSAPAEAMAOA0B/w2tcokAAAAASUVORK5CYII=';

VImage _image(String base64Png, int depth) => VImage.fromJson({
      'id': 7,
      'swt': 'Image',
      'width': 10,
      'height': 10,
      'imageData': {
        'width': 10,
        'height': 10,
        'depth': depth,
        'data': base64Png,
      },
    });

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('an application image decodes to pixels', () async {
    for (final e in {8: _png8bpp, 24: _png24bpp}.entries) {
      final decoded = await ImageUtils.decodeVImageToUIImage(_image(e.value, e.key));
      expect(decoded, isNotNull, reason: 'depth ${e.key} did not decode');
      expect(decoded!.width, 10);
    }
  });

  test('GC#drawImage of an application image yields something to draw', () async {
    for (final e in {8: _png8bpp, 24: _png24bpp}.entries) {
      final opArgs = VGCDrawImageImageintintintintintintintint(
        destX: 0, destY: 0, destWidth: -1, destHeight: -1,
        srcX: 0, srcY: 0, srcWidth: -1, srcHeight: -1,
      )..image = _image(e.value, e.key);

      final shape = await ImageShape.fromVImageDetailed(opArgs.image!, opArgs, null);
      expect(shape.image, isNotNull,
          reason: 'depth ${e.key}: drawImage produced a shape with nothing in it');
      expect(shape.srcRect, isNotNull, reason: 'depth ${e.key}: no source rectangle');
      expect(shape.destRect.width, 10, reason: 'depth ${e.key}: wrong destination size');
    }
  });
}
