// A CCombo whose list is open has Java focus, so Java receives the arrow keys and moves the
// selection as native CCombo does; the menu's own keyboard highlight must not overwrite the field.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/ccombo.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

class _CapturingCComboSwt extends CComboSwt<VCCombo> {
  const _CapturingCComboSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VCCombo val, String ev, VEvent? payload) => onEvent(ev, payload);
}

VCCombo _combo(int style) => VCCombo()
  ..swt = 'CCombo'
  ..id = 11
  ..style = style
  ..enabled = true
  ..items = const ['MD', 'TVD', 'TVDSS', 'TWT']
  ..text = 'TVD'
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 200
    ..height = 24);

Future<List<(String, String?)>> _openAndPress(
    WidgetTester tester, VCCombo value, List<LogicalKeyboardKey> keys) async {
  final events = <(String, String?)>[];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Center(
      child: SizedBox(
        width: 200,
        height: 24,
        child: _CapturingCComboSwt(
          value: value,
          onEvent: (ev, payload) => events.add((ev, payload?.text)),
        ),
      ),
    ),
  ));
  await tester.pumpAndSettle();

  await tester.tap(find.byIcon(Icons.arrow_drop_down).first);
  await tester.pumpAndSettle();
  expect(find.text('TWT'), findsWidgets, reason: 'sanity: the list is open');

  for (final key in keys) {
    await tester.sendKeyEvent(key);
    await tester.pumpAndSettle();
  }
  return events;
}

String _field(WidgetTester tester) =>
    tester.widget<EditableText>(find.byType(EditableText).first).controller.text;

void main() {
  const down = LogicalKeyboardKey.arrowDown;

  for (final (name, style) in [('editable', 0), ('READ_ONLY', SWT.READ_ONLY)]) {
    testWidgets('$name: opening the list gives the combo focus', (tester) async {
      final events = await _openAndPress(tester, _combo(style), []);

      expect(events.map((e) => e.$1), contains('Focus/FocusIn'),
          reason: 'Java routes the arrow keys to its focus control; without a FocusIn they never '
              'reach this combo while its list is open');
      expect(events.map((e) => e.$1), isNot(contains('Focus/FocusOut')));
    });

    testWidgets('$name: an arrow key leaves the field on the selection Java owns', (tester) async {
      final events = await _openAndPress(tester, _combo(style), [down, down]);

      expect(_field(tester), 'TVD',
          reason: 'the menu highlight is not the SWT selection; Java moves it and pushes it back');
      expect(events.where((e) => e.$1.startsWith('Selection/')), isEmpty,
          reason: 'Java selects from the same key; a second Selection from here duplicates it');
    });

    testWidgets('$name: Enter on the open list keeps the selected item', (tester) async {
      final value = _combo(style);
      final events = await _openAndPress(tester, value, [down, LogicalKeyboardKey.enter]);

      expect(value.text, 'TVD');
      expect(_field(tester), 'TVD');
      expect(events.where((e) => e.$1.startsWith('Selection/') && e.$2 != 'TVD'), isEmpty,
          reason: 'Enter must not report an empty selection');
    });
  }
}
