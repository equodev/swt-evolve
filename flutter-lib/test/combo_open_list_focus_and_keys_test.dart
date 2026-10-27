// SWT parity: opening a drop-down Combo's list with the mouse focuses the Combo, and the open list
// takes the keyboard -- the arrows move through it, Enter picks, Escape closes -- without SWT seeing
// those keys, so an Enter or Escape meant for the list never presses a dialog's default button or
// closes it.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/combo.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/key_forwarding.dart';

class _CapturingComboSwt extends ComboSwt<VCombo> {
  const _CapturingComboSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VCombo val, String ev, VEvent? payload) => onEvent(ev, payload);
}

VCombo _combo(int style) => VCombo()
  ..swt = 'Combo'
  ..id = 7
  ..style = style
  ..enabled = true
  ..items = const ['MD', 'TVD', 'TVDSS', 'TWT']
  ..text = 'TVD'
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 120
    ..height = 30);

Future<List<(String, VEvent?)>> _pump(WidgetTester tester, int style) async {
  final events = <(String, VEvent?)>[];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Center(
      child: SizedBox(
        width: 120,
        height: 30,
        child: _CapturingComboSwt(
          value: _combo(style),
          onEvent: (ev, payload) => events.add((ev, payload)),
        ),
      ),
    ),
  ));
  await tester.pumpAndSettle();
  return events;
}

Future<void> _openWithArrow(WidgetTester tester) async {
  await tester.tap(find.byIcon(Icons.arrow_drop_down));
  await tester.pumpAndSettle();
  expect(find.text('TWT'), findsOneWidget, reason: 'sanity: the arrow opens the list');
}

void main() {
  const escape = KeyDownEvent(
    physicalKey: PhysicalKeyboardKey.escape,
    logicalKey: LogicalKeyboardKey.escape,
    timeStamp: Duration.zero,
  );

  for (final (name, style) in const [('editable', SWT.DROP_DOWN), ('READ_ONLY', SWT.READ_ONLY)]) {
    testWidgets('$name: opening the list with the arrow focuses the Combo', (tester) async {
      final events = await _pump(tester, style);
      await _openWithArrow(tester);

      expect(events.map((e) => e.$1), contains('Focus/FocusIn'),
          reason: 'the keys must go to the Combo whose list is open, not the previous control');
    });

    testWidgets('$name: ArrowDown then Enter picks the next item', (tester) async {
      final events = await _pump(tester, style);
      await _openWithArrow(tester);

      await tester.sendKeyEvent(LogicalKeyboardKey.arrowDown);
      await tester.pumpAndSettle();
      await tester.sendKeyEvent(LogicalKeyboardKey.enter);
      await tester.pumpAndSettle();

      final selections = events.where((e) => e.$1 == 'Selection/Selection').toList();
      expect(selections.map((e) => e.$2?.text), ['TVDSS']);
      expect(find.text('TWT'), findsNothing, reason: 'picking an item closes the list');
    });

    testWidgets('$name: Escape closes the list without a selection', (tester) async {
      final events = await _pump(tester, style);
      await _openWithArrow(tester);

      await tester.sendKeyEvent(LogicalKeyboardKey.arrowDown);
      await tester.sendKeyEvent(LogicalKeyboardKey.escape);
      await tester.pumpAndSettle();

      expect(find.text('TWT'), findsNothing);
      expect(events.where((e) => e.$1 == 'Selection/Selection'), isEmpty);
    });

    testWidgets('$name: the open list keeps its keys from SWT', (tester) async {
      await _pump(tester, style);
      expect(openListClaimsKey(escape), isFalse, reason: 'a closed list takes no keys');

      await _openWithArrow(tester);
      expect(openListClaimsKey(escape), isTrue,
          reason: 'Escape closes the list, not the dialog around the Combo');

      await tester.sendKeyEvent(LogicalKeyboardKey.escape);
      await tester.pumpAndSettle();
      expect(openListClaimsKey(escape), isFalse);
    });
  }
}
