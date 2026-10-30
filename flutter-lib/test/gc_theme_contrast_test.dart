import 'dart:convert';
import 'dart:math' as math;
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/tagged_values.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/gen/rgb.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';
import 'package:swtflutter/src/impl/utils/widget_utils.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import 'package:swtflutter/src/theme/theme_extensions/canvas_theme_extension.dart';

/// On a themed canvas a fill keeps its distance to the control's normal fill, and a declared hover
/// or selection takes the theme's color for that role.

const _gcId = 4250;

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

VRGB _vrgb(int argb) => VRGB()
  ..red = argb >> 16 & 0xFF
  ..green = argb >> 8 & 0xFF
  ..blue = argb & 0xFF;

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

  group('lstar', () {
    test('matches the CIE 1976 L* of known sRGB colors', () {
      expect(lstar(const Color(0xFF000000)), 0);
      expect(lstar(const Color(0xFFFFFFFF)), closeTo(100, 0.01));
      expect(lstar(const Color(0xFF808080)), closeTo(53.59, 0.05));
      expect(lstar(const Color(0xFF777777)), closeTo(50.03, 0.05));
      expect(lstar(const Color(0xFF010101)), closeTo(0.27, 0.01), reason: 'the linear segment');
    });

    test('withLstar keeps the hue and reaches the L* asked for', () {
      final moved = withLstar(const Color(0xFF336699), 70);

      expect(lstar(moved), closeTo(70, 0.1));
      expect(HSLColor.fromColor(moved).hue, closeTo(HSLColor.fromColor(const Color(0xFF336699)).hue, 1));
    });
  });

  /// Mounts a themed app and hands the drawer the context, as GCImpl.build() does. With no
  /// [ground], the drawer has no host control, so the fill it paints with most is its normal one.
  Future<GCDrawer> themedDrawer(WidgetTester tester, CanvasThemeExtension theme, {int? ground}) async {
    final drawer = GCDrawer.embedded(VGC()
      ..swt = 'GC'
      ..id = _gcId);
    addTearDown(drawer.dispose);

    await tester.pumpWidget(MaterialApp(
      theme: ThemeData(extensions: <ThemeExtension<dynamic>>[theme]),
      home: _Ground(ground == null ? null : _vColor(ground)),
    ));
    final host = tester.element(find.byType(_Ground));
    drawer.syncContext(host);
    if (ground != null) drawer.widgetContext = host;
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

    expect(lstar(band), greaterThan(lstar(selection)));
    expect(lstar(selection), greaterThan(lstar(normal) + 15),
        reason: 'a selection must stand out from the normal fill');
    expect(lstar(band), lessThan(60), reason: 'the offset is compressed, never saturated');
  });

  testWidgets('dark theme: the darker highlights of a light application move up, not into black',
      (tester) async {
    final drawer = await themedDrawer(tester, _canvasTheme(_darkFill, _darkHighlight));
    final normal = fill(drawer, _white, times: 10);

    final band = fill(drawer, _warmBand);
    final hover = fill(drawer, _warmHover);
    final selection = fill(drawer, _warmSelection);

    expect(lstar(band) - lstar(normal), inInclusiveRange(2, 8),
        reason: 'subtle banding stays subtle');
    expect(lstar(hover), greaterThan(lstar(normal) + 10),
        reason: 'a subtle hover must still show on the dark fill');
    expect(lstar(selection), greaterThan(lstar(hover) + 8),
        reason: 'the selection was further from the normal fill than the hover, so it stays further');
  });

  testWidgets('light theme: neutral fills keep the order of their distance to the normal fill',
      (tester) async {
    final drawer = await themedDrawer(tester, _canvasTheme(_lightFill, _lightHighlight));
    final normal = fill(drawer, _white, times: 10);

    final band = fill(drawer, _lightGrey);
    final selection = fill(drawer, _selectionGrey);

    expect(lstar(normal), greaterThan(lstar(band)));
    expect(lstar(band), greaterThan(lstar(selection)));
    expect(lstar(normal) - lstar(selection), greaterThan(5),
        reason: 'a selection must stand out from the normal fill');
  });

  testWidgets('the control background is the normal fill from the first fill on', (tester) async {
    final drawer = await themedDrawer(tester, _canvasTheme(_darkFill, _darkHighlight), ground: _white);

    expect(lstar(fill(drawer, _warmHover)), greaterThan(lstar(_darkFill) + 10),
        reason: 'measured against the background, not against the first fill counted');
    expect(fill(drawer, _white), _darkFill);
  });

  testWidgets('an opaque text background keeps its contrast like a fill', (tester) async {
    final drawer = await themedDrawer(tester, _canvasTheme(_darkFill, _darkHighlight));
    fill(drawer, _white, times: 10);

    drawer.state.background = _vColor(_warmSelection);

    expect(drawer.textBackgroundColor, fill(drawer, _warmSelection));
    expect(lstar(drawer.textBackgroundColor), greaterThan(lstar(_darkFill) + 10));
  });

  testWidgets('within one paint an application color always paints the same way', (tester) async {
    final drawer = await themedDrawer(tester, _canvasTheme(_darkFill, _darkHighlight));
    fill(drawer, _white, times: 5);
    _endPaint();
    await tester.pump();

    final first = fill(drawer, _warmHover);
    final last = fill(drawer, _warmHover, times: 20);

    expect(first, isNot(_darkFill));
    expect(last, first,
        reason: 'the hover outnumbering the earlier normal fill must not change it mid-paint');
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
    drawer.state
      ..hoverFills = [_vrgb(_warmHover)]
      ..selectionFills = [_vrgb(_warmSelection)];
    final normal = fill(drawer, _white, times: 10);

    expect(fill(drawer, _warmHover), _roleHover);
    expect(fill(drawer, _warmSelection), _roleSelection);
    expect(lstar(fill(drawer, _warmBand)), greaterThan(lstar(normal)),
        reason: 'a fill with no declared role still keeps its contrast');
  });

  testWidgets('declared roles only apply to a themed canvas', (tester) async {
    final drawer = await themedDrawer(tester, _canvasTheme(_darkFill, _darkHighlight));
    setConfigFlags(ConfigFlags()..disable_swt_canvas_colors = false);
    drawer.state.hoverFills = [_vrgb(_warmHover)];

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

/// Stands in for the control a GC draws on, whose background is [ground].
class _Ground extends StatefulWidget {
  const _Ground(this.ground);

  final VColor? ground;

  @override
  State<_Ground> createState() => _GroundState();
}

class _GroundState extends State<_Ground> implements GCGround {
  @override
  VColor? get swtGround => widget.ground;

  @override
  Color? get renderedGround => null;

  @override
  Widget build(BuildContext context) => const SizedBox();
}

/// Ends the paint in flight, as Java's gcDispose does.
void _endPaint() {
  final action = utf8.encode('GC/$_gcId/gcDispose');
  final body = TaggedValues.encode({'fullRepaint': true});
  final frame = Uint8List(2 + action.length + body.length);
  frame[0] = (action.length >> 8) & 0xFF;
  frame[1] = action.length & 0xFF;
  frame.setRange(2, 2 + action.length, action);
  frame.setRange(2 + action.length, frame.length, body);
  EquoCommService.commForTesting.receiveBinary(frame);
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
