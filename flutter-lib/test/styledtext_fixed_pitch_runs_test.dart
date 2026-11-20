// In a fixed-pitch font, the text before a tab is as wide as its length in columns: the tab is placed
// without laying that text out, at exactly the width laying it out gives.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/font.dart';
import 'package:swtflutter/src/gen/fontdata.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/styledtext.dart';
import 'package:swtflutter/src/gen/styledtextrenderer.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/styledtext_evolve.dart';

double _laidOut(String run, TextStyle style) {
  final tp = TextPainter(text: TextSpan(text: run, style: style), textDirection: TextDirection.ltr)..layout();
  final width = tp.width;
  tp.dispose();
  return width;
}

VStyledText _value(String text) => VStyledText()
  ..swt = 'StyledText'
  ..id = 1
  ..font = (VFont()
    ..fontData = [
      VFontData()
        ..name = 'Ahem'
        ..height = 10
        ..style = SWT.NORMAL
    ])
  ..renderer = (VStyledTextRenderer()
    ..ascent = 1
    ..descent = 1)
  ..style = SWT.MULTI | SWT.V_SCROLL
  ..enabled = true
  ..editable = true
  ..caretOffset = 0
  ..text = text
  ..wordWrap = false
  ..tabs = 4
  ..bounds = (VRectangle()
    ..width = 400
    ..height = 1000);

void main() {
  testWidgets('a run is as wide from its length as laid out', (tester) async {
    for (final size in [10.0, 13.0, 14.7]) {
      final style = TextStyle(fontFamily: 'Ahem', fontSize: size);
      for (final run in ['Line 12', 'int x = 5;   ', '  a', 'x', '{}[]()<>!?', 'Line 99999']) {
        expect(TextShape.fixedPitchWidth(run, style), closeTo(_laidOut(run, style), 1e-3), reason: '"$run" at $size');
      }
    }
  });

  testWidgets('a font that spaces words apart, or text past ASCII, is laid out', (tester) async {
    expect(TextShape.fixedPitchWidth('a b', const TextStyle(fontFamily: 'Ahem', fontSize: 10, wordSpacing: 3)), isNull);
    expect(TextShape.fixedPitchWidth('naïve', const TextStyle(fontFamily: 'Ahem', fontSize: 10)), isNull);
  });

  testWidgets('a tab after text in the middle of a line measures no run', (tester) async {
    final text = [for (var i = 0; i < 40; i++) 'Line $i\tvalue'].join('\n');
    TextShape.debugRunsMeasured = 0;
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
          width: 400,
          height: 1000,
          child: StyledTextSwt<VStyledText>(key: GlobalKey<StyledTextImpl>(), value: _value(text))),
    ));
    await tester.pump(Duration.zero);
    expect(TextShape.debugRunsMeasured, 0);
  });
}
