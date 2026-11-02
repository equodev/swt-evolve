// A document past the per-character payload limit sends per-character x for the rows in view only.
// A wrapped line much longer than the view is mostly out of it, and asking a caret position for
// each of its characters is what describing it costs: thousands of them, of which a few hundred
// are sent.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/impl/styledtext_evolve.dart';

const _size = Size(400, 600);

String _longLine() {
  final sb = StringBuffer();
  while (sb.length < 26000) {
    sb.write('lorem ipsum dolor sit amet consectetur adipiscing elit ');
  }
  return '$sb\nend';
}

TextShape _shape(String text) => TextShape(
      text,
      Offset.zero,
      const TextStyle(fontSize: 12),
      null,
      null,
      true,
      _size,
      true,
      1,
      null,
      null,
      null,
      14.0,
      4,
    );

void main() {
  setUp(debugResetLineLayoutCache);

  test('a long wrapped line in view measures caret positions for the rows in view only', () {
    final text = _longLine();
    TextShape.debugCaretQueries = 0;
    final banded = _shape(text).computeGeometryTable(visibleTop: 0, visibleBottom: _size.height);

    final sentChars = banded.rows
        .where((row) => row.charX != null)
        .fold<int>(0, (sum, row) => sum + row.e - row.s + 1);
    expect(sentChars, greaterThan(0), reason: 'the rows in view carry their per-character x');
    expect(TextShape.debugCaretQueries, sentChars,
        reason: 'a caret position is asked for each character sent, and for none of the rest');
  });

  test('the rows in view carry the per-character x the whole line measures', () {
    final text = _longLine();
    final banded = _shape(text).computeGeometryTable(visibleTop: 0, visibleBottom: _size.height);
    debugResetLineLayoutCache();
    final whole = _shape(text).computeGeometryTable();

    final sent = banded.rows.where((row) => row.charX != null).toList();
    expect(sent, isNotEmpty);
    for (final row in sent) {
      final same = whole.rows.singleWhere((r) => r.s == row.s && r.e == row.e);
      expect(row.toMap(), same.toMap(), reason: 'row ${row.s}..${row.e}');
    }
    expect([for (final row in banded.rows) (row.s, row.e, row.x, row.y, row.w, row.h)],
        [for (final row in whole.rows) (row.s, row.e, row.x, row.y, row.w, row.h)],
        reason: 'every row sits where the whole line puts it');
  });
}
