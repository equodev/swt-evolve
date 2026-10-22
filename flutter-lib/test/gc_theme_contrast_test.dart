import 'dart:math' as math;

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import 'package:swtflutter/src/theme/theme_extensions/canvas_theme_extension.dart';

/// A themed Canvas paints every fill from one theme slot, which erased how an application sets a
/// hover or a selection apart from its normal background: a themed NatTable showed neither. Each
/// fill now keeps that difference: it moves away from the theme's fill as far as it was from the
/// normal one, and the hue it adds becomes a tint toward the theme's highlight.

const _darkFill = Color(0xFF1D1C1F);
const _darkHighlight = Color(0xFFFF52A8);
const _lightFill = Color(0xFFEFECF3);
const _lightHighlight = Color(0xFFE0338A);

const _navy = 0xFF1F2937;
const _white = 0xFFFFFFFF;
const _lightGrey = 0xFFF0F0F0;
const _selectionGrey = 0xFFC0C0C0;
const _hoverYellow = 0xFFFFFF00;
// A table styled for a light background: warm banding, hover and selection, each a little darker.
const _warmBand = 0xFFFCF6ED;
const _warmHover = 0xFFF6E6D6;
const _warmSelection = 0xFFDBC7B4;

const _roleHover = Color(0xFF336699);
const _roleSelection = Color(0xFF993366);

CanvasThemeExtension _canvasTheme(Color fill, Color highlight) => CanvasThemeExtension(
      defaultWidth: 64.0,
      defaultHeight: 64.0,
      backgroundColor: fill,
      foregroundColor: const Color(0xFF808080),
      fillColor: fill,
      strokeColor: const Color(0xFF808080),
      lineColor: const Color(0xFF808080),
      pointColor: const Color(0xFF808080),
      textColor: const Color(0xFF808080),
      textBackgroundColor: fill,
      focusColor: const Color(0xFF808080),
      gradientStartColor: const Color(0xFF808080),
      gradientEndColor: fill,
      patternStartColor: const Color(0xFF808080),
      patternEndColor: fill,
      imageTintColor: const Color(0xFF808080),
      highlightColor: highlight,
      hoverFillColor: _roleHover,
      selectionFillColor: _roleSelection,
      glyphTintMaxSide: 64,
      glyphTintChannelTolerance: 4,
    );

void main() {
  setUp(() {
    resetConfigFlags();
    setConfigFlags(ConfigFlags()..disable_swt_canvas_colors = true);
  });
  tearDown(resetConfigFlags);

  /// Mounts a themed app and hands the drawer the context, as GCImpl.build() does. The drawer has
  /// no host control, so the fill it paints with most is its normal background.
  Future<GCDrawer> themedDrawer(WidgetTester tester, CanvasThemeExtension theme) async {
    final drawer = GCDrawer.embedded(VGC()
      ..swt = 'GC'
      ..id = 4250);
    addTearDown(drawer.dispose);

    late BuildContext context;
    await tester.pumpWidget(MaterialApp(
      theme: ThemeData(extensions: <ThemeExtension<dynamic>>[theme]),
      home: Builder(builder: (c) {
        context = c;
        return const SizedBox();
      }),
    ));
    drawer.syncContext(context);
    return drawer;
  }

  Color fill(GCDrawer drawer, int argb, {int times = 1}) {
    drawer.state.background = _vColor(argb);
    late Color color;
    for (var i = 0; i < times; i++) {
      color = drawer.fillColor;
    }
    return color;
  }

  testWidgets('the normal fill takes the theme fill unchanged', (tester) async {
    final drawer = await themedDrawer(tester, _canvasTheme(_darkFill, _darkHighlight));

    expect(fill(drawer, _navy, times: 5), _darkFill);
  });

  testWidgets('dark theme: neutral fills keep the order of their distance to the normal fill',
      (tester) async {
    final drawer = await themedDrawer(tester, _canvasTheme(_darkFill, _darkHighlight));
    final normal = fill(drawer, _navy, times: 10);

    final band = fill(drawer, _white);
    final selection = fill(drawer, _selectionGrey);

    expect(_lstar(band), greaterThan(_lstar(selection)));
    expect(_lstar(selection), greaterThan(_lstar(normal) + 15),
        reason: 'a selection must stand out from the normal fill');
    expect(_lstar(band), lessThan(60), reason: 'the offset is compressed, never saturated');
  });

  testWidgets('dark theme: the darker highlights of a light application move up, not into black',
      (tester) async {
    final drawer = await themedDrawer(tester, _canvasTheme(_darkFill, _darkHighlight));
    final normal = fill(drawer, _white, times: 10);

    final band = fill(drawer, _warmBand);
    final hover = fill(drawer, _warmHover);
    final selection = fill(drawer, _warmSelection);

    expect(_lstar(band) - _lstar(normal), inInclusiveRange(2, 8),
        reason: 'subtle banding stays subtle');
    expect(_lstar(hover), greaterThan(_lstar(normal) + 10),
        reason: 'a subtle hover must still show on the dark fill');
    expect(_lstar(selection), greaterThan(_lstar(hover) + 8),
        reason: 'the selection was further from the normal fill than the hover, so it stays further');
  });

  testWidgets('light theme: neutral fills keep the order of their distance to the normal fill',
      (tester) async {
    final drawer = await themedDrawer(tester, _canvasTheme(_lightFill, _lightHighlight));
    final normal = fill(drawer, _white, times: 10);

    final band = fill(drawer, _lightGrey);
    final selection = fill(drawer, _selectionGrey);

    expect(_lstar(normal), greaterThan(_lstar(band)));
    expect(_lstar(band), greaterThan(_lstar(selection)));
    expect(_lstar(normal) - _lstar(selection), greaterThan(5),
        reason: 'a selection must stand out from the normal fill');
  });

  for (final (mode, theme, normal, band) in [
    ('dark', _canvasTheme(_darkFill, _darkHighlight), _navy, _white),
    ('light', _canvasTheme(_lightFill, _lightHighlight), _white, _lightGrey),
  ]) {
    testWidgets('$mode theme: a hue the application adds shows as the highlight on every row',
        (tester) async {
      final drawer = await themedDrawer(tester, theme);
      final normalFill = fill(drawer, normal, times: 10);
      final bandFill = fill(drawer, band);

      final hover = fill(drawer, _hoverYellow);

      expect(_hueDistance(hover, theme.highlightColor), lessThan(20),
          reason: 'the hover takes the highlight hue');
      expect(_rgbDistance(hover, normalFill), greaterThan(0.15));
      expect(_rgbDistance(hover, bandFill), greaterThan(0.15),
          reason: 'a hover must not blend with a row of the banding');
    });
  }

  testWidgets('a fill declared a hover or a selection takes the theme color for that role',
      (tester) async {
    final drawer = await themedDrawer(tester, _canvasTheme(_darkFill, _darkHighlight));
    drawer.state.fillRoles = 'F6E6D6:hover,DBC7B4:selection';
    final normal = fill(drawer, _white, times: 10);

    expect(fill(drawer, _warmHover), _roleHover);
    expect(fill(drawer, _warmSelection), _roleSelection);
    expect(_lstar(fill(drawer, _warmBand)), greaterThan(_lstar(normal)),
        reason: 'a fill with no declared role still keeps its contrast');
  });

  testWidgets('declared roles only apply to a themed canvas', (tester) async {
    final drawer = await themedDrawer(tester, _canvasTheme(_darkFill, _darkHighlight));
    setConfigFlags(ConfigFlags()..disable_swt_canvas_colors = false);
    drawer.state.fillRoles = 'F6E6D6:hover';

    expect(fill(drawer, _warmHover), const Color(_warmHover));
  });

  testWidgets('without disable_swt_canvas_colors the application fills pass through',
      (tester) async {
    final drawer = await themedDrawer(tester, _canvasTheme(_darkFill, _darkHighlight));
    setConfigFlags(ConfigFlags()..disable_swt_canvas_colors = false);
    fill(drawer, _navy, times: 10);

    expect(fill(drawer, _hoverYellow), const Color(_hoverYellow));
  });
}

double _lstar(Color c) {
  final y = c.computeLuminance();
  return y > 0.008856 ? 116 * math.pow(y, 1 / 3) - 16 : 903.3 * y;
}

double _hueDistance(Color a, Color b) {
  final d = (HSLColor.fromColor(a).hue - HSLColor.fromColor(b).hue).abs() % 360;
  return math.min(d, 360 - d);
}

double _rgbDistance(Color a, Color b) {
  final dr = a.r - b.r, dg = a.g - b.g, db = a.b - b.b;
  return math.sqrt(dr * dr + dg * dg + db * db) / math.sqrt(3);
}

VColor _vColor(int argb) => VColor()
  ..alpha = (argb >> 24) & 0xFF
  ..red = (argb >> 16) & 0xFF
  ..green = (argb >> 8) & 0xFF
  ..blue = argb & 0xFF;
