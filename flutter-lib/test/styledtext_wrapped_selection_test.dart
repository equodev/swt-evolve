// A selection across the rows of a wrapped line highlights only the selected glyphs, as TextLayout.draw
// does on GTK and Windows, on every OS. Cocoa's NSLayoutManager also fills to the layout's edges between
// the rows; Evolve keeps the behaviour two of the three platforms share.

import 'dart:ui' as ui;

import 'package:flutter/foundation.dart';
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
  tearDown(() => debugDefaultTargetPlatformOverride = null);

  const line = 'aaaaaaa bbbbbbb ccccccc ddddddd eeeeeee';
  for (final platform in [TargetPlatform.macOS, TargetPlatform.windows, TargetPlatform.linux]) {
    for (final (name, props, secondRowStart) in [
      ('', const LineProperties(), 0.0),
      (' with a wrap indent', const LineProperties(wrapIndent: 40), 40.0),
    ]) {
      test('a selection across the rows of a wrapped line stays on its glyphs$name, ${platform.name}', () async {
        debugDefaultTargetPlatformOverride = platform;
        final shape = _shape(line, props,
            selection: SelectionInfo(start: 10, end: 20, selectionForeground: const Color(0xFFFFFFFF)));
        await _paint(shape);
        expect(shape.debugPaintedSelectionRects.map((r) => (r.left, r.right)).toList(),
            [(100.0, 160.0), (secondRowStart, secondRowStart + 40)]);
      });
    }
  }

  test('a selection inside one row of a wrapped line stays on its glyphs', () async {
    final shape = _shape(line, const LineProperties(),
        selection: SelectionInfo(start: 2, end: 5, selectionForeground: const Color(0xFFFFFFFF)));
    await _paint(shape);
    final rects = shape.debugPaintedSelectionRects;
    expect(rects.map((r) => (r.left, r.right)).toList(), [(20.0, 50.0)]);
  });
}
