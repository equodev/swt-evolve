// A line's height is known without laying it out when it is unwrapped printable ASCII in styles
// that share their vertical metrics. Known that way, it has to be exactly the height laying the
// line out gives, or every line below it is drawn somewhere else.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/impl/styledtext_evolve.dart';

const _base = TextStyle(fontSize: 12);

const _lines = [
  'public void method1(int value) {',
  '\tint total = value * 3 + offset;\t// tabbed',
  '',
  '    System.out.println("line 3");',
  'café au lait',
  'emoji \u{1F600} here',
  '中文',
  '~!@#\$%^&*()_+{}|:"<>?`-=[]\\;\',./',
];

TextShape _shape(TextEditingState? state, {bool wrap = false}) => TextShape(
      _lines.join('\n'),
      Offset.zero,
      _base,
      null,
      null,
      wrap,
      const Size(400, 600),
      true,
      1,
      null,
      state,
      null,
      14.0,
      4,
    );

/// Ranges in [style] over the first word of every line.
TextEditingState _ranges(TextStyle style, {int rise = 0}) {
  final ranges = <StyleRange>[];
  var offset = 0;
  for (final line in _lines) {
    final word = RegExp(r'\w+').firstMatch(line);
    if (word != null) {
      ranges.add(StyleRange(start: offset + word.start, end: offset + word.end, style: style, rise: rise));
    }
    offset += line.length + 1;
  }
  return TextEditingState(characterRanges: ranges);
}

/// Every line known without layout has exactly its laid-out height; [known] lines are.
void _expectExact(TextShape shape, {int? known}) {
  var answered = 0;
  for (final (i, (shapeless, measured)) in shape.debugShapelessHeights().indexed) {
    if (shapeless == null) continue;
    answered++;
    expect(shapeless, measured, reason: 'line $i: "${_lines[i]}"');
  }
  if (known != null) expect(answered, known);
}

void main() {
  setUp(debugResetLineLayoutCache);

  test('plain lines are known exactly, other scripts are laid out', () {
    _expectExact(_shape(null), known: 5);
  });

  test('a colour range keeps the line known', () {
    _expectExact(_shape(_ranges(_base.copyWith(color: const Color(0xFF7F0055)))), known: 5);
  });

  test('a bold range is known exactly whenever it is known at all', () {
    _expectExact(_shape(_ranges(_base.copyWith(fontWeight: FontWeight.bold))));
  });

  test('a larger or raised range is laid out', () {
    _expectExact(_shape(_ranges(_base.copyWith(fontSize: 20))), known: 1);
    _expectExact(_shape(_ranges(_base, rise: 3)), known: 1);
  });

  test('a wrapped line is laid out', () {
    _expectExact(_shape(null, wrap: true), known: 0);
  });
}
