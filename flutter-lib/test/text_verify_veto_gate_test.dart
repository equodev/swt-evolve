// SWT contract: a VerifyListener runs BEFORE a keystroke's character is displayed — a vetoed
// keystroke is never rendered. The async bridge applies edits optimistically, so a vetoed
// character was rendered first and only reverted a round-trip later (and the revert push could
// be swallowed by the stale-echo guard, leaving the vetoed text on screen).
//
// Java flags a Verify-hooked field with `{swt}/{id}/modify/vetoable`; the field then holds each
// edit un-rendered, forwards it as a Modify proposal, and applies or drops it on the
// `{swt}/{id}/modify/verdict` answer. A `doit:false` verdict on an already-applied edit (no
// vetoable flag — e.g. the embedded backend) instead clears the echo guard so Java's corrective
// push is applied rather than mistaken for a stale echo.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
// The io transport directly: `flutter test` runs on the VM, where comm.dart resolves to it —
// but the analyzer resolves the conditional export to the web variant, which has no test hook.
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/text.dart';

VRectangle _bounds(int w, int h) => VRectangle()
  ..x = 0
  ..y = 0
  ..width = w
  ..height = h;

Widget _app(Widget child) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: child,
    );

VText _value(String text) => VText()
  ..swt = 'Text'
  ..id = 1
  ..style = 0 // no READ_ONLY -> editable
  ..enabled = true
  ..editable = true
  ..text = text
  ..bounds = _bounds(200, 30);

Widget _appWith(VText v) => _app(SizedBox(
      width: 200,
      height: 30,
      child: TextSwt<VText>(value: v),
    ));

/// Delivers an inbound frame exactly as the transport would (2-byte name length,
/// name, JSON body) so the widget's channel subscriptions receive it.
void _receiveJson(String actionId, Object payload) {
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
  testWidgets('a vetoable field never renders an edit until Java allows it',
      (tester) async {
    await tester.pumpWidget(_appWith(_value('1234')));

    // Java: this field has a VerifyListener — edits need a verdict.
    _receiveJson('Text/1/modify/vetoable', {'value': true});
    await tester.pump();

    final field = find.byType(EditableText);
    await tester.enterText(field, '12347');
    await tester.pump();

    expect(find.text('12347'), findsNothing,
        reason: 'SWT parity: the edit must not render before the Verify verdict');
    expect(find.text('1234'), findsOneWidget);

    // Java's VerifyListener vetoed (e.g. 12347 > 9999): the edit is dropped.
    _receiveJson('Text/1/modify/verdict', {'doit': false});
    await tester.pump();

    expect(find.text('12347'), findsNothing,
        reason: 'a vetoed keystroke is never displayed');
    expect(find.text('1234'), findsOneWidget);

    // An allowed edit still goes through on doit:true.
    await tester.enterText(field, '1239');
    await tester.pump();
    expect(find.text('1239'), findsNothing,
        reason: 'still gated until the verdict');

    _receiveJson('Text/1/modify/verdict', {'doit': true});
    await tester.pump();
    expect(find.text('1239'), findsOneWidget,
        reason: 'an accepted edit is applied on the verdict');
  });

  testWidgets(
      'a doit:false verdict on an applied edit lets the corrective push through',
      (tester) async {
    await tester.pumpWidget(_appWith(_value('1234')));

    // No vetoable flag (embedded backend): the edit applies optimistically.
    final field = find.byType(EditableText);
    await tester.enterText(field, '12347');
    await tester.pump();
    expect(find.text('12347'), findsOneWidget,
        reason: 'sanity: ungated edit applies locally');

    // Java rejected the Modify; its corrective push carries the pre-edit text,
    // which the stale-echo guard would otherwise swallow (the focus baseline).
    _receiveJson('Text/1/modify/verdict', {'doit': false});
    await tester.pump();
    await tester.pumpWidget(_appWith(_value('1234')));
    await tester.pump();

    expect(find.text('1234'), findsOneWidget,
        reason: "Java's rejection push must revert the field, not be treated "
            'as a stale echo');
    expect(find.text('12347'), findsNothing);
  });
}
