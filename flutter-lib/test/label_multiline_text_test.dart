import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

// A plain SWT Label (no SWT.WRAP) still honours the line delimiters inside its
// text -- it just never wraps on its own. lsp4e's Folding preference page relies
// on that: it labels the checkbox group with "\nInitially fold these elements:",
// using the leading newline as the spacer above the group.
const String _foldingLabel = '\nInitially fold these elements:';

VLabel _label(String text, {required int width, required int height}) => VLabel()
  ..id = 1
  ..style = SWT.NONE
  ..text = text
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = width
    ..height = height);

Widget _wrap(VLabel value, {required double width, required double height}) =>
    EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: width,
        height: height,
        child: LabelSwt<VLabel>(value: value),
      ),
    );

Text _text(WidgetTester tester) =>
    tester.widget<Text>(find.byType(Text).first);

void main() {
  testWidgets('a label keeps every line of a multi-line text', (tester) async {
    // The height is what Java's LabelSizes computes for two lines.
    await tester.pumpWidget(_wrap(
      _label(_foldingLabel, width: 240, height: 42),
      width: 300,
      height: 60,
    ));
    await tester.pumpAndSettle();

    final text = _text(tester);
    expect(text.data, _foldingLabel);
    expect(text.maxLines, isNot(1),
        reason: 'clamping to one line drops every line after the first -- '
            'here the only visible line would be the leading empty one');
  });

  testWidgets('the text of a multi-line label is actually painted',
      (tester) async {
    await tester.pumpWidget(_wrap(
      _label(_foldingLabel, width: 240, height: 42),
      width: 300,
      height: 60,
    ));
    await tester.pumpAndSettle();

    // Two lines of text must be taller than a single line of the same style.
    final multiline = tester.getSize(find.byType(Text).first);

    await tester.pumpWidget(_wrap(
      _label('Initially fold these elements:', width: 240, height: 42),
      width: 300,
      height: 60,
    ));
    await tester.pumpAndSettle();
    final single = tester.getSize(find.byType(Text).first);

    expect(multiline.height, greaterThan(single.height),
        reason: 'the leading newline must produce a second, rendered line');
  });

  testWidgets('a single-line label without SWT.WRAP still never wraps',
      (tester) async {
    const long =
        'a single line that is far too long to fit inside the given bounds';
    await tester.pumpWidget(_wrap(
      _label(long, width: 80, height: 21),
      width: 300,
      height: 60,
    ));
    await tester.pumpAndSettle();

    final text = _text(tester);
    expect(text.softWrap, isFalse);
    expect(tester.getSize(find.byType(Text).first).height, lessThan(30),
        reason: 'a non-WRAP label must stay on one line when the text has no '
            'delimiters of its own');
  });
}
