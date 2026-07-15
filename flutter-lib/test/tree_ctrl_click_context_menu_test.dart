// On macOS, Ctrl+Click is a secondary click: it must open the control's
// context menu and keep the clicked item selected, instead of toggling
// multi-selection (issue #727). Cmd+Click remains the toggle modifier.

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
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

Future<void> _modifierClick(
  WidgetTester tester,
  Finder target,
  LogicalKeyboardKey modifier,
) async {
  await tester.sendKeyDownEvent(modifier);
  await tester.tap(target);
  await tester.sendKeyUpEvent(modifier);
  await tester.pump();
}

void main() {
  final macOS = TargetPlatformVariant.only(TargetPlatform.macOS);

  testWidgets('macOS Ctrl+Click on an unselected item selects it and opens '
      'the context menu', (tester) async {
    final value = _tree(items: [_item(10, 'Node 0'), _item(11, 'Node 1')]);
    await tester.pumpWidget(_wrap(value));
    await tester.pump();

    await _modifierClick(
        tester, find.text('Node 1'), LogicalKeyboardKey.controlLeft);
    await tester.pump();

    expect(_selectedIds(value), equals([11]));
    expect(find.text('Menu Action'), findsOneWidget,
        reason: 'Ctrl+Click must open the context menu on macOS');
  }, variant: macOS);

  testWidgets('macOS Ctrl+Click on a selected item keeps the selection '
      'instead of toggling it off', (tester) async {
    final value = _tree(items: [_item(10, 'Node 0'), _item(11, 'Node 1')]);
    await tester.pumpWidget(_wrap(value));
    await tester.pump();

    await tester.tap(find.text('Node 0'));
    await tester.pump();
    expect(_selectedIds(value), equals([10]));

    await _modifierClick(
        tester, find.text('Node 0'), LogicalKeyboardKey.controlLeft);
    await tester.pump();

    expect(_selectedIds(value), equals([10]),
        reason: 'Ctrl+Click must not deselect the item on macOS');
    expect(find.text('Menu Action'), findsOneWidget);
  }, variant: macOS);

  testWidgets('macOS Cmd+Click still toggles multi-selection and does not '
      'open the context menu', (tester) async {
    // Non-adjacent items: selecting neighbours trips an unrelated
    // hairline-border debug assertion in the selected-row painting.
    final value = _tree(
        items: [_item(10, 'Node 0'), _item(11, 'Node 1'), _item(12, 'Node 2')]);
    await tester.pumpWidget(_wrap(value));
    await tester.pump();

    await tester.tap(find.text('Node 0'));
    await tester.pump();

    await _modifierClick(
        tester, find.text('Node 2'), LogicalKeyboardKey.metaLeft);
    await tester.pump();

    expect(_selectedIds(value), equals([10, 12]));
    expect(find.text('Menu Action'), findsNothing);

    // Space the second click out in real time so it doesn't register as a
    // double-click (DoubleTapDetector compares DateTime.now(), not the fake
    // test clock).
    await tester.runAsync(
        () => Future<void>.delayed(const Duration(milliseconds: 400)));
    await _modifierClick(
        tester, find.text('Node 2'), LogicalKeyboardKey.metaLeft);
    await tester.pump();

    expect(_selectedIds(value), equals([10]));
    expect(find.text('Menu Action'), findsNothing);
  }, variant: macOS);
}
