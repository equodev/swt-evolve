// Enter in a date or time field, and a double click on a calendar's day, are a DateTime's
// DefaultSelection. A drop-down's calendar opens on F4 and Alt+Down, and Escape closes it with the
// date left as it was.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/datetime.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widgets.dart' show wireChannelNames;

class _CapturingDateTimeSwt extends DateTimeSwt<VDateTime> {
  const _CapturingDateTimeSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VDateTime val, int channel, VEvent? payload) => onEvent(wireChannelNames[channel]!, payload);
}

VDateTime _dateTime(int style, {double width = 200, double height = 30}) => VDateTime()
  ..swt = 'DateTime'
  ..id = 1
  ..style = style
  ..enabled = true
  ..year = 2020
  ..month = 5
  ..day = 15
  ..hours = 10
  ..minutes = 20
  ..seconds = 30
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = width.toInt()
    ..height = height.toInt());

void main() {
  late List<String> events;
  late List<int> selectionMasks;

  Future<void> pump(WidgetTester tester, int style, {double width = 200, double height = 30}) async {
    events = [];
    selectionMasks = [];
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(
          width: width,
          height: height,
          child: _CapturingDateTimeSwt(
            value: _dateTime(style, width: width, height: height),
            onEvent: (ev, p) {
              events.add(ev);
              if (ev == 'Selection/Selection') selectionMasks.add(p?.stateMask ?? 0);
            },
          ),
        ),
      ),
    ));
    await tester.pumpAndSettle();
  }

  List<String> selections() => events.where((e) => e.startsWith('Selection/')).toList();

  Future<void> clickCentre(WidgetTester tester) async {
    await tester.tapAt(tester.getRect(find.byType(_CapturingDateTimeSwt)).center);
    await tester.pumpAndSettle();
  }

  Future<void> press(WidgetTester tester, LogicalKeyboardKey key) async {
    await tester.sendKeyEvent(key);
    await tester.pumpAndSettle();
  }

  testWidgets('Enter in a date field is its DefaultSelection', (tester) async {
    await pump(tester, SWT.DATE | SWT.MEDIUM);
    await clickCentre(tester);
    await press(tester, LogicalKeyboardKey.enter);
    expect(selections(), ['Selection/DefaultSelection']);
  });

  testWidgets('Enter in a time field is its DefaultSelection', (tester) async {
    await pump(tester, SWT.TIME | SWT.MEDIUM);
    await clickCentre(tester);
    await press(tester, LogicalKeyboardKey.enter);
    expect(selections(), ['Selection/DefaultSelection']);
  });

  testWidgets('a double click on a day of a calendar selects it, then is its DefaultSelection', (tester) async {
    await pump(tester, SWT.CALENDAR, width: 260, height: 260);
    await tester.tap(find.text('8'));
    await tester.pump(const Duration(milliseconds: 50));
    await tester.tap(find.text('8'));
    await tester.pumpAndSettle();
    expect(selections(), ['Selection/Selection', 'Selection/DefaultSelection']);
  });

  testWidgets('a single click on a day of a calendar is no DefaultSelection', (tester) async {
    await pump(tester, SWT.CALENDAR, width: 260, height: 260);
    await tester.tap(find.text('8'));
    await tester.pumpAndSettle();
    expect(selections(), ['Selection/Selection']);
  });

  testWidgets('a calendar selects a day on the press, before the release', (tester) async {
    await pump(tester, SWT.CALENDAR, width: 260, height: 260);
    final gesture = await tester.startGesture(tester.getCenter(find.text('8')));
    await tester.pump();
    expect(selections(), ['Selection/Selection'], reason: 'the day is selected while the button is down');
    expect(selectionMasks, [SWT.BUTTON1], reason: 'with the button that pressed it held');
    await gesture.up();
    await tester.pumpAndSettle();
    expect(selections(), ['Selection/Selection']);
  });

  testWidgets('a double click reports its DefaultSelection on the second release', (tester) async {
    await pump(tester, SWT.CALENDAR, width: 260, height: 260);
    await tester.tap(find.text('8'));
    await tester.pump(const Duration(milliseconds: 50));
    final second = await tester.startGesture(tester.getCenter(find.text('8')));
    await tester.pump();
    expect(selections(), ['Selection/Selection'], reason: 'the second press selects nothing new');
    await second.up();
    await tester.pumpAndSettle();
    expect(selections(), ['Selection/Selection', 'Selection/DefaultSelection']);
  });

  testWidgets('a click on the day a calendar already selects changes nothing and reports no Selection', (tester) async {
    await pump(tester, SWT.CALENDAR, width: 260, height: 260);
    await tester.tap(find.text('15'));
    await tester.pumpAndSettle();
    expect(selections(), isEmpty);
  });

  testWidgets('a day of the drop-down calendar is picked on the press, with the button held', (tester) async {
    await pump(tester, SWT.DATE | SWT.MEDIUM | SWT.DROP_DOWN);
    await tester.tap(find.byIcon(Icons.arrow_drop_down));
    await tester.pumpAndSettle();
    final gesture = await tester.startGesture(tester.getCenter(find.text('8')));
    await tester.pump();
    expect(selections(), ['Selection/Selection'], reason: 'picked while the button is down');
    expect(selectionMasks, [SWT.BUTTON1]);
    expect(find.text('June 2020'), findsNothing, reason: 'and the calendar closes');
    await gesture.up();
    await tester.pumpAndSettle();
    expect(selections(), ['Selection/Selection']);
  });

  testWidgets('Escape closes the calendar the drop-down opened and leaves the date', (tester) async {
    await pump(tester, SWT.DATE | SWT.MEDIUM | SWT.DROP_DOWN);
    await tester.tap(find.byIcon(Icons.arrow_drop_down));
    await tester.pumpAndSettle();
    expect(find.text('June 2020'), findsOneWidget, reason: 'sanity: the button opens the calendar');
    await press(tester, LogicalKeyboardKey.escape);
    expect(find.text('June 2020'), findsNothing);
    expect(selections(), isEmpty);
  });

  testWidgets('F4 opens the calendar of a focused drop-down, and again closes it', (tester) async {
    await pump(tester, SWT.DATE | SWT.MEDIUM | SWT.DROP_DOWN);
    await clickCentre(tester);
    await press(tester, LogicalKeyboardKey.f4);
    expect(find.text('June 2020'), findsOneWidget);
    await press(tester, LogicalKeyboardKey.f4);
    expect(find.text('June 2020'), findsNothing);
    expect(selections(), isEmpty);
  });

  testWidgets('Alt+Down opens the calendar of a focused drop-down', (tester) async {
    await pump(tester, SWT.DATE | SWT.MEDIUM | SWT.DROP_DOWN);
    await clickCentre(tester);
    await tester.sendKeyDownEvent(LogicalKeyboardKey.altLeft);
    await press(tester, LogicalKeyboardKey.arrowDown);
    await tester.sendKeyUpEvent(LogicalKeyboardKey.altLeft);
    await tester.pumpAndSettle();
    expect(find.text('June 2020'), findsOneWidget);
    expect(selections(), isEmpty, reason: 'Alt+Down opens the calendar rather than stepping the field');
  });
}
