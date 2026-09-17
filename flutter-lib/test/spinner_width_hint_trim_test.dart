// The spinner's size model and its renderer have to agree on one number: how
// much of the widget's width is chrome rather than number.
//
// Sizes.computeSize(DartSpinner, ...) answers a layout's width hint with
// `hint + trim`, the way SwtSpinner.computeSize/computeTrim do natively, so the
// stepper is added beside the number instead of eating into it. That only holds
// if this side takes exactly the trim the Java side reserved: take more and the
// number loses room, take less and the stepper is drawn past the bounds a layout
// gave the control - outside the row, and in a tight dialog outside the shell,
// which is where the arrows went missing.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/spinner.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/rectangle.dart';

// Border (1 each side) + the stepper column (20) + the field's own horizontal
// padding (8 each side). Keep in step with SPINNER_* in Sizes.java and with
// spinner_theme_settings.dart.
const int _trim = 2 + 20 + 16;

// What GridLayout hands the control for `GridData.widthHint = 40`.
const int _hint = 40;

VSpinner _spinner(int width) => VSpinner()
  ..id = 31
  ..style = SWT.BORDER
  ..enabled = true
  ..minimum = 1
  ..maximum = 1000
  ..selection = 1
  ..increment = 1
  ..textLimit = 4
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = width
    ..height = 32);

Future<void> _pump(WidgetTester tester, VSpinner value) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SpinnerSwt<VSpinner>(value: value),
    ),
  ));
  await tester.pumpAndSettle();
}

void main() {
  testWidgets('the hinted width reaches the number, not the chrome',
      (WidgetTester tester) async {
    await _pump(tester, _spinner(_hint + _trim));

    expect(tester.getSize(find.byType(EditableText)).width, _hint.toDouble());
  });

  testWidgets('both stepper arrows are drawn inside the bounds',
      (WidgetTester tester) async {
    await _pump(tester, _spinner(_hint + _trim));

    final Rect box = tester.getRect(find.byType(SpinnerSwt<VSpinner>));
    for (final IconData arrow in [Icons.arrow_drop_up, Icons.arrow_drop_down]) {
      final Finder icon = find.byIcon(arrow);
      expect(icon, findsOneWidget);
      final Rect drawn = tester.getRect(icon);
      expect(drawn.left, greaterThanOrEqualTo(box.left),
          reason: '$arrow starts left of the spinner bounds');
      expect(drawn.right, lessThanOrEqualTo(box.right),
          reason: '$arrow is drawn past the right edge of the spinner bounds');
    }
  });
}
