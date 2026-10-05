import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/gen/cursor.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/impl/image_mouse_cursor.dart';

/// An SWT cursor made from an image names no system cursor; on the Windows desktop the engine can
/// show the image itself, everywhere else it falls back to the arrow.
void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  Future<Uint8List> onePixelPng(Color color) async {
    final recorder = ui.PictureRecorder();
    Canvas(recorder).drawRect(const Rect.fromLTWH(0, 0, 1, 1), Paint()..color = color);
    final image = await recorder.endRecording().toImage(1, 1);
    final png = await image.toByteData(format: ui.ImageByteFormat.png);
    return png!.buffer.asUint8List();
  }

  VCursor imageCursor(Uint8List png, {int id = 7}) => VCursor()
    ..hotspotX = 3
    ..hotspotY = 1
    ..image = (VImage()
      ..id = id
      ..imageData = (VImageData()
        ..data = png
        ..width = 1
        ..height = 1));

  tearDown(() => debugDefaultTargetPlatformOverride = null);

  test('rgbaToBgra swaps red and blue and keeps green and alpha', () {
    expect(rgbaToBgra(Uint8List.fromList([1, 2, 3, 4])), [3, 2, 1, 4]);
  });

  test('a system cursor arrives without a hotspot', () {
    // Java leaves default values out of a nested cursor.
    final cursor = VCursor.fromJson({'cursorStyle': 21});
    expect([cursor.cursorStyle, cursor.hotspotX, cursor.image], [21, null, null]);
  });

  test('a cursor without an image is not an image cursor', () {
    debugDefaultTargetPlatformOverride = TargetPlatform.windows;
    expect(imageMouseCursorOf(VCursor()..cursorStyle = 21), isNull);
  });

  test('off the Windows desktop an image cursor has nothing to show it with', () async {
    debugDefaultTargetPlatformOverride = TargetPlatform.macOS;
    expect(imageMouseCursorOf(imageCursor(await onePixelPng(Colors.red))), isNull);
  });

  testWidgets('on Windows the image is registered once and shown by name', (tester) async {
    debugDefaultTargetPlatformOverride = TargetPlatform.windows;
    final calls = <MethodCall>[];
    tester.binding.defaultBinaryMessenger.setMockMethodCallHandler(SystemChannels.mouseCursor, (
      call,
    ) async {
      calls.add(call);
      return null;
    });
    final png = await tester.runAsync(() => onePixelPng(const Color(0xFF112233)));
    final cursor = imageMouseCursorOf(imageCursor(png!))!;

    // What the mouse tracker does when the pointer enters a region showing this cursor.
    await tester.runAsync(() async {
      // ignore: invalid_use_of_protected_member
      await cursor.createSession(0).activate();
      // ignore: invalid_use_of_protected_member
      await cursor.createSession(0).activate();
    });

    final created = calls.where((c) => c.method == 'createCustomCursor/windows').toList();
    expect(created, hasLength(1));
    final args = created.single.arguments as Map;
    expect(args['name'], cursor.name);
    expect(args['buffer'], [0x33, 0x22, 0x11, 0xFF]);
    expect([args['width'], args['height'], args['hotX'], args['hotY']], [1, 1, 3.0, 1.0]);
    expect(
      calls
          .where((c) => c.method == 'setCustomCursor/windows')
          .map((c) => (c.arguments as Map)['name']),
      [cursor.name, cursor.name],
    );
    debugDefaultTargetPlatformOverride = null;
  });
}
