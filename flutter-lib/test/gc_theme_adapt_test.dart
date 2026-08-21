import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import 'package:swtflutter/src/theme/theme_extensions/canvas_theme_extension.dart';

/// Custom-drawn content picks its colors in Java, so under the dark theme it painted a white
/// surface into a dark app. An application can hand that over to the theme with
/// disable_swt_canvas_colors: every color a GC paints with then comes from its own slot in the
/// Canvas theme. Off by default — the drawing is the application's until it asks.

// A distinct value per slot, so a getter reading the wrong one fails instead of coinciding.
const _fill = Color(0xFF111111);
const _stroke = Color(0xFF222222);
const _line = Color(0xFF333333);
const _point = Color(0xFF444444);
const _text = Color(0xFF555555);
const _textBackground = Color(0xFF666666);
const _focus = Color(0xFF777777);
const _gradientStart = Color(0xFF888888);
const _gradientEnd = Color(0xFF999999);
const _patternStart = Color(0xFFAAAAAA);
const _patternEnd = Color(0xFFBBBBBB);
const _imageTint = Color(0xFFCCCCCC);
const _canvasBackground = Color(0xFFDDDDDD);

const _canvasTheme = CanvasThemeExtension(
  defaultWidth: 64.0,
  defaultHeight: 64.0,
  backgroundColor: _canvasBackground,
  foregroundColor: Color(0xFFEEEEEE),
  fillColor: _fill,
  strokeColor: _stroke,
  lineColor: _line,
  pointColor: _point,
  textColor: _text,
  textBackgroundColor: _textBackground,
  focusColor: _focus,
  gradientStartColor: _gradientStart,
  gradientEndColor: _gradientEnd,
  patternStartColor: _patternStart,
  patternEndColor: _patternEnd,
  imageTintColor: _imageTint,
);

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  /// Mounts a themed app and hands the drawer the context, as GCImpl.build() does.
  Future<GCDrawer> themedDrawer(WidgetTester tester) async {
    final drawer = GCDrawer.embedded(VGC()
      ..swt = 'GC'
      ..id = 4243
      ..background = _vColor(0xFF, 0xFF, 0xFF)
      ..foreground = _vColor(0x00, 0x00, 0x00));
    addTearDown(drawer.dispose);

    late BuildContext context;
    await tester.pumpWidget(MaterialApp(
      theme: ThemeData(extensions: const <ThemeExtension<dynamic>>[_canvasTheme]),
      home: Builder(builder: (c) {
        context = c;
        return const SizedBox();
      }),
    ));
    drawer.syncContext(context);
    return drawer;
  }

  testWidgets('every painted role reads its own theme slot', (tester) async {
    final drawer = await themedDrawer(tester);
    setConfigFlags(ConfigFlags()..disable_swt_canvas_colors = true);

    expect(drawer.fillColor, _fill);
    expect(drawer.strokeColor, _stroke);
    expect(drawer.lineColor, _line);
    expect(drawer.pointColor, _point);
    expect(drawer.textColor, _text);
    expect(drawer.textBackgroundColor, _textBackground);
    expect(drawer.focusColor, _focus);
    expect(drawer.gradientStartColor, _gradientStart);
    expect(drawer.gradientEndColor, _gradientEnd);
    expect(drawer.imageTintColor, _imageTint);
    expect(drawer.bg, _canvasBackground, reason: 'the Canvas background is its own slot');
  });

  testWidgets('by default every role keeps the color the application painted with',
      (tester) async {
    // A theme is mounted and still ignored: without the flag the drawing is the application's.
    final drawer = await themedDrawer(tester);

    // The GC state carries one background and one foreground; each role takes whichever of the
    // two it is painted from.
    expect(drawer.fillColor, Colors.white);
    expect(drawer.textBackgroundColor, Colors.white);
    expect(drawer.gradientEndColor, Colors.white);
    expect(drawer.strokeColor, Colors.black);
    expect(drawer.lineColor, Colors.black);
    expect(drawer.pointColor, Colors.black);
    expect(drawer.textColor, Colors.black);
    expect(drawer.focusColor, Colors.black);
    expect(drawer.gradientStartColor, Colors.black);
  });

  testWidgets('use_swt_colors wins, so asking for the application colors everywhere covers drawing',
      (tester) async {
    final drawer = await themedDrawer(tester);
    setConfigFlags(ConfigFlags()
      ..disable_swt_canvas_colors = true
      ..use_swt_colors = true);

    expect(drawer.fillColor, Colors.white);
    expect(drawer.textColor, Colors.black);
  });

  test('a paint op that beats the first build falls back to the SWT defaults', () {
    // The standalone image drawer renders offscreen and never goes through a build, so it has no
    // theme to read even when asked for one.
    setConfigFlags(ConfigFlags()..disable_swt_canvas_colors = true);
    final drawer = GCDrawer.embedded(VGC()
      ..swt = 'GC'
      ..id = 4244);
    addTearDown(drawer.dispose);

    expect(drawer.fillColor, const Color(0xFFFFFFFF));
    expect(drawer.textColor, const Color(0xFF1F1F1F));
  });
}

VColor _vColor(int r, int g, int b) => VColor()
  ..alpha = 0xFF
  ..red = r
  ..green = g
  ..blue = b;
