// The geometry table Java answers its position API from is rebuilt on every frame whose
// layout changed — which, while someone types, is every keystroke. Describing a document
// costs one TextPainter layout per logical line plus one getOffsetForCaret per character,
// and a keystroke rewrites exactly one of those lines: on a ~120-line file the other 119
// were being re-measured from scratch, at ~15 ms a frame, behind every key the user hit.
//
// These tests pin the two halves of the memo that fixes it — that an edit re-measures only
// the line it touched, and that the table Java receives is byte-for-byte the one a cold
// render side would have sent.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/impl/styledtext_evolve.dart';

const _size = Size(400, 600);
const _lineCount = 120;

List<String> _baseLines() => List.generate(
      _lineCount,
      (i) => 'line $i: the quick brown fox jumps over the lazy dog',
    );

TextEditingState _highlight(String text) {
  final ranges = <StyleRange>[];
  int offset = 0;
  for (final line in text.split('\n')) {
    for (final m in RegExp(r'\w+').allMatches(line).take(4)) {
      ranges.add(StyleRange(
        start: offset + m.start,
        end: offset + m.end,
        style: const TextStyle(fontSize: 12, color: Color(0xFF7F0055)),
      ));
    }
    offset += line.length + 1;
  }
  return TextEditingState(characterRanges: ranges);
}

TextEditingState _highlightPlus(String text, StyleRange extra) {
  final state = _highlight(text);
  return TextEditingState(
    characterRanges: <StyleRange>[...state.characterRanges, extra]
      ..sort((a, b) => a.start.compareTo(b.start)),
  );
}

TextShape _shapeWith(String text, TextEditingState? state,
        {bool wrap = false, TextStyle style = const TextStyle(fontSize: 12)}) =>
    TextShape(
      text,
      Offset.zero,
      style,
      null,
      null,
      null,
      wrap,
      _size,
      true,
      1,
      null,
      state,
      null,
      14.0,
      4,
    );

TextShape _shape(String text, {required bool wrap, bool styled = false}) =>
    TextShape(
      text,
      Offset.zero,
      const TextStyle(fontSize: 12),
      null,
      null,
      null,
      wrap,
      _size,
      true,
      1,
      null,
      styled ? _highlight(text) : null,
      null,
      14.0,
      4,
    );

String _typeInto(List<String> lines, int lineIndex) {
  final edited = List<String>.of(lines);
  edited[lineIndex] = '${edited[lineIndex]}x';
  return edited.join('\n');
}

void main() {
  setUp(debugResetLineLayoutCache);

  test('a keystroke re-measures the line it changed, not the document', () {
    final lines = _baseLines();
    _shape(lines.join('\n'), wrap: false).computeGeometry();

    TextShape.debugLayoutLineCalls = 0;
    _shape(_typeInto(lines, 60), wrap: false).computeGeometry();

    expect(
      TextShape.debugLayoutLineCalls,
      lessThanOrEqualTo(1),
      reason: 'every line but the edited one is laid out exactly as before, so '
          'describing the document again must not re-measure them',
    );
  });

  test('the same holds for a syntax-highlighted document', () {
    final lines = _baseLines();
    _shape(lines.join('\n'), wrap: false, styled: true).computeGeometry();

    TextShape.debugLayoutLineCalls = 0;
    _shape(_typeInto(lines, 60), wrap: false, styled: true).computeGeometry();

    expect(TextShape.debugLayoutLineCalls, lessThanOrEqualTo(1));
  });

  for (final wrap in [false, true]) {
    test('a table built over the cache matches a cold one (wrap: $wrap)', () {
      final edited = _typeInto(_baseLines(), 60);

      debugResetLineLayoutCache();
      final cold = _shape(edited, wrap: wrap, styled: true).computeGeometry();

      // Warm the cache on the document before the keystroke, then describe the
      // edited one — the path a typing session actually takes.
      debugResetLineLayoutCache();
      _shape(_baseLines().join('\n'), wrap: wrap, styled: true)
          .computeGeometry();
      final warm = _shape(edited, wrap: wrap, styled: true).computeGeometry();

      expect(warm, isNotNull);
      expect(
        warm,
        equals(cold),
        reason: 'Java resolves offsets, pixels and text bounds out of this table; '
            'a cached line must describe itself exactly as a freshly measured one',
      );
    });
  }

  test('adding one style range re-measures only the lines it covers', () {
    final text = _baseLines().join('\n');
    final before = _highlight(text);
    _shapeWith(text, before).computeGeometry();

    // Four characters on one line, in a colour the document does not already use.
    final line60 = text.split('\n').take(60).fold<int>(0, (a, l) => a + l.length + 1);
    final extra = StyleRange(
      start: line60 + 2,
      end: line60 + 6,
      style: const TextStyle(fontSize: 12, color: Color(0xFF0000FF)),
    );

    TextShape.debugLayoutLineCalls = 0;
    _shapeWith(text, _highlightPlus(text, extra)).computeGeometry();

    expect(
      TextShape.debugLayoutLineCalls,
      lessThanOrEqualTo(1),
      reason: 'the range covers one line; the other 119 are the same text at the same '
          'width in the same metrics, so their measurements still stand',
    );
  });

  // An unstyled document gets one range covering it in the widget's own style; dropping it for the
  // first real range must not invalidate every line.
  test('the first style range does not make the whole document look new', () {
    final text = _baseLines().join('\n');
    const defaultStyle = TextStyle(fontSize: 12);

    // What the widget builds while nothing is styled.
    _shapeWith(text, TextEditingState(characterRanges: [
      StyleRange(start: 0, end: text.length, style: defaultStyle),
    ])).computeGeometry();

    TextShape.debugLayoutLineCalls = 0;
    _shapeWith(text, TextEditingState(characterRanges: [
      StyleRange(
        start: 0,
        end: 4,
        style: const TextStyle(fontSize: 12, color: Color(0xFFFF0000)),
      ),
    ])).computeGeometry();

    expect(
      TextShape.debugLayoutLineCalls,
      lessThanOrEqualTo(1),
      reason: 'the covering range laid every line out in the widget\'s own style, which is how '
          'they would be laid out with no range at all — losing it changes no measurement',
    );
  });

  // A decoded range names weight, slant and baseline at their defaults where the widget's style
  // leaves them unsaid; the two must still count as the same layout.
  group('a range that moves no glyph is not a new layout', () {
    const widgetStyle = TextStyle(
      fontSize: 16,
      fontFamily: 'Liberation Mono',
      textBaseline: TextBaseline.alphabetic,
    );

    // What the renderer decode produces for a range that only sets a colour.
    StyleRange colourOnly(int start, int end) => StyleRange(
          start: start,
          end: end,
          style: const TextStyle(
            fontSize: 16,
            fontFamily: 'Liberation Mono',
            fontWeight: FontWeight.normal,
            fontStyle: FontStyle.normal,
            color: Color(0xFFFF0000),
          ),
        );

    test('recolouring keeps the layout signature', () {
      final text = _baseLines().join('\n');
      final before = _shapeWith(text, TextEditingState(characterRanges: const []),
          style: widgetStyle);
      final after = _shapeWith(
          text, TextEditingState(characterRanges: [colourOnly(10, 14)]),
          style: widgetStyle);

      expect(after.layoutSignature, before.layoutSignature);
    });

    test('but a bold range is', () {
      final text = _baseLines().join('\n');
      final before = _shapeWith(text, TextEditingState(characterRanges: const []),
          style: widgetStyle);
      final bold = _shapeWith(
          text,
          TextEditingState(characterRanges: [
            StyleRange(
              start: 10,
              end: 14,
              style: const TextStyle(
                fontSize: 16,
                fontFamily: 'Liberation Mono',
                fontWeight: FontWeight.bold,
              ),
            ),
          ]),
          style: widgetStyle);

      expect(bold.layoutSignature, isNot(before.layoutSignature),
          reason: 'bold text is wider, so the lines it falls on must be measured again');
    });

    test('and the measurements carry over', () {
      final text = _baseLines().join('\n');
      final before = _shapeWith(text, TextEditingState(characterRanges: const []),
          style: widgetStyle);
      before.computeGeometry();

      final after = _shapeWith(
          text, TextEditingState(characterRanges: [colourOnly(10, 14)]),
          style: widgetStyle);
      after.inheritMeasurementsFrom(before);

      TextShape.debugLayoutLineCalls = 0;
      after.computeGeometry();
      expect(TextShape.debugLayoutLineCalls, lessThanOrEqualTo(1),
          reason: 'the document was measured for the shape before it, and nothing moved');
    });
  });

  // Lines an edit did not reach keep the previous shape's measurements, which must equal fresh ones.
  group('lines carried across an edit', () {
    Map<String, dynamic>? carried(String before, String after,
        {TextEditingState Function(String)? styles}) {
      final style = styles ?? _highlight;
      debugResetLineLayoutCache();
      final previous = _shapeWith(before, style(before));
      previous.computeGeometry(visibleTop: 0, visibleBottom: 600);
      final next = _shapeWith(after, style(after));
      next.inheritMeasurementsFrom(previous);
      return next.computeGeometry(visibleTop: 0, visibleBottom: 600);
    }

    Map<String, dynamic>? cold(String after, {TextEditingState Function(String)? styles}) {
      debugResetLineLayoutCache();
      return _shapeWith(after, (styles ?? _highlight)(after))
          .computeGeometry(visibleTop: 0, visibleBottom: 600);
    }

    test('a keystroke', () {
      final before = _baseLines().join('\n');
      final after = _typeInto(_baseLines(), 60);
      expect(carried(before, after), equals(cold(after)));
    });

    test('a line inserted, moving every line below it', () {
      final lines = _baseLines();
      final before = lines.join('\n');
      final after = (List<String>.of(lines)..insert(30, 'a new line')).join('\n');
      expect(carried(before, after), equals(cold(after)));
    });

    test('an edit that makes a line it did not touch bold', () {
      // What opening a comment or a string does: the edit restyles lines well past itself, and a
      // bold line is wider, so its old measurement no longer holds.
      final lines = _baseLines();
      final before = lines.join('\n');
      final after = _typeInto(lines, 10);
      final line90 = after.split('\n').take(90).fold<int>(0, (a, l) => a + l.length + 1);
      TextEditingState boldLater(String text) => text == after
          ? _highlightPlus(text, StyleRange(
              start: line90,
              end: line90 + 20,
              style: const TextStyle(fontSize: 12, fontWeight: FontWeight.bold),
            ))
          : _highlight(text);
      expect(carried(before, after, styles: boldLater), equals(cold(after, styles: boldLater)));
    });
  });

  test('a wrap toggle is a different layout, not a cache hit', () {
    final text = _baseLines().join('\n');
    final unwrapped = _shape(text, wrap: false).computeGeometry()!;
    final wrapped = _shape(text, wrap: true).computeGeometry()!;

    expect(
      (wrapped['lines'] as List).length,
      greaterThan((unwrapped['lines'] as List).length),
      reason: 'wrapping splits logical lines into more visual rows, and the cache '
          'keys on the width a line was laid out against',
    );
  });
}
