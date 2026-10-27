// Dropping ranges covered by a wider one must match the quadratic reference without its cost.

import 'dart:math';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/styledtext_evolve.dart';

StyleRange _r(int start, int end) =>
    StyleRange(start: start, end: end, style: const TextStyle(fontSize: 12));

/// The quadratic reference: checks every range kept so far.
List<StyleRange> _reference(List<StyleRange> sorted) {
  final kept = <StyleRange>[];
  for (final range in sorted) {
    final duplicate = kept.any((e) => e.start <= range.start && e.end >= range.end);
    if (!duplicate) kept.add(range);
  }
  return kept;
}

void main() {
  test('a range covered by a wider one is dropped', () {
    final out = dedupeSortedRanges([_r(0, 100), _r(10, 20), _r(10, 100), _r(100, 120)]);
    expect(out.map((r) => [r.start, r.end]), [
      [0, 100],
      [100, 120],
    ]);
  });

  test('overlapping but not covered ranges are all kept', () {
    final out = dedupeSortedRanges([_r(0, 10), _r(5, 20), _r(15, 30)]);
    expect(out, hasLength(3));
  });

  test('an empty list stays empty', () {
    expect(dedupeSortedRanges(const []), isEmpty);
  });

  test('it answers exactly what asking every kept range answered', () {
    // Shapes a highlighter actually produces: nested runs, repeated runs, adjacent runs, and
    // whole-document ranges that cover everything after them.
    final rnd = Random(20260921);
    for (var trial = 0; trial < 400; trial++) {
      final ranges = <StyleRange>[];
      for (var i = 0; i < rnd.nextInt(40); i++) {
        final start = rnd.nextInt(200);
        final len = rnd.nextInt(60);
        ranges.add(_r(start, start + len));
      }
      ranges.sort((a, b) => a.start.compareTo(b.start));

      final fast = dedupeSortedRanges(ranges);
      final slow = _reference(ranges);
      expect(fast.map((r) => [r.start, r.end]).toList(),
          slow.map((r) => [r.start, r.end]).toList(),
          reason: 'trial $trial');
    }
  });
}
