// On macOS desktop the OS owns the menu bar, so the client must draw none of its own: no strip
// above the toolbar, no hamburger button inside it, and no layout space reserved for either -- the
// menus are all in the system bar, and drawing them here too would put every one of them in two
// places at once.
//
// Covered end to end through the real widget chain a running app builds:
// DecorationsSwt -> DecorationsMenuData -> ToolbarComposite("MainToolbar") -> HorizontalMenuBar /
// VerticalMenuButton.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/custom/toolbar_composite.dart';
import 'package:swtflutter/src/gen/menu.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/decorations_align.dart';
import 'package:swtflutter/src/impl/decorations_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

import 'support/menu_bar_fixtures.dart';

/// The application menu, which the Display carries and the client draws ahead of the Shell's own
/// menus. On macOS desktop it is in the system bar too, so it must not appear here either.
VMenu _applicationMenu() => VMenu()
  ..id = 100
  ..style = SWT.BAR
  ..enabled = true
  ..items = [cascade(101, 'Evolve', 'About Evolve')];

Future<void> _pumpShell(
  WidgetTester tester, {
  required DecorationsAlign align,
  required bool systemMenuBar,
}) async {
  // Each pump is a fresh app, so it must not inherit the widgets the last one registered: a value
  // is held per widget id for as long as that widget exists, and pumping a second shell that reuses
  // an id would otherwise be told it already knows that widget and keep the first one's bounds.
  VRegistry.instance.clear();
  setConfigFlags(ConfigFlags()
    ..decorations_align = align
    ..system_menu_bar = systemMenuBar);
  applySystemMenu(_applicationMenu());
  // With the OS drawing the menus, `DartMainToolbar` reports the same bar it reports for a Shell
  // with no menu bar at all -- one row, no strip offset.
  await pumpDecorations(
      tester,
      decorations(
        menuBar: fileMenuBar(),
        children: [mainToolbar(withMenuStrip: !systemMenuBar && !align.isVertical)],
      ));
}

final _menuIcon = find.byIcon(Icons.menu);
final _barItem = find.text('File');
final _appMenuItem = find.text('Evolve');

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  for (final align in DecorationsAlign.values) {
    testWidgets('$align draws no in-window menu bar when the OS owns it', (tester) async {
      await _pumpShell(tester, align: align, systemMenuBar: true);

      expect(find.byType(HorizontalMenuBar), findsNothing,
          reason: 'no strip row is added to the toolbar column');
      expect(_barItem, findsNothing);
      expect(_appMenuItem, findsNothing,
          reason: 'the application menu is in the system bar too');
      expect(_menuIcon, findsNothing,
          reason: 'no hamburger button, so it reserves no width either');
    });
  }

  testWidgets('the same menu bar still shows when the window owns it', (tester) async {
    // Guards the flag itself: without this the tests above would pass on a bar that never renders.
    await _pumpShell(tester, align: DecorationsAlign.hleft, systemMenuBar: false);

    expect(find.byType(HorizontalMenuBar), findsOneWidget);
    expect(_barItem, findsOneWidget);
    expect(_appMenuItem, findsOneWidget);
  });

  testWidgets('an OS-owned menu bar takes no width in the toolbar row', (tester) async {
    // Both ends are checked because the toolbar row always builds a VerticalMenuButton at each
    // side and lets the button itself decide whether it belongs to the current alignment.
    final atStart = find.byType(VerticalMenuButton).at(0);
    final atEnd = find.byType(VerticalMenuButton).at(1);

    await _pumpShell(tester, align: DecorationsAlign.vleft, systemMenuBar: false);
    expect(tester.getSize(atStart).width, greaterThan(0));

    await _pumpShell(tester, align: DecorationsAlign.vleft, systemMenuBar: true);
    expect(tester.getSize(atStart).width, 0.0,
        reason: 'the hidden button must not keep reserving toolbar width');
    expect(tester.getSize(atEnd).width, 0.0);
  });

  testWidgets('an OS-owned menu bar leaves the toolbar row at the top', (tester) async {
    await _pumpShell(tester, align: DecorationsAlign.hleft, systemMenuBar: false);
    final withMenu = tester.getTopLeft(find.text('Save')).dy;

    await _pumpShell(tester, align: DecorationsAlign.hleft, systemMenuBar: true);
    final withoutMenu = tester.getTopLeft(find.text('Save')).dy;

    expect(withoutMenu, lessThan(withMenu),
        reason: 'the missing strip must not keep pushing the toolbar row down');
  });
}
