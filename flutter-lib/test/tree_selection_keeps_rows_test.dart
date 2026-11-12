// A new selection rebuilds only the rows it changes: the rows newly selected or unselected, and the
// row above each, which draws its edge against the next one. And a row whose text changed is still
// measured again by a Tree rebuilt for nothing but its selection.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;

import 'delivery/support/deliver.dart';

const double _width = 200;
const double _height = 300;

VTreeItem _item(int id, {int seq = 1, String? text}) => VTreeItem()
  ..id = id
  ..seq = seq
  ..text = text ?? 'Item $id'
  ..texts = [text ?? 'Item $id'];

final _items = [for (var id = 10; id < 15; id++) _item(id)];

VTree _tree({int seq = 1, List<VTreeItem> selection = const []}) => VTree()
  ..id = 1
  ..seq = seq
  ..style = SWT.H_SCROLL | SWT.V_SCROLL
  ..enabled = true
  ..headerVisible = false
  ..linesVisible = false
  ..columns = []
  ..items = _items
  ..selection = selection
  ..bounds = (VRectangle()
    ..width = _width.toInt()
    ..height = _height.toInt());

Future<void> _mount(WidgetTester tester) async {
  freshClient();
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(width: _width, height: _height, child: TreeSwt<VTree>(value: _tree())),
  ));
  await tester.pumpAndSettle();
}

double? _horizontalScrollExtent(WidgetTester tester) {
  for (final scrollable in tester.widgetList<Scrollable>(find.byType(Scrollable))) {
    if (scrollable.axisDirection == AxisDirection.right) {
      return tester.state<ScrollableState>(find.byWidget(scrollable)).position.maxScrollExtent;
    }
  }
  return null;
}

void main() {
  testWidgets('a new selection rebuilds only the rows it changes', (tester) async {
    await _mount(tester);
    final rebuilt = <int>{};
    debugOnRebuildDirtyWidget = (element, _) {
      final widget = element.widget;
      if (widget is TreeItemSwt) rebuilt.add(widget.value.id);
    };
    addTearDown(() => debugOnRebuildDirtyWidget = null);

    await deliverChange(_tree(seq: 2, selection: [_items[2]]), keys: [WireKey.k_Tree_selection], base: 1);
    await tester.pumpAndSettle();
    debugOnRebuildDirtyWidget = null;

    expect(rebuilt, {12, 11}, reason: 'the selected row, and the row above, which draws its edge against it');
  });

  testWidgets('a row whose text grew is measured again when only the selection follows',
      (tester) async {
    await _mount(tester);
    final narrow = _horizontalScrollExtent(tester) ?? 0;

    final long = 'Item 13 with a label far wider than the tree that shows it';
    await deliverChange(_item(13, seq: 2, text: long), keys: [WireKey.k_Item_text, WireKey.k_TreeItem_texts], base: 1);
    await tester.pumpAndSettle();
    await deliverChange(_tree(seq: 2, selection: [_items[0]]), keys: [WireKey.k_Tree_selection], base: 1);
    await tester.pumpAndSettle();

    expect(find.text(long), findsOneWidget);
    expect(_horizontalScrollExtent(tester), greaterThan(narrow));
  });
}
