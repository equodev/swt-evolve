import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/impl/styledtext_evolve.dart';

/// A tab has no glyph in Flutter, so it is painted as a zero-height placeholder aligned
/// on the baseline (see TextShape._expandTabStops). Its box therefore starts at the
/// baseline, not at the top of the line — and a caret that resolves to it would inherit
/// that offset and be drawn most of a line too low. The caret must span the line box
/// whatever run it happens to land on, so these assertions are all vertical.
void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  const style = TextStyle(fontSize: 10, height: 1.2);
  const canvas = Size(1000, 200);
  const lineHeight = 12.0;

  TextShape shapeFor(String text, int caretOffset) => TextShape(
    text,
    Offset.zero,
    style,
    null, // clipRect
    null, // textSpan
    CaretInfo(
      offset: caretOffset,
      width: 1.0,
      height: lineHeight,
      color: const Color(0xFF000000),
      visible: true,
      blinking: false,
      styledTextId: 1,
      blinkRate: 500,
    ),
    false, // wordWrap
    canvas,
    true, // editable
    null, // styledTextId
    null, // onTextChanged
    null, // editingState
    null, // selectionInfo
    lineHeight,
    4, // tabs
  );

  test('the caret after a trailing tab sits at the top of its line', () {
    // A caret at the end of its line resolves with upstream affinity — onto the tab
    // placeholder for "a\t", onto a real glyph for "ab". Both are the same line, so
    // both carets must share a top; only the first one can inherit the baseline.
    final afterTab = shapeFor('a\t', 2).caretRect()!;
    final afterChar = shapeFor('ab', 2).caretRect()!;

    expect(
      afterTab.top,
      afterChar.top,
      reason: 'the tab placeholder must not drag the caret down to the baseline',
    );
    expect(afterTab.top, 0.0, reason: 'line 0 starts at the top of the shape');
  });

  test('the caret on an auto-indented new line sits on that line, not below it', () {
    // The reported symptom: Enter on an indented line leaves a line holding only the
    // carried-over tab, so the caret has nothing but the placeholder to resolve to.
    final caret = shapeFor('\ta\n\t', 4).caretRect()!;

    expect(
      caret.top,
      lineHeight,
      reason: 'the caret belongs at the top of line 1, not a baseline further down',
    );
  });

  test('a tab-only line keeps the caret on the line for a mid-document tab', () {
    // Not an auto-indent case: a tab-only line in the middle of a document.
    final caret = shapeFor('a\n\t\nb', 3).caretRect()!;

    expect(caret.top, lineHeight, reason: 'line 1 top');
  });

  test('a caret on a line without tabs is unaffected', () {
    expect(shapeFor('ab\ncd', 4).caretRect()!.top, lineHeight);
    expect(shapeFor('ab\ncd', 1).caretRect()!.top, 0.0);
  });
}
