// Toggling word wrap on a StyledText the caret is already in must reach the painted layout.
//
// A click into the editor enters local edit mode, which pins what is painted to the server
// snapshot taken at that click. A later state push carrying the same text — exactly what a bare
// setWordWrap(true) produces — took a branch that refreshed only the style ranges, so every
// server-side presentation property, `wordWrap` among them, stayed at its pre-click value: the
// live V* said the wrap was on while the layout kept measuring lines as if it were off.
//
// The probe is where a click lands. With wrap on, a long line spans several visual rows, so a
// click below the first row is still inside it; with the wrap lost, the same point falls past the
// whole (much shorter) text and clamps to its end. Typing one character makes that visible in the
// public text.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/styledtext.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/styledtext_evolve.dart';

// Three canvas widths wide: wrapped it takes four visual rows, unwrapped one.
const _longLine =
    'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb';
const _text = 'aaaa\n$_longLine\ncccc';
const _size = Size(200, 400);

void main() {
  VStyledText value({required bool wrap}) => VStyledText()
    ..swt = 'StyledText'
    ..id = 1
    ..style = SWT.H_SCROLL | SWT.V_SCROLL
    ..enabled = true
    ..editable = true
    ..caretOffset = 0
    ..text = _text
    ..wordWrap = wrap
    ..bounds = (VRectangle()
      ..x = 0
      ..y = 0
      ..width = _size.width.toInt()
      ..height = _size.height.toInt());

  Widget appWith(GlobalKey<StyledTextImpl> key, VStyledText v) => EvolveApp(
        theme: ThemeMode.light,
        contentWidget: SizedBox(
          width: _size.width,
          height: _size.height,
          child: StyledTextSwt<VStyledText>(key: key, value: v),
        ),
      );

  testWidgets('a wrap toggle while the caret is in the editor reaches the layout',
      (tester) async {
    final key = GlobalKey<StyledTextImpl>();
    await tester.pumpWidget(appWith(key, value(wrap: false)));
    // initState doesn't run extraSetState, so a second push builds the text shape.
    await tester.pumpWidget(appWith(key, value(wrap: false)));

    // Click on the first line: the editor takes the caret and enters local edit mode.
    await tester.tapAt(const Offset(5, 5));
    await tester.pump();

    // Toggle Word Wrap: Java pushes the same text back with wordWrap on.
    await tester.pumpWidget(appWith(key, value(wrap: true)));
    await tester.pump();

    // y = 40 is the long line's third wrapped row when the wrap took effect, and past the end of
    // the whole text when it did not.
    await tester.tapAt(const Offset(5, 40));
    await tester.pump();

    await simulateKeyDownEvent(LogicalKeyboardKey.keyX, character: 'X');
    await simulateKeyUpEvent(LogicalKeyboardKey.keyX);
    await tester.pump();

    final lines = (key.currentState!.state.text ?? '').split('\n');
    expect(
      lines[1],
      contains('X'),
      reason:
          'with wrap on, a click below the long line\'s first row is still inside that line — '
          'landing anywhere else means the layout is still measuring it unwrapped',
    );
  });
}
