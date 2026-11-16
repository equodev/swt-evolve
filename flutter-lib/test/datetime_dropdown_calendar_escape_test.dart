// Escape closes the calendar of a DateTime(SWT.DATE | SWT.DROP_DOWN), as Win32's does.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/datetime.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/key_forwarding.dart';

VDateTime _dropDownDate() => VDateTime()
  ..swt = 'DateTime'
  ..id = 94
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

Future<void> _pumpAndOpenCalendar(WidgetTester tester) async {
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

  await tester.tap(find.byIcon(Icons.arrow_drop_down).last);
  await tester.pumpAndSettle();
  expect(find.text('January 2026'), findsOneWidget, reason: 'sanity: the calendar is open');
}

void main() {
  testWidgets('Escape closes the open calendar', (tester) async {
    await _pumpAndOpenCalendar(tester);

    await tester.sendKeyEvent(LogicalKeyboardKey.escape);
    await tester.pumpAndSettle();

    expect(find.text('January 2026'), findsNothing, reason: 'Escape closes the calendar');
    expect(find.byIcon(Icons.arrow_drop_up), findsNothing,
        reason: 'the drop-down button shows the calendar closed');
  });

  testWidgets('the Escape that closes the open calendar reaches SWT as one that closes a popup', (tester) async {
    const escapeDown = KeyDownEvent(
        physicalKey: PhysicalKeyboardKey.escape,
        logicalKey: LogicalKeyboardKey.escape,
        timeStamp: Duration.zero);
    await _pumpAndOpenCalendar(tester);
    expect(openListClaimsKey(escapeDown), isFalse, reason: 'the press reaches SWT, as on Win32');
    expect(openPopupClosesOnKey(escapeDown), isTrue,
        reason: 'it reaches SWT with doit=false, so it closes no dialog around the DateTime');

    await tester.sendKeyEvent(LogicalKeyboardKey.escape);
    await tester.pumpAndSettle();
    expect(openPopupClosesOnKey(escapeDown), isFalse, reason: 'a closed calendar closes nothing');
  });

  testWidgets('Escape leaves the date as it was', (tester) async {
    await _pumpAndOpenCalendar(tester);

    await tester.sendKeyEvent(LogicalKeyboardKey.escape);
    await tester.pumpAndSettle();

    final texts = tester
        .widgetList<Text>(find.descendant(
            of: find.byType(DateTimeSwt<VDateTime>), matching: find.byType(Text)))
        .map((t) => t.data)
        .whereType<String>();
    expect(texts, containsAll(['01', '15', '2026']), reason: 'the field still shows 01/15/2026');
  });
}
