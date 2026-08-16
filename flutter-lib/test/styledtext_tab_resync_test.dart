// Pressing Tab in a multi-line editable StyledText must not move focus off the editor.
//
// SWT keeps Tab as content there instead of traversing (StyledText.handleTraverse), and Java
// relies on it: the Flutter editor skips control characters, so Java applies the tab itself.
// Flutter's focus traversal has no such rule — before the fix the first Tab moved focus to the
// next focusable widget, so every following keystroke was stranded (no Key/VerifyKey/Modify
// reached Java at all) until the user clicked back into the editor.
//
// The sibling Focus below is load-bearing: with a single focusable in the tree, traversal has
// nowhere to go and the bug cannot reproduce.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/styledtext.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/styledtext_evolve.dart';

void main() {
  VStyledText value(String text, int caretOffset, {int style = 0}) => VStyledText()
    ..swt = 'StyledText'
    ..id = 1
    ..style = style
    ..enabled = true
    ..editable = true
    ..caretOffset = caretOffset
    ..text = text
    ..bounds = (VRectangle()
      ..x = 0
      ..y = 0
      ..width = 200
      ..height = 40);

  Widget appWith(GlobalKey<StyledTextImpl> key, VStyledText v, FocusNode sibling) => EvolveApp(
        theme: ThemeMode.light,
        contentWidget: Column(
          children: [
            SizedBox(
              width: 200,
              height: 40,
              child: StyledTextSwt<VStyledText>(key: key, value: v),
            ),
            SizedBox(
              width: 200,
              height: 40,
              child: Focus(focusNode: sibling, child: const SizedBox.expand()),
            ),
          ],
        ),
      );

  Future<GlobalKey<StyledTextImpl>> startEditing(
      WidgetTester tester, FocusNode sibling, {int style = 0}) async {
    final key = GlobalKey<StyledTextImpl>();
    await tester.pumpWidget(appWith(key, value('', 0, style: style), sibling));
    // initState doesn't run extraSetState, so a second push is needed to build the text
    // shape the tap enters edit mode on (in production Java always sends this push).
    await tester.pumpWidget(appWith(key, value('', 0, style: style), sibling));
    await tester.tap(find.byType(StyledTextSwt<VStyledText>));
    await tester.pump();
    return key;
  }

  testWidgets('StyledText keeps focus and keeps accepting keystrokes after a Tab',
      (tester) async {
    final sibling = FocusNode();
    addTearDown(sibling.dispose);
    final key = await startEditing(tester, sibling);

    await simulateKeyDownEvent(LogicalKeyboardKey.keyA, character: 'a');
    await simulateKeyUpEvent(LogicalKeyboardKey.keyA);
    await tester.pump();
    expect(key.currentState!.state.text, 'a',
        reason: 'sanity: keystrokes reach the local buffer before the Tab');

    await simulateKeyDownEvent(LogicalKeyboardKey.tab, character: '\t');
    await simulateKeyUpEvent(LogicalKeyboardKey.tab);
    await tester.pump();

    expect(sibling.hasFocus, isFalse,
        reason: 'Tab must not traverse out of a multi-line editable StyledText');

    // Java applies the tab itself and pushes the tabbed text back.
    await tester.pumpWidget(appWith(key, value('a\t', 2), sibling));
    await tester.pump();

    await simulateKeyDownEvent(LogicalKeyboardKey.keyB, character: 'b');
    await simulateKeyUpEvent(LogicalKeyboardKey.keyB);
    await tester.pump();

    expect(key.currentState!.state.text, 'a\tb',
        reason: 'typing must resume after a Tab, not be stranded on a widget that lost focus');
  });

  testWidgets('Tab still traverses out of a single-line StyledText', (tester) async {
    final sibling = FocusNode();
    addTearDown(sibling.dispose);
    await startEditing(tester, sibling, style: SWT.SINGLE);

    await simulateKeyDownEvent(LogicalKeyboardKey.tab, character: '\t');
    await simulateKeyUpEvent(LogicalKeyboardKey.tab);
    await tester.pump();

    expect(sibling.hasFocus, isTrue,
        reason: 'SWT traverses on Tab when the StyledText is SINGLE-line');
  });

  testWidgets('Shift+Tab still traverses out of a multi-line StyledText', (tester) async {
    final sibling = FocusNode();
    addTearDown(sibling.dispose);
    await startEditing(tester, sibling);

    await simulateKeyDownEvent(LogicalKeyboardKey.shiftLeft);
    await simulateKeyDownEvent(LogicalKeyboardKey.tab, character: '\t');
    await simulateKeyUpEvent(LogicalKeyboardKey.tab);
    await simulateKeyUpEvent(LogicalKeyboardKey.shiftLeft);
    await tester.pump();

    expect(sibling.hasFocus, isTrue,
        reason: 'SWT traverses when a modifier is held (handleTraverse MODIFIER_MASK)');
  });
}
