// A frame that arrives before anything is listening for it.
//
// Java writes a widget when it has something to say, not when the client happens to be ready to
// render it, so a payload routinely reaches a channel nobody is subscribed to yet. The comm holds
// it and replays it on the first subscription - and both ways that can go wrong were once real
// bugs, which is why the comm used to refuse to do either and asked Java to re-serialize instead.
//
// These are those two bugs, driven through the delivery path that decides now.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/delivery_gate.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

import 'support/deliver.dart';

VLabel _label(String text, {required int seq}) => VLabel()
  ..id = 9
  ..seq = seq
  ..style = SWT.NONE
  ..text = text
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 160
    ..height = 20);

Future<void> _mount(WidgetTester tester, VLabel value) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 200,
      height: 60,
      child: LabelSwt<VLabel>(value: value),
    ),
  ));
  await tester.pumpAndSettle();
}

void main() {
  testWidgets('content sent before the widget mounts still reaches it', (tester) async {
    // The dialog case: Java describes the content immediately after the update that causes it to
    // be mounted, so the content lands on a channel with nothing listening yet. Dropping it left
    // the dialog empty.
    await deliverFrame('Label/9', {
      'swt': 'Label',
      'id': 9,
      '_s': 20,
      'style': SWT.NONE,
      'text': 'sent before anything was listening',
    });

    await _mount(tester, _label('what the parent was carrying', seq: 10));

    expect(find.text('sent before anything was listening'), findsOneWidget,
        reason: 'the frame was held for exactly this moment, and it describes the widget later '
            'than the copy it was mounted with');
  });

  testWidgets('a snapshot from before the widget was last told anything does not rewind it',
      (tester) async {
    // The other half: a payload buffered long ago, from before the state the widget now holds.
    // Replaying that one blanked a whole pane on its first reveal.
    await _mount(tester, _label('current', seq: 30));
    await deliverWhole(_label('current', seq: 30));
    await tester.pumpAndSettle();

    await deliverFrame('Label/9', {
      'swt': 'Label',
      'id': 9,
      '_s': 5,
      'style': SWT.NONE,
      'text': 'a snapshot from before all this',
    });
    await tester.pumpAndSettle();

    expect(find.text('current'), findsOneWidget);
    expect(deliveryGate.duplicates, greaterThan(0),
        reason: 'refused as a description of the past, which is not an error - nothing has to be '
            'asked for again');
    expect((VRegistry.instance.valueOn('Label/9') as VLabel).text, 'current');
  });

  testWidgets('a change buffered behind the widget it changes does not displace it',
      (tester) async {
    // What an Eclipse startup did a hundred times over. Java writes a widget on its own channel
    // from the moment it counts it delivered, which is well before anything here is listening: a
    // widget delivered inside its parent's payload, or one created hidden. So a whole frame and
    // the changes that follow it can all land on a channel with no subscriber - and holding only
    // the newest dropped the widget and kept a change relative to it, which fits nothing. The
    // client answered the only way it could, by asking for the whole widget all over again.
    freshClient();
    await deliverFrame('Label/9', {
      'swt': 'Label',
      'id': 9,
      '_s': 20,
      'style': SWT.NONE,
      'text': 'as first delivered',
    });
    await deliverFrame('Label/9', {
      'swt': 'Label',
      'id': 9,
      '_s': 21,
      '_b': 20,
      '_d': ['text'],
      'text': 'and changed since',
    });

    await _mount(tester, _label('what the parent was carrying', seq: 10));

    expect(find.text('and changed since'), findsOneWidget);
    expect(deliveryGate.recoveries, 0,
        reason: 'the change was applied to the state it was computed from, which arrived first');
  });
}
