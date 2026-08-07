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

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/custom/toolbar_composite.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/decorations.dart';
import 'package:swtflutter/src/gen/menu.dart';
import 'package:swtflutter/src/gen/menuitem.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/toolitem.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/decorations_align.dart';
import 'package:swtflutter/src/impl/decorations_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

const double _viewportWidth = 800;
const double _viewportHeight = 300;

VRectangle _rect(int x, int y, int width, int height) => VRectangle()
  ..x = x
  ..y = y
  ..width = width
  ..height = height;

VMenu _submenu() => VMenu()
  ..id = 300
  ..style = SWT.DROP_DOWN
  ..enabled = true
  ..items = [
    VMenuItem()
      ..id = 301
      ..style = SWT.PUSH
      ..enabled = true
      ..text = 'Exit',
  ];

/// The bar menu as Java serializes it: a BAR menu whose items are the CASCADE entries.
VMenu _menuBar({required bool withItems}) {
  final cascade = VMenuItem()
    ..id = 201
    ..style = SWT.CASCADE
    ..enabled = true
    ..text = 'File'
    ..menu = _submenu();
  return VMenu()
    ..id = 200
    ..style = SWT.BAR
    ..enabled = true
    ..items = withItems ? [cascade] : <VMenuItem>[];
}

VToolBar _toolBar({int y = 5}) => VToolBar()
  ..id = 11
  ..style = SWT.FLAT
  ..enabled = true
  ..visible = true
  ..bounds = _rect(0, y, 200, 40)
  ..items = [
    VToolItem()
      ..id = 12
      ..style = SWT.PUSH
      ..enabled = true
      ..text = 'Save',
  ];

/// Java's own geometry for the bar: with a menu strip it is taller and the toolbar row starts below
/// the strip; without one the row is the whole bar. The row's y matters because the toolbar honours
/// it, so a fixture that always used 0 would not describe any real state.
VComposite _mainToolbar({required bool withMenuStrip}) => VComposite()
  ..id = 10
  ..swt = 'MainToolbar'
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = _rect(0, 0, _viewportWidth.toInt(), withMenuStrip ? 68 : 40)
  ..children = [_toolBar(y: withMenuStrip ? 33 : 5)];

VDecorations _decorations(VMenu? menuBar, {required bool withMenuStrip}) => VDecorations()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = _rect(0, 0, _viewportWidth.toInt(), _viewportHeight.toInt())
  ..menuBar = menuBar
  ..children = [_mainToolbar(withMenuStrip: withMenuStrip)];

Future<void> _pumpShell(
  WidgetTester tester, {
  required DecorationsAlign align,
  required VMenu? menuBar,
}) async {
  setConfigFlags(ConfigFlags()..decorations_align = align);
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: _viewportWidth,
      height: _viewportHeight,
      child: DecorationsSwt<VDecorations>(
        value: _decorations(
          menuBar,
          withMenuStrip: !align.isVertical && (menuBar?.items?.isNotEmpty ?? false),
        ),
      ),
    ),
  ));
  // A second pump lets the toolbar's post-layout measurement of the vertical menu button settle.
  await tester.pump();
}

final _menuIcon = find.byIcon(Icons.menu);
final _barItem = find.text('File');

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  group('a menu bar with items shows in the alignment it was configured for', () {
    testWidgets('hleft paints the strip and starts it on the left', (tester) async {
      await _pumpShell(tester,
          align: DecorationsAlign.hleft, menuBar: _menuBar(withItems: true));

      expect(find.byType(HorizontalMenuBar), findsOneWidget);
      expect(_barItem, findsOneWidget);
      expect(_menuIcon, findsNothing, reason: 'horizontal never uses the hamburger button');
      expect(tester.getTopLeft(_barItem).dx, lessThan(_viewportWidth / 2));
    });

    testWidgets('hright paints the strip and ends it on the right', (tester) async {
      await _pumpShell(tester,
          align: DecorationsAlign.hright, menuBar: _menuBar(withItems: true));

      expect(find.byType(HorizontalMenuBar), findsOneWidget);
      expect(_barItem, findsOneWidget);
      expect(_menuIcon, findsNothing);
      expect(tester.getBottomRight(_barItem).dx, greaterThan(_viewportWidth / 2));
    });

    testWidgets('vleft collapses the bar into a button at the toolbar start', (tester) async {
      await _pumpShell(tester,
          align: DecorationsAlign.vleft, menuBar: _menuBar(withItems: true));

      expect(find.byType(HorizontalMenuBar), findsNothing);
      expect(_barItem, findsNothing, reason: 'the items live inside the popup, not in a strip');
      expect(_menuIcon, findsOneWidget);
      expect(tester.getTopLeft(_menuIcon).dx, lessThan(_viewportWidth / 2));
    });

    testWidgets('vright collapses the bar into a button at the toolbar end', (tester) async {
      await _pumpShell(tester,
          align: DecorationsAlign.vright, menuBar: _menuBar(withItems: true));

      expect(find.byType(HorizontalMenuBar), findsNothing);
      expect(_barItem, findsNothing);
      expect(_menuIcon, findsOneWidget);
      expect(tester.getBottomRight(_menuIcon).dx, greaterThan(_viewportWidth / 2));
    });
  });

  group('a menu bar with no items shows nothing at all', () {
    for (final align in DecorationsAlign.values) {
      testWidgets('$align hides an itemless menu bar', (tester) async {
        await _pumpShell(tester, align: align, menuBar: _menuBar(withItems: false));

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
        align: DecorationsAlign.vleft, menuBar: _menuBar(withItems: true));
    expect(tester.getSize(atStart).width, greaterThan(0));
    expect(tester.getSize(atEnd).width, 0.0);

    await _pumpShell(tester,
        align: DecorationsAlign.vleft, menuBar: _menuBar(withItems: false));
    expect(tester.getSize(atStart).width, 0.0,
        reason: 'the hidden button must not keep reserving toolbar width');
    expect(tester.getSize(atEnd).width, 0.0);
  });

  testWidgets('an itemless horizontal menu bar leaves the toolbar row at the top',
      (tester) async {
    await _pumpShell(tester,
        align: DecorationsAlign.hleft, menuBar: _menuBar(withItems: true));
    final withMenu = tester.getTopLeft(find.text('Save')).dy;

    await _pumpShell(tester,
        align: DecorationsAlign.hleft, menuBar: _menuBar(withItems: false));
    final withoutMenu = tester.getTopLeft(find.text('Save')).dy;

    expect(withoutMenu, lessThan(withMenu),
        reason: 'the hidden strip must not keep pushing the toolbar row down');
  });
}
