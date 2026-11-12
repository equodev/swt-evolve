// A tab or font change lays a StyledText's shape out again. What that costs has to follow the lines
// drawn, not the document: the style runs of a line are found when the line is asked about, and the
// text is scanned for its line delimiters only when the text changed.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/font.dart';
import 'package:swtflutter/src/gen/fontdata.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/styledtext.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;
import 'package:swtflutter/src/impl/styledtext_evolve.dart';

import 'delivery/support/deliver.dart';

const _style = TextStyle(fontSize: 10, height: 1.2);
const _bold = TextStyle(fontSize: 10, height: 1.2, fontWeight: FontWeight.bold);
const _canvas = Size(400, 200);

TextShape _shape(String text, List<StyleRange> runs) => TextShape(
      text,
      Offset.zero,
      _style,
      null, // textSpan
      null, // caretInfo
      false, // wordWrap
      _canvas,
      true, // editable
      null, // styledTextId
      null, // onTextChanged
      TextEditingState(characterRanges: runs),
      null, // selectionInfo
      0.0, // lineHeight
      4,
    );

/// The runs that share at least one character with [line], the last line taking every run that
/// starts on it.
List<StyleRange> _expectedRuns(String text, List<StyleRange> runs, int line) {
  final starts = [0];
  for (var i = 0; i < text.length; i++) {
    if (text.codeUnitAt(i) == 0x0A) starts.add(i + 1);
  }
  final from = starts[line];
  final last = line + 1 == starts.length;
  return [
    for (final run in runs)
      if (run.end > run.start && run.end > from && (last || run.start < starts[line + 1])) run,
  ];
}

VFont _font(int height) => VFont()
  ..fontData = [
    VFontData()
      ..name = 'Ahem'
      ..height = height
      ..style = SWT.NORMAL
  ];

VStyledText _value(String text, {int seq = 10}) => VStyledText()
  ..swt = 'StyledText'
  ..id = 1
  ..seq = seq
  ..font = _font(10)
  ..style = SWT.MULTI | SWT.V_SCROLL
  ..enabled = true
  ..editable = true
  ..caretOffset = 0
  ..text = text
  ..tabs = 4
  ..wordWrap = false
  ..bounds = (VRectangle()
    ..width = 400
    ..height = 200);

Future<void> _mount(WidgetTester tester, VStyledText value) async {
  await tester.pumpWidget(const SizedBox());
  freshClient();
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(width: 400, height: 200, child: StyledTextSwt<VStyledText>(value: value)),
  ));
  await tester.pumpAndSettle();
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('a line\'s style runs are found without sorting every line\'s', () {
    final text = List.filled(100000, 'line').join('\n');
    final shape = _shape(text, [StyleRange(start: 0, end: text.length, style: _style)]);
    TextShape.debugLinesBucketed = 0;

    expect(shape.debugRunsOnLine(0), hasLength(1));
    expect(shape.debugRunsOnLine(99999), hasLength(1));
    expect(TextShape.debugLinesBucketed, 0);
  });

  test('a line carries the runs that reach it, whatever their order', () {
    const text = 'zero\none\ntwo\nthree\n\nfive';
    final runs = [
      const StyleRange(start: 0, end: 2, style: _bold),
      // From line 0 into line 2, ending on line 2's first character.
      const StyleRange(start: 3, end: 10, style: _style),
      // Overlapping the one before it.
      const StyleRange(start: 9, end: 15, style: _bold),
      // Ending at line 3's start: its delimiter is the last character it covers.
      const StyleRange(start: 15, end: 19, style: _style),
      const StyleRange(start: 21, end: 26, style: _style),
    ];
    final withEmpty = [...runs]..insert(4, const StyleRange(start: 20, end: 20, style: _bold));
    for (final (name, given) in [
      ('in order', runs),
      ('out of order', runs.reversed.toList()),
      ('with an empty run', withEmpty),
    ]) {
      final shape = _shape(text, given);
      for (var line = 0; line < 6; line++) {
        expect(shape.debugRunsOnLine(line), _expectedRuns(text, given, line), reason: '$name, line $line');
      }
      expect(shape.debugRunsOnLine(6), isEmpty, reason: '$name, past the last line');
    }
  });

  /// Mounts a 20,000-line document, then changes its tabs and its font, the counters reset between.
  Future<void> changeTabsThenFont(WidgetTester tester) async {
    final text = List.filled(20000, 'a\tline').join('\r\n');
    await _mount(tester, _value(text));
    TextShape.debugLinesBucketed = 0;
    StyledTextImpl.debugDelimiterScans = 0;
    StyledTextImpl.debugRebuildCalls = 0;

    applyChange(_value(text, seq: 11)..tabs = 8, keys: [WireKey.k_StyledText_tabs], base: 10);
    await tester.pumpAndSettle();
    applyChange(_value(text, seq: 12)..font = _font(14), keys: [WireKey.k_Control_font], base: 11);
    await tester.pumpAndSettle();
    expect(StyledTextImpl.debugRebuildCalls, greaterThan(0));
  }

  testWidgets('a tab or font change sorts no line\'s runs', (tester) async {
    await changeTabsThenFont(tester);
    expect(TextShape.debugLinesBucketed, 0);
  });

  testWidgets('a tab or font change does not scan the text for its delimiters again', (tester) async {
    await changeTabsThenFont(tester);
    expect(StyledTextImpl.debugDelimiterScans, 0);
  });

  testWidgets('a new text is scanned for its line delimiters', (tester) async {
    await _mount(tester, _value('one\r\ntwo'));
    StyledTextImpl.debugDelimiterScans = 0;

    applyChange(_value('one\ntwo\r\nthree', seq: 11), keys: [WireKey.k_StyledText_text], base: 10);
    await tester.pumpAndSettle();

    expect(StyledTextImpl.debugDelimiterScans, 1);
    expect(find.byType(StyledTextSwt<VStyledText>), findsOneWidget);
  });
}
