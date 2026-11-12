// A scroll step brings new lines into the view and into the geometry band Java is told about. Each
// of them is laid out once, and that one layout serves the paint, the band's rows and the caret
// positions they carry; a caret position costs one query per character, not two.

import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/impl/styledtext_evolve.dart';

const _style = TextStyle(fontSize: 12, fontFamily: 'Liberation Mono');
const _bold = TextStyle(fontSize: 12, fontFamily: 'Liberation Mono', fontWeight: FontWeight.bold, color: Color(0xFF00008B));
const _view = Size(600, 400);

/// Java-like source with keyword runs, as an editor highlights one.
String _source(int lines) => [
      for (var i = 0; i < lines; i++)
        switch (i % 5) {
          0 => 'public void method$i(int value) {',
          1 => '    int total = value * $i + offset;',
          2 => '\tif (total > limit) return; // keep the limit',
          3 => '    System.out.println("line $i");  ',
          _ => '}',
        }
    ].join('\n');

List<StyleRange> _keywords(String text) => [
      for (final m in RegExp(r'public|void|int|if|return').allMatches(text))
        StyleRange(start: m.start, end: m.end, style: _bold),
    ];

TextShape _shape(String text) => TextShape(text, Offset.zero, _style, null, null, false, _view, true, 1, null,
    TextEditingState(characterRanges: _keywords(text)), null, 14.0, 4, null, const LineProperties(), 0, null,
    TextDirection.ltr, false, const [], const [], 0, true);

void _paint(TextShape shape, double scrollTop) {
  final recorder = ui.PictureRecorder();
  shape.draw(Canvas(recorder)..clipRect(Rect.fromLTWH(0, scrollTop, _view.width, _view.height)));
  recorder.endRecording().dispose();
}

/// What the client does for one scroll position: paints the view, then describes the band around it.
GeometryTable _step(TextShape shape, double scrollTop) {
  _paint(shape, scrollTop);
  final step = shape.lineHeight * 20;
  final top = ((scrollTop / step).floor() - 1) * step;
  final bottom = (((scrollTop + _view.height) / step).ceil() + 1) * step;
  return shape.computeBandTable(top, bottom);
}

void main() {
  setUp(() {
    debugResetLineLayoutCache();
    TextShape.debugCaretXByQuery = false;
  });

  test('a scroll step lays out each line it brings in once', () {
    final shape = _shape(_source(3000));
    expect(shape.bandsGeometry, isTrue, reason: 'the scenario is the banded geometry of a large editor');
    final seen = <int>{};
    var firstStep = true;
    for (var line = 0; line < 600; line += 30) {
      TextShape.debugLayoutLineCalls = 0;
      TextShape.debugPainterLayouts = 0;
      TextShape.debugCaretQueries = 0;
      final table = _step(shape, line * shape.lineHeight);
      final layouts = TextShape.debugLayoutLineCalls + TextShape.debugPainterLayouts;
      final caretQueries = TextShape.debugCaretQueries;
      final before = seen.length;
      seen.addAll(TextShape.debugPaintedLines);
      for (var i = 0; i < table.rows.length; i++) {
        seen.add(table.bandFirstLine! + i);
      }
      final brought = seen.length - before;
      if (firstStep) {
        firstStep = false;
        continue;
      }
      expect(layouts, lessThanOrEqualTo(brought),
          reason: 'scrolled to line $line: $brought lines came into the view or the band, and they were laid out '
              '$layouts times');
      expect(caretQueries, lessThanOrEqualTo(brought),
          reason: 'scrolled to line $line: only the end of each new line asks for a caret, '
              'but $caretQueries were asked for');
    }
  });

  test('describing a whole document keeps the paragraphs the view was painted with', () {
    // Under the band's threshold, the table describes every line with its caret positions.
    final shape = _shape(_source(400));
    expect(shape.bandsGeometry, isFalse);
    _paint(shape, 0);
    shape.computeGeometryTable(visibleTop: 0, visibleBottom: _view.height);
    TextShape.debugPainterLayouts = 0;
    _paint(shape, 0);
    expect(TextShape.debugPainterLayouts, 0,
        reason: 'a paragraph evicted from the paint cache is disposed, and the view may be rasterised again from it');
  });

  test('the band carries the caret positions the caret query finds', () {
    final text = _source(3000);
    final top = 300 * 14.0;
    final fast = _step(_shape(text), top).toMap();
    debugResetLineLayoutCache();
    TextShape.debugCaretXByQuery = true;
    final queried = _step(_shape(text), top).toMap();
    expect(fast, queried);
  });
}
