// Every StyledText keys its line layouts in one shared cache. A line one editor measured in its font
// must not be taken by another editor in a different font.

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
import 'package:swtflutter/src/impl/widget_config.dart';

VStyledText _editor(int id, String text, int fontHeight) => VStyledText()
  ..swt = 'StyledText'
  ..id = id
  ..style = SWT.MULTI
  ..enabled = true
  ..visible = true
  ..editable = true
  ..caretOffset = 0
  ..text = text
  ..renderer = (VStyledTextRenderer()
    ..regularFont = (VFont()
      ..fontData = [
        VFontData()
          ..name = 'Ahem'
          ..height = fontHeight
          ..style = 0
      ]))
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 300
    ..height = 150);

Widget _app(List<VStyledText> editors) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Column(children: [
        for (final editor in editors)
          SizedBox(width: 300, height: 150, child: StyledTextSwt<VStyledText>(value: editor)),
      ]),
    );

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  testWidgets('an empty line is as tall as the other lines of its own editor', (tester) async {
    final large = _editor(1, 'x\n\ny', 30);
    await tester.pumpWidget(_app([large]));
    await tester.pump();

    await tester.pumpWidget(_app([large, _editor(2, 'a\n\nb', 10)]));
    await tester.pump();

    final rows = (StyledTextImpl.debugLastGeometry!['lines'] as List).cast<Map<String, dynamic>>();
    expect(rows.map((r) => r['l']), [0, 1, 2], reason: 'sanity: the small editor pushed last');
    expect(rows[1]['h'], rows[0]['h'], reason: 'the empty line is measured in this editor\'s font');
  });
}
