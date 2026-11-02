// A large document is not laid out line by line to describe it: lines off screen whose height is
// known without laying them out are described by that height and an estimated width, and measured
// when they are shown, as the native renderer measures lines of a fixed height. What is on screen,
// and where every line is, has to be what laying the whole document out gives.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/impl/styledtext_evolve.dart';

const _style = TextStyle(fontSize: 12);

/// Enough lines to be past the size every line is described in full for.
final _lines = [
  for (var i = 0; i < 1500; i++)
    switch (i % 4) {
      0 => 'public void method$i(int value) {',
      1 => '\tint total = value * $i + offset;',
      2 => '',
      _ => i == 1203 ? '    café $i' : '    System.out.println("line $i");',
    }
];

TextShape _shape() => TextShape(
      _lines.join('\n'),
      Offset.zero,
      _style,
      null,
      null,
      false,
      const Size(400, 300),
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

  test('a band described lazily matches the whole document laid out', () {
    const top = 1200.0, bottom = 1500.0;
    TextShape.debugLayoutLineCalls = 0;
    final lazy = _shape().computeGeometryTable(visibleTop: top, visibleBottom: bottom);
    expect(TextShape.debugLayoutLineCalls, lessThan(100),
        reason: 'only the lines on screen, and the ones whose height needs laying out, are laid out');
    final whole = _shape().computeGeometryTable();

    expect(lazy.rows.length, whole.rows.length);
    expect(lazy.contentHeight, whole.contentHeight);
    for (var i = 0; i < whole.rows.length; i++) {
      final a = whole.rows[i], b = lazy.rows[i];
      expect((b.l, b.s, b.e, b.y, b.h), (a.l, a.s, a.e, a.y, a.h), reason: 'row $i');
      final onScreen = a.y + a.h >= top && a.y <= bottom;
      if (onScreen) {
        expect(b.w, a.w, reason: 'row $i is on screen');
        expect(b.charX, isNotNull, reason: 'row $i is on screen');
      }
    }
    expect(lazy.contentWidth, lessThanOrEqualTo(whole.contentWidth));
  });

  test('a line outside the plain ones is laid out wherever it is', () {
    final lazy = _shape().computeGeometryTable(visibleTop: 0, visibleBottom: 300);
    final whole = _shape().computeGeometryTable();
    final row = lazy.rows.indexWhere((r) => r.l == 1203);
    expect(lazy.rows[row].w, whole.rows[row].w);
  });
}
