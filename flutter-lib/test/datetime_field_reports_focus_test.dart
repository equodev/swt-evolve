// Java routes the keys the client forwards to its focus control, so a DateTime field the user clicks
// must report FocusIn: otherwise its arrow keys also reach the control that was focused before it.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/datetime.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

class _CapturingDateTimeSwt extends DateTimeSwt<VDateTime> {
  const _CapturingDateTimeSwt({required super.value, required this.onEvent});

  final void Function(String ev) onEvent;

  @override
  void sendEvent(VDateTime val, String ev, VEvent? payload) => onEvent(ev);
}

VDateTime _date(int id) => VDateTime()
  ..swt = 'DateTime'
  ..id = id
  ..style = SWT.DATE | SWT.DROP_DOWN
  ..enabled = true
  ..year = 2026
  ..month = 0
  ..day = id == 1 ? 15 : 20
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 160
    ..height = 24);

void main() {
  testWidgets('clicking a DateTime field reports FocusIn, and leaving it FocusOut', (tester) async {
    final events = <String>[];
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Column(
        children: [
          for (final id in [1, 2])
            SizedBox(
              width: 160,
              height: 24,
              child: _CapturingDateTimeSwt(value: _date(id), onEvent: (ev) => events.add('$id $ev')),
            ),
        ],
      ),
    ));
    await tester.pumpAndSettle();

    await tester.tap(find.text('15'));
    await tester.pumpAndSettle();
    expect(events, contains('1 Focus/FocusIn'));

    events.clear();
    await tester.tap(find.text('20'));
    await tester.pumpAndSettle();
    expect(events, containsAll(['1 Focus/FocusOut', '2 Focus/FocusIn']));
  });
}
