import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/impl/styledtext_evolve.dart';

/// Tab rendering is a measurement problem, so every assertion here is geometric:
/// it maps a pixel position back to a document offset through the same funnel the
/// painter and the caret use. The test font advances every glyph — space included —
/// by exactly fontSize, so with fontSize 10 one column is 10px and a tabs=4 stop
/// falls every 40px.
void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  const style = TextStyle(fontSize: 10, height: 1.2);
  const canvas = Size(1000, 200);

  TextShape shapeFor(String text, {int tabs = 4}) => TextShape(
    text,
    Offset.zero,
    style,
    null, // clipRect
    null, // textSpan
    null, // caretInfo
    false, // wordWrap
    canvas,
    true, // editable
    null, // styledTextId
    null, // onTextChanged
    null, // editingState
    null, // selectionInfo
    0.0, // lineHeight
    tabs,
  );

  test('a leading tab advances a full tab stop, not one column', () {
    final shape = shapeFor('\thello');

    // With tabs=4 the '\t' occupies 0..40 and "hello" starts at column 4, so the
    // 'h' spans 40..50 and the 'o' spans 80..90.
    expect(
      shape.getOffsetFromPosition(const Offset(42, 5), canvas),
      1,
      reason: "'h' must be painted at the tab stop (x=40), not one column in",
    );
    expect(
      shape.getOffsetFromPosition(const Offset(82, 5), canvas),
      5,
      reason: 'the rest of the line must shift by the whole tab stop',
    );
  });

  test('a tab advances to the next stop, not by a fixed width', () {
    final shape = shapeFor('ab\tc');

    // "ab" ends at x=20, so the tab advances 2 columns to the next multiple of 40
    // — not a fixed 4 columns, which would put 'c' at x=60.
    expect(
      shape.getOffsetFromPosition(const Offset(42, 5), canvas),
      3,
      reason: "'c' belongs at the next tab stop (x=40)",
    );
  });

  test('consecutive tabs each advance one stop', () {
    final shape = shapeFor('\t\tx');

    // The reported symptom: after Enter+Tab the line holds "\t\t", and the caret
    // sat one column in instead of two stops out.
    expect(shape.getOffsetFromPosition(const Offset(82, 5), canvas), 2);
  });

  test('the tab stop honours the widget tab width', () {
    final shape = shapeFor('\thello', tabs: 8);

    expect(
      shape.getOffsetFromPosition(const Offset(82, 5), canvas),
      1,
      reason: 'with tabs=8 the first stop is at x=80',
    );
  });

  test('tab expansion keeps hit-test offsets on later lines aligned', () {
    final shape = shapeFor('\thello\n\tworld');

    // Second line, well inside 'w' (40..50) — offsets must still count the tab as
    // a single character.
    expect(shape.getOffsetFromPosition(const Offset(42, 17), canvas), 8);
  });

  test('lines without tabs are measured exactly as before', () {
    final shape = shapeFor('hello');

    expect(shape.getOffsetFromPosition(const Offset(2, 5), canvas), 0);
    expect(shape.getOffsetFromPosition(const Offset(42, 5), canvas), 4);
  });
}
