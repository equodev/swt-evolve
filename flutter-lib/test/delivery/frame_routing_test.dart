// The wiring: a widget record is routed by what it is. Whole records replace, changes merge into
// what is held, ones that do not fit ask for the widget again.
//
// Driven by the bytes Java writes - a frame of records - rather than by calling the handler, so the
// decode, the routing and the rebuild are all in the picture.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/channel_header.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/delivery_gate.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;

import 'support/deliver.dart';

const int _id = 77;
const String _channel = 'Label/$_id';

VLabel _label(String text, {int seq = 10}) => VLabel()
  ..id = _id
  ..seq = seq
  ..style = SWT.NONE
  ..text = text;

Future<void> _mount(WidgetTester tester, VLabel value) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 200,
      height: 40,
      child: LabelSwt<VLabel>(value: value),
    ),
  ));
  await tester.pumpAndSettle();
}

void main() {
  setUp(() => deliveryGate.reset());

  testWidgets('a whole record replaces what is rendered', (tester) async {
    await _mount(tester, _label('before'));

    await deliverWhole(_label('after', seq: 11));
    await tester.pumpAndSettle();

    expect(find.text('after'), findsOneWidget);
    expect(deliveryGate.recoveries, 0);
  });

  testWidgets('a change changes only what it names', (tester) async {
    await _mount(tester, _label('before'));
    await deliverWhole(_label('whole', seq: 11));
    await tester.pumpAndSettle();

    await deliverChange(_label('partial', seq: 12), keys: [WireKey.k_Label_text], base: 11);
    await tester.pumpAndSettle();

    expect(find.text('partial'), findsOneWidget,
        reason: 'the update named text, so text is what changed - and the widget rebuilt to show it');
    expect(deliveryGate.recoveries, 0,
        reason: 'it fitted the state held, so nothing had to be asked for again');
  });

  testWidgets('a change that does not fit asks for the widget instead of guessing',
      (tester) async {
    await _mount(tester, _label('before'));
    await deliverWhole(_label('whole', seq: 11));
    await tester.pumpAndSettle();

    // Computed from a state this client never held - a frame was lost, or overtaken.
    await deliverChange(_label('never applied', seq: 20), keys: [WireKey.k_Label_text], base: 19);
    await tester.pumpAndSettle();

    expect(find.text('whole'), findsOneWidget,
        reason: 'applying a change to a state it was not computed from is silent corruption; the '
            'widget keeps what it had until it is told the whole thing');
    expect(deliveryGate.recoveries, 1);
  });

  testWidgets('a change for a widget never seen whole is refused', (tester) async {
    await _mount(tester, _label('before'));

    // Mounting is not the same as having been told: the value came from the parent's payload, and
    // nothing has described it yet.
    await deliverChange(_label('partial', seq: 12), keys: [WireKey.k_Label_text], base: 11);
    await tester.pumpAndSettle();

    expect(find.text('before'), findsOneWidget);
    expect(deliveryGate.recoveries, 1);
  });

  testWidgets('a repeated change is dropped rather than re-applied', (tester) async {
    await _mount(tester, _label('before'));
    await deliverWhole(_label('whole', seq: 11));
    await tester.pumpAndSettle();

    await deliverChange(_label('once', seq: 12), keys: [WireKey.k_Label_text], base: 11);
    await tester.pumpAndSettle();
    await deliverChange(_label('once', seq: 12), keys: [WireKey.k_Label_text], base: 11);
    await tester.pumpAndSettle();

    expect(find.text('once'), findsOneWidget);
    expect(deliveryGate.duplicates, 1);
    expect(deliveryGate.recoveries, 0,
        reason: 'a frame arriving twice is redundant, not a delivery failure, and must not send the '
            'client back to Java for a widget it already has');
  });
}
