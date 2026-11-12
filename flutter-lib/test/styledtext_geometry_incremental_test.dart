// The geometry table Java answers its position API from is built after every edit from the table
// before it, wherever the edit left the inputs of a line as they were. Whatever an editor goes
// through, the table after each step must be the one an editor that was handed the result directly
// builds: same rows, same content width (the horizontal scroll range), same content height.

import 'dart:math';
import 'dart:math' as math;

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/impl/styledtext_evolve.dart';

const _style = TextStyle(fontSize: 12, fontFamily: 'Liberation Mono');
const _view = Size(600, 400);

/// The styles runs are drawn in, shared as the client shares a decoded palette between frames.
final _palette = [
  for (var i = 0; i < 6; i++)
    TextStyle(fontSize: 12, fontFamily: 'Liberation Mono', color: Color(0xFF7F0055 + 0x203010 * i)),
  const TextStyle(fontSize: 12, fontFamily: 'Liberation Mono', fontWeight: FontWeight.bold),
];

/// A style that widens a laid-out line, where a width estimated from the line's length does not:
/// what tells a width measured from one estimated, or measured in other runs, apart.
const _spaced = TextStyle(fontSize: 12, fontFamily: 'Liberation Mono', letterSpacing: 3);

/// A source file's worth of lines: indented code, blank lines, tabs, a few long ones.
String _document(int lines) {
  final out = StringBuffer();
  for (var i = 0; i < lines; i++) {
    if (i > 0) out.write('\n');
    if (i % 17 == 0) continue;
    if (i % 23 == 0) {
      out.write('\tcase $i:\t\treturn value$i;');
    } else if (i % 97 == 0) {
      out.write('    // ${'a long comment that goes well past the others ' * 3}$i');
    } else {
      out.write('        final total$i = prices.get(sku).times(quantity); // line $i');
    }
  }
  return out.toString();
}

/// Words coloured from the palette by where they start, as a highlighter colours a file.
List<StyleRange> _highlight(String text, {Set<int> boldLines = const {}, Set<int> spacedLines = const {}}) {
  final ranges = <StyleRange>[];
  var offset = 0;
  var line = 0;
  for (final l in text.split('\n')) {
    for (final m in RegExp(r'\w+').allMatches(l)) {
      ranges.add(StyleRange(
        start: offset + m.start,
        end: offset + m.end,
        style: spacedLines.contains(line)
            ? _spaced
            : boldLines.contains(line)
                ? _palette.last
                : _palette[m.start % 6],
      ));
    }
    offset += l.length + 1;
    line++;
  }
  return ranges;
}

class _Editor {
  _Editor(this.text, {this.wrap = false, this.width = 600}) : props = {9: _currentLine} {
    restyle();
    shape = _build();
    table();
  }

  static const _currentLine = LineProperties(background: Color(0xFF373737));

  String text;
  final bool wrap;
  double width;
  Set<int> boldLines = {};
  Set<int> spacedLines = {};
  Map<int, LineProperties> props;
  late List<StyleRange> ranges;
  late TextShape shape;
  double scrollTop = 0;

  /// One run over the whole text, as a view that sets no styles of its own is sent, rather than a
  /// highlighter's words.
  bool oneRun = false;

  void restyle() => ranges = oneRun
      ? [StyleRange(start: 0, end: text.length, style: _palette.first)]
      : _highlight(text, boldLines: boldLines, spacedLines: spacedLines);

  TextEditingState get state => TextEditingState(characterRanges: ranges, lineProperties: props);

  TextShape _build() => TextShape(text, Offset.zero, _style, null, null, wrap, Size(width, _view.height), true, 1,
      null, state, null, 14.0, 4, null, const LineProperties(), 0, null, TextDirection.ltr, false, const [],
      const [], 0, !wrap);

  /// The table as an editor scrolled to [scrollTop] describes it.
  Map<String, dynamic> table([TextShape? of]) => (of ?? shape)
      .computeGeometryTable(visibleTop: scrollTop, visibleBottom: scrollTop + _view.height)
      .toMap();

  /// Replaces [replaced] characters at [start] with [inserted], as Java's text change does, and
  /// checks the table that follows against a fresh editor's.
  void edit(int start, int replaced, String inserted, {String? because}) {
    final before = text;
    text = text.replaceRange(start, start + replaced, inserted);
    noteTextEdit(before, text, start, replaced, inserted);
    restyle();
    step(because ?? 'edit at $start: $replaced replaced by ${inserted.length}');
  }

  /// The same edit, its shape replaced before it describes the document, as when a frame builds
  /// several shapes and describes only the last.
  void editUndescribed(int start, int replaced, String inserted) {
    final before = text;
    text = text.replaceRange(start, start + replaced, inserted);
    noteTextEdit(before, text, start, replaced, inserted);
    restyle();
    shape = _build()..inheritMeasurementsFrom(shape);
  }

  /// A new shape after the inputs changed, inheriting from the one before; its table must be a fresh one's.
  void step(String because, {void Function(TextShape)? beforeItsTable}) {
    final next = _build()..inheritMeasurementsFrom(shape);
    shape = next;
    beforeItsTable?.call(next);
    final incremental = table();
    final fresh = table(_build());
    expect(_firstDifference(incremental, fresh), isNull, reason: because);
  }

  /// Where two tables first differ, said in a line; null when they do not.
  static String? _firstDifference(Map<String, dynamic> got, Map<String, dynamic> want) {
    for (final key in ['charCount', 'contentWidth', 'contentHeight']) {
      if (got[key] != want[key]) return '$key ${got[key]}, a fresh editor\'s ${want[key]}';
    }
    final a = got['lines'] as List, b = want['lines'] as List;
    for (var i = 0; i < math.min(a.length, b.length); i++) {
      if (a[i].toString() != b[i].toString()) return 'row $i: ${a[i]}, a fresh editor\'s ${b[i]}';
    }
    return a.length == b.length ? null : '${a.length} rows, a fresh editor\'s ${b.length}';
  }

  int offsetOfLine(int line) {
    var at = 0;
    for (var i = 0; i < line; i++) {
      at = text.indexOf('\n', at) + 1;
    }
    return at;
  }

  int lineCount() => '\n'.allMatches(text).length + 1;

  double contentWidth() => table()['contentWidth'] as double;
}

void main() {
  // Past the size below which the whole document is described with its character positions.
  const lines = 1200;

  group('typing', () {
    test('a character at the top, inside the view', () {
      final e = _Editor(_document(lines));
      final at = e.offsetOfLine(3) + 10;
      e.edit(at, 0, 'x');
      e.edit(at + 1, 0, 'y');
      e.edit(at, 2, '');
    });

    test('a character far below the view', () {
      final e = _Editor(_document(lines));
      final at = e.offsetOfLine(900) + 12;
      e.edit(at, 0, 'x');
      e.edit(at, 1, '');
    });

    test('at the very start and the very end of the document', () {
      final e = _Editor(_document(lines));
      e.edit(0, 0, 'x');
      e.edit(e.text.length, 0, 'z');
      e.edit(0, 1, '');
      e.edit(e.text.length - 1, 1, '');
    });

    test('into a line of tabs', () {
      final e = _Editor(_document(lines));
      final at = e.offsetOfLine(23) + 1;
      e.edit(at, 0, '\t');
      e.edit(at, 0, 'ab');
      e.edit(at, 3, '');
    });
  });

  group('lines', () {
    test('Enter in the middle of a line, then Backspace joins it again', () {
      final e = _Editor(_document(lines));
      final at = e.offsetOfLine(5) + 20;
      e.edit(at, 0, '\n');
      e.edit(at, 0, '\n        ');
      e.edit(at, 10, '');
    });

    test('Enter at the start and at the end of a line', () {
      final e = _Editor(_document(lines));
      e.edit(e.offsetOfLine(7), 0, '\n');
      final end = e.offsetOfLine(9) - 1;
      e.edit(end, 0, '\n');
    });

    test('a new line far below the view moves every line after it', () {
      final e = _Editor(_document(lines));
      e.edit(e.offsetOfLine(1000), 0, 'inserted line\n');
      e.edit(e.offsetOfLine(1000), 14, '');
    });

    test('pasting several lines and deleting a block of lines', () {
      final e = _Editor(_document(lines));
      final at = e.offsetOfLine(12) + 8;
      e.edit(at, 0, 'one\ntwo\n\tthree\nfour');
      final from = e.offsetOfLine(40);
      e.edit(from, e.offsetOfLine(60) - from, '');
    });

    test('deleting everything and typing again', () {
      final e = _Editor(_document(lines));
      e.edit(0, e.text.length, '');
      e.edit(0, 0, 'a\nb');
    });
  });

  group('the content width, which is the horizontal scroll range', () {
    test('grows when a line out of view outgrows the widest', () {
      final e = _Editor(_document(lines));
      final before = e.contentWidth();
      e.edit(e.offsetOfLine(800) + 5, 0, 'w' * 300);
      expect(e.contentWidth(), greaterThan(before));
    });

    test('grows when a line in view outgrows the widest, and shrinks back when it is cut', () {
      final e = _Editor(_document(lines));
      final before = e.contentWidth();
      final at = e.offsetOfLine(4) + 5;
      e.edit(at, 0, 'w' * 300);
      expect(e.contentWidth(), greaterThan(before));
      e.edit(at, 300, '');
      expect(e.contentWidth(), before);
    });

    test('shrinks when the widest line is deleted', () {
      final e = _Editor(_document(lines));
      final widest = e.offsetOfLine(97);
      final before = e.contentWidth();
      e.edit(widest, e.offsetOfLine(98) - widest, '');
      expect(e.contentWidth(), lessThanOrEqualTo(before));
    });

    test('follows the view getting narrower and wider', () {
      final e = _Editor(_document(lines));
      e.width = 300;
      e.step('narrower view');
      e.width = 900;
      e.step('wider view');
    });
  });

  group('styles and line properties', () {
    test('a line out of view turned bold, and back', () {
      final e = _Editor(_document(lines));
      e.boldLines = {700};
      e.restyle();
      e.step('bold line out of view');
      e.boldLines = {};
      e.restyle();
      e.step('bold line back');
    });

    test('a line in view turned bold', () {
      final e = _Editor(_document(lines));
      e.boldLines = {6};
      e.restyle();
      e.step('bold line in view');
    });

    test('the current line moving, in view and far below it', () {
      final e = _Editor(_document(lines));
      e.props = {12: _Editor._currentLine};
      e.step('current line in view');
      e.props = {1100: _Editor._currentLine};
      e.step('current line far below');
    });

    test('a vertical indent above a line out of view, and taken away', () {
      final e = _Editor(_document(lines));
      e.props = {9: _Editor._currentLine, 600: const LineProperties(verticalIndent: 9)};
      e.step('vertical indent out of view');
      e.props = {9: _Editor._currentLine};
      e.step('vertical indent removed');
    });

    test('an indent and an alignment of its own on a line out of view', () {
      final e = _Editor(_document(lines));
      e.props = {9: _Editor._currentLine, 650: const LineProperties(indent: 40), 651: const LineProperties(alignment: 1 << 17)};
      e.step('indented and centred lines');
    });
  });

  group('widths that were measured', () {
    test('a line laid out while spaced, out of view since, takes its width without spacing once that goes', () {
      final e = _Editor(_document(lines));
      e.spacedLines = {700};
      e.restyle();
      e.step('line 700 spaced');
      e.scrollTop = 695 * 14.0;
      e.step('scrolled to line 700, which is laid out');
      e.scrollTop = 0;
      e.step('scrolled back to the top');
      e.spacedLines = {};
      e.restyle();
      e.step('line 700 no longer spaced');
    });

    test('a line far below takes the width its twin in view was laid out to', () {
      // Line 5 is made the same text, in the same runs, as line 900: laying it out caches what line 900 looks up.
      final e = _Editor(_document(lines));
      final twin = e.text.split('\n')[900];
      e.spacedLines = {900};
      e.restyle();
      e.step('line 900 spaced');
      final from = e.offsetOfLine(5);
      e.spacedLines = {5, 900};
      e.edit(from, e.offsetOfLine(6) - 1 - from, twin, because: 'line 5 made line 900\'s twin');
    });
  });

  test('lines laid out between two passes, as a frame paints them, are described by their layout', () {
    final e = _Editor(_document(lines));
    e.spacedLines = {700};
    e.restyle();
    e.step('line 700 spaced');
    e.step('every line laid out before the table', beforeItsTable: (shape) => shape.debugShapelessHeights());
  });

  test('shapes replaced before they describe the document leave the next one described as fresh', () {
    final e = _Editor(_document(lines));
    final at = e.offsetOfLine(4) + 9;
    e.editUndescribed(at, 0, 'x');
    e.editUndescribed(at + 1, 0, '\n');
    e.editUndescribed(e.offsetOfLine(900), 0, 'w' * 40);
    e.edit(at, 1, '', because: 'the first shape described after three that were not');
    e.boldLines = {500};
    e.restyle();
    e.shape = e._build()..inheritMeasurementsFrom(e.shape);
    e.step('a restyle undescribed, then a step');
  });

  group('scrolling', () {
    test('to the middle and the end, typing at each', () {
      final e = _Editor(_document(lines));
      e.scrollTop = 600 * 14.0;
      e.step('scrolled to the middle');
      e.edit(e.offsetOfLine(605) + 9, 0, 'x');
      e.scrollTop = (lines - 20) * 14.0;
      e.step('scrolled to the end');
      e.edit(e.offsetOfLine(lines - 5) + 3, 0, '\n');
    });
  });

  test('a long run of random edits, restyles and scrolls', () {
    final e = _Editor(_document(lines));
    final random = Random(11);
    final pieces = ['x', 'yz', '\n', '\t', '\n    ', 'w' * 120, ''];
    for (var i = 0; i < 150; i++) {
      final kind = random.nextInt(10);
      if (kind < 6) {
        final start = random.nextInt(e.text.length + 1);
        final replaced = random.nextInt(min(30, e.text.length - start) + 1) * (random.nextBool() ? 1 : 0);
        e.edit(start, replaced, pieces[random.nextInt(pieces.length)], because: 'random edit $i');
      } else if (kind < 8) {
        e.boldLines = {random.nextInt(e.lineCount())};
        e.restyle();
        e.step('random restyle $i');
      } else if (kind < 9) {
        e.props = {random.nextInt(e.lineCount()): _Editor._currentLine};
        e.step('random current line $i');
      } else {
        e.scrollTop = random.nextInt(e.lineCount()) * 14.0;
        e.step('random scroll $i');
      }
    }
  });

  for (final (name, document) in [
    ('lines of one length', _document(lines)),
    ('the same lines over and over', List.generate(lines, (i) => _document(40).split('\n')[i % 40]).join('\n')),
  ]) {
    test('a folded block leaves the rows out of view as the pass before described them: $name', () {
      // Lines of one length, and lines repeated word for word, are most of a source file: laying
      // out or painting the lines in view must not make the rest look changed.
      final e = _Editor(document);
      final from = e.offsetOfLine(12);
      final to = e.offsetOfLine(32);
      final block = e.text.substring(from, to);
      final inView = (_view.height / 14).ceil() + 2;
      for (var k = 0; k < 2; k++) {
        TextShape.debugGeometryRowsReused = 0;
        e.edit(from, to - from, '', because: 'fold');
        expect(TextShape.debugGeometryRowsReused, greaterThan(e.lineCount() - 12 - 3 * inView), reason: 'fold');
        TextShape.debugGeometryRowsReused = 0;
        e.edit(from, 0, block, because: 'unfold');
        expect(TextShape.debugGeometryRowsReused, greaterThan(e.lineCount() - 32 - 3 * inView), reason: 'unfold');
      }
    });
  }

  test('typing under one run over the whole document leaves the rows out of view as they were', () {
    final e = _Editor(_document(lines))
      ..oneRun = true
      ..restyle();
    e.step('one run');
    final inView = (_view.height / 14).ceil() + 2;
    final at = e.offsetOfLine(12) + 10;
    for (var k = 0; k < 3; k++) {
      TextShape.debugGeometryRowsReused = 0;
      e.edit(at + k, 0, 'x');
      expect(TextShape.debugGeometryRowsReused, greaterThan(e.lineCount() - 3 * inView), reason: 'keystroke $k');
    }
    e.edit(e.offsetOfLine(600), 0, 'new\n', because: 'a line added under the run');
  });

  test('a wrapped document follows its edits too', () {
    final e = _Editor(_document(400), wrap: true, width: 300);
    e.edit(e.offsetOfLine(3) + 10, 0, 'x' * 80);
    e.edit(e.offsetOfLine(300), 0, 'new\n');
  });
}
