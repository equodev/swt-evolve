// A Tree that only changes size is laid out again, not rebuilt, and neither are its rows: a sash
// drag resizes it on every step, and a rebuild built every row again. Its rows end at the width,
// and their cells where, a fresh mount at the new bounds puts them: with columns Java sized, or with
// no columns.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treecolumn.dart';
import 'package:swtflutter/src/gen/treeitem.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;

import 'delivery/support/deliver.dart';

VRectangle _rect(int x, int y, int width, int height) => VRectangle()
  ..x = x
  ..y = y
  ..width = width
  ..height = height;

enum _Columns { none, sized }

List<VTreeItem> _items(_Columns columns) => [
      for (var id = 10; id < 15; id++)
        VTreeItem()
          ..id = id
          ..seq = 1
          ..text = 'Item $id'
          ..texts = columns == _Columns.none ? ['Item $id'] : ['Item $id', 'Kind $id'],
    ];

List<VTreeColumn> _columns(_Columns columns) => switch (columns) {
      _Columns.none => [],
      _Columns.sized => [
          VTreeColumn()
            ..id = 650
            ..text = 'Name'
            ..width = 120,
          VTreeColumn()
            ..id = 651
            ..text = 'Kind'
            ..width = 90,
        ],
    };

VTree _tree(VRectangle bounds, _Columns columns, {int seq = 10}) => VTree()
  ..id = 601
  ..seq = seq
  ..style = SWT.H_SCROLL | SWT.V_SCROLL
  ..enabled = true
  ..visible = true
  ..headerVisible = false
  ..linesVisible = false
  ..columns = _columns(columns)
  ..items = _items(columns)
  ..bounds = bounds;

/// Mounts [tree] as the one child of a window-sized Composite, which lays it out by its bounds.
Future<void> _mount(WidgetTester tester, VTree tree) async {
  await tester.pumpWidget(const SizedBox());
  freshClient();
  final window = VComposite()
    ..id = 599
    ..style = SWT.NONE
    ..seq = 10
    ..enabled = true
    ..visible = true
    ..bounds = _rect(0, 0, 600, 300)
    ..children = [tree];
  await tester.pumpWidget(
    EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(width: 600, height: 300, child: CompositeSwt<VComposite>(value: window)),
      ),
    ),
  );
  await tester.pumpAndSettle();
}

/// Counts the rebuilds of the Tree and of its rows from here on.
({int Function() tree, int Function() rows}) _countBuilds() {
  var tree = 0, rows = 0;
  debugOnRebuildDirtyWidget = (element, _) {
    if (element.widget is TreeSwt) tree++;
    if (element.widget is TreeItemSwt) rows++;
  };
  addTearDown(() => debugOnRebuildDirtyWidget = null);
  return (tree: () => tree, rows: () => rows);
}

/// The Tree's size, each row's rectangle, and where each cell's text is.
List<Object> _geometry(WidgetTester tester, _Columns columns) => [
      tester.getSize(find.byType(TreeSwt<VTree>)),
      for (final row in find.byType(TreeItemSwt<VTreeItem>).evaluate()) tester.getRect(find.byWidget(row.widget)),
      for (var id = 10; id < 15; id++) ...[
        tester.getTopLeft(find.text('Item $id')),
        if (columns != _Columns.none) tester.getTopLeft(find.text('Kind $id')),
      ],
    ];

void main() {
  for (final columns in _Columns.values) {
    testWidgets('a wider Tree (${columns.name} columns) is laid out again, its rows not built again',
        (tester) async {
      await _mount(tester, _tree(_rect(0, 0, 420, 150), columns));
      final builds = _countBuilds();

      await deliverChange(_tree(_rect(0, 0, 540, 180), columns, seq: 11),
          keys: [WireKey.k_Control_bounds], base: 10);
      await tester.pumpAndSettle();

      expect(builds.tree(), 0);
      expect(builds.rows(), 0);
      final resized = _geometry(tester, columns);

      await _mount(tester, _tree(_rect(0, 0, 540, 180), columns));
      expect(resized, _geometry(tester, columns), reason: 'where a Tree mounted at those bounds lays it all out');
    });
  }
}
