// Icons as Java sends them, and the wait for their asynchronous renders.

import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/rendering.dart';
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

/// Paints [icon] in a [side] x [side] box and returns a reader of its painted pixels. A bitmap
/// decodes on the real clock, which pumpAndSettle does not wait for, so this waits until something
/// is painted; a wrong color is painted too, so a wrong render still fails.
Future<Color Function(int x, int y)> paintedIconPixels(WidgetTester tester, Widget icon,
    {required int side}) async {
  final boundaryKey = GlobalKey();
  await tester.pumpWidget(Directionality(
    textDirection: TextDirection.ltr,
    child: Center(
      child: RepaintBoundary(
        key: boundaryKey,
        child: SizedBox(width: side.toDouble(), height: side.toDouble(), child: icon),
      ),
    ),
  ));
  await tester.pumpAndSettle();

  late ByteData bytes;
  Future<void> capture() => tester.runAsync(() async {
        final boundary = tester.renderObject(find.byKey(boundaryKey)) as RenderRepaintBoundary;
        final image = await boundary.toImage();
        bytes = (await image.toByteData(format: ui.ImageByteFormat.rawRgba))!;
        image.dispose();
      });
  bool painted() {
    for (var i = 3; i < bytes.lengthInBytes; i += 4) {
      if (bytes.getUint8(i) != 0) return true;
    }
    return false;
  }

  await capture();
  for (var i = 0; i < 100 && !painted(); i++) {
    await tester.runAsync(() => Future<void>.delayed(const Duration(milliseconds: 20)));
    await tester.pump();
    await capture();
  }
  return (x, y) {
    final o = (y * side + x) * 4;
    return Color.fromARGB(
        bytes.getUint8(o + 3), bytes.getUint8(o), bytes.getUint8(o + 1), bytes.getUint8(o + 2));
  };
}
