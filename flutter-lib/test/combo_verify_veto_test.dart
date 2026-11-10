// SWT contract: a Verify listener's veto keeps the text it refused out of the field. An editable
// Combo applies a typed edit or a picked item at once and asks Java after, so a veto has to undo
// it: Java pushes the text it kept, then answers `{swt}/{id}/modify/verdict` with doit:false. The
// push carries the text the field showed before the edit, which the stale-echo guard would take
// for an echo of its own, so the verdict is what puts the kept text back.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
// The io transport directly: `flutter test` runs on the VM, where comm.dart resolves to it --
// but the analyzer resolves the conditional export to the web variant, which has no test hook.
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/tagged_values.dart';
import 'package:swtflutter/src/gen/combo.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

import 'delivery/support/deliver.dart';
import 'support/channels.dart' as channels;

VCombo _value(String text, {int seq = 1}) => VCombo()
  ..swt = 'Combo'
  ..id = 7
  ..seq = seq
  ..style = SWT.DROP_DOWN
  ..enabled = true
  ..items = const ['alpha', 'bravo', 'charlie']
  ..text = text
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 160
    ..height = 30);

void _deliver(String actionId, Object payload) {
  EquoCommService.commForTesting.receiveBinary(channels.frame(actionId, TaggedValues.encode(payload)));
}

Future<void> _pump(WidgetTester tester) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Center(child: SizedBox(width: 160, height: 30, child: ComboSwt<VCombo>(value: _value('alpha')))),
  ));
  await tester.pumpAndSettle();
}

String _shown(WidgetTester tester) => tester.widget<EditableText>(find.byType(EditableText)).controller.text;

/// What Java does on a veto: it pushes the text it kept, then answers the verdict.
Future<void> _javaRefuses(WidgetTester tester, String kept) async {
  await deliverWhole(_value(kept, seq: 2));
  await tester.pump();
  _deliver('Combo/7/modify/verdict', {'doit': false});
  await tester.pumpAndSettle();
}

void main() {
  testWidgets('a vetoed typed edit puts back the text Java kept', (tester) async {
    await _pump(tester);
    await tester.tap(find.byType(EditableText));
    await tester.pump();
    await tester.enterText(find.byType(EditableText), 'alphax');
    await tester.pump();
    expect(_shown(tester), 'alphax', reason: 'sanity: the edit shows at once');

    await _javaRefuses(tester, 'alpha');

    expect(_shown(tester), 'alpha');
  });

  testWidgets('a vetoed pick puts back the text Java kept', (tester) async {
    await _pump(tester);
    await tester.tap(find.byType(EditableText));
    await tester.pump();
    await tester.tap(find.byIcon(Icons.arrow_drop_down));
    await tester.pumpAndSettle();
    await tester.tap(find.text('charlie'));
    await tester.pumpAndSettle();
    expect(_shown(tester), 'charlie', reason: 'sanity: the pick shows at once');

    await _javaRefuses(tester, 'alpha');

    expect(_shown(tester), 'alpha');
  });
}
