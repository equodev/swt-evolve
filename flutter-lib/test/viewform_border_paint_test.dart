// A ViewForm draws what SWT's ViewForm.onPaint draws: with a border, a one-point line round the
// form and, unless SWT.FLAT, a two-point highlight band inside it; and a one-point separator under
// the top row, from inside the border and the highlight to the same inset on the right. All of it
// lies under the children, in the theme's border and focused-border colours.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import 'package:swtflutter/src/theme/theme_extensions/composite_theme_extension.dart';
import 'package:swtflutter/src/gen/viewform.dart';
import 'package:swtflutter/src/gen/rectangle.dart';

import 'support/painted_pixels.dart';

const _width = 300, _height = 200;

/// The theme's border colour, which the lines take, and its focused-border colour, which the band
/// takes; read from the theme the form was last drawn in.
late List<int> _grey;
late List<int> _blue;
const _white = [255, 255, 255];
const _ground = Color(0xFFFFFFFF);
const _green = [0, 255, 0];

VComposite _child(int id, VRectangle bounds) => VComposite()
  ..id = id
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..background = vRgb(_green)
  ..bounds = bounds;

VViewForm _form({required bool border, int highlight = 0, int separator = -1, List<VControl>? children}) => VViewForm()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..background = vRgb(_white)
  ..bounds = vRect(0, 0, _width, _height)
  ..borderVisible = border
  ..highlight = highlight
  ..separator = separator
  ..children = children;

Future<PaintedPixels> _pixels(WidgetTester tester, VViewForm form, {ThemeMode mode = ThemeMode.light}) async {
  final painted = await paintPixels(tester, ViewFormSwt<VViewForm>(value: form), width: _width, height: _height, mode: mode);
  final theme = painted.theme.extension<CompositeThemeExtension>()!;
  _grey = rgbOf(Color.alphaBlend(theme.borderColor, _ground));
  _blue = rgbOf(Color.alphaBlend(theme.focusedBorderColor, _ground));
  return painted;
}

void main() {
  // The application's colours, so the backgrounds below are the ones the values carry.
  setUp(() => setConfigFlags(ConfigFlags()..use_swt_colors = true));
  tearDown(resetConfigFlags);

  testWidgets('a border is a one-point line round the form with the highlight band inside it', (tester) async {
    final data = await _pixels(tester, _form(border: true, highlight: 2));
    for (final p in [
      [0, 100],
      [299, 100],
      [150, 0],
      [150, 199],
      [0, 0],
      [299, 199],
    ]) {
      expect(data.at(p[0], p[1]), _grey, reason: 'border at $p');
    }
    for (final p in [
      [1, 100],
      [2, 100],
      [297, 100],
      [298, 100],
      [150, 1],
      [150, 2],
      [150, 197],
      [150, 198],
    ]) {
      expect(data.at(p[0], p[1]), _blue, reason: 'highlight at $p');
    }
    for (final p in [
      [3, 100],
      [296, 100],
      [150, 3],
      [150, 196],
    ]) {
      expect(data.at(p[0], p[1]), _white, reason: 'inside the highlight at $p');
    }
  });

  testWidgets('a flat border has no highlight', (tester) async {
    final data = await _pixels(tester, _form(border: true));
    expect(data.at(0, 100), _grey);
    expect(data.at(1, 100), _white);
    expect(data.at(150, 1), _white);
  });

  testWidgets('without a border nothing is drawn round the form', (tester) async {
    final data = await _pixels(tester, _form(border: false));
    expect(data.at(0, 100), _white);
    expect(data.at(150, 0), _white);
  });

  testWidgets('the separator runs between the border and the highlight on both sides', (tester) async {
    final data = await _pixels(tester, _form(border: true, highlight: 2, separator: 34));
    expect(data.at(3, 34), _grey);
    expect(data.at(150, 34), _grey);
    expect(data.at(296, 34), _grey);
    expect(data.at(2, 34), _blue, reason: 'the separator starts inside the highlight');
    expect(data.at(297, 34), _blue, reason: 'and ends inside it');
    expect(data.at(150, 33), _white);
    expect(data.at(150, 35), _white);
  });

  testWidgets('without a border the separator spans the form', (tester) async {
    final data = await _pixels(tester, _form(border: false, separator: 34));
    expect(data.at(0, 34), _grey);
    expect(data.at(299, 34), _grey);
    final flat = await _pixels(tester, _form(border: true, separator: 34));
    expect(flat.at(1, 34), _grey);
    expect(flat.at(298, 34), _grey);
  });

  testWidgets('a form without a separator draws none', (tester) async {
    final data = await _pixels(tester, _form(border: false));
    expect(data.at(150, 34), _white);
  });

  testWidgets('the children are drawn over what the form draws', (tester) async {
    final data = await _pixels(
      tester,
      _form(
        border: true,
        highlight: 2,
        separator: 34,
        children: [_child(2, vRect(3, 3, 294, 30)), _child(3, vRect(100, 30, 50, 10))],
      ),
    );
    expect(data.at(20, 20), _green, reason: 'a child inside the border');
    expect(data.at(120, 34), _green, reason: 'a child over the separator hides it');
    expect(data.at(200, 34), _grey, reason: 'the separator beside it');
    expect(data.at(0, 20), _grey, reason: 'the border with children');
  });

  testWidgets('the lines follow the theme into dark mode', (tester) async {
    final light = await _pixels(tester, _form(border: true, highlight: 2));
    final lightLine = _grey;
    final dark = await _pixels(tester, _form(border: true, highlight: 2), mode: ThemeMode.dark);
    expect(_grey, isNot(lightLine), reason: 'the dark theme has a border colour of its own');
    expect(dark.at(0, 100), _grey);
    expect(dark.at(1, 100), _blue);
    expect(light.at(0, 100), lightLine);
  });
}
