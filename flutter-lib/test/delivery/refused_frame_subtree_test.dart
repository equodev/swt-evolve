// What happens to the widgets a frame carried when the frame itself cannot be applied.
//
// A partial update names the state it was computed from, and one that does not match what is held
// cannot be merged - the widget is asked for again instead. But the frame still carries complete
// descriptions of the widgets beneath it, and those are the only descriptions they will ever get:
// the sender counts a widget it wrote as delivered and names it from then on. Dropping them leaves
// each one held by nobody and subscribed to by nobody, which nothing afterwards repairs - the
// answer to a request for one arrives on a channel with no listener. One refused frame would orphan
// a whole subtree for good, and every later update to any of it is a recovery that cannot recover.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/delivery_gate.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/swt.dart';

const String _parent = 'Composite/7';
const String _carried = 'Label/101';

Future<void> _deliver(String actionId, Object payload) async {
  final actionBytes = utf8.encode(actionId);
  final body = utf8.encode(json.encode(payload));
  final frame = Uint8List(2 + actionBytes.length + body.length);
  frame[0] = (actionBytes.length >> 8) & 0xFF;
  frame[1] = actionBytes.length & 0xFF;
  frame.setRange(2, 2 + actionBytes.length, actionBytes);
  frame.setRange(2 + actionBytes.length, frame.length, body);
  EquoCommService.commForTesting.receiveBinary(frame);
  await Future<void>.delayed(Duration.zero);
}

/// An update to the parent computed from a state this client does not hold, carrying two widgets
/// nothing has ever described.
Map<String, dynamic> _refusedFrameCarryingChildren() => {
      'swt': 'Composite',
      'id': 7,
      '_s': 40,
      '_b': 39, // the client holds 2, so this fits nothing
      '_d': ['children'],
      'children': [
        {'swt': 'Label', 'id': 101, '_s': 41, 'style': SWT.NONE, 'text': 'one'},
        {'swt': 'Label', 'id': 102, '_s': 42, 'style': SWT.NONE, 'text': 'two'},
      ],
    };

void main() {
  late VRegistry registry;

  setUp(() {
    deliveryGate.reset();
    registry = VRegistry();
    registry.register(VComposite()
      ..id = 7
      ..seq = 2
      ..style = SWT.NONE
      ..children = []);
    deliveryGate.applied(_parent, 2);
  });
  tearDown(() => registry.clear());

  test('a widget described inside a refused frame is still held', () async {
    await _deliver(_parent, _refusedFrameCarryingChildren());

    expect(deliveryGate.recoveries, 1, reason: 'the parent itself is asked for again');
    expect((registry.valueOn(_carried) as VLabel?)?.text, 'one',
        reason: 'the description it carried is the only one this widget will ever be sent');
    expect((registry.valueOn('Label/102') as VLabel?)?.text, 'two');
    expect(deliveryGate.heldSeq(_carried), 41,
        reason: 'and the stamp with it, or the next update to it fits nothing either');
  });

  test('and its own next update merges instead of recovering again', () async {
    await _deliver(_parent, _refusedFrameCarryingChildren());

    await _deliver(_carried, {
      'swt': 'Label',
      'id': 101,
      '_s': 43,
      '_b': 41,
      '_d': ['text'],
      'text': 'one changed',
    });

    expect((registry.valueOn(_carried) as VLabel).text, 'one changed');
    expect(deliveryGate.recoveries, 1,
        reason: 'one refused frame must not turn every widget under it into a recovery of its '
            'own - that is the cascade this guards against');
  });

  test('only the frame itself is asked for again, not what it carried', () async {
    await _deliver(_parent, _refusedFrameCarryingChildren());

    expect(deliveryGate.recoveries, 1,
        reason: 'both widgets the frame carried would otherwise have been orphaned, and each one '
            'asked for separately');
    expect(deliveryGate.heldSeq('Label/102'), 42,
        reason: 'held on its own stamp, so it is a widget this client has rather than one it '
            'still has to ask about');
  });
}
