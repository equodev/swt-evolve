// A CHECK box is flipped optimistically in Flutter and confirmed by Java a round-trip later. The
// confirmation carries the value Flutter already holds, so `setValue` skips the rebuild when the
// semantics tree is on — and it must still adopt the payload's `seq`. Otherwise the state keeps the
// stamp of the update before the confirmation, and the ancestor snapshot serialized while the click
// was in flight outranks it in `didUpdateWidget` and rewinds the box to unchecked.
//
// That race is the Eclipse Preferences composition: the click also moves focus, focus dirties the
// surrounding ScrolledComposite, and its payload is serialized before Java's Selection handler runs.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/button_evolve.dart';

const int _boxId = 77;

VButton _box({required int seq, required bool selection}) => VButton()
  ..swt = 'Button'
  ..id = _boxId
  ..seq = seq
  ..style = SWT.CHECK
  ..enabled = true
  ..selection = selection
  ..text = 'Show heap status'
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 160
    ..height = 20);

VComposite _parent(int seq, List<VControl> children) => VComposite()
  ..swt = 'Composite'
  ..id = 1
  ..seq = seq
  ..style = SWT.NONE
  ..children = children;

/// What Java puts on the wire: the value's own JSON plus the write stamp, which the serializer
/// emits separately (`seq` is out of `toJson` so it never affects value equality).
Map<String, dynamic> _pushed({required int seq, required bool selection}) =>
    {..._box(seq: seq, selection: selection).toJson(), 'seq': seq};

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

bool? _selection(WidgetTester tester) =>
    tester.state<ButtonImpl>(find.byType(ButtonSwt<VButton>)).state.selection;

void main() {
  testWidgets("Java's confirmation outranks the ancestor snapshot that raced it",
      (WidgetTester tester) async {
    // The E2E surface — and any screen reader — builds the semantics tree, which is the only
    // case where setValue takes its skip-the-rebuild path.
    final semantics = tester.ensureSemantics();

    Widget host(VComposite value) => EvolveApp(
          theme: ThemeMode.light,
          contentWidget: SizedBox(
            width: 300,
            height: 60,
            child: CompositeSwt<VComposite>(value: value),
          ),
        );

    await tester.pumpWidget(host(_parent(10, [_box(seq: 11, selection: false)])));
    await tester.pumpAndSettle();
    expect(_selection(tester), isNot(true), reason: 'sanity: the box starts unchecked');

    // The user ticks it: Flutter flips its own copy and sends the Selection to Java.
    await tester.tap(find.text('Show heap status'));
    await tester.pumpAndSettle();
    expect(_selection(tester), isTrue, reason: 'sanity: the optimistic flip happened');

    // Java answers on the box's own channel with the value Flutter already shows — same JSON,
    // newer stamp.
    _receive('Button/$_boxId', _pushed(seq: 30, selection: true));
    await tester.pumpAndSettle();

    // The ancestor was serialized while the click was in flight, so its copy of the box is still
    // unchecked — and its stamp is older than the confirmation's.
    await tester.pumpWidget(host(_parent(29, [_box(seq: 28, selection: false)])));
    await tester.pumpAndSettle();

    expect(_selection(tester), isTrue,
        reason: 'the ancestor snapshot (seq 28) predates the confirmation (seq 30) and must not '
            'rewind the box to unchecked');

    semantics.dispose();
  });
}
