import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/impl/styledtext_evolve.dart';

/// The test font advances one em per glyph, so with fontSize 10 a character is
/// 10px wide and a canvas 200px wide holds exactly 20 of them.
const _charWidth = 10.0;
const _lineHeight = 10.0;
const _canvas = Size(200, 400);

// Line 1 is 60 characters — three canvas widths — so a wrapping layout gives it
// three visual rows where an unwrapped one gives it a single row.
const _longLine =
    'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb';
const _text = 'aaaa\n$_longLine\ncccc\ndddd\neeee';
const _lineTwoStart = 5 + 60 + 1; // 'aaaa\n' + the long line + '\n'

TextShape _shape({required bool wordWrap}) => TextShape(
  _text,
  Offset.zero,
  const TextStyle(fontSize: 10, height: 1.0),
  null, // clipRect
  null, // textSpan
  null, // caretInfo
  wordWrap,
  _canvas,
  true, // editable
  1, // styledTextId
  null, // onTextChanged
  null, // editingState
  null, // selectionInfo
  _lineHeight,
);

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('hit-test resolves the clicked line when wordWrap is off', () {
    final shape = _shape(wordWrap: false);

    // Line 2 ('cccc') is painted at y 20..30 because nothing wraps, and column 2
    // starts at x 20.
    final offset = shape.getOffsetFromPosition(
      const Offset(2 * _charWidth + 2, 2 * _lineHeight + 5),
      _canvas,
    );

    expect(
      offset,
      _lineTwoStart + 2,
      reason:
          'with wrapping off the long line occupies one painted row, so a click '
          'below it must not be pulled up by its would-be wrapped rows',
    );
  });

  test('hit-test follows the wrapped rows when wordWrap is on', () {
    final shape = _shape(wordWrap: true);

    // Line 1 wraps into three rows spanning y 10..40, so y 25 is its second row:
    // 20 characters in, plus column 2.
    final offset = shape.getOffsetFromPosition(
      const Offset(2 * _charWidth + 2, 25),
      _canvas,
    );

    expect(offset, 5 + 20 + 2);
  });
}
