// A CBanner draws what SWT's CBanner.onPaint draws: a one-point line across the banner above the
// bottom control, and between the left and the right the curve Java sends as a polyline, with a
// copy a point to either side in a colour three quarters of the way to the background and a stripe
// along the bottom edge that fades from the background into the curve. All of it lies under the
// children, in the theme's border colour.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/gen/cbanner.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import 'package:swtflutter/src/theme/theme_extensions/composite_theme_extension.dart';

import 'support/painted_pixels.dart';

const _width = 300, _height = 60;
const _white = [255, 255, 255];
const _ground = Color(0xFFFFFFFF);
const _green = [0, 255, 0];

/// The theme's border colour, which the lines take, read from the theme the banner was last drawn
/// in; and the colour of the copies, three quarters of the way from it to the white background.
late List<int> _black;
List<int> get _shade => [for (final c in _black) c + (3 * (255 - c)) ~/ 4];

/// The simple curve of a 60-point banner starting at 213, as CBanner.wireCurveLine builds it.
const _curveLine = [214, 59, 213, 59, 214, 59, 215, 58, 216, 57, 216, 2, 217, 1, 218, 0, 218, 0, 300, 0];

VCBanner _banner({List<int>? curveLine, int bottomLine = -1, List<VControl>? children}) => VCBanner()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..background = vRgb(_white)
  ..bounds = vRect(0, 0, _width, _height)
  ..curveLine = curveLine
  ..bottomLine = bottomLine
  ..children = children;

Future<PaintedPixels> _pixels(WidgetTester tester, VCBanner banner, {ThemeMode mode = ThemeMode.light}) async {
  final painted = await paintPixels(tester, CBannerSwt<VCBanner>(value: banner), width: _width, height: _height, mode: mode);
  final theme = painted.theme.extension<CompositeThemeExtension>()!;
  _black = rgbOf(Color.alphaBlend(theme.borderColor, _ground));
  return painted;
}

void main() {
  // The application's colours, so the backgrounds below are the ones the values carry.
  setUp(() => setConfigFlags(ConfigFlags()..use_swt_colors = true));
  tearDown(resetConfigFlags);

  testWidgets('the curve is drawn with a shaded copy on either side', (tester) async {
    final data = await _pixels(tester, _banner(curveLine: _curveLine));
    expect(data.at(216, 30), _black, reason: 'the curve rises at its start plus 3');
    expect(data.at(215, 30), _shade, reason: 'the copy a point to the left');
    expect(data.at(217, 30), _shade, reason: 'the copy a point to the right');
    expect(data.at(213, 30), _white);
    expect(data.at(219, 30), _white);
  });

  testWidgets('the curve runs on along the top edge to the right end', (tester) async {
    final data = await _pixels(tester, _banner(curveLine: _curveLine));
    expect(data.at(250, 0), _black);
    expect(data.at(299, 0), _black);
    expect(data.at(250, 1), _white);
  });

  testWidgets('the bottom stripe fades from the background into the curve over 200 points', (tester) async {
    final data = await _pixels(tester, _banner(curveLine: _curveLine));
    final start = data.at(13, 59)[0], middle = data.at(113, 59)[0], end = data.at(212, 59)[0];
    final line = _black[0], half = (255 + line) ~/ 2, spread = (255 - line) ~/ 7 + 1;
    expect(start, greaterThan(255 - spread), reason: 'the background where the stripe starts');
    expect(middle, inInclusiveRange(half - spread, half + spread), reason: 'half way');
    expect(end, lessThan(line + spread), reason: 'the curve colour where it meets the curve');
    expect(data.at(5, 59), _white, reason: 'nothing left of the stripe');
    expect(data.at(113, 58), _white, reason: 'the stripe is one point high');
  });

  testWidgets('without a curve nothing is drawn between left and right', (tester) async {
    final data = await _pixels(tester, _banner());
    expect(data.at(216, 30), _white);
    expect(data.at(250, 0), _white);
    expect(data.at(113, 59), _white);
  });

  testWidgets('a line is drawn across the banner above the bottom control', (tester) async {
    final data = await _pixels(tester, _banner(bottomLine: 48));
    expect(data.at(0, 48), _black);
    expect(data.at(150, 48), _black);
    expect(data.at(299, 48), _black);
    expect(data.at(150, 47), _white);
    expect(data.at(150, 49), _white);
  });

  testWidgets('the children are drawn over the curve', (tester) async {
    final data = await _pixels(
      tester,
      _banner(
        curveLine: _curveLine,
        children: [
          VComposite()
            ..id = 2
            ..style = SWT.NONE
            ..enabled = true
            ..visible = true
            ..background = vRgb(_green)
            ..bounds = vRect(210, 20, 20, 20),
        ],
      ),
    );
    expect(data.at(216, 30), _green, reason: 'a child over the curve hides it');
    expect(data.at(216, 10), _black, reason: 'the curve beside the child');
  });

  testWidgets('the lines follow the theme into dark mode', (tester) async {
    await _pixels(tester, _banner(bottomLine: 48));
    final lightLine = _black;
    final dark = await _pixels(tester, _banner(bottomLine: 48), mode: ThemeMode.dark);
    expect(_black, isNot(lightLine), reason: 'the dark theme has a border colour of its own');
    expect(dark.at(150, 48), _black);
  });
}
