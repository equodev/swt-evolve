// On macOS the application menu lives in the system menu bar, which the OS owns: a tree rendered in
// a browser tab cannot reach it, because there the window belongs to the browser. The Display
// update therefore carries that menu too, and the menu bar Evolve draws inside the window shows it
// ahead of the Shell's own menus -- the position the OS gives it.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/custom/toolbar_composite.dart';
import 'package:swtflutter/src/gen/menu.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/decorations_align.dart';
import 'package:swtflutter/src/impl/decorations_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

import 'support/menu_bar_fixtures.dart';

/// The application menu as Java serializes it: a holder whose single item is the app-name cascade.
VMenu _applicationMenu() => VMenu()
  ..id = 400
  ..style = SWT.BAR
  ..enabled = true
  ..items = [cascade(401, 'Example App', 'About Example App')];

Future<void> _pumpShell(
  WidgetTester tester, {
  required DecorationsAlign align,
  required VMenu? menuBar,
  VMenu? applicationMenu,
}) async {
  setConfigFlags(ConfigFlags()..decorations_align = align);
  applySystemMenu(applicationMenu);
  await pumpDecorations(
      tester,
      decorations(
        menuBar: menuBar,
        children: [mainToolbar(withMenuStrip: true)],
      ));
}

final _appItem = find.text('Example App');
final _shellItem = find.text('File');
final _menuIcon = find.byIcon(Icons.menu);

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  testWidgets('the strip shows the application menu before the Shell menus', (tester) async {
    await _pumpShell(tester,
        align: DecorationsAlign.hleft,
        menuBar: fileMenuBar(withItems: true),
        applicationMenu: _applicationMenu());

    expect(find.byType(HorizontalMenuBar), findsOneWidget);
    expect(_appItem, findsOneWidget);
    expect(tester.getTopLeft(_appItem).dx, lessThan(tester.getTopLeft(_shellItem).dx));
  });

  testWidgets('the hamburger menu lists the application menu first', (tester) async {
    await _pumpShell(tester,
        align: DecorationsAlign.vleft,
        menuBar: fileMenuBar(withItems: true),
        applicationMenu: _applicationMenu());

    expect(_menuIcon, findsOneWidget);
    await tester.tap(_menuIcon);
    await tester.pumpAndSettle();

    expect(_appItem, findsOneWidget);
    expect(tester.getTopLeft(_appItem).dy, lessThan(tester.getTopLeft(_shellItem).dy));
  });

  testWidgets('an application menu alone is enough to show the bar', (tester) async {
    await _pumpShell(tester,
        align: DecorationsAlign.hleft,
        menuBar: fileMenuBar(withItems: false),
        applicationMenu: _applicationMenu());

    expect(_appItem, findsOneWidget);
  });

  testWidgets('nothing is shown where there is no application menu', (tester) async {
    await _pumpShell(tester,
        align: DecorationsAlign.hleft, menuBar: fileMenuBar(withItems: false));

    expect(find.byType(HorizontalMenuBar), findsNothing);
    expect(_appItem, findsNothing);
  });
}
