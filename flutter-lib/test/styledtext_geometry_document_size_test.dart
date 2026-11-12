// With rows of one height, the geometry outside the band around the viewport follows from the row
// height and the line starts, which Java already holds. A font change moves every row, but what
// the client sends Java for it must not grow with the document: a document four times as long
// costs the same bytes on the geometry channel.

import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/widget_wire.dart';
import 'package:swtflutter/src/gen/font.dart';
import 'package:swtflutter/src/gen/fontdata.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/styledtext.dart';
import 'package:swtflutter/src/gen/styledtextrenderer.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/styledtext_evolve.dart';

import 'delivery/support/deliver.dart';

const _small = 4000;
const _large = 16000;

/// Tabbed lines, every one as tall as the others: `Line j<TAB>value`.
String _document(int lines) => [for (var j = 0; j < lines; j++) 'Line $j\tvalue'].join('\n');

VFont _font(int height) => VFont()
  ..fontData = [
    VFontData()
      ..name = 'Liberation Mono'
      ..height = height
      ..style = SWT.NORMAL
  ];

/// StyledTextRenderer's LineInfo flag for a line that sets its own vertical indent.
const _verticalIndentFlag = 1 << 9;

/// A line out of view that `StyledText#setLineVerticalIndent` moved down.
const _indentedLine = 100;

/// The editor as Java sends it: [fixed] is `StyledText#isFixedLineHeight`, false once a style
/// gives a line its own height; [indented] gives [_indentedLine] a vertical indent.
VStyledText _value(String text,
        {int id = 1, int fontHeight = 10, bool wrap = false, bool fixed = true, bool indented = false}) =>
    VStyledText()
      ..swt = 'StyledText'
      ..id = id
      ..style = SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
      ..enabled = true
      ..editable = true
      ..caretOffset = 0
      ..text = text
      ..font = _font(fontHeight)
      ..wordWrap = wrap
      ..fixedLineHeight = fixed
      ..renderer = (VStyledTextRenderer()
        ..ascent = fontHeight
        ..descent = 3
        ..lines = indented
            ? [
                for (var j = 0; j < _indentedLine; j++) null,
                VLineInfo()
                  ..flags = _verticalIndentFlag
                  ..verticalIndent = 9,
              ]
            : null)
      ..bounds = (VRectangle()
        ..width = 600
        ..height = 400);

/// The last geometry push [_geometryBytesAfter] saw.
Uint8List? _lastPush;

/// The bytes the client sends on the geometry channel after [change], for a document of [lines].
Future<int> _geometryBytesAfter(WidgetTester tester, int lines, VStyledText Function(String, int) change,
    {bool wrap = false, bool fixed = true, bool indented = false}) async {
  final text = _document(lines);
  final app = EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
        width: 600,
        height: 400,
        child: StyledTextSwt<VStyledText>(
            key: GlobalKey<StyledTextImpl>(),
            value: _value(text, id: lines, wrap: wrap, fixed: fixed, indented: indented))),
  );
  await tester.pumpWidget(app);
  await tester.pumpWidget(app);
  await tester.pump(Duration.zero);
  expect(StyledTextImpl.debugGeometryPushes, greaterThan(0), reason: 'the opening table reached Java');

  var bytes = 0;
  _lastPush = null;
  EquoCommBase.sendObserverForTesting = (action, payload) {
    if (!action.endsWith('/TextGeometry')) return;
    _lastPush = payload as Uint8List;
    bytes += _lastPush!.length;
  };
  addTearDown(() => EquoCommBase.sendObserverForTesting = null);
  StyledTextImpl.debugGeometryPushes = 0;
  await deliverWhole(change(text, lines)..seq = 2);
  // The table goes out right after the frame it describes.
  await tester.pump();
  await tester.pump(Duration.zero);
  EquoCommBase.sendObserverForTesting = null;
  expect(StyledTextImpl.debugGeometryPushes, greaterThan(0), reason: 'the change moved every row, so a table went out');
  // Unmount, so the next document starts from a fresh editor.
  await tester.pumpWidget(const SizedBox());
  return bytes;
}

void main() {
  setUp(() {
    StyledTextImpl.debugGeometryPushes = 0;
    StyledTextImpl.debugLastGeometry = null;
  });

  testWidgets('a font change sends the same geometry for a document four times as long', (tester) async {
    VStyledText change(String text, int id) => _value(text, id: id, fontHeight: 11);
    final small = await _geometryBytesAfter(tester, _small, change);
    final large = await _geometryBytesAfter(tester, _large, change);

    expect(large, lessThan(small * 1.25),
        reason: 'a font change on $_large lines sent $large bytes of geometry, on $_small lines $small: '
            'with rows of one height only the band around the viewport needs describing');
  }, timeout: const Timeout(Duration(minutes: 2)));

  testWidgets('a band push carries its row height and first line after the content height', (tester) async {
    await _geometryBytesAfter(tester, _large, (text, id) => _value(text, id: id, fontHeight: 11));

    final table = StyledTextImpl.debugLastGeometry!;
    final push = WidgetWireReader(_lastPush!);
    expect(push.u8(), 2, reason: 'a band, not a whole table or a splice');
    push
      ..int32() // version
      ..int32(); // charCount
    expect(push.float64(), table['contentWidth']);
    expect(push.float64(), table['contentHeight']);
    expect(push.float64(), table['rowHeight']);
    // The view is at the top, so the band starts at line 0: one byte, the zigzag varint of 0.
    expect(_lastPush![push.pos], 0x00, reason: 'the band of a view at the top starts at line 0');
    expect(push.int32(), 0);
    expect(table['bandFirstLine'], 0);
    expect(push.varint(), (table['lines'] as List).length, reason: 'only the band\'s rows follow');
  }, timeout: const Timeout(Duration(minutes: 2)));

  // Wrapped lines, lines of their own height and a line moved down by a vertical indent are rows
  // Java cannot work out, so the table describes every one of them.
  for (final (name, wrap, fixed, indented) in [
    ('wrapped lines', true, true, false),
    ('lines of their own height', false, false, false),
    ('a line with a vertical indent', false, true, true),
  ]) {
    testWidgets('an editor with $name sends a table of every line', (tester) async {
      VStyledText change(String text, int id) =>
          _value(text, id: id, fontHeight: 11, wrap: wrap, fixed: fixed, indented: indented);
      await _geometryBytesAfter(tester, _large, change, wrap: wrap, fixed: fixed, indented: indented);

      final table = StyledTextImpl.debugLastGeometry!;
      expect(table.containsKey('bandFirstLine'), isFalse, reason: 'the table is not a band');
      final rows = table['lines'] as List;
      expect(rows.first['l'], 0, reason: 'the table starts at the first line');
      expect(rows.last['l'], _large - 1, reason: 'the table ends at the last line');
    }, timeout: const Timeout(Duration(minutes: 2)));
  }
}
