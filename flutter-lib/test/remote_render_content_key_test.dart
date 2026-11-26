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

  test('a render named like one already rasterised takes its pixels', () {
    final first = rasterOf(fill(const Color(0xFF102030)), 1, 30, 4242);
    final second = rasterOf(fill(const Color(0xFF102030)), 1, 30, 4242);

    expect(second.isCloneOf(first), isTrue);
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
