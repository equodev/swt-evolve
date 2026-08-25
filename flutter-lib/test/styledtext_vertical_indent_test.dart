import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/impl/styledtext_evolve.dart';

/// StyledText.setLineVerticalIndent adds space ABOVE a line's text, inside the
/// line's own box: the box top stays where the previous line ended, the box
/// grows by the indent, and the glyphs (caret, selection, hit targets) sit
/// vIndent below the box top.
///
/// The test font advances one em per glyph: fontSize 10 → 10px per character,
/// 10px line height.
const _lineHeight = 10.0;
const _canvas = Size(200, 400);
const _vIndent = 30;

const _text = 'aaaa\nbbbb\ncccc';
const _lineOneStart = 5;
const _lineTwoStart = 10;

TextShape _shape({TextEditingState? editingState, CaretInfo? caret}) =>
    TextShape(
      _text,
      Offset.zero,
      const TextStyle(fontSize: 10, height: 1.0),
      null, // clipRect
      null, // textSpan
      caret,
      false, // wordWrap
      _canvas,
      true, // editable
      1, // styledTextId
      null, // onTextChanged
      editingState,
      null, // selectionInfo
      _lineHeight,
    );

TextEditingState _indentLineOne() => const TextEditingState(
  characterRanges: [],
  lineProperties: {1: LineProperties(verticalIndent: _vIndent)},
);

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('geometry gives the indented line a taller box and shifts lines below', () {
    final plain = _shape().computeGeometry()!;
    final indented = _shape(editingState: _indentLineOne()).computeGeometry()!;

    final plainLines = plain['lines'] as List;
    final lines = indented['lines'] as List;

    // line 0 untouched
    expect(lines[0]['y'], plainLines[0]['y']);
    expect(lines[0]['h'], plainLines[0]['h']);
    // line 1: box top unchanged, box grown by the indent, glyphs at y + vi
    expect(lines[1]['y'], plainLines[1]['y']);
    expect(lines[1]['h'], (plainLines[1]['h'] as double) + _vIndent);
    expect(lines[1]['vi'], _vIndent);
    // line 2 shifts down whole, and the content bottom follows
    expect(lines[2]['y'], (plainLines[2]['y'] as double) + _vIndent);
    expect(
      indented['contentHeight'],
      (plain['contentHeight'] as double) + _vIndent,
    );
  });

  test('a click in the indent band belongs to the indented line', () {
    final shape = _shape(editingState: _indentLineOne());

    // y 15 is inside line 1's indent band (box 10..50, glyphs from 40)
    final inBand = shape.getOffsetFromPosition(const Offset(2, 15), _canvas);
    expect(inBand, greaterThanOrEqualTo(_lineOneStart));
    expect(inBand, lessThan(_lineTwoStart));

    // the glyphs answer at their painted position, below the indent
    final onGlyphs = shape.getOffsetFromPosition(
      const Offset(22, _lineHeight + _vIndent + 5),
      _canvas,
    );
    expect(onGlyphs, _lineOneStart + 2);

    // line 2's glyphs moved down with it
    final lineTwo = shape.getOffsetFromPosition(
      const Offset(2, 2 * _lineHeight + _vIndent + 5),
      _canvas,
    );
    expect(lineTwo, _lineTwoStart);
  });

  test('the caret on an indented line sits below the indent', () {
    final caret = CaretInfo(
      offset: _lineOneStart,
      width: 2,
      height: _lineHeight,
      color: const Color(0xFF000000),
      styledTextId: 1,
    );
    final plainRect = _shape(caret: caret).caretRect()!;
    final indentedRect = _shape(
      editingState: _indentLineOne(),
      caret: caret,
    ).caretRect()!;

    expect(indentedRect.top, plainRect.top + _vIndent);
    expect(indentedRect.left, plainRect.left);
  });
}
