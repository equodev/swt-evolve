// A scroll reuses a band's pixels, so every visual difference must count as a different painting.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/styledtext_evolve.dart';

const _size = Size(400, 600);
const _default = TextStyle(fontSize: 12);

String _text() => List.generate(40, (i) => 'line $i: the quick brown fox').join('\n');

StyleRange _range(int start, int end, Color colour) => StyleRange(
      start: start,
      end: end,
      style: TextStyle(fontSize: 12, color: colour),
    );

TextShape _shape({
  String? text,
  TextEditingState? state,
  CaretInfo? caret,
  SelectionInfo? selection,
  Size canvas = _size,
  Offset off = Offset.zero,
}) =>
    TextShape(
      text ?? _text(),
      off,
      _default,
      null,
      null,
      caret,
      false,
      canvas,
      true,
      1,
      null,
      state,
      selection,
      14.0,
      4,
    );

void main() {
  test('a shape rebuilt from the same state paints the same', () {
    final ranges = [_range(0, 4, const Color(0xFFFF0000))];
    final a = _shape(state: TextEditingState(characterRanges: ranges));
    // A separate state object holding equal runs: what a rebuild produces when nothing changed.
    final b = _shape(state: TextEditingState(characterRanges: List.of(ranges)));

    expect(b.paintsSameAs(a), isTrue);
  });

  test('scrolling alone is not a different painting', () {
    // The view moves above the shape, so a scroll changes none of its inputs.
    final state = TextEditingState(characterRanges: [_range(0, 4, const Color(0xFFFF0000))]);
    expect(_shape(state: state).paintsSameAs(_shape(state: state)), isTrue);
  });

  test('a recoloured run is a different painting', () {
    final a = _shape(
        state: TextEditingState(characterRanges: [_range(0, 4, const Color(0xFFFF0000))]));
    final b = _shape(
        state: TextEditingState(characterRanges: [_range(0, 4, const Color(0xFF00FF00))]));

    expect(b.paintsSameAs(a), isFalse);
  });

  test('a run that moved is a different painting', () {
    final a = _shape(
        state: TextEditingState(characterRanges: [_range(0, 4, const Color(0xFFFF0000))]));
    final b = _shape(
        state: TextEditingState(characterRanges: [_range(2, 6, const Color(0xFFFF0000))]));

    expect(b.paintsSameAs(a), isFalse);
  });

  test('an added run is a different painting', () {
    final one = [_range(0, 4, const Color(0xFFFF0000))];
    final a = _shape(state: TextEditingState(characterRanges: one));
    final b = _shape(
        state: TextEditingState(
            characterRanges: [...one, _range(8, 12, const Color(0xFF0000FF))]));

    expect(b.paintsSameAs(a), isFalse);
  });

  test('a line background is a different painting', () {
    final a = _shape(state: TextEditingState(characterRanges: const []));
    final b = _shape(
        state: TextEditingState(
      characterRanges: const [],
      lineProperties: const {3: LineProperties(background: Color(0xFF202020))},
    ));

    expect(b.paintsSameAs(a), isFalse);
  });

  test('edited text is a different painting', () {
    expect(_shape(text: 'one\ntwo').paintsSameAs(_shape(text: 'one\ntwo!')), isFalse);
  });

  test('a resized view is a different painting', () {
    expect(
      _shape(canvas: const Size(400, 600)).paintsSameAs(_shape(canvas: const Size(500, 600))),
      isFalse,
      reason: 'the width a line wraps at, and how much of the document is on screen',
    );
  });

  test('a moved shape is a different painting', () {
    expect(_shape(off: Offset.zero).paintsSameAs(_shape(off: const Offset(0, 20))), isFalse);
  });

  test('a moved caret is not a different painting', () {
    // Carets have their own layer, and an empty selection moving with the caret draws nothing.
    final a = _shape(
        caret: CaretInfo(offset: 3, color: const Color(0xFF000000), styledTextId: 1),
        selection: SelectionInfo(start: 3, end: 3));
    final b = _shape(
        caret: CaretInfo(offset: 9, color: const Color(0xFF000000), styledTextId: 1),
        selection: SelectionInfo(start: 9, end: 9));

    expect(b.paintsSameAs(a), isTrue);
  });

  test('a grown selection is a different painting', () {
    final a = _shape(selection: SelectionInfo(start: 3, end: 5));
    final b = _shape(selection: SelectionInfo(start: 3, end: 6));

    expect(b.paintsSameAs(a), isFalse);
  });

  test('a selection that appears is a different painting', () {
    final a = _shape(selection: SelectionInfo(start: 3, end: 3));
    final b = _shape(selection: SelectionInfo(start: 3, end: 6));

    expect(b.paintsSameAs(a), isFalse);
  });
}
