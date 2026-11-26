import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/utils/image_utils.dart';

/// A render named (gcDispose's content key) like one already rasterised takes that one's pixels
/// instead of rasterising again.
void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  tearDown(RemoteRender.clearKeptRasters);

  ui.Picture fill(Color color) {
    final recorder = ui.PictureRecorder();
    ui.Canvas(recorder).drawRect(const Rect.fromLTWH(0, 0, 1, 30), Paint()..color = color);
    return recorder.endRecording();
  }

  var nextRef = 7000;
  ui.Image rasterOf(ui.Picture picture, int width, int height, int contentKey) {
    final ref = nextRef++;
    ImageUtils.registerRemotePicture(ref, picture, width, height, contentKey: contentKey);
    return ImageUtils.remoteRender(ref)!.rasteriseSync();
  }

  test('a render named like one already rasterised draws the same image', () {
    final first = rasterOf(fill(const Color(0xFF102030)), 1, 30, 4242);
    final second = rasterOf(fill(const Color(0xFF102030)), 1, 30, 4242);

    // The same image, not a clone: on the web renderers, disposing a render's clone of a kept image
    // blanked the images the other renders draw.
    expect(identical(second, first), isTrue);
  });

  testWidgets('a render disposed leaves the named image to the renders still drawing it', (tester) async {
    final kept = rasterOf(fill(const Color(0xFF102030)), 1, 30, 4343);
    final ref = nextRef;
    rasterOf(fill(const Color(0xFF102030)), 1, 30, 4343);

    ImageUtils.remoteRender(ref)!.dispose();
    await tester.pump();

    expect(kept.debugDisposed, isFalse);
  });

  testWidgets('a named image no longer kept is disposed once no render draws it', (tester) async {
    final ref = nextRef;
    final image = rasterOf(fill(const Color(0xFF102030)), 1, 30, 4444);

    RemoteRender.clearKeptRasters();
    await tester.pump();
    expect(image.debugDisposed, isFalse, reason: 'a render still draws it');

    ImageUtils.remoteRender(ref)!.dispose();
    await tester.pump();
    expect(image.debugDisposed, isTrue);
  });

  testWidgets('a named image no render draws stays kept until dropped', (tester) async {
    final ref = nextRef;
    final image = rasterOf(fill(const Color(0xFF102030)), 1, 30, 4545);

    ImageUtils.remoteRender(ref)!.dispose();
    await tester.pump();
    expect(image.debugDisposed, isFalse);
    expect(identical(rasterOf(fill(const Color(0xFF102030)), 1, 30, 4545), image), isTrue);

    ImageUtils.clearCache();
    await tester.pump();
    expect(image.debugDisposed, isFalse, reason: 'the render made last still draws it');
  });

  test('an unnamed render is rasterised on its own', () {
    final first = rasterOf(fill(const Color(0xFF102030)), 1, 30, 0);
    final second = rasterOf(fill(const Color(0xFF102030)), 1, 30, 0);

    expect(second.isCloneOf(first), isFalse);
  });

  test('a render of another name or size is rasterised on its own', () {
    final named = rasterOf(fill(const Color(0xFF102030)), 1, 30, 5151);

    expect(rasterOf(fill(const Color(0xFF102030)), 1, 30, 5152).isCloneOf(named), isFalse);
    expect(rasterOf(fill(const Color(0xFF102030)), 1, 28, 5151).isCloneOf(named), isFalse);
  });

  test('only a bounded number of named renders keep their pixels', () {
    for (var key = 1; key <= 200; key++) {
      rasterOf(fill(const Color(0xFF102030)), 1, 30, 90000 + key);
    }

    expect(RemoteRender.keptRasters, lessThanOrEqualTo(64));
  });

  test('clearing the image caches drops the kept pixels', () {
    rasterOf(fill(const Color(0xFF102030)), 1, 30, 6161);

    ImageUtils.clearCache();

    expect(RemoteRender.keptRasters, 0);
  });
}
