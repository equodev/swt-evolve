// A menu bar with no items must leave no trace of itself, in every `decorations_align` mode:
// horizontally aligned it must not paint its strip above the toolbar, and vertically aligned it
// must not put its hamburger button in the toolbar row (where it would also eat the width
// DartMainToolbar reserves for it).
//
// The four alignments are covered end to end through the real widget chain a running app builds:
// DecorationsSwt -> DecorationsMenuData -> ToolbarComposite("MainToolbar") -> HorizontalMenuBar /
// VerticalMenuButton -> MenuSwt.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/custom/toolbar_composite.dart';
import 'package:swtflutter/src/gen/menu.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/decorations_align.dart';
import 'package:swtflutter/src/impl/decorations_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

import 'delivery/support/deliver.dart';
import 'support/menu_bar_fixtures.dart';

Future<void> _pumpShell(
  WidgetTester tester, {
  required DecorationsAlign align,
  required VMenu? menuBar,
}) async {
  setConfigFlags(ConfigFlags()..decorations_align = align);
  // Each call mounts a scenario of its own rather than updating the one before it, and a widget's
  // state outlives the tree that rendered it - so this is a new client, not just a new tree.
  freshClient();
  // The row's y in Java's geometry matters because the toolbar honours it.
  final withMenuStrip = !align.isVertical && (menuBar?.items?.isNotEmpty ?? false);
  await pumpDecorations(
      tester,
      decorations(
        menuBar: menuBar,
        children: [mainToolbar(withMenuStrip: withMenuStrip)],
      ));
}

final _menuIcon = find.byIcon(Icons.menu);
final _barItem = find.text('File');

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  group('a menu bar with items shows in the alignment it was configured for', () {
    testWidgets('hleft paints the strip and starts it on the left', (tester) async {
      await _pumpShell(tester,
          align: DecorationsAlign.hleft, menuBar: fileMenuBar(withItems: true));

      expect(find.byType(HorizontalMenuBar), findsOneWidget);
      expect(_barItem, findsOneWidget);
      expect(_menuIcon, findsNothing, reason: 'horizontal never uses the hamburger button');
      expect(tester.getTopLeft(_barItem).dx, lessThan(viewportWidth / 2));
    });

    testWidgets('hright paints the strip and ends it on the right', (tester) async {
      await _pumpShell(tester,
          align: DecorationsAlign.hright, menuBar: fileMenuBar(withItems: true));

      expect(find.byType(HorizontalMenuBar), findsOneWidget);
      expect(_barItem, findsOneWidget);
      expect(_menuIcon, findsNothing);
      expect(tester.getBottomRight(_barItem).dx, greaterThan(viewportWidth / 2));
    });

    testWidgets('vleft collapses the bar into a button at the toolbar start', (tester) async {
      await _pumpShell(tester,
          align: DecorationsAlign.vleft, menuBar: fileMenuBar(withItems: true));

      expect(find.byType(HorizontalMenuBar), findsNothing);
      expect(_barItem, findsNothing, reason: 'the items live inside the popup, not in a strip');
      expect(_menuIcon, findsOneWidget);
      expect(tester.getTopLeft(_menuIcon).dx, lessThan(viewportWidth / 2));
    });

    testWidgets('vright collapses the bar into a button at the toolbar end', (tester) async {
      await _pumpShell(tester,
          align: DecorationsAlign.vright, menuBar: fileMenuBar(withItems: true));

      expect(find.byType(HorizontalMenuBar), findsNothing);
      expect(_barItem, findsNothing);
      expect(_menuIcon, findsOneWidget);
      expect(tester.getBottomRight(_menuIcon).dx, greaterThan(viewportWidth / 2));
    });
  });

  group('a menu bar with no items shows nothing at all', () {
    for (final align in DecorationsAlign.values) {
      testWidgets('$align hides an itemless menu bar', (tester) async {
        await _pumpShell(tester, align: align, menuBar: fileMenuBar(withItems: false));

        expect(find.byType(HorizontalMenuBar), findsNothing,
            reason: 'no strip row is added to the toolbar column');
        expect(_barItem, findsNothing);
        expect(_menuIcon, findsNothing,
            reason: 'no hamburger button, so it reserves no width either');
      });

      testWidgets('$align hides a shell with no menu bar at all', (tester) async {
        await _pumpShell(tester, align: align, menuBar: null);

        expect(find.byType(HorizontalMenuBar), findsNothing);
        expect(_menuIcon, findsNothing);
      });
    }
  });

  testWidgets('an itemless vertical menu bar takes no width in the toolbar row',
      (tester) async {
    // Both ends are checked because the toolbar row always builds a VerticalMenuButton at each
    // side and lets the button itself decide whether it belongs to the current alignment.
    final atStart = find.byType(VerticalMenuButton).at(0);
    final atEnd = find.byType(VerticalMenuButton).at(1);

    await _pumpShell(tester,
        align: DecorationsAlign.vleft, menuBar: fileMenuBar(withItems: true));
    expect(tester.getSize(atStart).width, greaterThan(0));
    expect(tester.getSize(atEnd).width, 0.0);

    await _pumpShell(tester,
        align: DecorationsAlign.vleft, menuBar: fileMenuBar(withItems: false));
    expect(tester.getSize(atStart).width, 0.0,
        reason: 'the hidden button must not keep reserving toolbar width');
    expect(tester.getSize(atEnd).width, 0.0);
  });

  testWidgets('an itemless horizontal menu bar leaves the toolbar row at the top',
      (tester) async {
    await _pumpShell(tester,
        align: DecorationsAlign.hleft, menuBar: fileMenuBar(withItems: true));
    final withMenu = tester.getTopLeft(find.text('Save')).dy;

    await _pumpShell(tester,
        align: DecorationsAlign.hleft, menuBar: fileMenuBar(withItems: false));
    final withoutMenu = tester.getTopLeft(find.text('Save')).dy;

    expect(withoutMenu, lessThan(withMenu),
        reason: 'the hidden strip must not keep pushing the toolbar row down');
  });
}
