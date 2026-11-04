// A pop-up owns the keyboard from the moment it shows. The client opens a right-click menu only once
// Java has answered its Show, so a key pressed in between must wait for the menu rather than reach
// the focused control: Escape closes the menu and only its release reaches the Shell, Enter picks the
// first item.

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
import 'package:swtflutter/src/impl/menu_evolve.dart';

import 'support/menu_shown_ack.dart';

const _displayId = 5;
const _menuId = 4100;
const _firstId = 4101;

VRectangle _rect(int w, int h) => VRectangle()
  ..x = 0
  ..y = 0
  ..width = w
  ..height = h;

VComposite _host() => VComposite()
  ..id = 4001
  ..style = SWT.NONE
  ..enabled = true
  ..bounds = _rect(300, 300)
  ..menu = (VMenu()
    ..id = _menuId
    ..style = SWT.POP_UP
    ..enabled = true
    ..visible = false
    ..items = [
      VMenuItem()
        ..id = _firstId
        ..style = SWT.PUSH
        ..enabled = true
        ..text = 'First',
      VMenuItem()
        ..id = _firstId + 1
        ..style = SWT.PUSH
        ..enabled = true
        ..text = 'Second',
    ]);

Widget _wrap() => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 300,
        height: 300,
        child: Stack(children: [
          Positioned.fill(child: DisplaySwt(value: VDisplay()..id = _displayId)),
          Positioned.fill(child: CompositeSwt<VComposite>(value: _host())),
        ]),
      ),
    );

var _from = 0;

/// The channels sent since the right-click.
List<String> _sent() => EquoCommService.commForTesting.sentFrames
    .skip(_from)
    .map(EquoCommBase.decodeFrame)
    .map((f) => f.$1)
    .toList();

Future<void> _rightClickWithoutAnswer(WidgetTester tester) async {
  EquoCommBase.recordSentFrames = true;
  addTearDown(() => EquoCommBase.recordSentFrames = false);
  await tester.pumpWidget(_wrap());
  await tester.pump();
  await tester.tapAt(const Offset(150, 150), buttons: kSecondaryButton);
  await tester.pump();
  _from = EquoCommService.commForTesting.sentFrames.length;
}

void main() {
  tearDown(VRegistry.instance.clear);

  testWidgets('Escape before Java answers the Show closes the menu once it opens', (tester) async {
    await _rightClickWithoutAnswer(tester);

    await tester.sendKeyEvent(LogicalKeyboardKey.escape);
    await tester.pump();
    expect(_sent().where((c) => c.startsWith('Display/$_displayId/Key/')), isEmpty,
        reason: 'the key waits for the menu instead of reaching the focused control');

    await ackMenuShown(tester, _menuId);

    expect(find.text('First'), findsNothing, reason: 'Escape closed the menu');
    final sent = _sent();
    expect(sent.where((c) => c.endsWith('/Hide')), hasLength(1));
    expect(sent.where((c) => c == 'Display/$_displayId/Key/KeyDown'), isEmpty,
        reason: 'the menu took the press');
    expect(sent.where((c) => c == 'Display/$_displayId/Key/KeyUp'), hasLength(1),
        reason: 'the release comes after the menu is down, so it reaches the Shell');
    expect(sent.indexWhere((c) => c.endsWith('/Hide')),
        lessThan(sent.indexOf('Display/$_displayId/Key/KeyUp')));
  });

  // The menu is open a frame before its first item takes the focus; a key in that frame still waits
  // for the menu.
  testWidgets('Escape in the frame between the menu opening and its focus closes the menu', (tester) async {
    await _rightClickWithoutAnswer(tester);
    deliverMenuShown(_menuId);
    await tester.idle();
    await tester.pump();
    expect(OpenMenus.any && OpenMenus.opening, isTrue, reason: 'the menu is open and does not have the focus yet');

    await tester.sendKeyEvent(LogicalKeyboardKey.escape);
    await tester.pumpAndSettle();

    expect(find.text('First'), findsNothing, reason: 'Escape closed the menu');
    final sent = _sent();
    expect(sent.where((c) => c.endsWith('/Hide')), hasLength(1));
    expect(sent.where((c) => c == 'Display/$_displayId/Key/KeyDown'), isEmpty, reason: 'the menu took the press');
    expect(sent.where((c) => c == 'Display/$_displayId/Key/KeyUp'), hasLength(1),
        reason: 'the release comes after the menu is down, so it reaches the Shell');
  });

  // Held keys are played to the menu one a frame. A key typed meanwhile comes after them, as it was
  // typed: the release of an Enter whose press still waits reaches the Shell once that press has
  // picked the item and closed the menu.
  testWidgets('a key typed while held keys are still being played waits behind them', (tester) async {
    await _rightClickWithoutAnswer(tester);
    // The menu opens with no item armed: the first Down arms First, the second the next.
    await tester.sendKeyEvent(LogicalKeyboardKey.arrowDown);
    await tester.sendKeyEvent(LogicalKeyboardKey.arrowDown);
    await tester.sendKeyDownEvent(LogicalKeyboardKey.enter);
    deliverMenuShown(_menuId);
    await tester.idle();
    await tester.pump();
    await tester.pump();
    expect(find.text('First'), findsOneWidget, reason: 'the held Enter has not been played yet');

    await tester.sendKeyUpEvent(LogicalKeyboardKey.enter);
    await tester.pumpAndSettle();

    final sent = _sent();
    expect(sent.where((c) => c.endsWith('/Selection')), ['MenuItem/${_firstId + 1}/Selection/Selection']);
    expect(sent.where((c) => c == 'Display/$_displayId/Key/KeyUp'), hasLength(1),
        reason: "Enter's release reaches the Shell after the menu is down");
    expect(sent.indexWhere((c) => c.endsWith('/Hide')), lessThan(sent.indexOf('Display/$_displayId/Key/KeyUp')));
  });

  // Every keyboard handler reads each key of one key message before the focused menu reads any of
  // them: a release in the same message as the press that closes the menu finds the menu still open.
  testWidgets('Escape pressed and released in one key message closes the menu, and the release reaches the Shell',
      (tester) async {
    await _rightClickWithoutAnswer(tester);
    await ackMenuShown(tester, _menuId);
    await tester.pumpAndSettle();
    expect(find.text('First'), findsOneWidget, reason: 'the menu is open');
    _from = EquoCommService.commForTesting.sentFrames.length;

    const down = KeyDownEvent(
        physicalKey: PhysicalKeyboardKey.escape, logicalKey: LogicalKeyboardKey.escape, timeStamp: Duration.zero);
    const up = KeyUpEvent(
        physicalKey: PhysicalKeyboardKey.escape, logicalKey: LogicalKeyboardKey.escape, timeStamp: Duration.zero);
    // As KeyEventManager hands over one message: each key to every handler, then all of them to the focus.
    HardwareKeyboard.instance.handleKeyEvent(down);
    HardwareKeyboard.instance.handleKeyEvent(up);
    ServicesBinding.instance.keyEventManager.keyMessageHandler!(KeyMessage(const [down, up], null));
    await tester.pumpAndSettle();

    expect(find.text('First'), findsNothing, reason: 'Escape closed the menu');
    final sent = _sent();
    expect(sent.where((c) => c.endsWith('/Hide')), hasLength(1));
    expect(sent.where((c) => c == 'Display/$_displayId/Key/KeyDown'), isEmpty, reason: 'the menu took the press');
    expect(sent.where((c) => c == 'Display/$_displayId/Key/KeyUp'), hasLength(1),
        reason: 'the release comes after the menu is down, so it reaches the Shell');
    expect(sent.indexWhere((c) => c.endsWith('/Hide')), lessThan(sent.indexOf('Display/$_displayId/Key/KeyUp')));
  });

  testWidgets("a release read while the menu stays open is the menu's", (tester) async {
    await _rightClickWithoutAnswer(tester);
    await ackMenuShown(tester, _menuId);
    await tester.pumpAndSettle();
    _from = EquoCommService.commForTesting.sentFrames.length;

    await tester.sendKeyEvent(LogicalKeyboardKey.arrowDown);
    await tester.pumpAndSettle();

    expect(find.text('First'), findsOneWidget, reason: 'the menu is still open');
    expect(_sent().where((c) => c.startsWith('Display/$_displayId/Key/')), isEmpty,
        reason: 'neither the press nor the release reaches the Shell');
  });

  testWidgets('Down and Enter before Java answers the Show pick the second item', (tester) async {
    await _rightClickWithoutAnswer(tester);

    // The menu opens with no item armed: the first Down arms First, the second the next.
    await tester.sendKeyEvent(LogicalKeyboardKey.arrowDown);
    await tester.sendKeyEvent(LogicalKeyboardKey.arrowDown);
    await tester.sendKeyEvent(LogicalKeyboardKey.enter);
    await tester.pump();
    expect(_sent().where((c) => c.startsWith('Display/$_displayId/Key/')), isEmpty);

    await ackMenuShown(tester, _menuId);
    await tester.pumpAndSettle();

    final sent = _sent();
    expect(sent.where((c) => c.endsWith('/Selection')), ['MenuItem/${_firstId + 1}/Selection/Selection']);
    expect(find.text('First'), findsNothing, reason: 'picking an item closes the menu');
  });
}
