// Probes the "exactly one copy of a widget" property.
//
// A widget can be referenced from more than one place in the serialized tree: a CTabFolder tab body
// is both a Control child of the folder and the `control` of its CTabItem, and a reparent is, for
// one message, a child present under both its old and its new parent. If each reference inflates its
// own value object, the widget exists twice on the client and an update reaches only one of them —
// the other keeps rendering whatever it last saw.
//
// A record names the widget by type and id, so both references name the same widget. This test
// asks what happens then.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/channel_header.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/swt.dart';

import 'support/deliver.dart';

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

    await deliverWhole(_label('after', seq: 9));
    await tester.pumpAndSettle();

    expect(find.text('after'), findsNWidgets(2),
        reason: 'one widget, one state: an update to Label/$_sharedId must reach every place that '
            'renders it, not just whichever subscribed last');
    expect(find.text('before'), findsNothing,
        reason: 'no reference may keep rendering the state the widget has moved on from');
  });
}
