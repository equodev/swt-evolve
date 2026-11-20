// A line's tabs are placed by measuring the text before each of them; the text after the last tab
// places nothing. An editor of indented code lays out its lines without measuring any run on its
// own, and measures the width of a tab once rather than once per line.

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

const _lines = 40;

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

Future<void> _mount(WidgetTester tester, String text) async {
  final app = EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
        width: 400,
        height: 1000,
        child: StyledTextSwt<VStyledText>(key: GlobalKey<StyledTextImpl>(), value: _value(text))),
  );
  await tester.pumpWidget(app);
  await tester.pump(Duration.zero);
}

void main() {
  testWidgets('indented lines measure no run, and a tab once', (tester) async {
    final code = [for (var i = 0; i < _lines; i++) '\t\tint value$i = compute($i);'].join('\n');
    TextShape.debugRunsMeasured = 0;
    await _mount(tester, code);
    expect(TextShape.debugRunsMeasured, lessThanOrEqualTo(2),
        reason: 'the tab width, once per shape, and no text after a line\'s last tab');
  });
}
