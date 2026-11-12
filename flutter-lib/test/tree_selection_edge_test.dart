// A selected row of a tree with columns closes the selection with its bottom edge unless the row
// shown below it is selected too, when the two are parted only by the row separator: the row below
// in the order the rows are shown, children of an open item included, whichever parent it is under.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treecolumn.dart';
import 'package:swtflutter/src/gen/treeitem.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;

import 'delivery/support/deliver.dart';

VTreeItem _item(int id, {int seq = 1, bool expanded = false, List<VTreeItem>? items}) => VTreeItem()
  ..id = id
  ..seq = seq
  ..text = 'Item $id'
  ..texts = ['Item $id', 'Detail $id']
  ..expanded = expanded
  ..items = items;

final _first = _item(1, expanded: true, items: [_item(11), _item(12)]);
final _second = _item(2, expanded: true, items: [_item(21), _item(22)]);

VTree _tree({int seq = 1, List<VTreeItem> selection = const []}) => VTree()
  ..id = 100
  ..seq = seq
  ..style = SWT.MULTI | SWT.V_SCROLL
  ..enabled = true
  ..headerVisible = false
  ..columns = [
    VTreeColumn()
      ..id = 201
      ..text = 'Name'
      ..width = 100,
    VTreeColumn()
      ..id = 202
      ..text = 'Detail'
      ..width = 100,
  ]
  ..items = [_first, _second]
  ..selection = selection
  ..bounds = (VRectangle()
    ..width = 200
    ..height = 300);

VTreeItem _byId(int id) =>
    [_first, ..._first.items!.cast<VTreeItem>(), _second, ..._second.items!.cast<VTreeItem>()]
        .firstWhere((item) => item.id == id);

Future<void> _select(WidgetTester tester, int seq, List<int> ids) async {
  await deliverChange(_tree(seq: seq, selection: [for (final id in ids) _byId(id)]),
      keys: [WireKey.k_Tree_selection], base: seq - 1);
  await tester.pumpAndSettle();
}

/// Whether the row of [id] ends on the row separator, as a selected row with a selected row below
/// does, rather than on the selection's edge.
bool _opensOntoNext(WidgetTester tester, int id) {
  final row = find.byWidgetPredicate((w) => w is TreeItemSwt && w.value.id == id);
  final box = tester.widget<Container>(find
      .descendant(
          of: row,
          matching: find.byWidgetPredicate((w) =>
              w is Container && w.decoration is BoxDecoration && (w.decoration as BoxDecoration).border is Border))
      .first);
  final border = (box.decoration as BoxDecoration).border as Border;
  expect(border.top, BorderSide.none, reason: 'the theme draws no selection edge, only the separator');
  return border.bottom != BorderSide.none;
}

void main() {
  testWidgets('the row below a selected row is found across parents, and again after one closes',
      (tester) async {
    freshClient();
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(width: 200, height: 300, child: TreeSwt<VTree>(value: _tree())),
    ));
    await tester.pumpAndSettle();

    await _select(tester, 2, [12, 2]);
    expect(_opensOntoNext(tester, 12), isTrue, reason: 'item 2 is the row below the last child of item 1');
    expect(_opensOntoNext(tester, 2), isFalse, reason: 'item 21 below it is not selected');

    await _select(tester, 3, [11, 21]);
    expect(_opensOntoNext(tester, 11), isFalse, reason: 'item 12 below it is not selected');

    await deliverChange(_item(1, seq: 2, items: _first.items!.cast<VTreeItem>()),
        keys: [WireKey.k_TreeItem_expanded], base: 1);
    await tester.pumpAndSettle();
    await _select(tester, 4, [1, 2, 21]);
    expect(_opensOntoNext(tester, 1), isTrue, reason: 'with item 1 closed, item 2 is the row below it');
    expect(_opensOntoNext(tester, 2), isTrue, reason: 'and item 2 has moved up to the second row');
  });
}
