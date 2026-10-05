// Flutter draws a CCombo's list, so Java knows it is open only when told: getListVisible() and the
// open list's keys depend on it. A listVisible Java sends opens or closes the list.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/ccombo.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';

class _CapturingCComboSwt extends CComboSwt<VCCombo> {
  const _CapturingCComboSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VCCombo val, String ev, VEvent? payload) => onEvent(ev, payload);
}

const _id = 12;
int _seq = 0;

VCCombo _combo() => VCCombo()
  ..swt = 'CCombo'
  ..id = _id
  ..style = 0
  ..enabled = true
  ..items = const ['MD', 'TVD', 'TVDSS', 'TWT']
  ..text = 'TVD'
  ..listVisible = false
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 200
    ..height = 24);

Future<List<int?>> _pump(WidgetTester tester) async {
  VRegistry.instance.clear();
  _seq = 0;
  final reports = <int?>[];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Center(
      child: SizedBox(
        width: 200,
        height: 24,
        child: _CapturingCComboSwt(
          value: _combo(),
          onEvent: (ev, payload) {
            if (ev == 'List/Visible') reports.add(payload?.detail);
          },
        ),
      ),
    ),
  ));
  await tester.pumpAndSettle();
  return reports;
}

Future<void> _javaSends(WidgetTester tester, bool listVisible) async {
  final base = _seq;
  _seq += 1;
  VRegistry.instance.apply('CCombo/$_id', <String, dynamic>{
    'id': _id,
    'swt': 'CCombo',
    '_s': _seq,
    '_b': base,
    '_d': <String>['listVisible'],
    'listVisible': listVisible,
  });
  await tester.pumpAndSettle();
}

// DropdownMenu also lays its entries out offstage to size itself; only the open list is hit-testable.
bool _listOpen() => find.text('TWT').hitTestable().evaluate().isNotEmpty;

void main() {
  testWidgets('opening and closing the list is reported to Java', (tester) async {
    final reports = await _pump(tester);

    await tester.tap(find.byIcon(Icons.arrow_drop_down).first);
    await tester.pumpAndSettle();
    expect(_listOpen(), isTrue, reason: 'sanity: the list is open');
    expect(reports, [1]);

    await tester.tap(find.text('TWT').hitTestable());
    await tester.pumpAndSettle();
    expect(_listOpen(), isFalse, reason: 'sanity: picking an item closed the list');
    expect(reports, [1, 0]);
  });

  testWidgets('a listVisible from Java opens and closes the drop-down list', (tester) async {
    final reports = await _pump(tester);

    await _javaSends(tester, true);
    expect(find.byType(DropdownMenu<String>), findsOneWidget,
        reason: 'the combo keeps its drop-down form, not the SIMPLE one');
    expect(_listOpen(), isTrue);

    await _javaSends(tester, false);
    expect(_listOpen(), isFalse);
    expect(reports, isEmpty, reason: 'what Java commanded is not reported back to it');
  });
}
