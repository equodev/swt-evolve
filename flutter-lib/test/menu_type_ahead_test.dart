// A letter typed in an open menu, measured against native SWT on each platform. Win32 treats an
// item's first letter as its mnemonic when no item carries the letter as an explicit `&` mnemonic:
// a letter several items share moves between them, a letter one item has activates it. GTK matches
// explicit mnemonics only. Cocoa type-selects: the typed letters accumulate, and the highlight goes
// to the first title in alphabetical order that starts with them, else to the last title before
// them, without activating anything.

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

const _menuId = 10;

VMenu _popup(List<String> labels) => VMenu()
  ..id = _menuId
  ..style = SWT.POP_UP
  ..enabled = true
  ..visible = true
  ..items = [
    for (final (i, label) in labels.indexed)
      VMenuItem()
        ..id = 100 + i
        ..style = SWT.PUSH
        ..enabled = true
        ..text = label,
  ];

Future<void> _openPopup(WidgetTester tester, List<String> labels) async {
  EquoCommBase.recordSentFrames = true;
  addTearDown(() => EquoCommBase.recordSentFrames = false);
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 600,
      height: 500,
      child: Stack(
        children: [
          Positioned.fill(child: DisplaySwt(value: VDisplay()..id = 5)),
          Positioned.fill(
            child: Align(alignment: Alignment.topLeft, child: MenuSwt<VMenu>(value: _popup(labels))),
          ),
        ],
      ),
    ),
  ));
  await tester.pumpAndSettle();
  await ackMenuShown(tester, _menuId);
  EquoCommService.commForTesting.clearSentFrames();
}

/// The labels of the items Java was told were selected, in order.
List<String> _selected(List<String> labels) => [
  for (final channel in EquoCommService.commForTesting.sentFrames
      .map(EquoCommBase.decodeFrame)
      .map((f) => f.$1)
      .where((channel) => channel.endsWith('/Selection/Selection')))
    labels[int.parse(channel.split('/')[1]) - 100],
];

Future<void> _type(WidgetTester tester, String letters) async {
  for (final letter in letters.split('')) {
    await tester.sendKeyEvent(
      LogicalKeyboardKey(LogicalKeyboardKey.keyA.keyId + letter.toLowerCase().codeUnitAt(0) - 0x61),
      character: letter,
    );
    await tester.pump();
  }
  await tester.pumpAndSettle();
}

Future<void> _enter(WidgetTester tester) async {
  await tester.sendKeyEvent(LogicalKeyboardKey.enter);
  await tester.pumpAndSettle();
}

const _plain = ['Alpha', 'Tango', 'Bravo', 'Tiger', 'tom'];
const _mixed = ['&Open', 'Tango', 'Tiger', '&Close', 'Te&st', 'Save'];

void main() {
  group('Windows', () {
    final windows = TargetPlatformVariant.only(TargetPlatform.windows);

    testWidgets('a first letter several items share cycles between them, wrapping', (tester) async {
      await _openPopup(tester, _plain);

      await _type(tester, 'tttt');
      expect(_selected(_plain), isEmpty, reason: 'a shared letter moves the highlight only');
      await _enter(tester);

      expect(_selected(_plain), ['Tango'], reason: 'Tango, Tiger, tom, then back to Tango');
    }, variant: windows);

    testWidgets('a first letter only one item has activates it', (tester) async {
      await _openPopup(tester, _plain);

      await _type(tester, 'b');

      expect(_selected(_plain), ['Bravo']);
      expect(find.text('Alpha'), findsNothing, reason: 'activating an item closes the menu');
    }, variant: windows);

    testWidgets('an explicit mnemonic wins over another item\'s first letter', (tester) async {
      await _openPopup(tester, _mixed);

      await _type(tester, 'tt');
      expect(_selected(_mixed), isEmpty);
      await _type(tester, 's');

      expect(_selected(_mixed), ['Te&st'], reason: '`s` is Te&st\'s mnemonic and Save\'s first letter');
    }, variant: windows);

    testWidgets('letters do not accumulate into a prefix', (tester) async {
      const labels = ['Tango', 'Tiger', 'Tim', 'Tom'];
      await _openPopup(tester, labels);

      await _type(tester, 'tim');
      await _enter(tester);

      expect(_selected(labels), ['Tango']);
    }, variant: windows);
  });

  testWidgets('GTK: a first letter that is no mnemonic does nothing', (tester) async {
    await _openPopup(tester, _plain);

    await _type(tester, 'tb');

    expect(_selected(_plain), isEmpty, reason: '`b` is Bravo\'s first letter, which Win32 would activate');
    expect(find.text('Alpha'), findsOneWidget);
  }, variant: TargetPlatformVariant.only(TargetPlatform.linux));

  group('macOS', () {
    final macOS = TargetPlatformVariant.only(TargetPlatform.macOS);

    testWidgets('a repeated letter searches the longer prefix, it does not cycle', (tester) async {
      await _openPopup(tester, _plain);

      await _type(tester, 'tt');
      await _enter(tester);

      expect(_selected(_plain), ['tom'], reason: 'no title starts with "tt"; tom is the last one before it');
    }, variant: macOS);

    testWidgets('an unmatched prefix lands on the last title before it in alphabetical order', (
      tester,
    ) async {
      await _openPopup(tester, _mixed);

      await _type(tester, 'tt');
      await _enter(tester);

      expect(_selected(_mixed), ['Tiger']);
    }, variant: macOS);

    testWidgets('a mnemonic letter only highlights', (tester) async {
      const labels = ['&Open', 'Tango', 'Tiger', '&Close', 'Save'];
      await _openPopup(tester, labels);

      await _type(tester, 'c');
      expect(_selected(labels), isEmpty, reason: 'Cocoa menus have no mnemonics to activate');
      await _enter(tester);

      expect(_selected(labels), ['&Close']);
    }, variant: macOS);
  });
}
