// A right-click (secondary tap) on a tree item must select that item and open
// its context menu, matching native SWT: the platform menu-detect reads the
// current selection, so if the right-click leaves selection on a previously
// clicked item, the menu shows the wrong item's options.

import 'package:flutter/foundation.dart';
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

List<int> _selectedIds(VTree value) =>
    (value.selection ?? []).map((item) => item.id).toList();

void main() {
  final desktop = TargetPlatformVariant(<TargetPlatform>{
    TargetPlatform.macOS,
    TargetPlatform.linux,
    TargetPlatform.windows,
  });

  testWidgets(
      'right-click on an unselected item selects it and opens the context menu',
      (tester) async {
    final value = _tree(items: [_item(10, 'Node 0'), _item(11, 'Node 1')]);
    await tester.pumpWidget(_wrap(value));
    await tester.pump();

    // Select item A with a normal left click.
    await tester.tap(find.text('Node 0'));
    await tester.pump();
    expect(_selectedIds(value), equals([10]));

    // Right-click item B (currently unselected).
    await tester.tap(find.text('Node 1'), buttons: kSecondaryButton);
    await tester.pump();

    expect(_selectedIds(value), equals([11]),
        reason: 'right-click must move selection to the clicked item');
    expect(find.text('Menu Action'), findsOneWidget,
        reason: 'right-click must open the context menu');
  }, variant: desktop);
}
