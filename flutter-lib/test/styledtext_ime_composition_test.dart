// StyledText must insert characters the OS composes, not just the ones a raw key carries.
//
// On a dead-key layout ("U.S. International – PC": `'` and `"` are dead keys committed with a
// following space) the composed character never appears on a raw key event: the dead key carries
// no character at all, and the committing space carries a space. An editor that inserts only
// `RawKeyEvent.character` therefore types a space where the user asked for a quote — string
// literals cannot be typed. The same mechanism blocks accented characters and every CJK IME.
//
// The platform side of a composition is modelled here the way the engine reports it: a value with
// a valid composing range while the composition is live, then a value with the composing range
// closed carrying the committed text.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/styledtext.dart';
import 'package:swtflutter/src/impl/styledtext_evolve.dart';

void main() {
  VStyledText value(String text, int caretOffset) => VStyledText()
    ..swt = 'StyledText'
    ..id = 1
    ..style = 0
    ..enabled = true
    ..editable = true
    ..caretOffset = caretOffset
    ..text = text
    ..bounds = (VRectangle()
      ..x = 0
      ..y = 0
      ..width = 200
      ..height = 40);

  Widget appWith(GlobalKey<StyledTextImpl> key, VStyledText v) => EvolveApp(
        theme: ThemeMode.light,
        contentWidget: SizedBox(
          width: 200,
          height: 40,
          child: StyledTextSwt<VStyledText>(key: key, value: v),
        ),
      );

  Future<GlobalKey<StyledTextImpl>> startEditing(WidgetTester tester) async {
    final key = GlobalKey<StyledTextImpl>();
    await tester.pumpWidget(appWith(key, value('', 0)));
    // initState doesn't run extraSetState, so a second push is needed to build the text
    // shape the tap enters edit mode on (in production Java always sends this push).
    await tester.pumpWidget(appWith(key, value('', 0)));
    await tester.tap(find.byType(StyledTextSwt<VStyledText>));
    await tester.pump();
    return key;
  }

  testWidgets('a dead-key sequence inserts the composed character, not the committing space',
      (tester) async {
    final key = await startEditing(tester);

    // The dead key itself: a raw key down carrying no character, and a live composition.
    await simulateKeyDownEvent(LogicalKeyboardKey.quote, character: '');
    tester.testTextInput.updateEditingValue(const TextEditingValue(
      text: '"',
      selection: TextSelection.collapsed(offset: 1),
      composing: TextRange(start: 0, end: 1),
    ));
    await simulateKeyUpEvent(LogicalKeyboardKey.quote);
    await tester.pump();

    // The committing space: its raw key down carries a space, and the composition closes on the
    // quote. Only the quote may reach the document.
    await simulateKeyDownEvent(LogicalKeyboardKey.space, character: ' ');
    tester.testTextInput.updateEditingValue(const TextEditingValue(
      text: '"',
      selection: TextSelection.collapsed(offset: 1),
    ));
    await simulateKeyUpEvent(LogicalKeyboardKey.space);
    await tester.pump();

    expect(key.currentState!.state.text, '"',
        reason: 'the composed quote must be inserted and the committing space must not');
  });

  testWidgets('an ordinary keystroke is inserted once, not once per input path', (tester) async {
    final key = await startEditing(tester);

    // No composition: the raw key down inserts, and the platform echo of the same character
    // must not insert a second copy.
    await simulateKeyDownEvent(LogicalKeyboardKey.keyA, character: 'a');
    tester.testTextInput.updateEditingValue(const TextEditingValue(
      text: 'a',
      selection: TextSelection.collapsed(offset: 1),
    ));
    await simulateKeyUpEvent(LogicalKeyboardKey.keyA);
    await tester.pump();

    expect(key.currentState!.state.text, 'a');
  });

  testWidgets('a multi-keystroke composition inserts only its committed text', (tester) async {
    final key = await startEditing(tester);

    // A CJK-style composition: several keystrokes build one composed run. A key routed to an IME
    // carries no character of its own (a browser reports `key: "Process"`), so the run reaches the
    // editor only as composing values and then one commit.
    await simulateKeyDownEvent(LogicalKeyboardKey.keyN, character: '');
    tester.testTextInput.updateEditingValue(const TextEditingValue(
      text: 'n',
      selection: TextSelection.collapsed(offset: 1),
      composing: TextRange(start: 0, end: 1),
    ));
    await simulateKeyUpEvent(LogicalKeyboardKey.keyN);
    await simulateKeyDownEvent(LogicalKeyboardKey.keyI, character: '');
    tester.testTextInput.updateEditingValue(const TextEditingValue(
      text: 'ni',
      selection: TextSelection.collapsed(offset: 2),
      composing: TextRange(start: 0, end: 2),
    ));
    await simulateKeyUpEvent(LogicalKeyboardKey.keyI);
    await tester.pump();

    expect(key.currentState!.state.text, '',
        reason: 'nothing is committed while the composition is still live');

    tester.testTextInput.updateEditingValue(const TextEditingValue(
      text: '你',
      selection: TextSelection.collapsed(offset: 1),
    ));
    await tester.pump();

    expect(key.currentState!.state.text, '你');
  });
}
