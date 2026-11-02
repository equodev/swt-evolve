// Under Canvas theming a GC's colors are resolved from the Canvas theme as the drawing arrives, so
// a theme switch reaches what a GC already painted (a NatTable grid) only once Java draws it again.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireChannel;
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import 'package:swtflutter/src/theme/theme_extensions/canvas_theme_extension.dart';

import 'support/gc_frames.dart';

const int _canvasId = 4712;

class _CapturingCanvasSwt extends CanvasSwt<VCanvas> {
  const _CapturingCanvasSwt({required super.value, required this.onEvent});

  final void Function(int channel) onEvent;

  @override
  void sendEvent(VCanvas val, int channel, VEvent? payload) => onEvent(channel);
}

VCanvas _canvas() => VCanvas()
  ..swt = 'Canvas'
  ..id = _canvasId
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 200
    ..height = 100);

/// One paint cycle filling the client area, as a NatTable cell background does.
Future<void> _paintCycle() async {
  receive('GC/$_canvasId/b',
      GcFrame().opNamed('fillRectangleintintintint', [0, 0, 200, 100]).bytes);
  receive('GC/$_canvasId/gcDispose', gcDisposeOf({'fullRepaint': true}));
  await Future<void>.microtask(() {});
}

/// The fill the control actually renders, as opposed to what the drawer is holding.
Color _paintedFill(WidgetTester tester) {
  final painters = tester
      .widgetList<CustomPaint>(find.byType(CustomPaint))
      .map((w) => w.painter)
      .whereType<ScenePainter>();
  final fills = [
    for (final p in painters) ...p.shapes.whereType<RectShape>().where((s) => s.isFilled)
  ];
  expect(fills, isNotEmpty, reason: 'sanity: the paint cycle fills a rectangle');
  return fills.last.color;
}

Future<void> _settle(WidgetTester tester) async {
  for (var i = 0; i < 20; i++) {
    await tester.pump(Duration.zero);
  }
  await tester.pumpAndSettle();
}

int _paints(List<int> events) => events.where((e) => e == WireChannel.paintPaint).length;

ConfigFlags _flags(String theme, {required bool themedCanvas}) => ConfigFlags()
  ..disable_swt_canvas_colors = themedCanvas
  ..force_theme = theme;

/// Mounts the canvas under the light theme and lets Java paint it once.
Future<List<int>> _paintedInLight(WidgetTester tester, {required bool themedCanvas}) async {
  final events = <int>[];
  applyConfigFlags(_flags('light', themedCanvas: themedCanvas));
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
        width: 200, height: 100, child: _CapturingCanvasSwt(value: _canvas(), onEvent: events.add)),
  ));
  await _settle(tester);
  await _paintCycle();
  await _settle(tester);
  return events;
}

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  testWidgets('switching the theme has Java repaint a themed canvas in the new theme',
      (tester) async {
    final events = await _paintedInLight(tester, themedCanvas: true);
    final light = _paintedFill(tester);
    final before = _paints(events);

    // The palette's pick, as Java echoes it back on swt.evolve.properties.
    applyConfigFlags(_flags('dark', themedCanvas: true));
    await _settle(tester);
    expect(_paints(events), before + 1,
        reason: 'the theme switch must ask Java to repaint the canvas: it kept the light fill '
            '(${light.toARGB32().toRadixString(16)}) until something else made Java paint');

    // Java's answer to that request.
    await _paintCycle();
    await _settle(tester);
    expect(_paintedFill(tester), isNot(light), reason: 'the repaint is drawn in the dark theme');
    expect(_paints(events), before + 1, reason: 'a scene drawn in the current theme asks no more');
  });

  testWidgets('a repaint drawn while the theme animates asks again for one in the new theme',
      (tester) async {
    final events = await _paintedInLight(tester, themedCanvas: true);
    final before = _paints(events);

    applyConfigFlags(_flags('dark', themedCanvas: true));
    // The switch's first frame still shows the light theme; a quarter into the 200 ms animation it
    // shows one between the two.
    await tester.pump();
    await tester.pump(const Duration(milliseconds: 50));
    expect(_paints(events), before + 1, reason: 'the theme switch asks Java to repaint the canvas');
    // Java answers while the switch animates: its colors come from a theme between the two.
    await _paintCycle();
    await _settle(tester);
    expect(_paints(events), before + 2,
        reason: 'a scene drawn mid-animation must ask for one drawn under the final theme');

    await _paintCycle();
    await _settle(tester);
    final dark = Theme.of(tester.element(find.byType(_CapturingCanvasSwt)))
        .extension<CanvasThemeExtension>()!;
    expect(_paintedFill(tester), dark.fillColor, reason: 'the canvas ends in the dark theme');
    expect(_paints(events), before + 2, reason: 'a scene drawn in the current theme asks no more');
  });

  testWidgets('a canvas keeping the application colors is not repainted on a theme switch',
      (tester) async {
    final events = await _paintedInLight(tester, themedCanvas: false);
    final before = _paints(events);

    applyConfigFlags(_flags('dark', themedCanvas: false));
    await _settle(tester);
    expect(_paints(events), before);
  });

  testWidgets('a rebuild that keeps the theme does not repaint the canvas', (tester) async {
    final events = await _paintedInLight(tester, themedCanvas: true);
    final before = _paints(events);

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
          width: 210, height: 100, child: _CapturingCanvasSwt(value: _canvas(), onEvent: events.add)),
    ));
    await _settle(tester);
    expect(_paints(events), before);
  });
}
