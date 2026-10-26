// Holding Alt underlines the mnemonic character of every mnemonic-bearing control, and releasing it
// takes the underlines away again. macOS has no mnemonic underlines at all, so there Alt (Option)
// changes nothing.

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/group.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/link.dart';
import 'package:swtflutter/src/gen/menu.dart';
import 'package:swtflutter/src/gen/menuitem.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tabfolder.dart';
import 'package:swtflutter/src/gen/tabitem.dart';
// ignore: unused_import
import 'package:swtflutter/src/impl/ctabfolder_evolve.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';
// ignore: unused_import
import 'package:swtflutter/src/impl/tabfolder_evolve.dart';
import 'package:swtflutter/src/impl/utils/mnemonic_text.dart';
import 'package:swtflutter/src/impl/utils/text_utils.dart';

import 'support/menu_shown_ack.dart';

final _altCuePlatforms =
    TargetPlatformVariant(const {TargetPlatform.windows, TargetPlatform.linux});
final _windows = TargetPlatformVariant.only(TargetPlatform.windows);

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VButton _button(String text, {int style = SWT.PUSH}) => VButton()
  ..id = 1
  ..style = style
  ..enabled = true
  ..text = text;

VLabel _label(String text) => VLabel()
  ..id = 2
  ..style = SWT.NONE
  ..enabled = true
  ..text = text;

VGroup _group(String text) => VGroup()
  ..id = 3
  ..style = SWT.NONE
  ..enabled = true
  ..bounds = _rect(0, 0, 300, 100)
  ..text = text;

VTabFolder _tabFolder(String tabText) => VTabFolder()
  ..id = 4
  ..style = SWT.NONE
  // Disabled: an enabled selected tab trips a debug-only Container assertion in the harness.
  ..enabled = false
  ..bounds = _rect(0, 0, 300, 100)
  ..items = [
    VTabItem()
      ..id = 5
      ..text = tabText
      ..control = (VComposite()
        ..id = 6
        ..style = SWT.NONE
        ..bounds = _rect(0, 18, 300, 82)),
  ];

VCTabFolder _cTabFolder(String tabText) => VCTabFolder()
  ..swt = 'CTabFolder'
  ..id = 7
  ..style = SWT.NONE
  ..selection = 0
  ..enabled = true
  ..visible = true
  ..bounds = _rect(0, 0, 300, 100)
  ..items = [
    VCTabItem()
      ..swt = 'CTabItem'
      ..id = 8
      ..text = tabText,
  ];

VLink _link(String text) => VLink()
  ..id = 9
  ..style = SWT.NONE
  ..enabled = true
  ..text = text;

VMenuItem _menuItem(int id, String text, {int style = SWT.PUSH}) => VMenuItem()
  ..id = id
  ..style = style
  ..enabled = true
  ..text = text;

VMenu _menu(int style, List<VMenuItem> items) => VMenu()
  ..id = 10
  ..style = style
  ..enabled = true
  ..visible = style == SWT.POP_UP
  ..items = items;

VMenu _dropDown(int id, List<VMenuItem> items) => VMenu()
  ..id = id
  ..style = SWT.DROP_DOWN
  ..enabled = true
  ..items = items;

/// The menu with a Display under it, as a shell hosts one; a pop-up is shown, as Java acks it.
Future<void> _pumpMenu(WidgetTester tester, VMenu menu) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 600,
      height: 400,
      child: Stack(children: [
        Positioned.fill(child: DisplaySwt(value: VDisplay()..id = 5)),
        Positioned.fill(
          child: Align(alignment: Alignment.topLeft, child: MenuSwt<VMenu>(value: menu)),
        ),
      ]),
    ),
  ));
  await tester.pumpAndSettle();
  if (menu.style == SWT.POP_UP) await ackMenuShown(tester, menu.id);
}

Future<void> _pump(WidgetTester tester, Widget control) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(width: 300, height: 100, child: control),
  ));
  await tester.pumpAndSettle();
}

/// The characters drawn underlined across every `RichText` under [of].
String _underlined(WidgetTester tester, Finder of) {
  final out = StringBuffer();
  for (final rich in tester.widgetList<RichText>(find.descendant(of: of, matching: find.byType(RichText)))) {
    rich.text.visitChildren((span) {
      if (span is TextSpan &&
          span.text != null &&
          (span.style?.decoration?.contains(TextDecoration.underline) ?? false)) {
        out.write(span.text);
      }
      return true;
    });
  }
  return out.toString();
}

Future<void> _expectUnderlinedWhileAltHeld(
    WidgetTester tester, Finder control, String expected) async {
  expect(_underlined(tester, control), isEmpty, reason: 'no underline before Alt is pressed');

  await tester.sendKeyDownEvent(LogicalKeyboardKey.altLeft);
  await tester.pump();
  expect(_underlined(tester, control), expected, reason: 'Alt held underlines the mnemonic');

  await tester.sendKeyUpEvent(LogicalKeyboardKey.altLeft);
  await tester.pump();
  expect(_underlined(tester, control), isEmpty, reason: 'releasing Alt removes the underline');
}

void main() {
  group('parseMnemonic', () {
    test('marks the character after the first single ampersand', () {
      final m = parseMnemonic('&Print');
      expect(m.text, 'Print');
      expect(m.index, 0);
    });

    test('a double ampersand is a literal and no mnemonic', () {
      final m = parseMnemonic('Save && Close');
      expect(m.text, 'Save & Close');
      expect(m.index, -1);
    });

    test('the first un-escaped ampersand wins and the rest are stripped', () {
      final m = parseMnemonic('A&&B &Cut &Paste');
      expect(m.text, 'A&B Cut Paste');
      expect(m.index, 4);
    });

    test('a trailing ampersand marks nothing', () {
      final m = parseMnemonic('Done&');
      expect(m.text, 'Done');
      expect(m.index, -1);
    });
  });

  group('on Windows and Linux', () {
    testWidgets('Button underlines its mnemonic while Alt is held', (tester) async {
      await _pump(tester, ButtonSwt<VButton>(value: _button('&Print')));
      await _expectUnderlinedWhileAltHeld(tester, find.byType(ButtonSwt<VButton>), 'P');
    }, variant: _altCuePlatforms);

    testWidgets('a check Button underlines its mnemonic', (tester) async {
      await _pump(tester, ButtonSwt<VButton>(value: _button('E&nable', style: SWT.CHECK)));
      await _expectUnderlinedWhileAltHeld(tester, find.byType(ButtonSwt<VButton>), 'n');
    }, variant: _windows);

    testWidgets('Label underlines its mnemonic', (tester) async {
      await _pump(tester, LabelSwt<VLabel>(value: _label('&Name:')));
      await _expectUnderlinedWhileAltHeld(tester, find.byType(LabelSwt<VLabel>), 'N');
    }, variant: _windows);

    testWidgets('Group underlines its mnemonic', (tester) async {
      await _pump(tester, GroupSwt<VGroup>(value: _group('&Options')));
      await _expectUnderlinedWhileAltHeld(tester, find.byType(GroupSwt<VGroup>), 'O');
    }, variant: _windows);

    testWidgets('TabItem underlines its mnemonic', (tester) async {
      await _pump(tester, TabFolderSwt<VTabFolder>(value: _tabFolder('&General')));
      await _expectUnderlinedWhileAltHeld(tester, find.byType(TabFolderSwt<VTabFolder>), 'G');
    }, variant: _windows);

    testWidgets('CTabItem underlines its mnemonic', (tester) async {
      await _pump(tester, CTabFolderSwt<VCTabFolder>(value: _cTabFolder('&General')));
      await _expectUnderlinedWhileAltHeld(tester, find.byType(CTabFolderSwt<VCTabFolder>), 'G');
    }, variant: _windows);

    testWidgets('Link underlines its mnemonic', (tester) async {
      await _pump(tester, LinkSwt<VLink>(value: _link('&Docs')));
      await _expectUnderlinedWhileAltHeld(tester, find.byType(LinkSwt<VLink>), 'D');
    }, variant: _windows);

    testWidgets('a Link with links underlines the first mnemonic of its drawn text', (tester) async {
      await _pump(tester, LinkSwt<VLink>(value: _link('See <a href="a?x=1&y=2">&docs</a> or &help')));
      await _expectUnderlinedWhileAltHeld(tester, find.byType(LinkSwt<VLink>), 'd');
    }, variant: _windows);

    testWidgets('a Link underlines a mnemonic outside its links', (tester) async {
      await _pump(tester, LinkSwt<VLink>(value: _link('Open &settings or <a>docs</a>')));
      await _expectUnderlinedWhileAltHeld(tester, find.byType(LinkSwt<VLink>), 's');
    }, variant: _windows);

    testWidgets('a menu bar item underlines its mnemonic', (tester) async {
      await _pumpMenu(tester, _menu(SWT.BAR, [
        _menuItem(31, '&File', style: SWT.CASCADE)..menu = _dropDown(32, [_menuItem(33, '&New')]),
      ]));
      await _expectUnderlinedWhileAltHeld(tester, find.byType(MenuSwt<VMenu>), 'F');
    }, variant: _windows);

    testWidgets('a menu item underlines its mnemonic, not its shortcut', (tester) async {
      final split = splitMenuItemText('&Save\tCtrl+S');
      expect(split.label, '&Save');
      expect(split.shortcut, 'Ctrl+S');
      await _pumpMenu(tester, _menu(SWT.POP_UP, [_menuItem(11, '&Save\tCtrl+S')]));
      await _expectUnderlinedWhileAltHeld(tester, find.byType(MenuItemSwt<VMenuItem>), 'S');
    }, variant: _windows);

    testWidgets('a cascade menu item underlines its mnemonic', (tester) async {
      await _pumpMenu(tester, _menu(SWT.POP_UP, [
        _menuItem(11, 'Recen&t', style: SWT.CASCADE)..menu = _dropDown(20, [_menuItem(21, '&First')]),
      ]));
      await _expectUnderlinedWhileAltHeld(tester, find.byType(MenuItemSwt<VMenuItem>), 't');
    }, variant: _windows);

    testWidgets('a mnemonic text that appears while Alt is held is underlined at once', (tester) async {
      await tester.sendKeyDownEvent(LogicalKeyboardKey.altLeft);
      await _pump(tester, ButtonSwt<VButton>(value: _button('&Print')));

      expect(_underlined(tester, find.byType(ButtonSwt<VButton>)), 'P',
          reason: 'the first text to listen must read Alt as held, as Alt+F opening a menu needs');
      await tester.sendKeyUpEvent(LogicalKeyboardKey.altLeft);
    }, variant: _windows);

    testWidgets('text without a mnemonic does not listen to Alt', (tester) async {
      await _pump(tester, ButtonSwt<VButton>(value: _button('Save && Close')));
      expect(
        find.descendant(
            of: find.byType(MnemonicText),
            matching: find.byType(ValueListenableBuilder<bool>)),
        findsNothing,
      );
      await tester.sendKeyDownEvent(LogicalKeyboardKey.altLeft);
      await tester.pump();
      expect(_underlined(tester, find.byType(ButtonSwt<VButton>)), isEmpty);
      await tester.sendKeyUpEvent(LogicalKeyboardKey.altLeft);
    }, variant: _windows);
  });

  testWidgets('macOS draws no mnemonic underline, Option held or not', (tester) async {
    await _pump(tester, ButtonSwt<VButton>(value: _button('&Print')));
    await tester.sendKeyDownEvent(LogicalKeyboardKey.altLeft);
    await tester.pump();
    expect(_underlined(tester, find.byType(ButtonSwt<VButton>)), isEmpty);
    await tester.sendKeyUpEvent(LogicalKeyboardKey.altLeft);
  }, variant: TargetPlatformVariant.only(TargetPlatform.macOS));
}
