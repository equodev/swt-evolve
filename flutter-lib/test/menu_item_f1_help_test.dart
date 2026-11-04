// On Windows, F1 on an armed menu item closes the menu, then sends that item its Help, a cascade
// item included. Elsewhere F1 leaves the menu as it is.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/menu.dart';
import 'package:swtflutter/src/gen/menuitem.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';

import 'support/menu_shown_ack.dart';

const _menuId = 4600;
const _ids = [4601, 4602, 4603];

VRectangle _rect(int w, int h) => VRectangle()
  ..x = 0
  ..y = 0
  ..width = w
  ..height = h;

/// One, Two and Three; with [cascadeFirst], One is a cascade with a submenu of one item.
VComposite _host({bool cascadeFirst = false}) => VComposite()
  ..id = 4610
  ..style = SWT.NONE
  ..enabled = true
  ..bounds = _rect(300, 300)
  ..menu = (VMenu()
    ..id = _menuId
    ..style = SWT.POP_UP
    ..enabled = true
    ..visible = false
    ..items = [
      for (final (i, text) in ['One', 'Two', 'Three'].indexed)
        VMenuItem()
          ..id = _ids[i]
          ..style = cascadeFirst && i == 0 ? SWT.CASCADE : SWT.PUSH
          ..enabled = true
          ..text = text
          ..menu = cascadeFirst && i == 0
              ? (VMenu()
                ..id = 4620
                ..style = SWT.DROP_DOWN
                ..enabled = true
                ..items = [
                  VMenuItem()
                    ..id = 4621
                    ..style = SWT.PUSH
                    ..enabled = true
                    ..text = 'Sub',
                ])
              : null,
    ]);

/// The menu's and items' events the client sent, in order, as `<event> <id>`.
List<String> _menuEvents() => EquoCommService.commForTesting.sentFrames
    .map(EquoCommBase.decodeFrame)
    .map((f) => f.$1)
    .where((c) => c.startsWith('MenuItem/') || c.startsWith('Menu/$_menuId/Menu/'))
    .map((c) => '${c.split('/').last} ${c.split('/')[1]}')
    .toList();

/// The detail of the menu's Hide (null when it carries no event): 1 announces a picked item's
/// Selection to come.
int? _menuHideDetail() {
  final hide = EquoCommService.commForTesting.sentFrames
      .singleWhere((f) => EquoCommBase.decodeFrame(f).$1 == 'Menu/$_menuId/Menu/Hide');
  return EquoCommBase.decodeFrame(hide).$2.isEmpty ? null : EquoCommBase.decodeEvent(hide).detail;
}

Future<void> _openByRightClick(WidgetTester tester, {bool cascadeFirst = false}) async {
  EquoCommBase.recordSentFrames = true;
  addTearDown(() => EquoCommBase.recordSentFrames = false);
  EquoCommService.commForTesting.clearSentFrames();
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 300,
      height: 300,
      child: Stack(children: [
        Positioned.fill(child: DisplaySwt(value: VDisplay()..id = 6)),
        Positioned.fill(child: CompositeSwt<VComposite>(value: _host(cascadeFirst: cascadeFirst))),
      ]),
    ),
  ));
  await tester.pump();
  await tester.tapAt(const Offset(100, 100), buttons: kSecondaryButton);
  await tester.pump();
  await ackMenuShown(tester, _menuId);
  expect(find.text('One'), findsOneWidget, reason: 'the menu is open');
}

void main() {
  tearDown(() {
    VRegistry.instance.clear();
    EquoCommService.commForTesting.clearSentFrames();
  });

  Future<void> armFirstAndPressF1(WidgetTester tester) async {
    await tester.sendKeyEvent(LogicalKeyboardKey.arrowDown);
    await tester.pumpAndSettle();
    EquoCommService.commForTesting.clearSentFrames();
    await tester.sendKeyEvent(LogicalKeyboardKey.f1);
    await tester.pumpAndSettle();
  }

  testWidgets('F1 on the armed item closes the menu, then sends that item its Help', (tester) async {
    await _openByRightClick(tester);
    await armFirstAndPressF1(tester);

    expect(find.text('One'), findsNothing, reason: 'F1 closes the menu');
    expect(_menuEvents(), ['Hide $_menuId', 'Help ${_ids[0]}']);
    expect(_menuHideDetail(), isNot(1), reason: 'a Help is not a pick, so Java hides the menu at once');
  }, variant: TargetPlatformVariant.only(TargetPlatform.windows));

  testWidgets('F1 on an armed cascade item sends the cascade item its Help', (tester) async {
    await _openByRightClick(tester, cascadeFirst: true);
    await armFirstAndPressF1(tester);

    expect(find.text('Two'), findsNothing, reason: 'F1 closes the menu');
    expect(_menuEvents(), ['Hide $_menuId', 'Help ${_ids[0]}']);
  }, variant: TargetPlatformVariant.only(TargetPlatform.windows));

  testWidgets('F1 off Windows leaves the menu open and sends no Help', (tester) async {
    await _openByRightClick(tester);
    await armFirstAndPressF1(tester);

    expect(find.text('One'), findsOneWidget, reason: 'the menu stays open');
    expect(_menuEvents(), isEmpty);
  }, variant: TargetPlatformVariant.only(TargetPlatform.linux));
}
