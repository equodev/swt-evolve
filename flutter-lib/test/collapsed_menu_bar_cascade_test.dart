// The menu bar collapsed into the menu button (decorations_align=vleft) lists the bar's titles; each
// one is a cascade that opens its menu the way it does in the horizontal bar: on a click, on hover,
// and on ArrowRight once it is the current item.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/semantics.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/decorations.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/menu.dart';
import 'package:swtflutter/src/gen/menuitem.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/toolitem.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/decorations_align.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

VMenuItem _item(int id, String text, {int style = SWT.PUSH}) => VMenuItem()
  ..id = id
  ..style = style
  ..enabled = true
  ..text = text;

VMenuItem _cascade(int id, String text, int menuId, List<VMenuItem> items) =>
    _item(id, text, style: SWT.CASCADE)
      ..menu = (VMenu()
        ..id = menuId
        ..style = SWT.DROP_DOWN
        ..enabled = true
        ..items = items);

// A bar of cascades only, as an RCP application's main menu is.
VMenu _menuBar() => VMenu()
  ..id = 30
  ..style = SWT.BAR
  ..enabled = true
  ..items = [
    _cascade(31, '&File', 32, [_item(33, 'E&xit'), _item(34, '&Login again')]),
    _cascade(35, '&Help', 36, [_item(37, '&About')]),
  ];

VRectangle _rect(int width, int height) => VRectangle()
  ..x = 0
  ..y = 0
  ..width = width
  ..height = height;

Future<void> _openCollapsedBar(WidgetTester tester) async {
  setConfigFlags(ConfigFlags()..decorations_align = DecorationsAlign.vleft);
  addTearDown(resetConfigFlags);
  await tester.pumpWidget(
    EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 600,
        height: 500,
        child: Stack(children: [
          Positioned.fill(child: DisplaySwt(value: VDisplay()..id = 5)),
          Positioned.fill(
            child: DecorationsSwt<VDecorations>(
              value: VDecorations()
                ..id = 1
                ..style = SWT.NONE
                ..enabled = true
                ..visible = true
                ..bounds = _rect(600, 500)
                ..menuBar = _menuBar()
                ..children = [
                  VComposite()
                    ..id = 40
                    ..swt = 'MainToolbar'
                    ..style = SWT.NONE
                    ..enabled = true
                    ..visible = true
                    ..bounds = _rect(600, 40)
                    ..children = [
                      VToolBar()
                        ..id = 41
                        ..style = SWT.FLAT
                        ..enabled = true
                        ..visible = true
                        ..bounds = _rect(200, 40)
                        ..items = [
                          VToolItem()
                            ..id = 42
                            ..style = SWT.PUSH
                            ..enabled = true
                            ..text = 'Tool',
                        ],
                    ],
                ],
            ),
          ),
        ]),
      ),
    ),
  );
  await tester.pumpAndSettle();
}

Future<TestGesture> _mouse(WidgetTester tester) async {
  final mouse = await tester.createGesture(kind: PointerDeviceKind.mouse);
  await mouse.addPointer(location: Offset.zero);
  addTearDown(mouse.removePointer);
  return mouse;
}

Future<void> _click(WidgetTester tester, TestGesture mouse, Finder target) async {
  final at = tester.getCenter(target);
  await mouse.moveTo(at);
  await tester.pumpAndSettle();
  await mouse.down(at);
  await mouse.up();
  await tester.pumpAndSettle();
}

// The pointer arrives and presses before a frame runs, as a quick click does.
Future<void> _clickRightAway(WidgetTester tester, TestGesture mouse, Finder target) async {
  final at = tester.getCenter(target);
  await mouse.moveTo(at);
  await mouse.down(at);
  await mouse.up();
  await tester.pumpAndSettle();
}

void main() {
  testWidgets('a click on a title opens its menu', (tester) async {
    await _openCollapsedBar(tester);
    final mouse = await _mouse(tester);
    await _click(tester, mouse, find.byIcon(Icons.menu));
    expect(find.text('File'), findsOneWidget, reason: 'the collapsed bar must list its titles');

    await _clickRightAway(tester, mouse, find.text('File'));

    expect(find.text('Exit'), findsOneWidget, reason: "File's menu must open");
  });

  testWidgets('a click on a title its hover opened keeps the menu open', (tester) async {
    await _openCollapsedBar(tester);
    final mouse = await _mouse(tester);
    await _click(tester, mouse, find.byIcon(Icons.menu));

    await _click(tester, mouse, find.text('File'));

    expect(find.text('Exit'), findsOneWidget, reason: "the click must not close File's menu");
  });

  // With the semantics tree on, Flutter web turns a click on a button into a semantics tap.
  testWidgets('a semantics tap on a title its hover opened keeps the menu open', (tester) async {
    final semantics = tester.ensureSemantics();
    await _openCollapsedBar(tester);
    final mouse = await _mouse(tester);
    await _click(tester, mouse, find.byIcon(Icons.menu));
    await mouse.moveTo(tester.getCenter(find.text('File')));
    await tester.pumpAndSettle();
    expect(find.text('Exit'), findsOneWidget, reason: "File's menu must open on hover");

    tester.binding.pipelineOwner.semanticsOwner!
        .performAction(tester.getSemantics(find.text('File')).id, SemanticsAction.tap);
    await tester.pumpAndSettle();

    expect(find.text('Exit'), findsOneWidget, reason: "the tap must not close File's menu");
    semantics.dispose();
  });

  testWidgets('hovering a title opens its menu', (tester) async {
    await _openCollapsedBar(tester);
    final mouse = await _mouse(tester);
    await _click(tester, mouse, find.byIcon(Icons.menu));

    await mouse.moveTo(tester.getCenter(find.text('Help')));
    await tester.pumpAndSettle();

    expect(find.text('About'), findsOneWidget, reason: "Help's menu must open on hover");
  });

  testWidgets('ArrowRight on the current title opens its menu', (tester) async {
    await _openCollapsedBar(tester);
    final mouse = await _mouse(tester);
    await _click(tester, mouse, find.byIcon(Icons.menu));

    await tester.sendKeyEvent(LogicalKeyboardKey.arrowDown);
    await tester.pumpAndSettle();
    await tester.sendKeyEvent(LogicalKeyboardKey.arrowRight);
    await tester.pumpAndSettle();

    expect(find.text('Exit'), findsOneWidget, reason: "File's menu must open on ArrowRight");
  });
}
