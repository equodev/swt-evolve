import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

// SWT.VERTICAL and SWT.V_SCROLL are the same bit (1 << 9), and a Label only reads it
// when SWT.SEPARATOR is set. On a text label the bit is inert, so it must not turn the
// text on its side. JFace clients reach this: a MessageDialog whose getMessageLabelStyle()
// returns SWT.WRAP | SWT.V_SCROLL builds its message label with that style.
const String _message =
    'A required field is still empty. Fill it in before saving.';

VLabel _label(int style, {required int width, required int height}) => VLabel()
  ..id = 1
  ..style = style
  ..text = _message
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

void main() {
  testWidgets('an inert vertical bit does not rotate a text label',
      (tester) async {
    await tester.pumpWidget(_wrap(
      _label(SWT.WRAP | SWT.V_SCROLL, width: 260, height: 38),
      width: 320,
      height: 80,
    ));
    await tester.pumpAndSettle();

    expect(find.byType(RotatedBox), findsNothing,
        reason: 'a Label without SWT.SEPARATOR has no vertical mode in SWT');

    final size = tester.getSize(find.byType(Text).first);
    expect(size.width, greaterThan(size.height),
        reason: 'the message must read across the label, not down it');
  });

  testWidgets('a separator label still honours SWT.VERTICAL', (tester) async {
    await tester.pumpWidget(_wrap(
      VLabel()
        ..id = 2
        ..style = SWT.SEPARATOR | SWT.VERTICAL
        ..text = ''
        ..bounds = (VRectangle()
          ..x = 0
          ..y = 0
          ..width = 2
          ..height = 40),
      width: 40,
      height: 60,
    ));
    await tester.pumpAndSettle();

    expect(find.byType(VerticalDivider), findsOneWidget);
  });
}
