import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/impl/styledtext_evolve.dart';

/// The caret's y is the sum of the heights of every line above it. Finding it must
/// not cost one TextPainter layout per preceding line on every repaint — with the
/// caret blinking on line N of a large document that is N layouts per frame. Line
/// tops are cached per shape and carried across the caret/selection copies a blink
/// tick creates, so a repaint lays out at most the caret's own line.
void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  const style = TextStyle(fontSize: 10, height: 1.2);
  const canvas = Size(400, 2400);
  const lineHeight = 12.0;

  TextShape shapeFor(
    String text,
    int caretOffset, {
    bool wordWrap = false,
    TextEditingState? editingState,
  }) => TextShape(
    text,
    Offset.zero,
    style,
    null, // clipRect
    null, // textSpan
    CaretInfo(
      offset: caretOffset,
      width: 1.0,
      height: lineHeight,
      color: const Color(0xFF000000),
      visible: true,
      blinking: false,
      styledTextId: 1,
      blinkRate: 500,
    ),
    wordWrap,
    canvas,
    true, // editable
    null, // styledTextId
    null, // onTextChanged
    editingState,
    null, // selectionInfo
    lineHeight,
    4, // tabs
  );

  Canvas recordingCanvas() => Canvas(ui.PictureRecorder());

  final text = List.generate(200, (i) => 'line $i').join('\n');

  test('a blink repaint lays out at most the caret line, not the document', () {
    final shape = shapeFor(text, text.length);
    shape.draw(recordingCanvas());

    // A blink tick repaints via a copyWithCaret copy of the same shape.
    final blink = shape.copyWithCaret(
      shape.caretInfo!.copyWith(visible: true),
    );
    TextShape.debugLayoutLineCalls = 0;
    expect(blink.caretRect(), isNotNull);

    expect(
      TextShape.debugLayoutLineCalls,
      lessThanOrEqualTo(1),
      reason: 'caret y must be a cached lookup, not a re-layout of all 199 '
          'preceding lines on every repaint',
    );
  });

  test('a hit test after a paint lays out at most the hit line', () {
    final shape = shapeFor(text, 0);
    shape.draw(recordingCanvas());

    TextShape.debugLayoutLineCalls = 0;
    final offset = shape.getOffsetFromPosition(
      const Offset(5, 199 * lineHeight + 1),
      canvas,
    );

    expect(offset, greaterThan(text.length - 10));
    expect(
      TextShape.debugLayoutLineCalls,
      lessThanOrEqualTo(1),
      reason: 'finding the clicked line must use the cached line tops',
    );
  });

  // Correctness guards for the cache: the cached path must place the caret
  // exactly where the uncached walk did, under the properties that make line
  // heights variable (indent, alignment, justify, word wrap).
  test('cached caret position matches a cold walk under mixed line properties', () {
    final mixedText = [
      'plain first line',
      'indented second line',
      'right aligned third line',
      'a justified line long enough to wrap around the canvas width several '
          'times so the wrapped height differs from a single line',
      'last',
    ].join('\n');
    final editingState = TextEditingState(
      characterRanges: const [],
      lineProperties: {
        1: LineProperties(indent: 24),
        2: LineProperties(alignment: 131072), // SWT.RIGHT
        3: LineProperties(justify: true),
      },
    );
    final caretOffset = mixedText.length - 2;

    final cold = shapeFor(
      mixedText,
      caretOffset,
      wordWrap: true,
      editingState: editingState,
    );
    final coldRect = cold.caretRect()!;

    final warm = shapeFor(
      mixedText,
      caretOffset,
      wordWrap: true,
      editingState: editingState,
    );
    warm.draw(recordingCanvas());
    final blink = warm.copyWithCaret(
      warm.caretInfo!.copyWith(visible: true),
    );
    final warmRect = blink.caretRect()!;

    expect(warmRect, coldRect);
  });

  test('cached hit test matches a cold hit test on every line', () {
    final shape = shapeFor(text, 0);
    final cold = <int>[
      for (int i = 0; i < 200; i++)
        shape.getOffsetFromPosition(Offset(5, i * lineHeight + 1), canvas),
    ];

    shape.draw(recordingCanvas());
    final warmShape = shape.copyWithCaret(
      shape.caretInfo!.copyWith(visible: true),
    );
    final warm = <int>[
      for (int i = 0; i < 200; i++)
        warmShape.getOffsetFromPosition(Offset(5, i * lineHeight + 1), canvas),
    ];

    expect(warm, cold);
  });
}
