// The keyboard reaches a button the way SWT's does: Space presses the focused button whatever its
// style, each button is one tab stop, a focus Java asks for lands on the button, and an arrow button
// (NO_FOCUS) is never focused. Return is the shell's; see button_enter_leaves_default_button_to_java_test.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widgets.dart' show wireChannelNames;
import 'package:swtflutter/src/impl/focus_requests.dart';
import 'package:swtflutter/src/impl/key_forwarding.dart';

class _CapturingButtonSwt extends ButtonSwt<VButton> {
  const _CapturingButtonSwt({required super.value, required this.onEvent});

  final void Function(String ev) onEvent;

  @override
  void sendEvent(VButton val, int channel, VEvent? payload) => onEvent(wireChannelNames[channel]!);
}

VButton _button(int id, int style) => VButton()
  ..id = id
  ..style = style
  ..enabled = true
  ..visible = true
  ..text = 'Button $id'
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 90
    ..height = 26);

/// Two buttons side by side, of [style] or of [style] then [secondStyle]; returns what each one
/// sent, by id.
Future<Map<int, List<String>>> _pumpButtons(WidgetTester tester, int style,
    {int? secondStyle}) async {
  final events = <int, List<String>>{1: [], 2: []};
  final styles = {1: style, 2: secondStyle ?? style};
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          for (final id in [1, 2])
            _CapturingButtonSwt(value: _button(id, styles[id]!), onEvent: events[id]!.add),
        ],
      ),
    ),
  ));
  await tester.pumpAndSettle();
  return events;
}

/// Tabs onto the first button and forgets what that sent.
Future<void> _focusFirst(WidgetTester tester, Map<int, List<String>> events) async {
  await tester.sendKeyEvent(LogicalKeyboardKey.tab);
  await tester.pumpAndSettle();
  expect(events[1], contains('Focus/FocusIn'), reason: 'sanity: Tab reaches the first button');
  events.forEach((_, sent) => sent.clear());
}

// An arrow button is never focused (see the test of its own below).
const _styles = {
  'push': SWT.PUSH,
  'toggle': SWT.TOGGLE,
  'check': SWT.CHECK,
  'radio': SWT.RADIO,
};

void main() {
  setUp(() {
    FocusRequests.instance.reset();
    startDisplayKeyForwarding();
  });
  tearDown(() {
    stopDisplayKeyForwarding();
    FocusRequests.instance.reset();
  });

  for (final MapEntry(key: name, value: style) in _styles.entries) {
    testWidgets('Space presses a focused $name button', (tester) async {
      final events = await _pumpButtons(tester, style);
      await _focusFirst(tester, events);

      await tester.sendKeyEvent(LogicalKeyboardKey.space);
      await tester.pumpAndSettle();

      expect(events[1], ['Selection/Selection']);
    });

    testWidgets('one Tab moves from a $name button to the next control', (tester) async {
      final events = await _pumpButtons(tester, style);
      await _focusFirst(tester, events);

      await tester.sendKeyEvent(LogicalKeyboardKey.tab);
      await tester.pumpAndSettle();

      expect(events[2], contains('Focus/FocusIn'));
    });

    testWidgets('a focus Java asks for lands on a $name button', (tester) async {
      final events = await _pumpButtons(tester, style);

      FocusRequests.instance.request(2);
      await tester.pumpAndSettle();

      expect(events[2], contains('Focus/FocusIn'),
          reason: 'the button Java focused is where Space has to go');
      await tester.sendKeyEvent(LogicalKeyboardKey.space);
      await tester.pumpAndSettle();
      expect(events[2], contains('Selection/Selection'));
    });
  }

  testWidgets('Tab passes an arrow button by, as SWT makes it NO_FOCUS', (tester) async {
    final events = await _pumpButtons(tester, SWT.ARROW | SWT.NO_FOCUS, secondStyle: SWT.PUSH);

    await tester.sendKeyEvent(LogicalKeyboardKey.tab);
    await tester.pumpAndSettle();

    expect(events[1], isNot(contains('Focus/FocusIn')));
    expect(events[2], contains('Focus/FocusIn'));
  });
}
