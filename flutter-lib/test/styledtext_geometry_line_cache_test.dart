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
