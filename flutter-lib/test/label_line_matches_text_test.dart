// A single-line label draws its text through LabelLineText; it has to draw what Text draws with the
// same arguments. This paints both, pixel for pixel, in each configuration a label line meets, and
// after a change of text on a line already laid out.

import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart' show FontLoader, rootBundle;
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/impl/label_evolve.dart';

Future<void> _loadFonts() async {
  final inter = FontLoader('Inter')..addFont(rootBundle.load('assets/fonts/Inter_18pt-Medium.ttf'));
  await inter.load();
}

typedef _Line = Widget Function(String text, TextStyle style, TextAlign align);

Widget _text(String text, TextStyle style, TextAlign align) => Text(text,
    style: style, textAlign: align, softWrap: false, overflow: TextOverflow.ellipsis, maxLines: 1);

Widget _line(String text, TextStyle style, TextAlign align) => LabelLineText(text, style: style, textAlign: align);

Widget _frame(Widget child, {required Key key, double width = 120, double height = 40, double scale = 1}) =>
    MaterialApp(
      home: MediaQuery(
        data: MediaQueryData(textScaler: TextScaler.linear(scale)),
        child: Align(
          alignment: Alignment.topLeft,
          child: RepaintBoundary(
            key: key,
            child: ColoredBox(
              color: Colors.white,
              child: SizedBox(width: width, height: height, child: child),
            ),
          ),
        ),
      ),
    );

Future<Uint8List> _pixels(WidgetTester tester, GlobalKey key) async {
  final boundary = key.currentContext!.findRenderObject()! as RenderRepaintBoundary;
  final bytes = await tester.runAsync(() async {
    final ui.Image image = await boundary.toImage();
    final data = await image.toByteData(format: ui.ImageByteFormat.rawRgba);
    return data!.buffer.asUint8List();
  });
  return bytes!;
}

void main() {
  setUpAll(_loadFonts);

  const enabled = TextStyle(fontFamily: 'Inter', fontSize: 12, color: Color(0xFF202020));
  const disabled = TextStyle(fontFamily: 'Inter', fontSize: 12, color: Color(0xFF9E9E9E));
  const bold = TextStyle(fontFamily: 'Inter', fontSize: 12, fontWeight: FontWeight.bold, color: Color(0xFF202020));
  const long = 'A label far too long for the room it is given';

  final cases = <String, (String, TextStyle, TextAlign, double, double, double)>{
    'plain': ('Label 12', enabled, TextAlign.start, 120, 40, 1),
    'disabled': ('Label 12', disabled, TextAlign.start, 120, 40, 1),
    'bold': ('Label 12', bold, TextAlign.start, 120, 40, 1),
    'ellipsized': (long, enabled, TextAlign.start, 120, 40, 1),
    'centred': ('Label 12', enabled, TextAlign.center, 120, 40, 1),
    'right': ('Label 12', enabled, TextAlign.right, 120, 40, 1),
    'scaled': ('Label 12', enabled, TextAlign.start, 120, 40, 1.5),
    'shorter than the line': ('Label 12', enabled, TextAlign.start, 120, 9, 1),
  };

  for (final MapEntry(key: name, value: (text, style, align, width, height, scale)) in cases.entries) {
    testWidgets('a label line draws what Text draws: $name', (tester) async {
      final key = GlobalKey();
      Widget frame(_Line line) =>
          _frame(line(text, style, align), key: key, width: width, height: height, scale: scale);
      await tester.pumpWidget(frame(_text));
      final expected = await _pixels(tester, key);
      await tester.pumpWidget(frame(_line));
      final actual = await _pixels(tester, key);
      expect(actual, expected);
    });
  }

  testWidgets('a label line given new text draws what Text draws with it', (tester) async {
    final key = GlobalKey();
    await tester.pumpWidget(_frame(_text('Label 12 v7', enabled, TextAlign.start), key: key));
    final expected = await _pixels(tester, key);
    await tester.pumpWidget(_frame(_line('Label 12', enabled, TextAlign.start), key: key));
    await tester.pumpWidget(_frame(_line('Label 12 v7', enabled, TextAlign.start), key: key));
    final actual = await _pixels(tester, key);
    expect(actual, expected);
    expect(find.text('Label 12 v7'), findsOneWidget);
  });

  testWidgets('a label line describes its text to semantics as Text does', (tester) async {
    final handle = tester.ensureSemantics();
    await tester.pumpWidget(_frame(_line('Label 12', enabled, TextAlign.start), key: GlobalKey()));
    expect(find.bySemanticsLabel('Label 12'), findsOneWidget);
    await tester.pumpWidget(_frame(_line('Label 12 v7', enabled, TextAlign.start), key: GlobalKey()));
    expect(find.bySemanticsLabel('Label 12 v7'), findsOneWidget);
    handle.dispose();
  });
}
