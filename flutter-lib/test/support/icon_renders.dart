// Icons as Java sends them, and the wait for their asynchronous renders.

import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';

/// A 16x16 PNG, the bytes of a `VImageData`.
Future<Uint8List> iconPng(WidgetTester tester) async {
  final bytes = await tester.runAsync(() async {
    final image = await createTestImage(width: 16, height: 16);
    final data = await image.toByteData(format: ui.ImageByteFormat.png);
    return data!.buffer.asUint8List();
  });
  return bytes!;
}

/// The icon render [item] shows: the widget its image `FutureBuilder` builds. A render is built once
/// per image, size and colour, so a recoloured icon is a different instance.
Widget shownIconRender(WidgetTester tester, Finder item) {
  final builder = find.descendant(of: item, matching: find.byType(FutureBuilder<Widget?>));
  Widget? shown;
  tester.element(builder).visitChildElements((child) => shown = child.widget);
  return shown!;
}

/// Lets the icon renders started so far complete, then pumps the frames that draw them.
Future<void> letIconRendersLand(WidgetTester tester) async {
  await tester.runAsync(() => Future<void>.delayed(const Duration(milliseconds: 100)));
  for (var i = 0; i < 4; i++) {
    await tester.pump(const Duration(milliseconds: 50));
  }
}
