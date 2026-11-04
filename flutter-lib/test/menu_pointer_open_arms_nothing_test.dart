// A context menu the pointer opened arms no item: the first Down arms the first item, the first Up
// the last, and Down Down Enter picks the second.

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

const _menuId = 4400;
const _ids = [4401, 4402, 4403];

VRectangle _rect(int w, int h) => VRectangle()
  ..x = 0
  ..y = 0
  ..width = w
  ..height = h;

VComposite _host() => VComposite()
  ..id = 4410
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
          ..style = SWT.PUSH
          ..enabled = true
          ..text = text,
    ]);

/// The items' Arms and Selections the client sent, in order, as `<event> <item id>`.
List<String> _itemEvents() => EquoCommService.commForTesting.sentFrames
    .map(EquoCommBase.decodeFrame)
    .map((f) => f.$1)
    .where((c) => c.startsWith('MenuItem/'))
    .map((c) => '${c.split('/').last} ${c.split('/')[1]}')
    .toList();

Future<void> _openByRightClick(WidgetTester tester) async {
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
        Positioned.fill(child: CompositeSwt<VComposite>(value: _host())),
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

  testWidgets('opening arms nothing; Down arms the first item, and Down Down Enter picks the second',
      (tester) async {
    await _openByRightClick(tester);
    expect(_itemEvents(), isEmpty, reason: 'no item is armed as the menu opens');

    await tester.sendKeyEvent(LogicalKeyboardKey.arrowDown);
    await tester.pumpAndSettle();
    expect(_itemEvents(), ['Arm ${_ids[0]}']);

    await tester.sendKeyEvent(LogicalKeyboardKey.arrowDown);
    await tester.sendKeyEvent(LogicalKeyboardKey.enter);
    await tester.pumpAndSettle();
    expect(_itemEvents(), ['Arm ${_ids[0]}', 'Arm ${_ids[1]}', 'Selection ${_ids[1]}']);
  });

  testWidgets('Up in a menu with no item armed arms the last one', (tester) async {
    await _openByRightClick(tester);

    await tester.sendKeyEvent(LogicalKeyboardKey.arrowUp);
    await tester.pumpAndSettle();

    expect(_itemEvents(), ['Arm ${_ids[2]}']);
    await tester.sendKeyEvent(LogicalKeyboardKey.escape);
    await tester.pumpAndSettle();
    expect(find.text('One'), findsNothing, reason: 'Escape closes the menu');
  });
}
