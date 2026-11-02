// A date clicked anywhere in its field takes the keys: the arrows step the field under the caret,
// Home and End take it to its first and last value (years 1752 and 9999, as the Javadoc states),
// and the page keys change nothing, as on Win32 and Cocoa.

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

VDateTime _date({int style = SWT.DATE | SWT.MEDIUM}) => VDateTime()
  ..swt = 'DateTime'
  ..id = 1
  ..style = style
  ..enabled = true
  ..year = 2020
  ..month = 5
  ..day = 15
  ..hours = 10
  ..minutes = 30
  ..seconds = 45
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 200
    ..height = 60);

void main() {
  late List<String> selections;
  late List<String> events;

  Future<void> pump(WidgetTester tester, VDateTime value) async {
    selections = [];
    events = [];
    final time = (value.style! & SWT.TIME) != 0;
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(
          width: 200,
          height: 60,
          child: _CapturingDateTimeSwt(
            value: value,
            onEvent: (ev, p) {
              events.add(ev);
              if (ev == 'Selection/Selection') {
                selections.add(time ? '${p!.height}:${p.count}:${p.index}' : '${p!.x}-${p.y}-${p.width}');
              }
            },
          ),
        ),
      ),
    ));
    await tester.pumpAndSettle();
  }

  Rect bounds(WidgetTester tester) => tester.getRect(find.byType(_CapturingDateTimeSwt));

  Future<void> tapAt(WidgetTester tester, Offset at) async {
    await tester.tapAt(at);
    await tester.pumpAndSettle();
  }

  Future<void> pumpAndClickCentre(WidgetTester tester) async {
    await pump(tester, _date());
    await tapAt(tester, bounds(tester).center);
  }

  bool focusIsInTheDateTime() =>
      FocusManager.instance.primaryFocus?.context
          ?.findAncestorWidgetOfExactType<_CapturingDateTimeSwt>() !=
      null;

  Future<void> press(WidgetTester tester, LogicalKeyboardKey key) async {
    await tester.sendKeyEvent(key);
    await tester.pumpAndSettle();
  }

  testWidgets('a click at the centre lets Up and Down step the field nearest it', (tester) async {
    await pumpAndClickCentre(tester);
    await press(tester, LogicalKeyboardKey.arrowUp);
    await press(tester, LogicalKeyboardKey.arrowDown);
    expect(selections, ['2021-5-15', '2020-5-15']);
  });

  testWidgets('Home and End take the field to its first and last value', (tester) async {
    await pumpAndClickCentre(tester);
    await press(tester, LogicalKeyboardKey.home);
    await press(tester, LogicalKeyboardKey.end);
    expect(selections, ['1752-5-15', '9999-5-15']);
  });

  testWidgets('End on a field already at its last value fires no Selection', (tester) async {
    await pumpAndClickCentre(tester);
    await press(tester, LogicalKeyboardKey.end);
    await press(tester, LogicalKeyboardKey.end);
    expect(selections, ['9999-5-15']);
  });

  testWidgets('a click left of the fields picks the first one', (tester) async {
    await pumpAndClickCentre(tester);
    final box = bounds(tester);
    await tapAt(tester, Offset(box.left + 1, box.bottom - 2));
    await press(tester, LogicalKeyboardKey.arrowUp);
    expect(selections, ['2020-6-15']);
  });

  testWidgets('a click past the last field picks the last one', (tester) async {
    await pump(tester, _date());
    final box = bounds(tester);
    // Between the year and the stepper on its right.
    await tapAt(tester, Offset(box.right - 20, box.center.dy));
    await press(tester, LogicalKeyboardKey.arrowUp);
    expect(selections, ['2021-5-15']);
  });

  testWidgets('Page Up and Page Down leave the date as it is', (tester) async {
    await pumpAndClickCentre(tester);
    await press(tester, LogicalKeyboardKey.pageDown);
    await press(tester, LogicalKeyboardKey.pageUp);
    expect(selections, isEmpty);
    expect(find.text('2020'), findsOneWidget);
  });

  testWidgets('Home and End take a time field to its first and last value', (tester) async {
    await pump(tester, _date(style: SWT.TIME | SWT.MEDIUM));
    final box = bounds(tester);
    await tapAt(tester, Offset(box.left + 1, box.center.dy));
    await press(tester, LogicalKeyboardKey.home);
    await press(tester, LogicalKeyboardKey.end);
    expect(selections, ['0:30:45', '23:30:45']);
  });

  testWidgets('a click on the stepper takes the focus, so the arrows follow it', (tester) async {
    await pump(tester, _date());
    await tapAt(tester, tester.getCenter(find.byIcon(Icons.arrow_drop_up)));
    await press(tester, LogicalKeyboardKey.arrowUp);
    expect(selections, ['2020-6-15', '2020-7-15']);
    expect(events.where((e) => e == 'Focus/FocusIn'), hasLength(1));
  });

  testWidgets('a click on the drop-down button takes the focus', (tester) async {
    await pump(tester, _date(style: SWT.DATE | SWT.MEDIUM | SWT.DROP_DOWN));
    await tapAt(tester, tester.getCenter(find.byIcon(Icons.arrow_drop_down)));
    expect(focusIsInTheDateTime(), isTrue);
    expect(events.where((e) => e == 'Focus/FocusIn'), hasLength(1));
  });

  testWidgets('a click on a field reports one FocusIn', (tester) async {
    await pumpAndClickCentre(tester);
    expect(focusIsInTheDateTime(), isTrue);
    expect(events.where((e) => e == 'Focus/FocusIn'), hasLength(1));
  });
}
