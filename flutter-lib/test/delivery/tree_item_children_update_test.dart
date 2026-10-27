// A Tree draws its rows from one flattened list of every visible item, so an item's own channel
// changing what is under it (expanding, gaining children) is a change the Tree has to redraw for.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';

import 'support/deliver.dart';

const int _treeId = 700;

VTreeItem _item(int id, String text, {required int seq, bool? expanded, List<VTreeItem>? items}) =>
    VTreeItem()
      ..id = id
      ..seq = seq
      ..text = text
      ..expanded = expanded
      ..items = items;

VTree _tree(List<VTreeItem> items, {required int seq}) => VTree()
  ..id = _treeId
  ..seq = seq
  ..style = SWT.NONE
  ..items = items
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 300
    ..height = 400);

Future<void> _mount(WidgetTester tester, VTree value) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(width: 300, height: 400, child: TreeSwt<VTree>(value: value)),
  ));
  await tester.pumpAndSettle();
}

void main() {
  testWidgets('children an item gains on its own channel are drawn under it', (tester) async {
    // A lazily populated category: collapsed, with the placeholder child that gives it an expander.
    await _mount(
        tester,
        _tree([
          _item(1, 'Tutorial Examples', seq: 1, expanded: false, items: [_item(10, '', seq: 1)]),
          _item(2, 'Classic Examples', seq: 1),
        ], seq: 1));
    expect(find.text('Data'), findsNothing);

    // Expanding it replaces the placeholder with the real children; only the item is described.
    await deliverChange(
        _item(1, 'Tutorial Examples', seq: 10, expanded: true, items: [
          _item(11, 'Data', seq: 10),
          _item(12, 'Configuration', seq: 10),
        ]),
        changed: ['expanded', 'items'],
        base: 1);
    await tester.pumpAndSettle();

    expect(find.text('Data'), findsOneWidget,
        reason: 'the Tree draws every row, so it is what has to redraw when an item gains children');
    expect(find.text('Configuration'), findsOneWidget);
  });
}
