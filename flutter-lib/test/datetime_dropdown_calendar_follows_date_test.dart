// An open DateTime(SWT.DATE | SWT.DROP_DOWN) calendar follows every change of the control's date.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/datetime.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

const int _dateTimeId = 93;

VDateTime _dropDownDate({required int seq, required int year, required int month, required int day}) =>
    VDateTime()
      ..swt = 'DateTime'
      ..id = _dateTimeId
      ..seq = seq
      ..style = SWT.DATE | SWT.DROP_DOWN
      ..enabled = true
      ..year = year
      ..month = month
      ..day = day
      ..bounds = (VRectangle()
        ..x = 0
        ..y = 0
        ..width = 120
        ..height = 24);

/// Delivers an inbound frame exactly as the transport would (2-byte name length, name, JSON body).
void _receive(String actionId, Object payload) {
  final actionBytes = utf8.encode(actionId);
  final body = utf8.encode(json.encode(payload));
  final frame = Uint8List(2 + actionBytes.length + body.length);
  frame[0] = (actionBytes.length >> 8) & 0xFF;
  frame[1] = actionBytes.length & 0xFF;
  frame.setRange(2, 2 + actionBytes.length, actionBytes);
  frame.setRange(2 + actionBytes.length, frame.length, body);
  EquoCommService.commForTesting.receiveBinary(frame);
}

Future<void> _pumpAndOpenCalendar(WidgetTester tester, {String? selectField}) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(
        width: 200,
        height: 24,
        child: DateTimeSwt<VDateTime>(
            value: _dropDownDate(seq: 1, year: 2026, month: 0, day: 15)),
      ),
    ),
  ));
  await tester.pumpAndSettle();

  if (selectField != null) {
    await tester.tap(find.text(selectField));
    await tester.pump();
  }
  await tester.tap(find.byIcon(Icons.arrow_drop_down).last);
  await tester.pumpAndSettle();
  expect(find.text('January 2026'), findsOneWidget,
      reason: 'sanity: the drop-down arrow opens the calendar on the field date');
}

void main() {
  testWidgets('a keyboard edit in the field moves the open calendar', (tester) async {
    await _pumpAndOpenCalendar(tester, selectField: '2026');

    await tester.sendKeyEvent(LogicalKeyboardKey.arrowDown);
    await tester.pumpAndSettle();

    expect(find.text('2025'), findsOneWidget, reason: 'sanity: the field took the edit');
    expect(find.text('January 2025'), findsOneWidget,
        reason: 'the open calendar shows the edited year');
    expect(find.text('January 2026'), findsNothing);
  });

  testWidgets('a Java setDate moves the open calendar and its selected day', (tester) async {
    await _pumpAndOpenCalendar(tester);

    _receive('DateTime/$_dateTimeId', {
      ..._dropDownDate(seq: 2, year: 2024, month: 5, day: 20).toJson(),
      '_s': 2,
    });
    await tester.pumpAndSettle();

    expect(find.text('June 2024'), findsOneWidget,
        reason: 'the open calendar shows the date Java set');
    expect(find.text('January 2026'), findsNothing);
  });

  testWidgets('a date change brings a paged calendar back to the month of the date', (tester) async {
    await _pumpAndOpenCalendar(tester);

    await tester.tap(find.byIcon(Icons.chevron_right));
    await tester.pumpAndSettle();
    expect(find.text('February 2026'), findsOneWidget, reason: 'sanity: the calendar paged');

    _receive('DateTime/$_dateTimeId', {
      ..._dropDownDate(seq: 2, year: 2026, month: 0, day: 16).toJson(),
      '_s': 2,
    });
    await tester.pumpAndSettle();

    expect(find.text('January 2026'), findsOneWidget,
        reason: 'the calendar shows the month of the new date');
  });
}
