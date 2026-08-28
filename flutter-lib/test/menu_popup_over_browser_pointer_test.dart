// Every menu popup must carry a PointerInterceptorScope over its panel.
//
// On web an SWT Browser is a real DOM <iframe> while menus are painted on Flutter's canvas, so the
// browser routes a click inside the iframe's rectangle to the iframe no matter what Flutter drew
// above it. The interceptor is the only thing that puts a DOM element of our own there; a popup
// without one is simply dead wherever it overlaps a Browser, and only there -- which is why this is
// asserted structurally instead of being left to a rendering test.
//
// The hamburger popup (decorations_align=vleft, which collapses the whole menu bar into one popup)
// was the call site that had none.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
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
import 'package:swtflutter/src/impl/utils/pointer.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

const double _viewportWidth = 800;
const double _viewportHeight = 400;

VRectangle _rect(int x, int y, int width, int height) => VRectangle()
  ..x = x
  ..y = y
  ..width = width
  ..height = height;

VMenu _dropDown(int id, String entry) => VMenu()
  ..id = id
  ..style = SWT.DROP_DOWN
  ..enabled = true
  ..items = [
    VMenuItem()
      ..id = id + 1
      ..style = SWT.PUSH
      ..enabled = true
      ..enabledEffective = true
      ..text = entry,
  ];

VMenu _menuBar() => VMenu()
  ..id = 200
  ..style = SWT.BAR
  ..enabled = true
  ..items = [
    VMenuItem()
      ..id = 201
      ..style = SWT.CASCADE
      ..enabled = true
      ..enabledEffective = true
      ..text = 'File'
      ..menu = _dropDown(300, 'Exit'),
  ];

VComposite _mainToolbar({required bool withMenuStrip}) => VComposite()
  ..id = 10
  ..swt = 'MainToolbar'
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = _rect(0, 0, _viewportWidth.toInt(), withMenuStrip ? 68 : 40)
  ..children = [
    VToolBar()
      ..id = 11
      ..style = SWT.FLAT
      ..enabled = true
      ..visible = true
      ..bounds = _rect(0, withMenuStrip ? 33 : 5, 200, 40)
      ..items = [
        VToolItem()
          ..id = 12
          ..style = SWT.PUSH
          ..enabled = true
          ..text = 'Save',
      ],
  ];

Future<void> _pumpShell(WidgetTester tester, DecorationsAlign align) async {
  setConfigFlags(ConfigFlags()..decorations_align = align);
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
          ..menuBar = _menuBar()
          ..children = [_mainToolbar(withMenuStrip: !align.isVertical)],
      ),
    ),
  ));
  await tester.pump();
}

/// The panel is only covered if the interceptor is an ANCESTOR of the entries -- a scope sitting
/// somewhere else in the overlay would leave the same hole the bug had.
void _expectEntryCovered(String entry) {
  expect(
    find.ancestor(
      of: find.text(entry),
      matching: find.byType(PointerInterceptorScope),
    ),
    findsOneWidget,
    reason: '"$entry" is drawn in a popup with no DOM element of its own, so a Browser '
        'underneath it takes the click instead',
  );
}

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  testWidgets('the hamburger popup covers its panel', (tester) async {
    await _pumpShell(tester, DecorationsAlign.vleft);

    await tester.tap(find.byIcon(Icons.menu));
    await tester.pumpAndSettle();

    _expectEntryCovered('File');
  });

  testWidgets('a cascade submenu covers its panel', (tester) async {
    await _pumpShell(tester, DecorationsAlign.hleft);

    await tester.tap(find.text('File'));
    await tester.pumpAndSettle();

    _expectEntryCovered('Exit');
  });
}
