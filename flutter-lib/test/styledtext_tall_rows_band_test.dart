// The painted band follows the rows as laid out. Rows taller than the font (here, line spacing)
// make the document longer than the line count times the font's height, and the band keeps painting
// text past where that estimate ends.

import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/styledtext.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

const double _width = 300;
const double _height = 200;

final String _text = List.generate(200, (i) => 'line $i').join('\n');

VStyledText _scrolled(int topPixel) => VStyledText()
  ..swt = 'StyledText'
  ..id = 1
  ..style = SWT.MULTI
  ..enabled = true
  ..visible = true
  ..editable = false
  ..caretOffset = 0
  ..text = _text
  ..lineSpacing = 20
  ..topPixel = topPixel
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = _width.toInt()
    ..height = _height.toInt());

Future<int> _darkPixels(WidgetTester tester, GlobalKey boundary) async {
  final ui.Image image = (await tester.runAsync(
      () => tester.renderObject<RenderRepaintBoundary>(find.byKey(boundary)).toImage()))!;
  final bytes = (await tester.runAsync(() => image.toByteData(format: ui.ImageByteFormat.rawRgba)))!
      .buffer
      .asUint8List();
  int dark = 0;
  for (int i = 0; i < bytes.length; i += 4) {
    if (bytes[i] < 96 && bytes[i + 1] < 96 && bytes[i + 2] < 96 && bytes[i + 3] > 0) dark++;
  }
  return dark;
}

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  testWidgets('text near the end of a document with tall rows is painted', (tester) async {
    final boundary = GlobalKey();
    // 200 rows of the 12px test font plus 20px spacing end near 6400px; 200 x 12px would end at 2400.
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: RepaintBoundary(
          key: boundary,
          child: SizedBox(
            width: _width,
            height: _height,
            child: StyledTextSwt<VStyledText>(value: _scrolled(6000)),
          ),
        ),
      ),
    ));
    await tester.pump();

    expect(tester.takeException(), isNull);
    expect(await _darkPixels(tester, boundary), greaterThan(0),
        reason: 'the rows under the view are drawn');
  });
}
