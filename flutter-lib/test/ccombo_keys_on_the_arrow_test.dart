// Tab leaves a READ_ONLY CCombo's focus on its arrow, since the field takes none. Enter and Space
// there are the field's keys, which Java answers: they must not press the arrow and open the list.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
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

// DropdownMenu also lays its entries out offstage to size itself; only the open list is hit-testable.
bool _listOpen() => find.text('TWT').hitTestable().evaluate().isNotEmpty;

Future<List<String>> _tabIntoReadOnlyCombo(WidgetTester tester) async {
  VRegistry.instance.clear();
  final events = <String>[];
  await tester.pumpWidget(
    EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Column(
        children: [
          const SizedBox(width: 200, child: TextField(autofocus: true)),
          SizedBox(
            width: 200,
            height: 24,
            child: _CapturingCComboSwt(
              value: VCCombo()
                ..swt = 'CCombo'
                ..id = 31
                ..style = SWT.READ_ONLY
                ..enabled = true
                ..items = const ['MD', 'TVD', 'TVDSS', 'TWT']
                ..text = 'TVD'
                ..listVisible = false
                ..bounds = (VRectangle()
                  ..x = 0
                  ..y = 0
                  ..width = 200
                  ..height = 24),
              onEvent: (ev, payload) => events.add(ev),
            ),
          ),
        ],
      ),
    ),
  );
  await tester.pumpAndSettle();
  await tester.sendKeyEvent(LogicalKeyboardKey.tab);
  await tester.pumpAndSettle();
  return events;
}

void main() {
  for (final (name, key) in [
    ('Enter', LogicalKeyboardKey.enter),
    ('Space', LogicalKeyboardKey.space),
  ]) {
    testWidgets('$name on a READ_ONLY CCombo reached by Tab leaves the list closed', (
      tester,
    ) async {
      final events = await _tabIntoReadOnlyCombo(tester);
      expect(events, contains('Focus/FocusIn'), reason: 'sanity: Tab reached the combo');

      await tester.sendKeyEvent(key);
      await tester.pumpAndSettle();

      expect(_listOpen(), isFalse);
      expect(events, isNot(contains('List/Visible')));
    });
  }

  testWidgets('a click on the arrow still opens the list', (tester) async {
    await _tabIntoReadOnlyCombo(tester);

    await tester.tap(find.byIcon(Icons.arrow_drop_down).first);
    await tester.pumpAndSettle();

    expect(_listOpen(), isTrue);
  });
}
