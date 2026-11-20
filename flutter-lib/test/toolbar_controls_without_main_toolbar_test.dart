// The optional controls row (theme palette, scaling control) lives in the main toolbar. An app with
// no main toolbar still gets it, exactly once, at the trailing end of the window's own top strip:
// the CSD title strip when there is one, else the Shell's own menu strip.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/custom/controls_item.dart';
import 'package:swtflutter/src/custom/csd/csd_scaffold.dart';
import 'package:swtflutter/src/gen/decorations.dart';
import 'package:swtflutter/src/gen/menu.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

import 'support/menu_bar_fixtures.dart';

final _chevron = find.byTooltip('Expand controls');
final _inMenuBar = find.descendant(of: find.byWidgetPredicate((w) => w is MenuSwt), matching: _chevron);

Future<void> _pump(WidgetTester tester, VDecorations shell, {bool overlayStrip = false}) async {
  VRegistry.instance.clear();
  setConfigFlags(ConfigFlags()
    ..show_scaling_control = true
    ..csd_placement = overlayStrip ? 'overlay' : 'false');
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: viewportWidth,
      height: viewportHeight,
      child: Column(children: [
        if (overlayStrip) const CsdOverlayStrip(controlsLeading: true),
        Expanded(child: DecorationsSwt<VDecorations>(value: shell)),
      ]),
    ),
  ));
  // The main toolbar is tracked after its first frame, and the strip rebuilds on the next one.
  await tester.pump();
  await tester.pump();
}

void main() {
  setUp(() {
    resetConfigFlags();
    resetMainToolbars();
  });
  tearDown(resetConfigFlags);

  testWidgets('the Shell\'s own menu strip holds the controls when there is no main toolbar',
      (tester) async {
    await _pump(tester, decorations(menuBar: fileMenuBar(), children: [], ownMenuStrip: true,
        menuStripHeight: 28));

    expect(_chevron, findsOneWidget);
    expect(_inMenuBar, findsOneWidget,
        reason: 'drawn by the menu bar, so it sits on the bar\'s own background and border');
  });

  testWidgets('a hidden main toolbar leaves the controls to the Shell\'s own menu strip',
      (tester) async {
    await _pump(tester, decorations(menuBar: fileMenuBar(), children: [mainToolbar(visible: false)],
        ownMenuStrip: true, menuStripHeight: 28));

    expect(_chevron, findsOneWidget);
    expect(_inMenuBar, findsOneWidget);
  });

  testWidgets('the CSD title strip holds the controls when there is no main toolbar',
      (tester) async {
    await _pump(tester, decorations(menuBar: fileMenuBar(), children: [], ownMenuStrip: true,
        menuStripHeight: 28), overlayStrip: true);

    expect(_chevron, findsOneWidget, reason: 'in the title strip, not again in the menu strip');
    expect(tester.getCenter(_chevron).dy, lessThan(kCsdOverlayStripHeight));
  });

  testWidgets('a main toolbar keeps the controls to itself', (tester) async {
    await _pump(tester, decorations(menuBar: fileMenuBar(), children: [mainToolbar(withMenuStrip: true)]),
        overlayStrip: true);

    expect(_chevron, findsOneWidget);
    expect(tester.getCenter(_chevron).dy, greaterThan(kCsdOverlayStripHeight),
        reason: 'in the toolbar, below the title strip');
  });
}
