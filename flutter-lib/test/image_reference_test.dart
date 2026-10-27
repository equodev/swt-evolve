import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';

/// An image arrives once and is named after that, so the client has to answer a name with the
/// image it already holds. A name it cannot answer would render as nothing at all.
void main() {
  test('an image named after being described resolves to the one held', () {
    final described = VImage.fromJson({
      'id': 4242,
      'swt': 'Image',
      'width': 16,
      'height': 16,
      'imageData': {'width': 16, 'height': 16, 'depth': 24},
    });
    expect(described.imageData, isNotNull);

    final named = VImage.fromJson({'id': 4242, 'swt': 'Image', '_r': 1});

    expect(identical(named, described), isTrue,
        reason: 'a named image must resolve to the one already held, or it draws nothing');
    expect(named.imageData, isNotNull);
  });

  test('an image with nothing to draw is not held in place of a real one', () {
    // A render-backed image is resolved through the render cache, not from here.
    final remote = VImage.fromJson({'id': 99, 'swt': 'Image', 'remoteRef': 7});
    expect(remote.imageData, isNull);

    final named = VImage.fromJson({'id': 99, 'swt': 'Image', '_r': 1});
    expect(identical(named, remote), isFalse);
    expect(named.isReference, isTrue,
        reason: 'a name nothing answers stays a name, so a caller can tell it apart from an image');
  });
}
