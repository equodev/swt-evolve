// StyledTextRenderer.drawBullet places a bullet at the start of the line, inside the width its glyph
// metrics reserve, whatever indent the line's text has on top of that width.

import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/styledtext.dart';
import 'package:swtflutter/src/gen/styledtextrenderer.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/styledtext_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

const _canvas = Size(200, 100);
const _black = Color(0xFF000000);

// The test font advances one em per glyph: fontSize 10 is 10px per character and a 10px row.
TextShape _shape(String text, LineProperties line, {bool wrap = false, SelectionInfo? selection}) => TextShape(
      text,
      Offset.zero,
      const TextStyle(fontSize: 10, height: 1.0, color: _black),
      null,
      null,
      null,
      wrap,
      _canvas,
      true,
      1,
      null,
      TextEditingState(characterRanges: [], lineProperties: {0: line}),
      selection,
      10,
    );

Future<(Uint8List, int)> _paint(TextShape shape) async {
  final recorder = ui.PictureRecorder();
  final canvas = Canvas(recorder)..drawRect(Offset.zero & _canvas, Paint()..color = const Color(0xFFFFFFFF));
  shape.draw(canvas);
  final image = await recorder.endRecording().toImage(_canvas.width.toInt(), _canvas.height.toInt());
  final data = await image.toByteData(format: ui.ImageByteFormat.rawRgba);
  return (data!.buffer.asUint8List(), image.width);
}

/// The columns of the first row of pixels that hold a dark pixel.
Set<int> _darkColumns(Uint8List bytes, int width, int y) => {
      for (int x = 0; x < width; x++)
        if (bytes[(y * width + x) * 4] < 64) x,
    };

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  testWidgets('the width a bullet reserves comes from the renderer with its text', (tester) async {
    final value = VStyledText()
      ..swt = 'StyledText'
      ..id = 1
      ..style = SWT.MULTI
      ..enabled = true
      ..visible = true
      ..text = 'x\ny'
      ..bounds = (VRectangle()
        ..x = 0
        ..y = 0
        ..width = 300
        ..height = 60)
      ..renderer = (VStyledTextRenderer()
        // INDENT: the 40 of the line plus the 20 its bullet reserves.
        ..lines = [VLineInfo()
          ..flags = 1 << 2
          ..indent = 60, null]
        ..bulletTexts = ['o', null]
        ..bulletWidths = [20, 0]);
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(width: 300, height: 60, child: StyledTextSwt<VStyledText>(value: value)),
      ),
    ));
    await tester.pump();
    final impl = tester.state<StyledTextImpl>(find.byType(StyledTextSwt<VStyledText>));
    expect(impl.debugRenderFacts()['bullets'], [
      {'line': 0.0, 'text': 'o', 'width': 20.0},
    ]);
  });

  test('an indented bulleted line draws its bullet at the line start, not before its text', () async {
    // indent 60 = the line's own 40 plus the 20 the bullet reserves (StyledTextHelper.wireLines).
    final (bytes, width) = await _paint(_shape('x', const LineProperties(indent: 60, bulletText: 'o', bulletWidth: 20)));
    final dark = _darkColumns(bytes, width, 5);
    expect(dark.where((x) => x < 20), isNotEmpty, reason: 'the bullet is inside the width it reserves at the line start');
    expect(dark.where((x) => x >= 20 && x < 60), isEmpty, reason: 'nothing is drawn in the line indent: $dark');
    expect(dark.where((x) => x >= 60 && x < 70), isNotEmpty, reason: 'the text starts after the indent');
  });

  test('a bullet on a line with no indent of its own sits right-aligned in its width', () async {
    final (bytes, width) = await _paint(_shape('x', const LineProperties(indent: 30, bulletText: 'o', bulletWidth: 30)));
    final dark = _darkColumns(bytes, width, 5);
    // drawBullet: x = max(0, width - bullet text width - BULLET_MARGIN) = 30 - 10 - 8.
    expect(dark.where((x) => x < 30).reduce((a, b) => a < b ? a : b), 12);
  });

  // Seven-glyph words: a row holds two of them (160px) and ends short of the 200px edge.
}
