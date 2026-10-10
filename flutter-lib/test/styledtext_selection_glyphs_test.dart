// StyledText draws a selection the way TextLayout.draw does: the highlight behind the selected
// text, and the selected glyphs over it in the selection foreground. An opaque highlight painted
// over the glyphs, with nothing drawn on top, hides the selected text altogether.

import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/point.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/styledtext.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/styledtext_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

const _highlight = Color(0xFF3399FF);
const _white = Color(0xFFFFFFFF);
const _black = Color(0xFF000000);

class _Pixels {
  _Pixels(this.bytes, this.width);

  final Uint8List bytes;
  final int width;

  Color at(int x, int y) {
    final i = (y * width + x) * 4;
    return Color.fromARGB(bytes[i + 3], bytes[i], bytes[i + 1], bytes[i + 2]);
  }

  /// The points inside [r], one pixel in from its edges, whose colour is [c].
  Set<(int, int)> matching(Rect r, Color c) => {
        for (int y = r.top.ceil() + 1; y < r.bottom.floor() - 1; y++)
          for (int x = r.left.ceil() + 1; x < r.right.floor() - 1; x++)
            if (_near(at(x, y), c)) (x, y),
      };

  static bool _near(Color a, Color b) =>
      ((a.r - b.r) * 255).abs() < 24 &&
      ((a.g - b.g) * 255).abs() < 24 &&
      ((a.b - b.b) * 255).abs() < 24;
}

Future<_Pixels> _toPixels(ui.Image image) async {
  final data = await image.toByteData(format: ui.ImageByteFormat.rawRgba);
  return _Pixels(data!.buffer.asUint8List(), image.width);
}

VStyledText _selected() => VStyledText()
  ..swt = 'StyledText'
  ..id = 1
  ..style = SWT.MULTI
  ..enabled = true
  ..visible = true
  ..editable = true
  ..text = 'alpha beta gamma'
  // " beta ": the spaces leave the highlight visible between the glyphs of the test font, which
  // fill their whole box.
  ..caretOffset = 11
  ..selection = (VPoint()
    ..x = 5
    ..y = 11)
  ..selectionRanges = [5, 6]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 300
    ..height = 40);

const _canvas = Size(200, 200);
const _lineHeight = 10.0;
const _line = 'aaaa bbbb cccc dddd eeee ffff gggg hhhh iiii jjjj kkkk';

/// One wrapping line in the test font (10px per glyph), selected over [selection] or not at all.
TextShape _shape(LineProperties line, {SelectionInfo? selection}) => TextShape(
      _line,
      Offset.zero,
      const TextStyle(fontSize: 10, height: 1.0, color: _black),
      null,
      null,
      null,
      true,
      _canvas,
      true,
      1,
      null,
      TextEditingState(characterRanges: [], lineProperties: {0: line}),
      selection,
      _lineHeight,
    );

Future<(_Pixels, List<Rect>)> _paint(TextShape shape) async {
  final recorder = ui.PictureRecorder();
  final canvas = Canvas(recorder)
    ..drawRect(Offset.zero & _canvas, Paint()..color = _white);
  shape.draw(canvas);
  final image = await recorder.endRecording().toImage(_canvas.width.toInt(), _canvas.height.toInt());
  return (await _toPixels(image), List.of(shape.debugPaintedSelectionRects));
}

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  testWidgets('selected text stays readable: its glyphs are drawn over the highlight', (tester) async {
    final boundary = GlobalKey();
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: RepaintBoundary(
          key: boundary,
          child: SizedBox(
            width: 300,
            height: 40,
            child: StyledTextSwt<VStyledText>(value: _selected()),
          ),
        ),
      ),
    ));
    await tester.pump();
    final impl = tester.state<StyledTextImpl>(find.byType(StyledTextSwt<VStyledText>));
    final facts = impl.debugRenderFacts();
    final rects = (facts['selectionRects'] as List).cast<Map<String, double>>();
    expect(rects, isNotEmpty);
    final r = rects.first;
    final highlight = Rect.fromLTWH(r['x']!, r['y']!, r['width']!, r['height']!);

    final pixels = (await tester.runAsync(() async {
      final render = tester.renderObject<RenderRepaintBoundary>(find.byKey(boundary));
      return _toPixels(await render.toImage());
    }))!;

    expect(facts['selectionBackground'], '#ff3399ff');
    expect(pixels.matching(highlight, _highlight), isNotEmpty, reason: 'the highlight is painted');
    expect(pixels.matching(highlight, _white), isNotEmpty,
        reason: 'the selected glyphs are drawn over the highlight in the default selection '
            'foreground, the COLOR_LIST_SELECTION_TEXT that StyledText#getSelectionForeground answers');
  });

  for (final (name, line, start, end) in [
    ('on a row a wrap indent moves', const LineProperties(wrapIndent: 40), 22, 37),
    ('on a centred line', const LineProperties(alignment: SWT.CENTER), 5, 9),
  ]) {
    test('the selected glyphs are drawn where the line puts its glyphs, $name', () async {
      final (plain, _) = await _paint(_shape(line));
      final (selected, rects) = await _paint(_shape(line,
          selection: SelectionInfo(start: start, end: end, selectionForeground: _white)));
      expect(rects, isNotEmpty);

      for (final r in rects) {
        final glyphs = plain.matching(r, _black);
        // The fill a selection gets past a wrapped row's end, or before the row it goes on to, has no glyphs.
        final rowContinuation = r.right == _canvas.width || r.left == 0;
        expect(glyphs.isNotEmpty || rowContinuation, isTrue, reason: 'the highlight $r covers glyphs of the plain paint');
        expect(selected.matching(r, _white), glyphs,
            reason: 'inside the highlight $r, the selection foreground is exactly where the glyphs are');
        expect(selected.matching(r, _black), isEmpty,
            reason: 'no glyph is left in its own colour, or drawn twice, inside the highlight $r');
      }
    });
  }
}
