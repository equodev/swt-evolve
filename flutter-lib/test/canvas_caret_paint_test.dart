// A Canvas draws its Caret: a bar at the caret's bounds, or the caret's image, inverting what is under
// it as SWT's caret does, while the caret is visible and its display's focus caret, blinking at its
// blink rate.

import 'dart:convert';
import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/caret.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/caret_evolve.dart';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

/// 16x16, opaque RGB 0x3F3E85.
const _navyPng =
    'iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAGUlEQVR42mOwt2v9TwlmGDVg1IBRA4aLAQAFjwEfVHM4mwAAAABJRU5ErkJggg==';

VCaret _caret({bool visible = true, bool focused = true, int blinkRate = 500, VImage? image}) => VCaret()
  ..swt = 'Caret'
  ..id = 21
  ..style = SWT.NONE
  ..bounds = image != null ? _rect(30, 12, 16, 16) : _rect(30, 12, 2, 18)
  ..image = image
  ..visible = visible
  ..focused = focused
  ..blinkRate = blinkRate;

VCanvas _canvas(VCaret? caret) => VCanvas()
  ..swt = 'Canvas'
  ..id = 20
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = _rect(0, 0, 200, 100)
  ..caret = caret;

final _boundary = GlobalKey();

/// What is under the caret: a colour no inversion leaves alone.
const _ground = Color(0xFF2878C8);

Future<void> _pump(WidgetTester tester, VCaret? caret) async {
  await tester.pumpWidget(
    EvolveApp(
      theme: ThemeMode.light,
      contentWidget: RepaintBoundary(
        key: _boundary,
        child: ColoredBox(
          color: _ground,
          child: SizedBox(width: 200, height: 100, child: CanvasSwt<VCanvas>(value: _canvas(caret))),
        ),
      ),
    ),
  );
  await tester.pump();
}

Finder _bar() =>
    find.descendant(of: find.byWidgetPredicate((w) => w is CaretSwt), matching: find.byType(InvertingCaret));

/// The painted pixels of the canvas, read back as RGBA.
Future<(ByteData, int)> _pixels(WidgetTester tester) async {
  late ByteData bytes;
  late int width;
  await tester.runAsync(() async {
    final boundary = tester.renderObject(find.byKey(_boundary)) as RenderRepaintBoundary;
    final image = await boundary.toImage();
    width = image.width;
    bytes = (await image.toByteData(format: ui.ImageByteFormat.rawRgba))!;
  });
  return (bytes, width);
}

List<int> _rgb((ByteData, int) pixels, int x, int y) {
  final (bytes, width) = pixels;
  final offset = (y * width + x) * 4;
  return [bytes.getUint8(offset), bytes.getUint8(offset + 1), bytes.getUint8(offset + 2)];
}

/// Disposes the tree, and with it the blink timer.
Future<void> _unmount(WidgetTester tester) => tester.pumpWidget(const SizedBox());

void main() {
  testWidgets('a visible focus caret is drawn at its bounds', (tester) async {
    await _pump(tester, _caret());

    expect(_bar(), findsOneWidget);
    final canvasOrigin = tester.getTopLeft(find.byWidgetPredicate((w) => w is CanvasSwt));
    expect(tester.getRect(_bar()), Rect.fromLTWH(canvasOrigin.dx + 30, canvasOrigin.dy + 12, 2, 18));
    await _unmount(tester);
  });

  testWidgets('the bar inverts what is under it', (tester) async {
    await _pump(tester, _caret(blinkRate: 0));
    final pixels = await _pixels(tester);
    final under = _rgb(pixels, 100, 50);
    expect(under, [0x28, 0x78, 0xC8], reason: 'the ground shows through the canvas');
    expect(_rgb(pixels, 31, 20), [for (final c in under) 255 - c]);
    expect(_rgb(pixels, 29, 20), under, reason: 'the caret covers only its bounds');
    await _unmount(tester);
  });

  testWidgets('an image caret draws its image inverting what is under it', (tester) async {
    final image = VImage()..imageData = (VImageData()..data = base64Decode(_navyPng));
    await _pump(tester, _caret(blinkRate: 0, image: image));
    await tester.runAsync(() async {
      await precacheImage(MemoryImage(base64Decode(_navyPng)), tester.element(find.byKey(_boundary)));
    });
    await tester.pump();
    final pixels = await _pixels(tester);
    final under = _rgb(pixels, 100, 50);
    final navy = [0x3F, 0x3E, 0x85];
    expect(_rgb(pixels, 38, 20), [for (var i = 0; i < 3; i++) (under[i] - navy[i]).abs()]);
    await _unmount(tester);
  });

  testWidgets('a caret that is not the focus caret is not drawn', (tester) async {
    await _pump(tester, _caret(focused: false));
    expect(find.byWidgetPredicate((w) => w is CaretSwt), findsOneWidget);
    expect(_bar(), findsNothing);
    await _unmount(tester);
  });

  testWidgets('a hidden caret is not drawn', (tester) async {
    await _pump(tester, _caret(visible: false));
    expect(_bar(), findsNothing);
    await _unmount(tester);
  });

  testWidgets('the caret blinks at its blink rate', (tester) async {
    await _pump(tester, _caret(blinkRate: 500));
    expect(_bar(), findsOneWidget);
    await tester.pump(const Duration(milliseconds: 500));
    expect(_bar(), findsNothing);
    await tester.pump(const Duration(milliseconds: 500));
    expect(_bar(), findsOneWidget);
    await _unmount(tester);
  });

  testWidgets('a blink rate of zero keeps the caret drawn', (tester) async {
    await _pump(tester, _caret(blinkRate: 0));
    await tester.pump(const Duration(seconds: 2));
    expect(_bar(), findsOneWidget);
    await _unmount(tester);
  });

  testWidgets('a canvas without a caret draws none', (tester) async {
    await _pump(tester, null);
    expect(find.byWidgetPredicate((w) => w is CaretSwt), findsNothing);
    await _unmount(tester);
  });
}
