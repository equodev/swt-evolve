// Zooming an editor makes every style anew; a font it was shown in before is not measured again.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/impl/styledtext_evolve.dart';

void main() {
  String document() => List.generate(1000, (i) => '    final total$i = prices.get(sku); // $i').join('\n');

  TextShape shape(String text, double fontSize) {
    TextStyle style([Color? color]) => TextStyle(fontSize: fontSize, fontFamily: 'Liberation Mono', color: color);
    final ranges = [
      for (var line = 0, offset = 0; line < 1000; offset += text.split('\n')[line].length + 1, line++)
        StyleRange(start: offset + 4, end: offset + 9, style: style(const Color(0xFF7F0055))),
    ];
    return TextShape(text, Offset.zero, style(), null, null, false, const Size(600, 400), true, 1, null,
        TextEditingState(characterRanges: ranges), null, fontSize + 2, 4, null, const LineProperties(), 0, null,
        TextDirection.ltr, false, const [], const [], 0, false);
  }

  test('zooming back to a font measures none of its styles again', () {
    final text = document();
    shape(text, 12).computeGeometryTable(visibleTop: 0, visibleBottom: 400);
    shape(text, 14).computeGeometryTable(visibleTop: 0, visibleBottom: 400);
    final measured = TextShape.debugStylesMeasured;

    shape(text, 12).computeGeometryTable(visibleTop: 0, visibleBottom: 400);
    shape(text, 14).computeGeometryTable(visibleTop: 0, visibleBottom: 400);
    expect(TextShape.debugStylesMeasured, measured);
  });
}
