// Java selecting an item out of view also scrolls it into view (setSelection shows the item): the
// row is on screen in the same frame as the selection, not one frame later.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;

import 'delivery/support/deliver.dart';

final _items = [
  for (var i = 0; i < 200; i++)
    VTreeItem()
      ..id = 100 + i
      ..seq = 1
      ..text = 'Row $i'
      ..items = [],
];

VTree _tree() => VTree()
  ..id = 1
  ..seq = 1
  ..style = SWT.SINGLE | SWT.V_SCROLL
  ..enabled = true
  ..visible = true
  ..items = _items
  ..bounds = (VRectangle()
    ..width = 300
    ..height = 300);

void main() {
  testWidgets('a selection Java scrolls to is shown in the frame that selects it', (tester) async {
    freshClient();
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(width: 300, height: 300, child: TreeSwt<VTree>(value: _tree())),
      ),
    ));
    await tester.pumpAndSettle();
    expect(find.text('Row 150'), findsNothing);

    final target = VTreeItem()..id = _items[150].id;
    await deliverChange(
        VTree()
          ..id = 1
          ..seq = 2
          ..selection = [target]
          ..topItem = target,
        keys: [WireKey.k_Tree_selection, WireKey.k_Tree_topItem],
        base: 1);
    await tester.pump();

    expect(find.text('Row 150'), findsOneWidget);
  });
}
