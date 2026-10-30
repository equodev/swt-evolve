// An editor whose lines all have one height (StyledText#isFixedLineHeight) is laid out as native SWT
// lays it out: every row as tall as the tallest line laid out so far, at least the renderer's line
// height. The rows are then known without measuring the lines, so the ones out of view are not
// measured: on desktop the client shares the main thread with the application.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/impl/styledtext_evolve.dart';

const _size = Size(400, 600);

String _document(int lines) =>
    List.generate(lines, (i) => i % 7 == 3 ? 'line $i\twith a tab' : 'line $i of a large file').join('\n');

/// The editor's font is 12, so a line lays out 12 tall.
TextShape _shape(String text,
        {required bool fixed, double javaLineHeight = 10, TextEditingState? editing}) =>
    TextShape(
      text,
      Offset.zero,
      const TextStyle(fontSize: 12),
      null,
      null,
      false,
      _size,
      true,
      1,
      null,
      editing,
      null,
      javaLineHeight,
      4,
      null,
      const LineProperties(),
      0,
      null,
      TextDirection.ltr,
      false,
      const [],
      const [],
      0,
      fixed,
    );

List<Map<String, dynamic>> _rows(Map<String, dynamic>? table) =>
    (table!['lines'] as List).cast<Map<String, dynamic>>();

void main() {
  setUp(debugResetLineLayoutCache);

  test('every row is the renderer\'s line height tall when no line is taller', () {
    final rows = _rows(_shape(_document(40), fixed: true, javaLineHeight: 16).computeGeometry());

    for (final row in rows) {
      expect(row['h'], 16.0, reason: 'line ${row['l']}');
      expect(row['y'], 16.0 * (row['l'] as num), reason: 'line ${row['l']}');
    }
  });

  test('every row is as tall as the tallest line laid out', () {
    // A line lays out 12 tall, more than the renderer's 10: as when a fallback glyph's font is taller.
    final rows = _rows(_shape(_document(40), fixed: true).computeGeometry());

    for (final row in rows) {
      expect(row['h'], 12.0, reason: 'line ${row['l']}');
      expect(row['y'], 12.0 * (row['l'] as num), reason: 'line ${row['l']}');
    }
  });

  test('a taller line coming into view makes every row its height', () {
    final text = _document(4000);
    final start = text.split('\n').take(3000).fold<int>(0, (n, l) => n + l.length + 1);
    final editing = TextEditingState(characterRanges: [
      StyleRange(start: start, end: start + 4, style: const TextStyle(fontSize: 24)),
    ]);
    final first = _shape(text, fixed: true, editing: editing);
    expect(_rows(first.computeGeometry(visibleTop: 0, visibleBottom: 600))[3999]['h'], 12.0);
    expect(first.rowsOutgrown, isFalse);

    first.computeGeometry(visibleTop: 3000 * 12.0, visibleBottom: 3000 * 12.0 + 600);
    expect(first.rowsOutgrown, isTrue, reason: 'line 3000 lays out taller than the rows placed');

    final next = _shape(text, fixed: true, editing: editing)..inheritMeasurementsFrom(first);
    final rows = _rows(next.computeGeometry(visibleTop: 0, visibleBottom: 600));
    final tall = rows[3000]['h'] as double;
    expect(tall, greaterThan(12.0));
    expect(rows[0]['h'], tall);
    expect(rows[3999]['y'], 3999 * tall);
  });

  test('a large document is laid out for the rows in view, not line by line', () {
    final text = _document(4000);
    expect(text.length, greaterThan(20000), reason: 'sanity: past the size described whole');

    TextShape.debugLayoutLineCalls = 0;
    final table = _shape(text, fixed: true).computeGeometry(visibleTop: 0, visibleBottom: 600);

    expect(_rows(table), hasLength(4000));
    expect(TextShape.debugLayoutLineCalls, lessThan(200));
  });

  test('without a fixed line height every line is still measured', () {
    // A line whose height is known without laying it out (printable ASCII in one style) is described
    // by that height; one with a character a fallback font may draw taller has to be laid out.
    final text = List.generate(4000, (i) => 'line $i of a large file é').join('\n');
    TextShape.debugLayoutLineCalls = 0;
    _shape(text, fixed: false).computeGeometry(visibleTop: 0, visibleBottom: 600);

    expect(TextShape.debugLayoutLineCalls, greaterThanOrEqualTo(4000));
  });

  test('a row out of view has the width it is measured at, tabs included', () {
    final text = _document(4000);
    final measured = _rows(_shape(text, fixed: false).computeGeometry());
    debugResetLineLayoutCache();
    final estimated = _rows(_shape(text, fixed: true).computeGeometry(visibleTop: 0, visibleBottom: 600));

    // The test font is fixed-pitch, so the column count gives the width exactly.
    for (final line in [1000, 1003, 3999]) {
      expect(estimated[line]['w'], measured[line]['w'], reason: 'line $line');
      expect(estimated[line]['cx'], isNull, reason: 'no caret positions measured for line $line');
    }
  });

  test('a line its own characters break onto several rows still takes one row', () {
    // A form feed breaks a layout even without wrapping; native SWT keeps the line to one row.
    final text = 'first\nbefore\u000cafter\nlast';
    final rows = _rows(_shape(text, fixed: true).computeGeometry());

    expect(rows.map((r) => r['l']), [0, 1, 2]);
    expect(rows.map((r) => r['h']), [12.0, 12.0, 12.0]);
    expect(rows.map((r) => r['y']), [0.0, 12.0, 24.0]);
  });
}
