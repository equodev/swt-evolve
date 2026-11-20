// A range whose GlyphMetrics are 0 wide takes no horizontal space, as on SWT: a console hides
// ANSI escape sequences that way and colours only the text between them.

import 'dart:math' as math;
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/glyphmetrics.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/styledtext.dart';
import 'package:swtflutter/src/gen/stylerange.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/styledtext_evolve.dart';

const _esc = '\u001b';
const _open = '$_esc[32m';
const _close = '$_esc[0;39m';
const _line1 = '${_open}INFO$_close started';
const _line2 = 'INFO started';

final _hidden = VStyleRange()
  ..metrics = (VGlyphMetrics()
    ..ascent = 0
    ..descent = 0
    ..width = 0);

final _green = VStyleRange()
  ..foreground = (VColor()
    ..alpha = 255
    ..red = 0
    ..green = 128
    ..blue = 0);

VStyledText _value() => VStyledText()
  ..swt = 'StyledText'
  ..id = 1
  ..style = SWT.H_SCROLL | SWT.V_SCROLL
  ..enabled = true
  ..editable = true
  ..caretOffset = 0
  ..text = '$_line1\n$_line2'
  ..styles = [_hidden, _green]
  ..styleIndex = [
    1,
    0, 0, _open.length,
    1, _open.length, 4,
    0, _open.length + 4, _close.length,
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 500
    ..height = 200);

List<double> _charX(Map<String, dynamic> row) {
  if (row['cx'] != null) return (row['cx'] as List).cast<num>().map((v) => v.toDouble()).toList();
  final cxu = (row['cxu'] as List).cast<num>();
  final n = (row['e'] as int) - (row['s'] as int);
  return [for (var i = 0; i <= n; i++) cxu[0] + i * cxu[1].toDouble()];
}

const _black = Color(0xFF000000);
const _textStyle = TextStyle(fontSize: 10, height: 1.0, color: _black);
const _canvas = Size(400, 200);

/// [lines] copies of [_line1], each with its two escapes hidden and "INFO" in green.
TextShape _console(int lines, {SelectionInfo? selection}) {
  final text = List.filled(lines, _line1).join('\n');
  final runs = <StyleRange>[];
  for (var i = 0, start = 0; i < lines; i++, start += _line1.length + 1) {
    final info = start + _open.length;
    runs
      ..add(StyleRange(start: start, end: info, style: _textStyle, glyphWidth: 0))
      ..add(StyleRange(start: info, end: info + 4, style: _textStyle.copyWith(color: const Color(0xFF008000))))
      ..add(StyleRange(start: info + 4, end: info + 4 + _close.length, style: _textStyle, glyphWidth: 0));
  }
  return TextShape(
    text,
    Offset.zero,
    _textStyle,
    null, // textSpan
    null, // caretInfo
    false, // wordWrap
    _canvas,
    true, // editable
    1,
    null, // onTextChanged
    TextEditingState(characterRanges: runs),
    selection,
    10.0, // lineHeight
  );
}

void main() {
  setUp(() => StyledTextImpl.debugLastGeometry = null);

  test('a point on an escape hits the visible text next to it', () {
    final shape = _console(1);
    // Every escape is 0 wide, so "INFO" starts at x 0 and each glyph is 10 wide.
    expect(shape.getOffsetFromPosition(const Offset(1, 5), _canvas), _open.length,
        reason: 'the left edge is the offset after the opening escape');
    expect(shape.getOffsetFromPosition(const Offset(21, 5), _canvas), _open.length + 2,
        reason: 'x 21 is inside "F", past the hidden characters');
    expect(shape.getOffsetFromPosition(const Offset(41, 5), _canvas), _open.length + 4 + _close.length,
        reason: 'past "INFO" the caret lands after the closing escape, before " started"');
  });

  test('a selection across an escape is as wide as the visible text it holds', () {
    // From the start of the line through "INFO" and the closing escape.
    final shape = _console(1, selection: SelectionInfo(start: 0, end: _open.length + 4 + _close.length));
    final recorder = ui.PictureRecorder();
    shape.draw(Canvas(recorder));
    recorder.endRecording().dispose();
    final rects = shape.debugPaintedSelectionRects;
    expect(rects, isNotEmpty);
    expect(rects.map((r) => r.left).reduce(math.min), closeTo(0, 0.5));
    expect(rects.map((r) => r.right).reduce(math.max), closeTo(40, 0.5),
        reason: 'the highlight covers "INFO" only: the escapes add no width');
  });

  test('laying out a long console checks only the escapes on the lines laid out', () {
    const lines = 3000;
    final shape = _console(lines);
    TextShape.debugGlyphWidthProbes = 0;
    final recorder = ui.PictureRecorder();
    shape.draw(Canvas(recorder));
    recorder.endRecording().dispose();
    // At most one probe per hidden run on the line for each character laid out; scanning every
    // run of the document instead costs thousands per character.
    expect(TextShape.debugGlyphWidthProbes, greaterThan(0));
    expect(TextShape.debugGlyphWidthProbes, lessThanOrEqualTo(2 * shape.text.length));
  });

  testWidgets('characters under zero-width metrics take no horizontal space', (tester) async {
    final key = GlobalKey<StyledTextImpl>();
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 500,
        height: 200,
        child: StyledTextSwt<VStyledText>(key: key, value: _value()),
      ),
    ));
    await tester.pump(Duration.zero);

    final rows = (StyledTextImpl.debugLastGeometry!['lines'] as List).cast<Map<String, dynamic>>();
    final x1 = _charX(rows[0]);
    final x2 = _charX(rows[1]);
    for (var i = 0; i <= _open.length; i++) {
      expect(x1[i], closeTo(0, 0.5), reason: 'offset $i inside the opening escape is at the left edge');
    }
    for (var i = 0; i <= 4; i++) {
      expect(x1[_open.length + i], closeTo(x2[i], 0.5), reason: 'offset $i of "INFO" matches the plain line');
    }
    final infoEnd = x1[_open.length + 4];
    for (var i = 1; i <= _close.length; i++) {
      expect(x1[_open.length + 4 + i], closeTo(infoEnd, 0.5),
          reason: 'offset $i inside the closing escape stays at the end of "INFO"');
    }
    expect(x1[_line1.length], closeTo(x2[_line2.length], 0.5),
        reason: 'the line ends where the same text without escapes does');
  });
}
