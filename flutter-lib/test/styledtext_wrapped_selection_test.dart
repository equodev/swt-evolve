// TextLayout.draw on Cocoa (NSLayoutManager's selection rectangles) highlights a selection that goes on
// past a wrapped row to the layout's right edge, and the rows it continues on from the layout's left edge.

import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/impl/styledtext_evolve.dart';

const _canvas = Size(200, 100);
const _black = Color(0xFF000000);

// The test font advances one em per glyph: fontSize 10 is 10px per character and a 10px row.
TextShape _shape(String text, LineProperties line, {SelectionInfo? selection}) => TextShape(
      text,
      Offset.zero,
      const TextStyle(fontSize: 10, height: 1.0, color: _black),
      null,
      null,
      null,
      true,
      _canvas,
      true,
      1,
      null,
      TextEditingState(characterRanges: [], lineProperties: {0: line}),
      selection,
      10,
    );

Future<void> _paint(TextShape shape) async {
  final recorder = ui.PictureRecorder();
  shape.draw(Canvas(recorder));
  (await recorder.endRecording().toImage(_canvas.width.toInt(), _canvas.height.toInt())).dispose();
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  const line = 'aaaaaaa bbbbbbb ccccccc ddddddd eeeeeee';
  for (final (name, props) in [
    ('', const LineProperties()),
    (' with a wrap indent', const LineProperties(wrapIndent: 40)),
  ]) {
    test('a selection across the rows of a wrapped line fills them to the layout edges$name', () async {
      final shape = _shape(line, props,
          selection: SelectionInfo(start: 10, end: 20, selectionForeground: const Color(0xFFFFFFFF)));
      await _paint(shape);
      final rects = shape.debugPaintedSelectionRects;
      final firstRow = rects.where((r) => r.top < 5).toList();
      final secondRow = rects.where((r) => r.top >= 5 && r.top < 15).toList();
      expect(firstRow, isNotEmpty);
      expect(secondRow, isNotEmpty);
      expect(firstRow.map((r) => r.left).reduce((a, b) => a < b ? a : b), 100, reason: 'from the first selected glyph');
      expect(firstRow.map((r) => r.right).reduce((a, b) => a > b ? a : b), _canvas.width,
          reason: 'the row the selection goes on from is highlighted to the right edge: $rects');
      expect(secondRow.map((r) => r.left).reduce((a, b) => a < b ? a : b), 0,
          reason: 'the row the selection goes on to is highlighted from the left edge: $rects');
      expect(rects.where((r) => r.top >= 15), isEmpty, reason: 'the row after the selection is not highlighted');
    });
  }

  test('a selection inside one row of a wrapped line stays on its glyphs', () async {
    final shape = _shape(line, const LineProperties(),
        selection: SelectionInfo(start: 2, end: 5, selectionForeground: const Color(0xFFFFFFFF)));
    await _paint(shape);
    final rects = shape.debugPaintedSelectionRects;
    expect(rects.map((r) => (r.left, r.right)).toList(), [(20.0, 50.0)]);
  });
}
