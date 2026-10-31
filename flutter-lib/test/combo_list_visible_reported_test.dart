// Flutter draws a Combo's drop-down list, so Java only knows whether it is open (getListVisible) if
// Flutter reports every open and close the user makes: F4, Alt+Down, the arrow, Escape, a pick, a
// click outside. What Java itself asked for is not reported back, and neither a push that left Java
// before it heard the report nor Java's echo of it changes the list the user is working in.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/gen/widgets.dart' show wireChannelNames;

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/combo.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/focus_requests.dart';

import 'delivery/support/deliver.dart';

const int comboId = 92;

class _CapturingComboSwt extends ComboSwt<VCombo> {
  const _CapturingComboSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VCombo val, int channel, VEvent? payload) => onEvent(wireChannelNames[channel]!, payload);
}

VCombo _combo({int seq = 1, bool listVisible = false, int style = SWT.READ_ONLY}) => VCombo()
  ..swt = 'Combo'
  ..id = comboId
  ..seq = seq
  ..style = style
  ..enabled = true
  ..items = const ['Alpha', 'Bravo', 'Charlie']
  ..text = 'Alpha'
  ..listVisible = listVisible
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 120
    ..height = 30);

/// Delivers [value] as Java describes it whole.
void _receive(VCombo value) => applyWhole(value);

Future<List<(String, VEvent?)>> _pump(WidgetTester tester,
    {bool focus = true, int style = SWT.READ_ONLY}) async {
  final events = <(String, VEvent?)>[];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(
        width: 120,
        height: 30,
        child: _CapturingComboSwt(
          value: _combo(style: style),
          onEvent: (ev, payload) => events.add((ev, payload)),
        ),
      ),
    ),
  ));
  await tester.pumpAndSettle();
  if (focus) {
    FocusRequests.instance.request(comboId);
    await tester.pumpAndSettle();
  }
  events.clear();
  return events;
}

/// The list reports, in order: 1 for opened, 0 for closed.
List<int?> _reports(List<(String, VEvent?)> events) =>
    events.where((e) => e.$1 == 'List/Visible').map((e) => e.$2?.detail).toList();

bool _listOpen() => find.text('Charlie').evaluate().isNotEmpty;

Future<void> _press(WidgetTester tester, LogicalKeyboardKey key) async {
  await tester.sendKeyEvent(key);
  await tester.pumpAndSettle();
}

Future<void> _altDown(WidgetTester tester) async {
  await tester.sendKeyDownEvent(LogicalKeyboardKey.altLeft);
  await tester.sendKeyEvent(LogicalKeyboardKey.arrowDown);
  await tester.sendKeyUpEvent(LogicalKeyboardKey.altLeft);
  await tester.pumpAndSettle();
}

void main() {
  setUp(() {
    FocusRequests.instance.reset();
    VRegistry.instance.clear();
  });
  tearDown(() => FocusRequests.instance.reset());

  testWidgets('F4 opening and Escape closing are reported', (tester) async {
    final events = await _pump(tester);

    await _press(tester, LogicalKeyboardKey.f4);
    expect(_listOpen(), isTrue);
    await _press(tester, LogicalKeyboardKey.escape);

    expect(_reports(events), [1, 0]);
  });

  testWidgets('Alt+Down opening and F4 closing are reported', (tester) async {
    final events = await _pump(tester);

    await _altDown(tester);
    await _press(tester, LogicalKeyboardKey.f4);

    expect(_reports(events), [1, 0]);
  });

  testWidgets('the arrow opening and a pick closing are reported, the close before the Selection',
      (tester) async {
    final events = await _pump(tester);

    await tester.tap(find.byIcon(Icons.arrow_drop_down));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Charlie'));
    await tester.pumpAndSettle();

    expect(_reports(events), [1, 0]);
    expect(
        events
            .map((e) => e.$1)
            .where((ev) => ev == 'List/Visible' || ev.startsWith('Selection/')),
        ['List/Visible', 'List/Visible', 'Selection/Selection']);
  });

  testWidgets('a click on the text of a read-only Combo opens its list and reports it',
      (tester) async {
    final events = await _pump(tester, focus: false);

    await tester.tapAt(const Offset(30, 15));
    await tester.pumpAndSettle();

    expect(_listOpen(), isTrue);
    expect(_reports(events), [1]);
  });

  testWidgets('a second click on the text of a read-only Combo closes its list', (tester) async {
    final events = await _pump(tester, focus: false);

    await tester.tapAt(const Offset(30, 15));
    await tester.pumpAndSettle();
    await tester.tapAt(const Offset(30, 15));
    await tester.pumpAndSettle();

    expect(_listOpen(), isFalse);
    expect(_reports(events), [1, 0]);
  });

  testWidgets('a press dragged across a read-only Combo leaves its list open', (tester) async {
    final events = await _pump(tester, focus: false);

    final TestGesture drag = await tester.startGesture(const Offset(5, 15));
    await tester.pump();
    for (double x = 25; x <= 115; x += 30) {
      await drag.moveTo(Offset(x, 15));
      await tester.pump();
    }
    await drag.up();
    await tester.pumpAndSettle();

    expect(_listOpen(), isTrue);
    expect(_reports(events), [1]);
  });

  testWidgets('a right-click does not focus a read-only Combo', (tester) async {
    final events = await _pump(tester, focus: false);

    await tester.tapAt(const Offset(30, 15), buttons: kSecondaryButton);
    await tester.pumpAndSettle();

    expect(events.map((e) => e.$1), isNot(contains(startsWith('Focus/'))));
    expect(_listOpen(), isFalse);
  });

  testWidgets('a right press on a read-only Combo closes its open list', (tester) async {
    final events = await _pump(tester, focus: false);

    await tester.tapAt(const Offset(30, 15));
    await tester.pumpAndSettle();
    await tester.tapAt(const Offset(30, 15), buttons: kSecondaryButton);
    await tester.pumpAndSettle();

    expect(_listOpen(), isFalse);
    expect(_reports(events), [1, 0]);
  });

  testWidgets('the mouse click that closes a read-only Combo\'s list keeps its focus',
      (tester) async {
    final events = await _pump(tester, focus: false);

    await tester.tapAt(const Offset(30, 15), kind: PointerDeviceKind.mouse);
    await tester.pumpAndSettle();
    events.clear();
    await tester.tapAt(const Offset(30, 15), kind: PointerDeviceKind.mouse);
    await tester.pumpAndSettle();

    expect(_listOpen(), isFalse);
    expect(events.map((e) => e.$1), isNot(contains('Focus/FocusOut')));
  });

  testWidgets('a mouse right-click on a focused read-only Combo keeps its focus', (tester) async {
    final events = await _pump(tester);

    await tester.tapAt(const Offset(30, 15),
        kind: PointerDeviceKind.mouse, buttons: kSecondaryButton);
    await tester.pumpAndSettle();

    expect(events.map((e) => e.$1), isNot(contains('Focus/FocusOut')));
  });

  testWidgets('a click on the text of a DROP_DOWN Combo closes its open list', (tester) async {
    final events = await _pump(tester, style: SWT.DROP_DOWN);

    await tester.tap(find.byIcon(Icons.arrow_drop_down));
    await tester.pumpAndSettle();
    expect(_listOpen(), isTrue);
    await tester.tapAt(const Offset(30, 15));
    await tester.pumpAndSettle();

    expect(_listOpen(), isFalse);
    expect(_reports(events), [1, 0]);
  });

  testWidgets('a click outside the open list closing it is reported', (tester) async {
    final events = await _pump(tester);

    await _press(tester, LogicalKeyboardKey.f4);
    await tester.tapAt(const Offset(600, 500));
    await tester.pumpAndSettle();

    expect(_listOpen(), isFalse);
    expect(_reports(events), [1, 0]);
  });

  testWidgets('a selection made with the list closed reports nothing about the list',
      (tester) async {
    final events = await _pump(tester);

    await _press(tester, LogicalKeyboardKey.arrowDown);

    expect(_reports(events), isEmpty);
  });

  testWidgets('Java opening and closing the list is not reported back', (tester) async {
    final events = await _pump(tester);

    _receive(_combo(seq: 2, listVisible: true));
    await tester.pumpAndSettle();
    expect(_listOpen(), isTrue);
    _receive(_combo(seq: 3, listVisible: false));
    await tester.pumpAndSettle();
    expect(_listOpen(), isFalse);

    expect(_reports(events), isEmpty);
  });

  testWidgets('a push Java sent before it heard of the open list does not close it',
      (tester) async {
    await _pump(tester);

    await _press(tester, LogicalKeyboardKey.f4);
    _receive(_combo(seq: 2, listVisible: false));
    await tester.pumpAndSettle();

    expect(_listOpen(), isTrue);
  });

  testWidgets("Java's echo of the open list keeps the item the keyboard points at",
      (tester) async {
    final events = await _pump(tester);

    await _press(tester, LogicalKeyboardKey.f4);
    await _press(tester, LogicalKeyboardKey.arrowDown);
    _receive(_combo(seq: 2, listVisible: true));
    await tester.pumpAndSettle();
    await _press(tester, LogicalKeyboardKey.enter);

    expect(
        events.where((e) => e.$1 == 'Selection/Selection').map((e) => e.$2?.text), ['Bravo']);
  });
}
