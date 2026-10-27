// An open menu owns the keyboard: the arrow keys move through it and its cascades, Enter activates
// an item, and none of it reaches the control that had the focus behind the menu.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/menu.dart';
import 'package:swtflutter/src/gen/menuitem.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';

import 'support/menu_shown_ack.dart';

const _openId = 11;
const _moreId = 12;
const _firstId = 21;
const _secondId = 22;
const _displayId = 5;

VMenuItem _item(int id, String text, {int style = SWT.PUSH}) => VMenuItem()
  ..id = id
  ..style = style
  ..enabled = true
  ..text = text;

VMenu _menu() => VMenu()
  ..id = 10
  ..style = SWT.POP_UP
  ..enabled = true
  ..visible = true
  ..items = [
    _item(_openId, 'Open...'),
    _item(_moreId, 'More', style: SWT.CASCADE)
      ..menu = (VMenu()
        ..id = 20
        ..style = SWT.DROP_DOWN
        ..enabled = true
        ..items = [_item(_firstId, 'First'), _item(_secondId, 'Second')]),
  ];

Widget _wrap() => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 600,
        height: 400,
        child: Stack(
          children: [
            Positioned.fill(child: DisplaySwt(value: VDisplay()..id = _displayId)),
            Positioned.fill(child: MenuSwt<VMenu>(value: _menu())),
          ],
        ),
      ),
    );

List<String> _sent(String event) => EquoCommService.commForTesting.sentFrames
    .map(EquoCommBase.decodeFrame)
    .map((f) => f.$1)
    .where((channel) => channel.endsWith('/$event'))
    .toList();

bool _selected(int id) =>
    _sent('Selection/Selection').any((channel) => channel.contains('/$id/'));

Future<void> _open(WidgetTester tester) async {
  EquoCommBase.recordSentFrames = true;
  addTearDown(() => EquoCommBase.recordSentFrames = false);
  await tester.pumpWidget(_wrap());
  await tester.pumpAndSettle();
  await ackMenuShown(tester, 10);
  expect(find.text('More'), findsOneWidget, reason: 'the menu must be open');
  EquoCommService.commForTesting.clearSentFrames();
}

Future<void> _press(WidgetTester tester, LogicalKeyboardKey key) async {
  await tester.sendKeyEvent(key);
  await tester.pumpAndSettle();
}

Future<void> _openCascadeByHover(WidgetTester tester) async {
  final mouse = await tester.createGesture(kind: PointerDeviceKind.mouse);
  await mouse.addPointer(location: Offset.zero);
  addTearDown(mouse.removePointer);
  await mouse.moveTo(tester.getCenter(find.text('More')));
  await tester.pumpAndSettle();
  expect(find.text('First'), findsOneWidget, reason: 'hover must open the cascade');
}

void main() {
  testWidgets('ArrowDown and Enter in a hover-opened cascade activate its item', (tester) async {
    await _open(tester);
    await _openCascadeByHover(tester);

    await _press(tester, LogicalKeyboardKey.arrowDown);
    await _press(tester, LogicalKeyboardKey.enter);

    expect(_selected(_firstId), isTrue,
        reason: 'sent: ${_sent('Selection/Selection')}; focus: ${FocusManager.instance.primaryFocus}');
  });

  testWidgets('ArrowRight opens the cascade and moves into it', (tester) async {
    await _open(tester);

    await _press(tester, LogicalKeyboardKey.arrowDown);
    await _press(tester, LogicalKeyboardKey.arrowRight);
    expect(find.text('First'), findsOneWidget, reason: 'ArrowRight must open the cascade');

    await _press(tester, LogicalKeyboardKey.arrowDown);
    await _press(tester, LogicalKeyboardKey.enter);

    expect(_selected(_secondId), isTrue,
        reason: 'sent: ${_sent('Selection/Selection')}; focus: ${FocusManager.instance.primaryFocus}');
  });

  testWidgets('ArrowUp in a hover-opened cascade starts from its last item', (tester) async {
    await _open(tester);
    await _openCascadeByHover(tester);

    await _press(tester, LogicalKeyboardKey.arrowUp);
    await _press(tester, LogicalKeyboardKey.enter);

    expect(_selected(_secondId), isTrue,
        reason: 'sent: ${_sent('Selection/Selection')}; focus: ${FocusManager.instance.primaryFocus}');
  });

  testWidgets('keys pressed in an open cascade are not forwarded to the focused control', (tester) async {
    await _open(tester);
    await _openCascadeByHover(tester);

    await _press(tester, LogicalKeyboardKey.arrowDown);
    await _press(tester, LogicalKeyboardKey.arrowRight);
    expect(_sent('Key/KeyDown'), isEmpty, reason: 'the open menu must keep every key');

    await _press(tester, LogicalKeyboardKey.enter);
    expect(find.text('More'), findsNothing, reason: 'Enter must close the menu');
    await _press(tester, LogicalKeyboardKey.arrowDown);
    expect(_sent('Key/KeyDown'), ['Display/$_displayId/Key/KeyDown'],
        reason: 'with the menu closed, keys reach the focused control again');
  });
}
