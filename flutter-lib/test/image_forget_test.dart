import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/gen/image.dart';

/// The client keeps every described image so Java can name it later. An application that makes a
/// fresh Image per paint and disposes it would grow that store by a frame's pixels on every paint,
/// so an image Java disposed has to leave it.
void main() {
  VImage describe(int id) => VImage.fromJson({
        'id': id,
        'swt': 'Image',
        'width': 64,
        'height': 64,
        'imageData': {'width': 64, 'height': 64, 'depth': 32, 'data': List.filled(64 * 64 * 4, 7)},
      });

  VImage name(int id) => VImage.fromJson({'id': id, 'swt': 'Image', '_r': 1});

  Future<void> nextTurn() => Future<void>.delayed(Duration.zero);

  test('images disposed in Java are not held once forgotten', () async {
    final before = heldImageCount;
    for (var paint = 0; paint < 50; paint++) {
      describe(510000 + paint);
      forgetImage(510000 + paint);
    }
    await nextTurn();

    expect(heldImageCount, before,
        reason: 'every disposed image stayed held, so a per-paint image grows the client forever');
    expect(name(510000).isReference, isTrue);
  });

  test('a frame applied right after the release still resolves the image', () async {
    final described = describe(520001);
    forgetImage(520001);

    expect(identical(name(520001), described), isTrue,
        reason: 'a draw queued ahead of the release would render nothing');
    await nextTurn();
  });

  test('a release does not drop a newer image that took the same id', () async {
    describe(530001);
    forgetImage(530001);
    final newer = describe(530001);
    await nextTurn();

    expect(identical(name(530001), newer), isTrue,
        reason: 'the release was for the image disposed before this one was described');
  });
}
