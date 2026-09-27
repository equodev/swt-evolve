// A released render ref may still be held by a committed frame; a picture has no clone(), so
// releasing the ref must not dispose it.

import 'dart:ui' as ui;

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/utils/image_utils.dart';

ui.Picture _picture() {
  final recorder = ui.PictureRecorder();
  ui.Canvas(recorder).drawRect(
      const ui.Rect.fromLTWH(0, 0, 8, 8), ui.Paint()..color = const ui.Color(0xFF00FF00));
  return recorder.endRecording();
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('a render still draws after its ref has been released', () async {
    const ref = 4242;
    final picture = _picture();
    ImageUtils.registerRemotePicture(ref, picture, 8, 8);
    final held = ImageUtils.remoteRender(ref)!.picture;
    expect(held, isNotNull);

    ImageUtils.releaseRemoteImage(ref);
    // The release is deferred by a turn, so let it run.
    await Future<void>.delayed(Duration.zero);
    await Future<void>.delayed(Duration.zero);

    expect(ImageUtils.remoteRender(ref), isNull,
        reason: 'the ref must stop resolving once Java has released it');

    // What a committed shape still holds has to remain drawable.
    final recorder = ui.PictureRecorder();
    ui.Canvas(recorder).drawPicture(held!);
    expect(recorder.endRecording(), isNotNull);
  });
}
