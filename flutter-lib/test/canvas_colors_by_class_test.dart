// disable_swt_canvas_colors_<Class> decides per control class whether its drawing takes the theme's
// colors, in either direction. A grid and a color ramp in the same window can therefore paint
// differently: the ramp's colors are its data. Java resolves the class and sends the decision with
// each GC's state; a GC no listed class covers carries none and follows the global flag.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/gen/widget.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

const _appBackground = Color(0xFFF0F0F0);

// Re-pumping a widget id keeps its first value, so every mount takes a fresh one.
var _nextId = 7100;

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VColor _vColor(int r, int g, int b) => VColor()
  ..alpha = 0xFF
  ..red = r
  ..green = g
  ..blue = b;

VGC _gcState({required bool? themed}) => VGC()
  ..swt = 'GC'
  ..id = _nextId++
  ..themedCanvas = themed
  ..background = _vColor(0xFF, 0xFF, 0xFF)
  ..foreground = _vColor(0x00, 0x00, 0x00);

VCanvas _canvasHostingAnEditor() => VCanvas()
  ..id = _nextId++
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..hasOwnBackground = true
  ..background = _vColor(0xFF, 0xFF, 0xFF)
  ..bounds = _rect(0, 0, 200, 200)
  ..children = [
    VText()
      ..id = _nextId++
      ..style = SWT.SINGLE
      ..enabled = true
      ..visible = true
      ..editable = true
      ..bounds = _rect(10, 10, 99, 19)
      ..text = 'cell'
      ..background = _vColor(0xF0, 0xF0, 0xF0)
      ..foreground = _vColor(0x00, 0x00, 0x00),
  ];

/// The app's context, from which a drawer resolves the Canvas theme.
Future<BuildContext> _pumpApp(WidgetTester tester) async {
  late BuildContext context;
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.dark,
    contentWidget: Builder(builder: (c) {
      context = c;
      return const SizedBox();
    }),
  ));
  return context;
}

GCDrawer _drawer(BuildContext context, VGC state) {
  final drawer = GCDrawer.embedded(state);
  addTearDown(drawer.dispose);
  drawer.syncContext(context);
  return drawer;
}

/// Mounts [canvas] and delivers its GC's state, as the GC overlay does once Java paints.
Future<void> _pumpCanvas(WidgetTester tester, VCanvas canvas, {required bool? themed}) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.dark,
    contentWidget: SizedBox(width: 200, height: 200, child: CanvasSwt<VCanvas>(value: canvas)),
  ));
  await tester.pumpAndSettle();
  tester
      .state<WidgetSwtState>(find.byWidgetPredicate((w) => w is CanvasSwt && w.value.id == canvas.id))
      .notifyGCReady(_gcState(themed: themed)..id = canvas.id);
  await tester.pumpAndSettle();
}

Color? _editorFill(WidgetTester tester) =>
    tester.widget<TextField>(find.byType(TextField)).decoration!.fillColor;

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  testWidgets('a GC on a listed control takes the theme while one on an unlisted control does not',
      (tester) async {
    setConfigFlags(ConfigFlags()..disable_swt_canvas_colors = false);
    final context = await _pumpApp(tester);

    final onGrid = _drawer(context, _gcState(themed: true));
    final onColorBar = _drawer(context, _gcState(themed: null));

    expect(onGrid.fillColor, isNot(Colors.white));
    expect(onGrid.textColor, isNot(Colors.black));
    expect(onColorBar.fillColor, Colors.white,
        reason: "the ramp's colors are its data, so they reach the screen unchanged");
    expect(onColorBar.textColor, Colors.black);
  });

  testWidgets('a class set to false keeps its colors when the global flag themes the rest',
      (tester) async {
    setConfigFlags(ConfigFlags()..disable_swt_canvas_colors = true);
    final context = await _pumpApp(tester);

    expect(_drawer(context, _gcState(themed: false)).fillColor, Colors.white);
    expect(_drawer(context, _gcState(themed: null)).fillColor, isNot(Colors.white));
  });

  testWidgets('use_swt_colors wins over the allowlist', (tester) async {
    setConfigFlags(ConfigFlags()..use_swt_colors = true);
    final context = await _pumpApp(tester);

    expect(_drawer(context, _gcState(themed: true)).fillColor, Colors.white);
  });

  testWidgets('a control hosted on a listed Canvas follows the theme with it', (tester) async {
    setConfigFlags(ConfigFlags()..disable_swt_canvas_colors = false);
    await _pumpCanvas(tester, _canvasHostingAnEditor(), themed: true);

    expect(_editorFill(tester), isNot(_appBackground));
  });

  testWidgets('a control hosted on an unlisted Canvas keeps the application colors',
      (tester) async {
    setConfigFlags(ConfigFlags()..disable_swt_canvas_colors = false);
    await _pumpCanvas(tester, _canvasHostingAnEditor(), themed: null);

    expect(_editorFill(tester), _appBackground);
  });

  test('without a decision for the GC the global flag alone decides', () {
    expect(gcUsesThemeColors(null), isFalse);
    setConfigFlags(ConfigFlags()..disable_swt_canvas_colors = true);
    expect(gcUsesThemeColors(null), isTrue);
  });
}
