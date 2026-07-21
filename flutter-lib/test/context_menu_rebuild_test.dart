// A right-click context menu is opened imperatively from Dart, so state.visible
// stays false — a state push while it is open (in Eclipse/RCP the SWT.Show
// listeners always repopulate the menu's items, pushing a fresh VMenu) rebuilds
// the anchor and must not be mistaken for a Java-driven setVisible(false) close.
// Once dismissed, later rebuilds must not reopen it either.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/menu.dart';
import 'package:swtflutter/src/gen/menuitem.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';

VTreeItem _item(int id, String text) => VTreeItem()
  ..id = id
  ..text = text;

VMenu _menu() => VMenu()
  ..id = 100
  ..style = SWT.POP_UP
  ..enabled = true
  ..items = [
    VMenuItem()
      ..id = 101
      ..style = SWT.PUSH
      ..enabled = true
      ..text = 'Menu Action',
  ];

VTree _tree({required List<VTreeItem> items}) => VTree()
  ..id = 1
  ..style = SWT.MULTI
  ..enabled = true
  ..items = items
  ..menu = _menu()
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 800
    ..height = 400);

Widget _wrap(VTree value) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 800,
        height: 400,
        child: TreeSwt<VTree>(value: value),
      ),
    );

Future<void> _rightClick(WidgetTester tester, Finder target) async {
  await tester.tap(target, buttons: kSecondaryButton);
  await tester.pump();
  await tester.pump();
}

void main() {
  testWidgets('a right-click context menu survives the state push that '
      'follows SWT.Show', (tester) async {
    final value = _tree(items: [_item(10, 'Node 0'), _item(11, 'Node 1')]);
    await tester.pumpWidget(_wrap(value));
    await tester.pump();

    await _rightClick(tester, find.text('Node 1'));
    expect(find.text('Menu Action'), findsOneWidget,
        reason: 'right-click must open the context menu');

    // A fresh VMenu instance, as deserialization produces: visible is unset,
    // exactly like the push RCP's Show listeners trigger while the menu is open.
    value.menu = _menu();
    await tester.pumpWidget(_wrap(value));
    await tester.pump();
    await tester.pump();

    expect(find.text('Menu Action'), findsOneWidget,
        reason: 'a state push while the menu is open must not close it');
  });

  testWidgets('a dismissed context menu stays closed across rebuilds',
      (tester) async {
    final value = _tree(items: [_item(10, 'Node 0'), _item(11, 'Node 1')]);
    await tester.pumpWidget(_wrap(value));
    await tester.pump();

    await _rightClick(tester, find.text('Node 1'));
    expect(find.text('Menu Action'), findsOneWidget);

    // Dismiss by clicking empty tree area outside the overlay.
    await tester.tapAt(const Offset(780, 390));
    await tester.pump();
    await tester.pump();
    expect(find.text('Menu Action'), findsNothing,
        reason: 'clicking outside must dismiss the menu');

    value.menu = _menu();
    await tester.pumpWidget(_wrap(value));
    await tester.pump();
    await tester.pump();

    expect(find.text('Menu Action'), findsNothing,
        reason: 'a later rebuild must not reopen a dismissed menu');
  });
}
