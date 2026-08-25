// Whether a Combo's dropdown is open is local UI state: Java only drives it through
// Combo.setListVisible, and every routine value push carries the flag it always carries. Treating
// such a push as a command closed the dropdown a click had just opened, so picking an item worked
// or not depending on whether an unrelated push landed in between.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/combo.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/combo_evolve.dart';

const int _comboId = 91;

VCombo _readOnlyCombo({required int seq, bool? listVisible}) => VCombo()
  ..swt = 'Combo'
  ..id = _comboId
  ..seq = seq
  ..style = SWT.READ_ONLY
  ..enabled = true
  ..items = const ['Combo A', 'Combo B', 'Combo C']
  ..text = 'Combo A'
  ..listVisible = listVisible
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 160
    ..height = 34);

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

Future<void> _pumpCombo(WidgetTester tester) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Center(
      child: SizedBox(
        width: 160,
        height: 34,
        child: ComboSwt<VCombo>(value: _readOnlyCombo(seq: 1)),
      ),
    ),
  ));
  await tester.pumpAndSettle();
}

void main() {
  testWidgets('a value push does not close a dropdown the user opened', (tester) async {
    await _pumpCombo(tester);

    await tester.tap(find.byType(ComboSwt<VCombo>));
    await tester.pumpAndSettle();
    expect(find.text('Combo B'), findsOneWidget,
        reason: 'sanity: tapping a read-only Combo opens its dropdown');

    // Any unrelated state push for this Combo. It carries listVisible exactly as Java always
    // sends it -- Java never learns the user opened the list.
    _receive('Combo/$_comboId', {..._readOnlyCombo(seq: 2).toJson(), 'seq': 2});
    await tester.pumpAndSettle();

    expect(find.text('Combo B'), findsOneWidget,
        reason: 'the dropdown stays open: the push carried no change of listVisible, so it is '
            'not a setListVisible(false) command');
  });

  testWidgets('Java closing the list through setListVisible is still honoured', (tester) async {
    await _pumpCombo(tester);

    await tester.tap(find.byType(ComboSwt<VCombo>));
    await tester.pumpAndSettle();
    expect(find.text('Combo B'), findsOneWidget);

    _receive('Combo/$_comboId',
        {..._readOnlyCombo(seq: 2, listVisible: true).toJson(), 'seq': 2});
    await tester.pumpAndSettle();
    _receive('Combo/$_comboId',
        {..._readOnlyCombo(seq: 3, listVisible: false).toJson(), 'seq': 3});
    await tester.pumpAndSettle();

    expect(find.text('Combo B'), findsNothing,
        reason: 'a genuine change of listVisible between two pushes closes the dropdown');
  });
}
