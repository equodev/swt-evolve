// PendingTextEchoes recognises Java's echo of an edit we forwarded and refuses to let it
// clobber newer local keystrokes. It drained each recognised value on sight — but Java pushes
// full-state snapshots for reasons other than acknowledging a Modify (a ControlHelper.paint
// -driven dirty() is the common one), so the SAME acknowledged text arrives again. Once
// drained, that re-send is no longer recognised, reads as a genuine external setText, and
// re-bases the editor onto it — discarding everything typed since it was acknowledged.
//
// Two symptoms came out of that: characters silently dropped, and — because a Modify carries a
// range computed against the client's own text — a client whose buffer was rewound sends the
// next keystroke's range against a document Java no longer has, landing the character at the
// wrong offset. Neither depends on frame time; both get likelier as the round trip lengthens.
//
// The matched value is kept as the new baseline so a re-send is still recognised as an echo,
// while anything never sent still applies as an external change.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/styledtext.dart';
import 'package:swtflutter/src/impl/styledtext_evolve.dart';

void main() {
  late GlobalKey<StyledTextImpl> key;
  late int nonce;

  VStyledText value(String text) => VStyledText()
    ..swt = 'StyledText'
    ..id = 1
    ..style = 0
    ..enabled = true
    ..editable = true
    ..caretOffset = text.length
    ..text = text
    // A field the editor does not read, varied so two pushes of the same text are two
    // distinct values here; over the wire every push is its own payload regardless.
    ..topPixel = nonce++
    ..bounds = (VRectangle()
      ..x = 0
      ..y = 0
      ..width = 200
      ..height = 40);

  Widget appWith(VStyledText v) => EvolveApp(
        theme: ThemeMode.light,
        contentWidget: SizedBox(
          width: 200,
          height: 40,
          child: StyledTextSwt<VStyledText>(key: key, value: v),
        ),
      );

  Future<void> startEditing(WidgetTester tester) async {
    await tester.pumpWidget(appWith(value('')));
    // initState doesn't run extraSetState, so a second push builds the text shape the tap
    // enters edit mode on (in production Java always sends this push).
    await tester.pumpWidget(appWith(value('')));
    await tester.tap(find.byType(StyledTextSwt<VStyledText>));
    await tester.pump();
  }

  Future<void> type(WidgetTester tester, LogicalKeyboardKey k, String ch) async {
    // The explicit `character` is required: the editor inserts from RawKeyEvent.character,
    // which a bare sendKeyEvent leaves null.
    await simulateKeyDownEvent(k, character: ch);
    await simulateKeyUpEvent(k);
    await tester.pump();
  }

  setUp(() {
    key = GlobalKey<StyledTextImpl>();
    nonce = 0;
  });

  testWidgets('a re-sent acknowledged state does not discard newer keystrokes',
      (tester) async {
    await startEditing(tester);
    await type(tester, LogicalKeyboardKey.keyA, 'a');
    await type(tester, LogicalKeyboardKey.keyB, 'b');
    expect(key.currentState!.state.text, 'ab', reason: 'sanity: keystrokes landed');

    // Java catches up and acknowledges the current state.
    await tester.pumpWidget(appWith(value('ab')));
    await tester.pump();

    await type(tester, LogicalKeyboardKey.keyC, 'c');
    expect(key.currentState!.state.text, 'abc', reason: 'sanity: keystrokes landed');

    // A repaint-driven push re-sends Java's current state — still 'ab', because the Modify
    // carrying 'c' has not been applied yet.
    await tester.pumpWidget(appWith(value('ab')));
    await tester.pump();

    expect(key.currentState!.state.text, 'abc',
        reason: 'a second push of an already-acknowledged state is still our own echo, '
            'not an external change to re-base onto');
  });

  testWidgets('an external change to a value we never sent still applies',
      (tester) async {
    await startEditing(tester);
    await type(tester, LogicalKeyboardKey.keyA, 'a');
    expect(key.currentState!.state.text, 'a', reason: 'sanity: keystroke landed');

    // A programmatic setText mid-edit — SWT swaps the content even while the user types.
    await tester.pumpWidget(appWith(value('replaced')));
    await tester.pump();

    expect(key.currentState!.state.text, 'replaced',
        reason: 'keeping the acknowledged baseline must not swallow a genuine external '
            'change; only values we actually sent are echoes');
  });

  testWidgets('an undo back past the acknowledged state still applies',
      (tester) async {
    await startEditing(tester);
    await type(tester, LogicalKeyboardKey.keyA, 'a');
    await type(tester, LogicalKeyboardKey.keyB, 'b');
    await tester.pumpWidget(appWith(value('ab')));
    await tester.pump();
    expect(key.currentState!.state.text, 'ab', reason: 'sanity: acknowledged');

    // Java-side undo returns the document to a state older than the baseline we hold.
    await tester.pumpWidget(appWith(value('')));
    await tester.pump();

    expect(key.currentState!.state.text, '',
        reason: 'an undo lands on a value no longer in flight, so it reads as the external '
            'change it is');
  });
}
