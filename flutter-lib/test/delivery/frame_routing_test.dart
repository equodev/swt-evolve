// The wiring: a frame arriving on a widget's channel is routed by what it is. Whole frames replace,
// partial ones merge into what is held, ones that do not fit ask for the widget again.
//
// Driven through the real transport - the same bytes the socket would deliver - rather than by
// calling the handler, so the decode, the routing and the rebuild are all in the picture.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/delivery_gate.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/swt.dart';

const int _id = 77;
const String _channel = 'Label/$_id';

VLabel _label(String text, {int seq = 10}) => VLabel()
  ..id = _id
  ..seq = seq
  ..style = SWT.NONE
  ..text = text;

Map<String, dynamic> _whole(String text, int seq) =>
    {'swt': 'Label', 'id': _id, '_s': seq, 'style': SWT.NONE, 'text': text};

Map<String, dynamic> _partial(String text, {required int seq, required int base}) =>
    {'swt': 'Label', 'id': _id, '_s': seq, '_b': base, '_d': ['text'], 'text': text};

/// Delivers a frame exactly as the transport would: 2-byte name length, name, JSON body.
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

  testWidgets('a whole frame replaces what is rendered', (tester) async {
    await _mount(tester, _label('before'));

    _receive(_channel, _whole('after', 11));
    await tester.pumpAndSettle();

    expect(find.text('after'), findsOneWidget);
    expect(deliveryGate.recoveries, 0);
  });

  testWidgets('a partial frame changes only what it names', (tester) async {
    await _mount(tester, _label('before'));
    _receive(_channel, _whole('whole', 11));
    await tester.pumpAndSettle();

    _receive(_channel, _partial('partial', seq: 12, base: 11));
    await tester.pumpAndSettle();

    expect(find.text('partial'), findsOneWidget,
        reason: 'the update named text, so text is what changed - and the widget rebuilt to show it');
    expect(deliveryGate.recoveries, 0,
        reason: 'it fitted the state held, so nothing had to be asked for again');
  });

  testWidgets('a partial frame that does not fit asks for the widget instead of guessing',
      (tester) async {
    await _mount(tester, _label('before'));
    _receive(_channel, _whole('whole', 11));
    await tester.pumpAndSettle();

    // Computed from a state this client never held - a frame was lost, or overtaken.
    _receive(_channel, _partial('never applied', seq: 20, base: 19));
    await tester.pumpAndSettle();

    expect(find.text('whole'), findsOneWidget,
        reason: 'applying a change to a state it was not computed from is silent corruption; the '
            'widget keeps what it had until it is told the whole thing');
    expect(deliveryGate.recoveries, 1);
  });

  testWidgets('a partial frame for a widget never seen whole is refused', (tester) async {
    await _mount(tester, _label('before'));

    // Mounting is not the same as having been told: the value came from the parent's payload, and
    // nothing has arrived on this channel yet.
    _receive(_channel, _partial('partial', seq: 12, base: 11));
    await tester.pumpAndSettle();

    expect(find.text('before'), findsOneWidget);
    expect(deliveryGate.recoveries, 1);
  });

  testWidgets('a repeated frame is dropped rather than re-applied', (tester) async {
    await _mount(tester, _label('before'));
    _receive(_channel, _whole('whole', 11));
    await tester.pumpAndSettle();

    _receive(_channel, _partial('once', seq: 12, base: 11));
    await tester.pumpAndSettle();
    _receive(_channel, _partial('once', seq: 12, base: 11));
    await tester.pumpAndSettle();

    expect(find.text('once'), findsOneWidget);
    expect(deliveryGate.duplicates, 1);
    expect(deliveryGate.recoveries, 0,
        reason: 'a frame arriving twice is redundant, not a delivery failure, and must not send the '
            'client back to Java for a widget it already has');
  });
}
