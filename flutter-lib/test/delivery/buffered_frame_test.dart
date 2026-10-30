// A record that arrives before anything renders the widget it describes.
//
// Java writes a widget when it has something to say, not when the client happens to be ready to
// render it, so a record routinely describes a widget nothing has mounted yet. The registry holds
// it, and the widget reads it once it mounts - and both ways that can go wrong were once real bugs:
// losing what arrived first, and letting an older copy rewind what is held.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/delivery_gate.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;

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
    // be mounted, so the content arrives before anything renders it. Dropping it left the dialog
    // empty.
    await deliverWhole(_label('sent before anything was listening', seq: 20));

    await _mount(tester, _label('what the parent was carrying', seq: 10));

    expect(find.text('sent before anything was listening'), findsOneWidget,
        reason: 'the record was held for exactly this moment, and it describes the widget later '
            'than the copy it was mounted with');
  });

  testWidgets('a snapshot from before the widget was last told anything does not rewind it',
      (tester) async {
    // The other half: a description from before the state the widget now holds. Taking that one
    // blanked a whole pane on its first reveal.
    await _mount(tester, _label('current', seq: 30));
    await deliverWhole(_label('current', seq: 30));
    await tester.pumpAndSettle();

    await deliverWhole(_label('a snapshot from before all this', seq: 5));
    await tester.pumpAndSettle();

    expect(find.text('current'), findsOneWidget);
    expect(deliveryGate.duplicates, greaterThan(0),
        reason: 'refused as a description of the past, which is not an error - nothing has to be '
            'asked for again');
    expect((VRegistry.instance.valueOn('Label/9') as VLabel).text, 'current');
  });

  testWidgets('a change arriving before the widget mounts does not displace it', (tester) async {
    // What a large application's startup did a hundred times over: a widget delivered inside its
    // parent, or one created hidden, is described whole and then changed well before anything
    // renders it. Holding only the newest dropped the widget and kept a change relative to it,
    // which fits nothing, and the client had to ask for the whole widget all over again.
    freshClient();
    await deliverWhole(_label('as first delivered', seq: 20));
    await deliverChange(_label('and changed since', seq: 21), keys: [WireKey.k_Label_text], base: 20);

    await _mount(tester, _label('what the parent was carrying', seq: 10));

    expect(find.text('and changed since'), findsOneWidget);
    expect(deliveryGate.recoveries, 0,
        reason: 'the change was applied to the state it was computed from, which arrived first');
  });
}
