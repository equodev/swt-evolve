// The menu bar driven from the keyboard, as on Windows: F10 or a lone Alt (resolved by Java) makes the
// bar active with its first title current, Left/Right move between titles and Down opens one. Every
// drop-down that opens sends its SWT.Show and every one that closes its SWT.Hide, however it opened.

import 'dart:convert';
import 'dart:typed_data';

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

const _displayId = 5;
const _barId = 30;
const _fileTitleId = 31;
const _fileMenuId = 32;
const _editTitleId = 41;
const _editMenuId = 42;

VMenuItem _item(int id, String text, {int style = SWT.PUSH}) => VMenuItem()
  ..id = id
  ..style = style
  ..enabled = true
  ..text = text;

VMenu _dropDown(int id, List<VMenuItem> items) => VMenu()
  ..id = id
  ..style = SWT.DROP_DOWN
  ..enabled = true
  ..items = items;

VMenu _menuBar() => VMenu()
  ..id = _barId
  ..style = SWT.BAR
  ..enabled = true
  ..items = [
    _item(_fileTitleId, '&File', style: SWT.CASCADE)
      ..menu = _dropDown(_fileMenuId, [_item(33, '&New'), _item(34, 'E&xit')]),
    _item(_editTitleId, '&Edit', style: SWT.CASCADE)
      ..menu = _dropDown(_editMenuId, [_item(43, '&Copy'), _item(44, '&Paste')]),
  ];

Widget _wrap() => EvolveApp(
  theme: ThemeMode.light,
  contentWidget: SizedBox(
    width: 600,
    height: 400,
    child: Stack(
      children: [
        Positioned.fill(child: DisplaySwt(value: VDisplay()..id = _displayId)),
        Positioned.fill(
          child: Align(alignment: Alignment.topLeft, child: MenuSwt<VMenu>(value: _menuBar())),
        ),
      ],
    ),
  ),
);

List<String> _sent(String suffix) => EquoCommService.commForTesting.sentFrames
    .map(EquoCommBase.decodeFrame)
    .map((f) => f.$1)
    .where((channel) => channel.endsWith(suffix))
    .toList();

Future<void> _fromJava(WidgetTester tester, String channel) async {
  final action = utf8.encode(channel);
  final body = utf8.encode('{}');
  final frame = Uint8List(2 + action.length + body.length);
  frame[0] = (action.length >> 8) & 0xFF;
  frame[1] = action.length & 0xFF;
  frame.setRange(2, 2 + action.length, action);
  frame.setRange(2 + action.length, frame.length, body);
  EquoCommService.commForTesting.receiveBinary(frame);
  await tester.pump();
  await tester.pumpAndSettle();
}

Future<void> _pumpMenuBar(WidgetTester tester) async {
  EquoCommBase.recordSentFrames = true;
  addTearDown(() => EquoCommBase.recordSentFrames = false);
  await tester.pumpWidget(_wrap());
  await tester.pumpAndSettle();
  EquoCommService.commForTesting.clearSentFrames();
}

/// Stands in for Java answering the drop-down's Show, when the client asked for one.
Future<void> _answerShow(WidgetTester tester, int menuId) async {
  if (_sent('Menu/$menuId/Menu/Show').isNotEmpty) await ackMenuShown(tester, menuId);
}

Future<void> _press(WidgetTester tester, LogicalKeyboardKey key) async {
  await tester.sendKeyEvent(key);
  await tester.pumpAndSettle();
}

void main() {
  group('Alt+letter on a title, as Java resolves it', () {
    testWidgets('sends the drop-down its Show, then opens it', (tester) async {
      await _pumpMenuBar(tester);

      await _fromJava(tester, 'MenuItem/$_fileTitleId/openMenu');
      await _answerShow(tester, _fileMenuId);

      expect(_sent('Menu/$_fileMenuId/Menu/Show'), hasLength(1));
      expect(find.text('New'), findsOneWidget, reason: 'the drop-down must open');
    });

    testWidgets('Escape closes the drop-down and sends its Hide', (tester) async {
      await _pumpMenuBar(tester);
      await _fromJava(tester, 'MenuItem/$_fileTitleId/openMenu');
      await _answerShow(tester, _fileMenuId);
      EquoCommService.commForTesting.clearSentFrames();

      await _press(tester, LogicalKeyboardKey.escape);

      expect(find.text('New'), findsNothing);
      expect(_sent('Menu/$_fileMenuId/Menu/Hide'), hasLength(1));
    });

    testWidgets('after Escape the bar stays active: Down opens the title again', (tester) async {
      await _pumpMenuBar(tester);
      await _fromJava(tester, 'MenuItem/$_fileTitleId/openMenu');
      await _answerShow(tester, _fileMenuId);
      await _press(tester, LogicalKeyboardKey.escape);
      EquoCommService.commForTesting.clearSentFrames();

      await _press(tester, LogicalKeyboardKey.arrowDown);
      await _answerShow(tester, _fileMenuId);

      expect(find.text('New'), findsOneWidget);
      expect(_sent('Display/$_displayId/Key/KeyDown'), isEmpty, reason: 'the bar takes the key');
    });
  });

  testWidgets('a click on a title sends its drop-down the Show', (tester) async {
    await _pumpMenuBar(tester);
    final mouse = await tester.createGesture(kind: PointerDeviceKind.mouse);
    await mouse.addPointer(location: Offset.zero);
    addTearDown(mouse.removePointer);
    await mouse.moveTo(tester.getCenter(find.text('File')));
    await mouse.down(tester.getCenter(find.text('File')));
    await mouse.up();
    await tester.pumpAndSettle();
    await _answerShow(tester, _fileMenuId);

    expect(_sent('Menu/$_fileMenuId/Menu/Show'), hasLength(1));
    expect(find.text('New'), findsOneWidget);
  });

  group('the bar activated by F10 or a lone Alt, as Java resolves it', () {
    testWidgets('Down opens the first title, with its first item current', (tester) async {
      await _pumpMenuBar(tester);
      await _fromJava(tester, 'Menu/$_barId/activate');

      await _press(tester, LogicalKeyboardKey.arrowDown);
      await _answerShow(tester, _fileMenuId);

      expect(_sent('Menu/$_fileMenuId/Menu/Show'), hasLength(1));
      expect(find.text('New'), findsOneWidget, reason: 'the first drop-down must open');
      await _press(tester, LogicalKeyboardKey.enter);
      expect(_sent('MenuItem/33/Selection/Selection'), hasLength(1));
    });

    testWidgets('Right moves to the next title, and Down opens that one', (tester) async {
      await _pumpMenuBar(tester);
      await _fromJava(tester, 'Menu/$_barId/activate');

      await _press(tester, LogicalKeyboardKey.arrowRight);
      await _press(tester, LogicalKeyboardKey.arrowDown);
      await _answerShow(tester, _editMenuId);

      expect(find.text('Copy'), findsOneWidget);
      expect(find.text('New'), findsNothing);
    });

    testWidgets('the keys the active bar takes do not reach Java', (tester) async {
      await _pumpMenuBar(tester);
      await _fromJava(tester, 'Menu/$_barId/activate');

      await _press(tester, LogicalKeyboardKey.arrowRight);
      await _press(tester, LogicalKeyboardKey.arrowLeft);
      await _press(tester, LogicalKeyboardKey.escape);

      expect(_sent('Display/$_displayId/Key/KeyDown'), isEmpty);
    });

    testWidgets('Escape leaves the bar: Down no longer opens a title', (tester) async {
      await _pumpMenuBar(tester);
      await _fromJava(tester, 'Menu/$_barId/activate');
      await _press(tester, LogicalKeyboardKey.escape);

      await _press(tester, LogicalKeyboardKey.arrowDown);
      await _answerShow(tester, _fileMenuId);

      expect(find.text('New'), findsNothing);
    });
  });
}
