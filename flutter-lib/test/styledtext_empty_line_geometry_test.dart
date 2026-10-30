// Java places every line from the geometry table the client pushes: getLinePixel, the top index,
// the scroll range. An empty line described taller than it is drawn put every line after it lower
// in Java than on screen, so an editor's ruler numbered its rows one too low once scrolled, and
// the scroll range reached past the end of the document.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart' show FontLoader, rootBundle;
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/font.dart';
import 'package:swtflutter/src/gen/fontdata.dart';
import 'package:swtflutter/src/gen/stylerange.dart';
import 'package:swtflutter/src/gen/styledtext.dart';
import 'package:swtflutter/src/gen/styledtextrenderer.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/styledtext_evolve.dart';

const _width = 400.0;
const _height = 300.0;

VStyledText _value(
  String text, {
  List<VStyleRange?>? styles,
  List<int>? styleIndex,
  VFont? font,
  int id = 1,
  int caret = 0,
}) => VStyledText()
  ..swt = 'StyledText'
  ..id = id
  ..font = font
  // Line metrics too small to matter, so a line is as tall as its font lays it out.
  ..renderer = (VStyledTextRenderer()
    ..ascent = 1
    ..descent = 1)
  ..style = SWT.H_SCROLL | SWT.V_SCROLL
  ..enabled = true
  ..editable = true
  ..caretOffset = caret
  ..text = text
  ..wordWrap = false
  ..styles = styles
  ..styleIndex = styleIndex
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = _width.toInt()
    ..height = _height.toInt());

Widget _app(GlobalKey<StyledTextImpl> key, VStyledText v) => EvolveApp(
  theme: ThemeMode.light,
  contentWidget: SizedBox(
    width: _width,
    height: _height,
    child: StyledTextSwt<VStyledText>(key: key, value: v),
  ),
);

void main() {
  setUp(() {
    StyledTextImpl.debugGeometryPushes = 0;
    StyledTextImpl.debugLastGeometry = null;
  });

  testWidgets('an empty line is described as tall as the lines around it', (tester) async {
    await _expectEvenRows(tester, _value(_text));
  });

  testWidgets('so is an empty line whose delimiter a style run covers', (tester) async {
    // A Java editor colours line delimiters too: here one run covers the delimiter that ends the
    // first line and the empty line's own, which is all of the empty line it touches.
    final grey = VStyleRange()
      ..foreground = (VColor()
        ..alpha = 255
        ..red = 128
        ..green = 128
        ..blue = 128);
    await _expectEvenRows(tester, _value(_text, styles: [grey], styleIndex: [1, 0, 13, 2]));
  });

  testWidgets('a font that loads after the first layout is what the lines are described in', (
    tester,
  ) async {
    // The editor's font is a bundled face, and on the web it can finish loading after the first
    // layout. A line measured in the stand-in font kept that height afterwards: an empty line,
    // whose layout no later style run changes, came out a row and a half tall.
    final mono = VFont()
      ..fontData = [
        VFontData()
          ..name = 'Liberation Mono'
          ..height = 12
          ..style = 0,
      ];
    final key = GlobalKey<StyledTextImpl>();
    await tester.pumpWidget(_app(key, _value(_text, font: mono)));
    await tester.pumpWidget(_app(key, _value(_text, font: mono)));
    await tester.pump(Duration.zero);
    final before = _rows();

    await tester.runAsync(() async {
      final loader = FontLoader('Liberation Mono')
        ..addFont(rootBundle.load('assets/fonts/LiberationMono-Regular.ttf'));
      await loader.load();
    });
    await tester.pump();
    // A caret move repaints, which describes the layout again.
    await tester.pumpWidget(_app(key, _value(_text, font: mono, caret: 1)));
    await tester.pump(Duration.zero);
    final after = _rows();

    // The same text laid out from the start in the loaded font.
    final fresh = GlobalKey<StyledTextImpl>();
    await tester.pumpWidget(_app(fresh, _value(_text, font: mono, id: 2)));
    await tester.pumpWidget(_app(fresh, _value(_text, font: mono, id: 2)));
    await tester.pump(Duration.zero);
    final reference = _rows();

    expect(
      before.first['h'],
      isNot(reference.first['h']),
      reason: 'sanity: the font changed what a line measures',
    );
    for (var i = 0; i < reference.length; i++) {
      expect(after[i]['h'], reference[i]['h'], reason: 'line $i in the loaded font');
      expect(after[i]['y'], reference[i]['y'], reason: 'line $i where the loaded font puts it');
    }
  });
}

const _text = 'package demo;\n\npublic class Long {\n    int a = 0;\n\n}';

Future<void> _expectEvenRows(WidgetTester tester, VStyledText v) async {
  final key = GlobalKey<StyledTextImpl>();
  await tester.pumpWidget(_app(key, v));
  // initState doesn't run extraSetState, so a second push builds the text shape.
  await tester.pumpWidget(_app(key, v));
  await tester.pump(Duration.zero);

  final rows = _rows();
  expect(rows, hasLength(6));
  final lineHeight = rows[0]['h'] as num;
  for (final row in rows) {
    expect(row['h'], lineHeight, reason: 'line ${row['l']} described at its own height');
  }
  for (var i = 0; i < rows.length; i++) {
    expect(
      rows[i]['y'],
      closeTo(i * lineHeight, 0.01),
      reason: 'line $i stacked under the ones before it',
    );
  }
}

List<Map<String, dynamic>> _rows() =>
    (StyledTextImpl.debugLastGeometry!['lines'] as List).cast<Map<String, dynamic>>();
