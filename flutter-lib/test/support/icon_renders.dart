// Icons as Java sends them, and the wait for their asynchronous renders.

import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/rendering.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/utils/image_utils.dart';

/// A 16x16 PNG, the bytes of a `VImageData`; filled with [color] when given, so two colours give
/// two different images.
Future<Uint8List> iconPng(WidgetTester tester, {Color? color}) async {
  final bytes = await tester.runAsync(() async {
    final ui.Image image;
    if (color == null) {
      image = await createTestImage(width: 16, height: 16);
    } else {
      final recorder = ui.PictureRecorder();
      Canvas(recorder).drawRect(const Rect.fromLTWH(0, 0, 16, 16), Paint()..color = color);
      image = await recorder.endRecording().toImage(16, 16);
    }
    final data = await image.toByteData(format: ui.ImageByteFormat.png);
    image.dispose();
    return data!.buffer.asUint8List();
  });
  return bytes!;
}

/// The colour painted at the centre of [icon], an `Image`. A bitmap decodes on the real clock, so
/// this first waits until the image has decoded.
Future<Color> paintedColorAtCenter(WidgetTester tester, Finder icon) async {
  final raw = find.descendant(of: icon, matching: find.byType(RawImage));
  for (var i = 0; i < 100 && tester.widget<RawImage>(raw).image == null; i++) {
    await tester.runAsync(() => Future<void>.delayed(const Duration(milliseconds: 20)));
    await tester.pump();
  }
  final boundary = tester.renderObject<RenderRepaintBoundary>(
      find.ancestor(of: icon, matching: find.byType(RepaintBoundary)).first);
  final center = boundary.globalToLocal(tester.getCenter(icon));
  final color = await tester.runAsync(() async {
    final image = await boundary.toImage();
    final bytes = (await image.toByteData(format: ui.ImageByteFormat.rawRgba))!;
    final o = (center.dy.floor() * image.width + center.dx.floor()) * 4;
    image.dispose();
    return Color.fromARGB(
        bytes.getUint8(o + 3), bytes.getUint8(o), bytes.getUint8(o + 1), bytes.getUint8(o + 2));
  });
  return color!;
}

/// The icon render [item] shows: the widget its image `FutureBuilder` builds. A render is built once
/// per image, size and colour, so a recoloured icon is a different instance.
Widget shownIconRender(WidgetTester tester, Finder item) {
  final builder = find.descendant(of: item, matching: find.byType(FutureBuilder<Widget?>));
  Widget? shown;
  tester.element(builder).visitChildElements((child) => shown = child.widget);
  return shown!;
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

/// Settles once no icon render is pending and every bitmap shown has decoded. A bitmap render
/// resolves only after its bytes decode, on the real clock, which pumpAndSettle does not advance.
Future<void> settleIconRenders(WidgetTester tester) async {
  bool loading() =>
      tester
          .widgetList<FutureBuilder<Widget?>>(find.byType(FutureBuilder<Widget?>))
          .any((builder) => ImageUtils.resolvedRender(builder.future!) == null) ||
      tester.widgetList<RawImage>(find.byType(RawImage)).any((raw) => raw.image == null);
  await tester.pump();
  for (var i = 0; i < 100 && loading(); i++) {
    await tester.runAsync(() => Future<void>.delayed(const Duration(milliseconds: 10)));
    await tester.pump();
  }
  await tester.pumpAndSettle();
}
