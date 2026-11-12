@Tags(['bench'])
library;

// What a keystroke costs the render side in a large highlighted document: the edit taken into the
// line cache, the shape that replaces the one before it, its line tops and the geometry table Java
// is sent. Not an assertion of a number - machines differ. It prints, so a change to this path can
// be measured before and after on the same machine.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/impl/styledtext_evolve.dart';

const _lines = 6500;
const _warmup = 20;
const _runs = 100;
const _size = Size(1000, 600);
const _style = TextStyle(fontSize: 12, fontFamily: 'Liberation Mono');
const _line = '        final Money total = prices.get(sku).times(quantity); // line';

/// The styles runs are drawn in, shared as the client shares a decoded palette between frames.
final _palette = List.generate(
    8, (i) => TextStyle(fontSize: 12, fontFamily: 'Liberation Mono', color: Color(0xFF7F0055 + 0x101010 * i)));

TextEditingState _highlight(String text) {
  final ranges = <StyleRange>[];
  var offset = 0;
  for (final line in text.split('\n')) {
    for (final m in RegExp(r'\w+').allMatches(line).take(6)) {
      ranges.add(StyleRange(
        start: offset + m.start,
        end: offset + m.end,
        style: _palette[m.start % _palette.length],
      ));
    }
    offset += line.length + 1;
  }
  // The current line's background, which an editor always has.
  return TextEditingState(characterRanges: ranges, lineProperties: const {9: LineProperties(background: Color(0xFF373737))});
}

/// A source editor's shape: fixed line height, no wrapping.
TextShape _shape(String text, TextEditingState state) => TextShape(text, Offset.zero, _style, null, null, false,
    _size, true, 1, null, state, null, 14.0, 4, null, const LineProperties(), 0, null, TextDirection.ltr, false,
    const [], const [], 0, true);

void main() {
  test('a keystroke near the top of a 6,500-line document', () {
    // Each line its own text, as a source file's are: no line answers for another's layout.
    final before = List.generate(_lines, (i) => '$_line $i').join('\n');
    // Inside the tenth line, past its indent.
    final tenth = List.generate(9, (i) => '$_line $i'.length + 1).fold(0, (a, b) => a + b) + 14;
    final after = before.replaceRange(tenth, tenth, 'x');
    final states = [_highlight(before), _highlight(after)];
    final texts = [before, after];

    var shape = _shape(before, states[0]);
    shape.computeGeometryTable(visibleTop: 0, visibleBottom: _size.height);
    final times = <int>[];
    final phases = [0, 0, 0];
    for (var i = 0; i < _warmup + _runs; i++) {
      final from = i % 2, to = 1 - from;
      final watch = Stopwatch()..start();
      // Typing the x, or deleting it again.
      if (to == 1) {
        noteTextEdit(texts[from], texts[to], tenth, 0, 'x');
      } else {
        noteTextEdit(texts[from], texts[to], tenth, 1, '');
      }
      final next = _shape(texts[to], states[to])..inheritMeasurementsFrom(shape);
      final shaped = watch.elapsedMicroseconds;
      next.textHeight;
      final topped = watch.elapsedMicroseconds;
      next.computeGeometryTable(visibleTop: 0, visibleBottom: _size.height);
      watch.stop();
      if (i >= _warmup) {
        times.add(watch.elapsedMicroseconds);
        phases[0] += shaped;
        phases[1] += topped - shaped;
        phases[2] += watch.elapsedMicroseconds - topped;
      }
      shape = next;
    }
    times.sort();
    // ignore: avoid_print
    print('[bench] render side keystroke, $_lines lines: median ${times[_runs ~/ 2]} us, '
        'p90 ${times[_runs * 9 ~/ 10]} us; mean shape ${phases[0] ~/ _runs} us, '
        'line tops ${phases[1] ~/ _runs} us, geometry ${phases[2] ~/ _runs} us');
  });
}
