// Verifies that a real drag gesture resolves to the correct target row/item — the position
// math in TableImpl#wrapTableForDrop / TreeImpl#wrapTreeForDrop that issue #755's fix touched
// most, and that DndFlutterTest (the Java-side suite) can't exercise: those tests synthesize
// itemId/index directly on a Java Event instead of performing an actual Flutter gesture, so
// they never run the Dart-side position resolution this test targets.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';

/// Starts a drag on [source] and moves incrementally to [target]'s center, pumping between
/// steps — Draggable needs to cross its touch-slop threshold and DragTarget needs a frame to
/// update its hover state before either behaves like a real drag. Returns the still-active
/// gesture so the caller can inspect mid-drag state before calling `up()`.
Future<TestGesture> _dragTo(
  WidgetTester tester,
  Finder source,
  Finder target,
) async {
  final start = tester.getCenter(source);
  final end = tester.getCenter(target);
  final gesture = await tester.startGesture(start);
  await tester.pump(const Duration(milliseconds: 50));
  const steps = 6;
  for (var i = 1; i <= steps; i++) {
    await gesture.moveTo(Offset.lerp(start, end, i / steps)!);
    await tester.pump(const Duration(milliseconds: 20));
  }
  return gesture;
}

/// The blue top-border decoration every DND drop target wraps its hovered row/item in (see
/// TableImpl#wrapCellForRowDrag, TreeItemImpl#_wrapItemForDrag) — the public, already-rendered
/// signal for "which row/item does the position math currently resolve to", used here instead
/// of reading any private hover field.
bool _isHoverDecoration(Widget widget) {
  if (widget is! DecoratedBox) return false;
  final decoration = widget.decoration;
  return decoration is BoxDecoration && decoration.border?.top.color == Colors.blue;
}

Finder _hoverDecorationOver(Finder content) =>
    find.ancestor(of: content, matching: find.byWidgetPredicate(_isHoverDecoration));

VTableColumn _col(int id, {int? width}) => VTableColumn()
  ..id = id
  ..width = width
  ..text = 'Col$id';

VTableItem _tableItem(int id, String text) => VTableItem()
  ..id = id
  ..texts = [text];

VTable _table(List<VTableItem> items) => VTable()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..headerVisible = false
  ..linesVisible = false
  ..dragSource = true
  ..dropTargetId = 1
  ..columns = [_col(1, width: 400)]
  ..items = items
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 400
    ..height = 300);

VTreeItem _treeItem(int id, String text, {List<VTreeItem>? items}) => VTreeItem()
  ..id = id
  ..text = text
  ..expanded = items != null
  ..items = items;

VTree _tree(List<VTreeItem> items) => VTree()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..dragSource = true
  ..dropTargetId = 1
  ..items = items
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 400
    ..height = 300);

Widget _wrap(Widget child) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(width: 400, height: 300, child: child),
    );

void main() {
  testWidgets(
    'dragging a Table row onto another row highlights that row, not the one it started on',
    (tester) async {
      final items = [
        _tableItem(10, 'Row 0'),
        _tableItem(11, 'Row 1'),
        _tableItem(12, 'Row 2'),
      ];
      await tester.pumpWidget(_wrap(TableSwt<VTable>(value: _table(items))));
      await tester.pump();

      final gesture = await _dragTo(tester, find.text('Row 0'), find.text('Row 2'));
      await tester.pump();

      expect(_hoverDecorationOver(find.text('Row 2')), findsOneWidget,
          reason: 'the row under the pointer must be highlighted');
      expect(_hoverDecorationOver(find.text('Row 0')), findsNothing);
      expect(_hoverDecorationOver(find.text('Row 1')), findsNothing);

      await gesture.up();
      await tester.pumpAndSettle();
    },
  );

  testWidgets(
    'dragging a Tree item onto a nested item highlights that exact item, not its parent',
    (tester) async {
      final tree = _tree([
        _treeItem(100, 'Parent', items: [
          _treeItem(101, 'Child A'),
          _treeItem(102, 'Child B'),
        ]),
        _treeItem(200, 'Other leaf'),
      ]);
      await tester.pumpWidget(_wrap(TreeSwt<VTree>(value: tree)));
      await tester.pump();

      final gesture = await _dragTo(tester, find.text('Other leaf'), find.text('Child B'));
      await tester.pump();

      expect(_hoverDecorationOver(find.text('Child B')), findsOneWidget,
          reason: 'the nested item under the pointer must be highlighted, not its parent');
      expect(_hoverDecorationOver(find.text('Parent')), findsNothing);
      expect(_hoverDecorationOver(find.text('Child A')), findsNothing);
      expect(_hoverDecorationOver(find.text('Other leaf')), findsNothing);

      await gesture.up();
      await tester.pumpAndSettle();
    },
  );
}
