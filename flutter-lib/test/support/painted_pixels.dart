import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/rectangle.dart';

/// What a widget painted, as rows of r, g, b, a, and the theme it was painted in.
class PaintedPixels {
  final ByteData data;
  final int width;
  final ThemeData theme;

  const PaintedPixels(this.data, this.width, this.theme);

  /// The r, g, b of the pixel at [x], [y].
  List<int> at(int x, int y) {
    final i = (y * width + x) * 4;
    return [data.getUint8(i), data.getUint8(i + 1), data.getUint8(i + 2)];
  }
}

/// Paints [child] at the top left of an EvolveApp in a [width] x [height] box and reads it back.
Future<PaintedPixels> paintPixels(WidgetTester tester, Widget child,
    {required int width, required int height, ThemeMode mode = ThemeMode.light}) async {
  final key = GlobalKey();
  await tester.pumpWidget(
    EvolveApp(
      theme: mode,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: RepaintBoundary(
          key: key,
          child: SizedBox(width: width.toDouble(), height: height.toDouble(), child: child),
        ),
      ),
    ),
  );
  await tester.pumpAndSettle();
  final boundary = key.currentContext!.findRenderObject()! as RenderRepaintBoundary;
  late ByteData data;
  await tester.runAsync(() async {
    final ui.Image image = await boundary.toImage();
    data = (await image.toByteData(format: ui.ImageByteFormat.rawRgba))!;
    image.dispose();
  });
  return PaintedPixels(data, width, Theme.of(key.currentContext!));
}

/// The r, g, b of [c].
List<int> rgbOf(Color c) => [(c.r * 255).round(), (c.g * 255).round(), (c.b * 255).round()];

VColor vRgb(List<int> c) => VColor()
  ..alpha = 255
  ..red = c[0]
  ..green = c[1]
  ..blue = c[2];

VRectangle vRect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;
