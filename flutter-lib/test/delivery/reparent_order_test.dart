// A widget that changed parents, when the two parents' frames arrive in either order.
//
// They are siblings, so nothing orders them. A departure is read out of what a frame stops
// carrying, so the old parent's frame says the widget went away and the new parent's says it
// arrived - and whichever is applied first has to leave the widget intact. What makes that possible
// is the new parent describing the widget rather than naming it: a description carries a write
// stamp, and the stamp is what tells a move from a disposal.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/delivery_gate.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/swt.dart';

const int _moverId = 42;
const int _underMoverId = 43;
const String _mover = 'Composite/$_moverId';
const String _underMover = 'Label/$_underMoverId';
const String _oldParent = 'Composite/8';
const String _newParent = 'Composite/7';

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

/// The new parent, whole, carrying the moved widget described - stamp and all, and what is under
/// it with it. A subtree is what actually moves: the part being dragged is a composite with the
/// editor inside it.
Map<String, dynamic> _newParentTakingIt(int seq, int moverSeq) => {
      'swt': 'Composite',
      'id': 7,
      '_s': seq,
      'style': SWT.NONE,
      'children': [
        {
          'swt': 'Composite',
          'id': _moverId,
          '_s': moverSeq,
          'style': SWT.NONE,
          'children': [
            {
              'swt': 'Label',
              'id': _underMoverId,
              '_s': moverSeq + 1,
              'style': SWT.NONE,
              'text': 'the part',
            },
          ],
        },
      ],
    };

/// The old parent, as an update naming the one list that changed.
Map<String, dynamic> _oldParentLettingGo(int seq) => {
      'swt': 'Composite',
      'id': 8,
      '_s': seq,
      '_b': 2,
      '_d': ['children'],
      'children': <Map<String, dynamic>>[],
    };

void main() {
  late VRegistry registry;

  setUp(() {
    deliveryGate.reset();
    registry = VRegistry();
  });
  tearDown(() => registry.clear());

  /// Both parents delivered, the mover - and the subtree under it - held by the old one.
  void settle() {
    final underMover = VLabel()
      ..id = _underMoverId
      ..seq = 1
      ..style = SWT.NONE
      ..text = 'the part';
    final mover = VComposite()
      ..id = _moverId
      ..seq = 1
      ..style = SWT.NONE
      ..children = [underMover];
    registry.register(underMover);
    registry.register(mover);
    registry.register(VComposite()
      ..id = 8
      ..seq = 2
      ..style = SWT.NONE
      ..children = [mover]);
    registry.register(VComposite()
      ..id = 7
      ..seq = 3
      ..style = SWT.NONE
      ..children = []);
    deliveryGate.applied(_oldParent, 2);
    deliveryGate.applied(_newParent, 3);
    deliveryGate.applied(_mover, 1);
    deliveryGate.applied(_underMover, 1);
  }

  void expectTheMoveLanded(String order) {
    expect(registry.isAwaiting(_mover), isFalse,
        reason: '$order: the widget moved; nothing here should be waiting to be told what it is');
    final children = (registry.valueOn(_newParent) as VComposite).children!;
    expect(children.single.id, _moverId,
        reason: "$order: the new parent's list holds the widget");

    // Letting go of a widget lets go of everything under it, so a widget dropped as a disposal
    // takes its subtree with it - which is why one mistaken drop empties a whole stack rather than
    // blanking one thing.
    expect(registry.valueOn(_underMover), isNotNull,
        reason: '$order: the subtree moved with the widget rather than being torn down');
    expect((registry.valueOn(_underMover) as VLabel).text, 'the part',
        reason: '$order: and it kept its state');
  }

  test('the new parent applied first', () async {
    settle();

    await _deliver(_newParent, _newParentTakingIt(18, 20));
    await _deliver(_oldParent, _oldParentLettingGo(19));

    expectTheMoveLanded('new parent first');
  });

  test('the old parent applied first', () async {
    settle();

    await _deliver(_oldParent, _oldParentLettingGo(19));
    await _deliver(_newParent, _newParentTakingIt(18, 20));

    expectTheMoveLanded('old parent first');
  });
}
