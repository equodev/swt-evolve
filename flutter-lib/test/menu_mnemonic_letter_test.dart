// In an open menu, typing an item's mnemonic letter is the platform's menu loop at work. On Windows
// and GTK the letter alone activates the item -- or opens a cascade and moves into it -- and a letter
// several items share only moves the current item between them. Cocoa strips mnemonics, and its
// menus select the item whose title starts with what was typed instead.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/tagged_values.dart';
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

import 'support/menu_shown_ack.dart';
import 'package:swtflutter/src/comm/channel_header.dart';
import 'support/channels.dart' as channels;

const _displayId = 5;
const _newId = 11;
const _openId = 12;
const _saveId = 13;
const _saveAsId = 14;
const _recentId = 15;
const _disabledId = 16;
const _firstId = 21;
const _secondId = 22;

VMenuItem _item(int id, String text, {int style = SWT.PUSH, bool enabled = true}) => VMenuItem()
  ..id = id
  ..style = style
  ..enabled = enabled
  ..text = text;

List<VMenuItem> _fileItems() => [
  _item(_newId, '&New\tCtrl+N'),
  _item(_openId, '&Open...'),
  _item(_saveId, '&Save'),
  _item(_saveAsId, 'Save &As...'),
  _item(17, 'Sea&rch', style: SWT.CHECK),
  _item(_disabledId, '&Print', enabled: false),
  _item(18, 'P&roperties'),
  _item(_recentId, 'Recen&t', style: SWT.CASCADE)
    ..menu = (VMenu()
      ..id = 20
      ..style = SWT.DROP_DOWN
      ..enabled = true
      ..items = [_item(_firstId, '&First'), _item(_secondId, '&Second')]),
];

VMenu _popup() => VMenu()
  ..id = 10
  ..style = SWT.POP_UP
  ..enabled = true
  ..visible = true
  ..items = _fileItems();

VMenu _menuBar() => VMenu()
  ..id = 30
  ..style = SWT.BAR
  ..enabled = true
  ..items = [
    _item(31, '&File', style: SWT.CASCADE)
      ..menu = (VMenu()
        ..id = 32
        ..style = SWT.DROP_DOWN
        ..enabled = true
        ..items = _fileItems()),
  ];

Widget _wrap(VMenu menu) => EvolveApp(
  theme: ThemeMode.light,
  contentWidget: SizedBox(
    width: 600,
    height: 500,
    child: Stack(
      children: [
        Positioned.fill(child: DisplaySwt(value: VDisplay()..id = _displayId)),
        Positioned.fill(
          child: Align(
            alignment: Alignment.topLeft,
            child: MenuSwt<VMenu>(value: menu),
          ),
        ),
      ],
    ),
  ),
);

List<String> _sent(String event) => EquoCommService.commForTesting.sentFrames
    .map(EquoCommBase.decodeFrame)
    .map((f) => f.$1)
    .where((channel) => channel.endsWith('/$event'))
    .toList();

List<int> _selections() => [
  for (final channel in _sent('Selection/Selection')) int.parse(channel.split('/')[1]),
];

Future<void> _openPopup(WidgetTester tester) async {
  EquoCommBase.recordSentFrames = true;
  addTearDown(() => EquoCommBase.recordSentFrames = false);
  await tester.pumpWidget(_wrap(_popup()));
  await tester.pumpAndSettle();
  await ackMenuShown(tester, 10);
  expect(find.text('Open...'), findsOneWidget, reason: 'the menu must be open');
  EquoCommService.commForTesting.clearSentFrames();
}

/// Stands in for Java: `MnemonicHelper` resolved Alt+letter to the menu bar title [itemId].
Future<void> _javaOpensMenuBarMenu(WidgetTester tester, int itemId) async {
  final body = TaggedValues.encode(const <String, Object?>{});
  EquoCommService.commForTesting.receiveBinary(channels.frame('MenuItem/$itemId/openMenu', body));
  await tester.pump();
  await tester.pumpAndSettle();
}

Future<void> _pumpMenuBar(WidgetTester tester) async {
  EquoCommBase.recordSentFrames = true;
  addTearDown(() => EquoCommBase.recordSentFrames = false);
  await tester.pumpWidget(_wrap(_menuBar()));
  await tester.pumpAndSettle();
  EquoCommService.commForTesting.clearSentFrames();
}

Future<void> _openMenuBarDropDown(WidgetTester tester) async {
  EquoCommBase.recordSentFrames = true;
  addTearDown(() => EquoCommBase.recordSentFrames = false);
  await tester.pumpWidget(_wrap(_menuBar()));
  await tester.pumpAndSettle();
  final mouse = await tester.createGesture(kind: PointerDeviceKind.mouse);
  await mouse.addPointer(location: Offset.zero);
  addTearDown(mouse.removePointer);
  await mouse.moveTo(tester.getCenter(find.text('File')));
  await mouse.down(tester.getCenter(find.text('File')));
  await mouse.up();
  await tester.pumpAndSettle();
  await ackMenuShown(tester, 32);
  expect(find.text('Open...'), findsOneWidget, reason: 'the drop-down must be open');
  EquoCommService.commForTesting.clearSentFrames();
}

VRectangle _rect(int width, int height) => VRectangle()
  ..x = 0
  ..y = 0
  ..width = width
  ..height = height;

/// The menu bar collapsed into the main toolbar's hamburger, as decorations_align=vleft draws it.
Future<void> _openHamburger(WidgetTester tester) async {
  await _pumpHamburger(tester);
  await tester.tap(find.byIcon(Icons.menu));
  await tester.pumpAndSettle();
  expect(find.text('File'), findsOneWidget, reason: 'the hamburger must be open');
  EquoCommService.commForTesting.clearSentFrames();
}

Future<void> _pumpHamburger(WidgetTester tester) async {
  EquoCommBase.recordSentFrames = true;
  addTearDown(() => EquoCommBase.recordSentFrames = false);
  setConfigFlags(ConfigFlags()..decorations_align = DecorationsAlign.vleft);
  addTearDown(resetConfigFlags);
  await tester.pumpWidget(
    EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 600,
        height: 500,
        child: Stack(children: [
        Positioned.fill(child: DisplaySwt(value: VDisplay()..id = _displayId)),
        Positioned.fill(child: DecorationsSwt<VDecorations>(
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
        )),
        ]),
      ),
    ),
  );
  await tester.pumpAndSettle();
}

Future<void> _type(WidgetTester tester, String character, LogicalKeyboardKey key) async {
  await tester.sendKeyEvent(key, character: character);
  await tester.pumpAndSettle();
}

Future<void> _press(WidgetTester tester, LogicalKeyboardKey key) async {
  await tester.sendKeyEvent(key);
  await tester.pumpAndSettle();
}

final _letterActivates = TargetPlatformVariant({TargetPlatform.windows, TargetPlatform.linux});
final _typeSelects = TargetPlatformVariant.only(TargetPlatform.macOS);

void main() {
  group('Windows and GTK', () {
    testWidgets('the mnemonic letter activates its item and closes the menu', (tester) async {
      await _openPopup(tester);

      await _type(tester, 'n', LogicalKeyboardKey.keyN);

      expect(_selections(), [_newId]);
      expect(find.text('Open...'), findsNothing, reason: 'activating an item closes the menu');
    }, variant: _letterActivates);

    testWidgets('the letter matches regardless of case', (tester) async {
      await _openPopup(tester);

      await _type(tester, 'O', LogicalKeyboardKey.keyO);

      expect(_selections(), [_openId]);
    }, variant: _letterActivates);

    testWidgets('a letter several items share cycles between them without activating', (
      tester,
    ) async {
      await _openPopup(tester);

      // `r` marks Sea&rch and P&roperties: the first press moves to one, the second to the other.
      await _type(tester, 'r', LogicalKeyboardKey.keyR);
      expect(_selections(), isEmpty, reason: 'a shared letter selects, it does not activate');
      await _type(tester, 'r', LogicalKeyboardKey.keyR);
      await _press(tester, LogicalKeyboardKey.enter);

      expect(_selections(), [18], reason: 'the second press must move on to P&roperties');
    }, variant: _letterActivates);

    testWidgets('a disabled item is skipped', (tester) async {
      await _openPopup(tester);

      await _type(tester, 'p', LogicalKeyboardKey.keyP);

      expect(_selections(), isEmpty);
      expect(
        find.text('Open...'),
        findsOneWidget,
        reason: 'no enabled item has `p`: the menu stays',
      );
    }, variant: _letterActivates);

    testWidgets('a letter only an unmarked character carries does nothing', (tester) async {
      await _openPopup(tester);

      // `e` appears in New, Open, Save but marks nothing.
      await _type(tester, 'e', LogicalKeyboardKey.keyE);

      expect(_selections(), isEmpty);
      expect(find.text('Open...'), findsOneWidget);
    }, variant: _letterActivates);

    testWidgets(
      'a cascade opens on its letter, and the letter of its item activates that item',
      (tester) async {
        await _openPopup(tester);

        await _type(tester, 't', LogicalKeyboardKey.keyT);
        expect(find.text('Second'), findsOneWidget, reason: 'the cascade must open');
        expect(_selections(), isEmpty);

        await _type(tester, 's', LogicalKeyboardKey.keyS);

        expect(_selections(), [
          _secondId,
        ], reason: "the submenu's own &Second wins over the parent's &Save");
      },
      variant: _letterActivates,
    );

    testWidgets('a cascade opened by its letter puts the keyboard on its first item', (
      tester,
    ) async {
      await _openPopup(tester);

      await _type(tester, 't', LogicalKeyboardKey.keyT);
      await _press(tester, LogicalKeyboardKey.enter);

      expect(_selections(), [_firstId]);
    }, variant: _letterActivates);

    testWidgets('the letter works in a menu bar drop-down opened with the mouse', (tester) async {
      await _openMenuBarDropDown(tester);

      await _type(tester, 'a', LogicalKeyboardKey.keyA);

      expect(_selections(), [_saveAsId]);
    }, variant: _letterActivates);

    testWidgets(
      'in the hamburger, the letter of a menu bar title opens it and the next letter picks its item',
      (tester) async {
        await _openHamburger(tester);

        await _type(tester, 'f', LogicalKeyboardKey.keyF);
        expect(find.text('Open...'), findsOneWidget, reason: '`f` must open File');
        await _type(tester, 'n', LogicalKeyboardKey.keyN);

        expect(_selections(), [_newId]);
      },
      variant: _letterActivates,
    );

    testWidgets('a letter typed in the open hamburger is not also sent to Java', (tester) async {
      await _openHamburger(tester);

      await _type(tester, 'f', LogicalKeyboardKey.keyF);

      expect(_sent('Key/KeyDown'), isEmpty, reason: 'an open menu owns the keyboard');
    }, variant: _letterActivates);
  });

  group('Alt+letter on a menu bar title, as Java resolves it', () {
    testWidgets('opens the drop-down with its first item current', (tester) async {
      await _pumpMenuBar(tester);

      await _javaOpensMenuBarMenu(tester, 31);
      await ackMenuShown(tester, 32);
      expect(find.text('Open...'), findsOneWidget, reason: 'the drop-down must open');
      await _press(tester, LogicalKeyboardKey.enter);

      expect(_selections(), [_newId]);
    });

    testWidgets('opens the collapsed menu bar and that title in it', (tester) async {
      EquoCommBase.recordSentFrames = true;
      addTearDown(() => EquoCommBase.recordSentFrames = false);
      await _pumpHamburger(tester);
      EquoCommService.commForTesting.clearSentFrames();

      await _javaOpensMenuBarMenu(tester, 31);
      expect(find.text('Open...'), findsOneWidget, reason: "File's menu must open in the hamburger");
      await _type(tester, 'n', LogicalKeyboardKey.keyN);

      expect(_selections(), [_newId]);
    }, variant: _letterActivates);
  });

  testWidgets('Escape closes a drop-down opened with the mouse', (tester) async {
    await _openMenuBarDropDown(tester);

    await _press(tester, LogicalKeyboardKey.escape);

    expect(find.text('Open...'), findsNothing);
  });

  group('macOS', () {
    testWidgets(
      'a letter selects the item whose title starts with it, without activating',
      (tester) async {
        await _openPopup(tester);

        await _type(tester, 'p', LogicalKeyboardKey.keyP);
        expect(_selections(), isEmpty, reason: 'Cocoa menus have no mnemonics to activate');
        expect(find.text('Open...'), findsOneWidget);

        await _press(tester, LogicalKeyboardKey.enter);
        expect(_selections(), [18], reason: 'type-select skips the disabled Print for Properties');
      },
      variant: _typeSelects,
    );

    testWidgets('typed letters accumulate into a prefix', (tester) async {
      await _openPopup(tester);

      await _type(tester, 's', LogicalKeyboardKey.keyS);
      await _type(tester, 'e', LogicalKeyboardKey.keyE);
      await _press(tester, LogicalKeyboardKey.enter);

      expect(_selections(), [17], reason: '"se" leads to Search, past Save');
    }, variant: _typeSelects);
  });
}
