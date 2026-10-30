// Every StyledText lays its lines out through one shared cache, keyed by a line's text and a token
// for the widget's style. Two editors in different fonts hold lines with the same text (an empty
// line, a lone brace): each has to be laid out in its own font, or Java places that editor's lines,
// its caret and its clicks by the other one's heights.

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

const _text = 'first\n\n}\nlast';

VFont _font(int height) => VFont()
  ..fontData = [
    VFontData()
      ..name = 'Ahem'
      ..height = height
      ..style = SWT.NORMAL
  ];

VStyledText _value(int id, int fontHeight) => VStyledText()
  ..swt = 'StyledText'
  ..id = id
  ..font = _font(fontHeight)
  // Line metrics too small to matter, so a line is as tall as its font lays it out.
  ..renderer = (VStyledTextRenderer()
    ..ascent = 1
    ..descent = 1)
  ..style = SWT.MULTI | SWT.V_SCROLL
  ..enabled = true
  ..editable = true
  ..caretOffset = 0
  ..text = _text
  ..wordWrap = false
  ..bounds = (VRectangle()
    ..width = 400
    ..height = 300);

List<double> _rowHeights() => [
      for (final row in (StyledTextImpl.debugLastGeometry!['lines'] as List).cast<Map<String, dynamic>>())
        (row['h'] as num).toDouble(),
    ];

Future<List<double>> _laidOut(WidgetTester tester, VStyledText v) async {
  StyledTextImpl.debugLastGeometry = null;
  final app = EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
        width: 400, height: 300, child: StyledTextSwt<VStyledText>(key: GlobalKey<StyledTextImpl>(), value: v)),
  );
  // The table goes out after the frame.
  await tester.pumpWidget(app);
  await tester.pumpWidget(app);
  await tester.pump(Duration.zero);
  return _rowHeights();
}

void main() {
  testWidgets('two editors in different fonts lay the same lines out each in its own font', (tester) async {
    final small = await _laidOut(tester, _value(1, 10));
    final large = await _laidOut(tester, _value(2, 30));

    expect(large, hasLength(small.length));
    for (var i = 0; i < small.length; i++) {
      expect(large[i], greaterThan(small[i]), reason: 'line $i, laid out in its own font');
    }
  });

  testWidgets('an editor in the same font as another reuses the lines that one laid out', (tester) async {
    debugResetLineLayoutCache();
    TextShape.debugLayoutLineCalls = 0;
    final first = await _laidOut(tester, _value(1, 10));
    final firstCalls = TextShape.debugLayoutLineCalls;
    expect(firstCalls, greaterThan(0));

    TextShape.debugLayoutLineCalls = 0;
    final second = await _laidOut(tester, _value(2, 10));

    expect(second, first);
    expect(TextShape.debugLayoutLineCalls, 0, reason: 'every line is in the cache, under the same style');
  });
}
