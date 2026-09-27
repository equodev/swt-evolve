// The cached paragraph carries its colours, so it cannot be keyed by the layout key alone, which
// ignores colour because recolouring moves no glyph.

import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/impl/styledtext_evolve.dart';

const _size = Size(400, 600);
const _widgetStyle = TextStyle(fontSize: 12);

String _text() => List.generate(
      20,
      (i) => 'line $i: the quick brown fox jumps over the lazy dog',
    ).join('\n');

/// A colour-only range as the renderer decode produces it, with the widget's own metrics.
StyleRange _coloured(int start, int end, Color colour) => StyleRange(
      start: start,
      end: end,
      style: TextStyle(
        fontSize: 12,
        fontWeight: FontWeight.normal,
        fontStyle: FontStyle.normal,
        color: colour,
      ),
    );

TextShape _shape(List<StyleRange> ranges) => TextShape(
      _text(),
      Offset.zero,
      _widgetStyle,
      null,
      null,
      null,
      false,
      _size,
      true,
      1,
      null,
      TextEditingState(characterRanges: ranges),
      null,
      14.0,
      4,
    );

int _paintersBuiltBy(TextShape shape) {
  TextShape.debugPainterLayouts = 0;
  final recorder = ui.PictureRecorder();
  shape.draw(Canvas(recorder));
  recorder.endRecording().dispose();
  return TextShape.debugPainterLayouts;
}

void main() {
  setUp(debugResetLineLayoutCache);

  test('repainting an unchanged document builds no paragraphs', () {
    final ranges = [_coloured(10, 14, const Color(0xFFFF0000))];
    _paintersBuiltBy(_shape(ranges));

    expect(
      _paintersBuiltBy(_shape(ranges)),
      0,
      reason: 'the same lines in the same colours at the same width draw the same pixels, '
          'which is what lets a scroll or a caret blink reuse them',
    );
  });

  test('recolouring a range repaints the line it falls on', () {
    _paintersBuiltBy(_shape([_coloured(10, 14, const Color(0xFFFF0000))]));

    expect(
      _paintersBuiltBy(_shape([_coloured(10, 14, const Color(0xFF00FF00))])),
      greaterThanOrEqualTo(1),
      reason: 'the range moves no glyph, so the line keeps its measurements — but it is drawn '
          'in a different colour, and a paragraph kept from before carries the old one',
    );
  });

  test('a highlight arriving on a painted line is drawn', () {
    _paintersBuiltBy(_shape(const []));

    expect(
      _paintersBuiltBy(_shape([_coloured(10, 14, const Color(0xFFFFFF00))])),
      greaterThanOrEqualTo(1),
      reason: 'an occurrence highlight lands on lines the editor is already showing',
    );
  });

  test('and so is one going away', () {
    _paintersBuiltBy(_shape([_coloured(10, 14, const Color(0xFFFFFF00))]));

    expect(
      _paintersBuiltBy(_shape(const [])),
      greaterThanOrEqualTo(1),
      reason: 'the caret moving off an identifier clears its highlights',
    );
  });
}
