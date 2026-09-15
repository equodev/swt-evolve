// Probes the "exactly one copy of a widget" property.
//
// A widget can be referenced from more than one place in the serialized tree: a CTabFolder tab body
// is both a Control child of the folder and the `control` of its CTabItem, and a reparent is, for
// one message, a child present under both its old and its new parent. If each reference inflates its
// own value object, the widget exists twice on the client and an update reaches only one of them —
// the other keeps rendering whatever it last saw.
//
// The channel is keyed by `{swt}/{id}`, so both copies subscribe to the same name. This test asks
// what happens then.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/swt.dart';

const int _sharedId = 777;

VLabel _label(String text, {int seq = 1}) => VLabel()
  ..id = _sharedId
  ..seq = seq
  ..style = SWT.NONE
  ..text = text;

VComposite _parent(int id, List<VControl> children) => VComposite()
  ..id = id
  ..seq = 1
  ..style = SWT.NONE
  ..children = children;

/// Delivers an inbound frame exactly as the transport would: 2-byte name length, name, JSON body.
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

void main() {
  testWidgets('an update reaches every place the widget is referenced from',
      (WidgetTester tester) async {
    // The same label id under two parents - what a tab body, or a reparent in flight, looks like.
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Column(children: [
        SizedBox(
            width: 200,
            height: 40,
            child: CompositeSwt<VComposite>(value: _parent(1, [_label('before')]))),
        SizedBox(
            width: 200,
            height: 40,
            child: CompositeSwt<VComposite>(value: _parent(2, [_label('before')]))),
      ]),
    ));
    await tester.pumpAndSettle();
    expect(find.text('before'), findsNWidgets(2), reason: 'both references render');

    _receive('Label/$_sharedId', {
      'swt': 'Label',
      'id': _sharedId,
      '_s': 9,
      'style': SWT.NONE,
      'text': 'after',
    });
    await tester.pumpAndSettle();

    expect(find.text('after'), findsNWidgets(2),
        reason: 'one widget, one state: an update on Label/$_sharedId must reach every place that '
            'renders it, not just whichever subscribed last');
    expect(find.text('before'), findsNothing,
        reason: 'no reference may keep rendering the state the widget has moved on from');
  });
}
