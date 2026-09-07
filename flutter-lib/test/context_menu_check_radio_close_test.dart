// Selecting a CHECK or RADIO item in a context menu must close the menu, the same as a PUSH
// item -- native menus dismiss on any item selection, and the selection itself is only flushed
// to Java once the menu closes (see MenuChangeNotifier.registerPendingChange /
// _sendPendingChanges). A CHECK/RADIO tap that doesn't close the menu leaves the selection
// stuck pending until an unrelated interaction happens to dismiss it.

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

import 'support/menu_shown_ack.dart';

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
      ..style = SWT.CHECK
      ..enabled = true
      ..text = 'Enable Feature',
    VMenuItem()
      ..id = 102
      ..style = SWT.RADIO
      ..enabled = true
      ..text = 'Option A',
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
  await ackMenuShown(tester, 100);
}

void main() {
  testWidgets('tapping a CHECK menu item closes the menu', (tester) async {
    final value = _tree(items: [_item(10, 'Node 0')]);
    await tester.pumpWidget(_wrap(value));
    await tester.pump();

    await _rightClick(tester, find.text('Node 0'));
    expect(find.text('Enable Feature'), findsOneWidget,
        reason: 'right-click must open the context menu');

    await tester.tap(find.text('Enable Feature'));
    await tester.pump();
    await tester.pump();
    await tester.pumpAndSettle();

    expect(find.text('Enable Feature'), findsNothing,
        reason: 'selecting a CHECK item must close the menu, same as PUSH');
    expect(find.text('Option A'), findsNothing,
        reason: 'the whole menu must be gone, not just the tapped item');
  });

  testWidgets('tapping a RADIO menu item closes the menu', (tester) async {
    final value = _tree(items: [_item(10, 'Node 0')]);
    await tester.pumpWidget(_wrap(value));
    await tester.pump();

    await _rightClick(tester, find.text('Node 0'));
    expect(find.text('Option A'), findsOneWidget,
        reason: 'right-click must open the context menu');

    await tester.tap(find.text('Option A'));
    await tester.pump();
    await tester.pump();
    await tester.pumpAndSettle();

    expect(find.text('Option A'), findsNothing,
        reason: 'selecting a RADIO item must close the menu, same as PUSH');
    expect(find.text('Enable Feature'), findsNothing,
        reason: 'the whole menu must be gone, not just the tapped item');
  });
}
