import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/impl/utils/image_utils.dart';

/// An Image an application filled with a GC (a colour swatch) shows what was drawn on it, not a
/// square in the theme's text colour.

const double _side = 12;

/// A swatch like ControlExample's `Tab.colorImage`: filled with [fill], a 1 px black border.
Future<ui.Image> _swatch(WidgetTester tester, Color fill) async =>
    (await tester.runAsync(() {
      final recorder = ui.PictureRecorder();
      final canvas = Canvas(recorder);
      canvas.drawRect(const Rect.fromLTWH(0, 0, _side, _side), Paint()..color = fill);
      canvas.drawRect(
        const Rect.fromLTWH(0.5, 0.5, _side - 1, _side - 1),
        Paint()
          ..color = const Color(0xFF000000)
          ..style = PaintingStyle.stroke
          ..strokeWidth = 1,
      );
      return recorder.endRecording().toImage(_side.toInt(), _side.toInt());
    }))!;

/// The swatch's render through [ImageUtils.buildVImageAsync], as a pixel reader.
Future<Color Function(int x, int y)> _render(WidgetTester tester, int ref, Color fill,
    {bool enabled = true}) async {
  ImageUtils.registerRemoteImage(ref, await _swatch(tester, fill));
  final image = VImage()
    ..remoteRef = ref
    ..width = _side.toInt()
    ..height = _side.toInt();
  final widget = (await tester.runAsync(
    () => ImageUtils.buildVImageAsync(image, size: _side, enabled: enabled),
  ))!;
  expect(widget, isNotNull);

  final key = GlobalKey();
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Center(
      child: RepaintBoundary(
        key: key,
        child: SizedBox(width: _side, height: _side, child: widget),
      ),
    ),
  ));
  await tester.pump();

  final boundary = key.currentContext!.findRenderObject()! as RenderRepaintBoundary;
  final bytes = (await tester.runAsync(() async {
    final shot = await boundary.toImage();
    return shot.toByteData(format: ui.ImageByteFormat.rawRgba);
  }))!;
  final width = boundary.size.width.toInt();
  final rgba = Uint8List.view(bytes.buffer);
  return (x, y) {
    final i = (y * width + x) * 4;
    return Color.fromARGB(rgba[i + 3], rgba[i], rgba[i + 1], rgba[i + 2]);
  };
}

/// Every pixel of the swatch: the 1 px border black, the inside [fill].
void _expectSwatch(Color Function(int x, int y) pixel, Color fill) {
  final last = _side.toInt() - 1;
  for (var y = 0; y <= last; y++) {
    for (var x = 0; x <= last; x++) {
      final border = x == 0 || y == 0 || x == last || y == last;
      expect(pixel(x, y), border ? const Color(0xFF000000) : fill, reason: 'pixel ($x, $y)');
    }
  }
}

void main() {
  setUp(ImageUtils.clearCache);

  testWidgets('a red swatch drawn with a GC paints red', (tester) async {
    _expectSwatch(await _render(tester, 71, const Color(0xFFFF0000)), const Color(0xFFFF0000));
  });

  testWidgets('a light grey swatch drawn with a GC paints its grey', (tester) async {
    _expectSwatch(await _render(tester, 72, const Color(0xFFF0F0F0)), const Color(0xFFF0F0F0));
  });

  testWidgets('a disabled swatch drawn with a GC is faded, still in its own colour', (tester) async {
    final pixel = (await _render(tester, 73, const Color(0xFFFF0000), enabled: false))(6, 6);
    expect(pixel, isNot(const Color(0xFFFF0000)), reason: 'disabled fades the image');
    expect((pixel.r * 255).round(), greaterThan((pixel.g * 255).round() + 64),
        reason: 'still red, not the disabled text colour: $pixel');
    expect(pixel.g, closeTo(pixel.b, 1 / 255));
  });
}
