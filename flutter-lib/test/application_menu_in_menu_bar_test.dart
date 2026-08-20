// On macOS the application menu lives in the system menu bar, which the OS owns: a tree rendered in
// a browser tab cannot reach it, because there the window belongs to the browser. The Display
// update therefore carries that menu too, and the menu bar Evolve draws inside the window shows it
// ahead of the Shell's own menus -- the position the OS gives it.

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

VMenuItem _cascade(int id, String text, String childText) => VMenuItem()
  ..id = id
  ..style = SWT.CASCADE
  ..enabled = true
  ..text = text
  ..menu = (VMenu()
    ..id = id + 1
    ..style = SWT.DROP_DOWN
    ..enabled = true
    ..items = [
      VMenuItem()
        ..id = id + 2
        ..style = SWT.PUSH
        ..enabled = true
        ..text = childText,
    ]);

/// The application menu as Java serializes it: a holder whose single item is the app-name cascade.
VMenu _applicationMenu() => VMenu()
  ..id = 400
  ..style = SWT.BAR
  ..enabled = true
  ..items = [_cascade(401, 'Example App', 'About Example App')];

VMenu _menuBar({required bool withItems}) => VMenu()
  ..id = 200
  ..style = SWT.BAR
  ..enabled = true
  ..items = withItems ? [_cascade(201, 'File', 'Exit')] : <VMenuItem>[];

VToolBar _toolBar() => VToolBar()
  ..id = 11
  ..style = SWT.FLAT
  ..enabled = true
  ..visible = true
  ..bounds = _rect(0, 33, 200, 40)
  ..items = [
    VToolItem()
      ..id = 12
      ..style = SWT.PUSH
      ..enabled = true
      ..text = 'Save',
  ];

VComposite _mainToolbar() => VComposite()
  ..id = 10
  ..swt = 'MainToolbar'
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = _rect(0, 0, _viewportWidth.toInt(), 68)
  ..children = [_toolBar()];

Future<void> _pumpShell(
  WidgetTester tester, {
  required DecorationsAlign align,
  required VMenu? menuBar,
  VMenu? applicationMenu,
}) async {
  setConfigFlags(ConfigFlags()..decorations_align = align);
  applySystemMenu(applicationMenu);
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: _viewportWidth,
      height: _viewportHeight,
      child: DecorationsSwt<VDecorations>(
        value: VDecorations()
          ..id = 1
          ..style = SWT.NONE
          ..enabled = true
          ..visible = true
          ..bounds = _rect(0, 0, _viewportWidth.toInt(), _viewportHeight.toInt())
          ..menuBar = menuBar
          ..children = [_mainToolbar()],
      ),
    ),
  ));
  await tester.pump();
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
        menuBar: _menuBar(withItems: true),
        applicationMenu: _applicationMenu());

    expect(find.byType(HorizontalMenuBar), findsOneWidget);
    expect(_appItem, findsOneWidget);
    expect(tester.getTopLeft(_appItem).dx, lessThan(tester.getTopLeft(_shellItem).dx));
  });

  testWidgets('the hamburger menu lists the application menu first', (tester) async {
    await _pumpShell(tester,
        align: DecorationsAlign.vleft,
        menuBar: _menuBar(withItems: true),
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
        menuBar: _menuBar(withItems: false),
        applicationMenu: _applicationMenu());

    expect(_appItem, findsOneWidget);
  });

  testWidgets('nothing is shown where there is no application menu', (tester) async {
    await _pumpShell(tester,
        align: DecorationsAlign.hleft, menuBar: _menuBar(withItems: false));

    expect(find.byType(HorizontalMenuBar), findsNothing);
    expect(_appItem, findsNothing);
  });
}
