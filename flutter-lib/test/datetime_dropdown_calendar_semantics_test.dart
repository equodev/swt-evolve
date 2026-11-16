// The drop-down button and the open calendar of a DateTime(SWT.DATE | SWT.DROP_DOWN) carry
// semantics identifiers of their own, so a test reaches them without coordinates.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/datetime.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

VDateTime _dropDownDate() => VDateTime()
  ..swt = 'DateTime'
  ..id = 95
  ..seq = 1
  ..style = SWT.DATE | SWT.DROP_DOWN
  ..enabled = true
  ..year = 2026
  ..month = 0
  ..day = 15
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 120
    ..height = 24);

void main() {
  testWidgets('the drop-down button and the open calendar are found by identifier', (tester) async {
    final semantics = tester.ensureSemantics();
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(
          width: 200,
          height: 24,
          child: DateTimeSwt<VDateTime>(value: _dropDownDate()),
        ),
      ),
    ));
    await tester.pumpAndSettle();

    final button = find.bySemanticsIdentifier('dropdown:DateTime/95');
    final calendar = find.bySemanticsIdentifier('calendar:DateTime/95');
    expect(button, findsOneWidget);
    expect(calendar, findsNothing, reason: 'the calendar starts closed');

    await tester.tap(button);
    await tester.pumpAndSettle();
    expect(calendar, findsOneWidget, reason: 'the button opens the calendar');
    expect(find.descendant(of: calendar, matching: find.text('January 2026')), findsOneWidget,
        reason: 'the identifier sits on the calendar itself');

    await tester.tap(button);
    await tester.pumpAndSettle();
    expect(calendar, findsNothing, reason: 'the button closes the calendar again');
    semantics.dispose();
  });
}
