// Verifies that a real drag gesture resolves to the correct target row/item — the position
// math in TableImpl#wrapTableForDrop / TreeImpl#wrapTreeForDrop that a prior regression fix touched
// most, and that DndFlutterTest (the Java-side suite) can't exercise: those tests synthesize
// itemId/index directly on a Java Event instead of performing an actual Flutter gesture, so
// they never run the Dart-side position resolution this test targets.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';

import 'package:swtflutter/src/impl/utils/dnd_utils.dart';

import 'delivery/support/deliver.dart';

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

/// A Table or Tree drop target draws on its hovered row/item whatever DND.FEEDBACK_* the drop
/// listener answered — the rendered signal for which row/item the position math resolved to.
bool _dropFeedbackOver(WidgetTester tester, Finder content) =>
    dropFeedbackAt(tester, tester.getCenter(content.first)) != null;

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

/// Scrolls the rows down by [pixels], as the user's wheel would.
void _scrollBy(WidgetTester tester, double pixels) {
  final vertical = find.byWidgetPredicate((w) => w is Scrollable && w.axisDirection == AxisDirection.down);
  tester.state<ScrollableState>(vertical.first).position.jumpTo(pixels);
}

Widget _wrap(Widget child) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(width: 400, height: 300, child: child),
    );

void main() {
  setUp(freshClient);

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
      await deliverEvent('DropTarget/1/Drop/negotiationResult',
          VEvent()..detail = Dnd.dropMove..feedback = Dnd.feedbackSelect);
      await tester.pump();

      expect(_dropFeedbackOver(tester, find.text('Row 2')), isTrue,
          reason: 'the row under the pointer must be highlighted');
      expect(_dropFeedbackOver(tester, find.text('Row 0')), isFalse);
      expect(_dropFeedbackOver(tester, find.text('Row 1')), isFalse);

      await gesture.up();
      await tester.pumpAndSettle();
      // Nothing answers the dragStart verdict here; let the fail-open timer run out.
      await tester.pump(const Duration(milliseconds: 400));
    },
  );

  testWidgets('a scrolled Table highlights the row under the pointer, not one scrolled away',
      (tester) async {
    await tester.pumpWidget(_wrap(TableSwt<VTable>(
        value: _table([for (var i = 0; i < 60; i++) _tableItem(10 + i, 'Row $i')]))));
    await tester.pump();
    _scrollBy(tester, 400);
    await tester.pump();

    final gesture = await _dragTo(tester, find.text('Row 22'), find.text('Row 24'));
    await deliverEvent('DropTarget/1/Drop/negotiationResult',
        VEvent()..detail = Dnd.dropMove..feedback = Dnd.feedbackSelect);
    await tester.pump();

    expect(_dropFeedbackOver(tester, find.text('Row 24')), isTrue);
    expect(_dropFeedbackOver(tester, find.text('Row 22')), isFalse);

    await gesture.up();
    await tester.pumpAndSettle();
    await tester.pump(const Duration(milliseconds: 400));
  });

  testWidgets('a scrolled Tree highlights the item under the pointer, not one scrolled away',
      (tester) async {
    await tester.pumpWidget(_wrap(TreeSwt<VTree>(
        value: _tree([for (var i = 0; i < 60; i++) _treeItem(100 + i, 'Item $i')]))));
    await tester.pump();
    _scrollBy(tester, 400);
    await tester.pump();

    final gesture = await _dragTo(tester, find.text('Item 16'), find.text('Item 18'));
    await deliverEvent('DropTarget/1/Drop/negotiationResult',
        VEvent()..detail = Dnd.dropMove..feedback = Dnd.feedbackSelect);
    await tester.pump();

    expect(_dropFeedbackOver(tester, find.text('Item 18')), isTrue);
    expect(_dropFeedbackOver(tester, find.text('Item 16')), isFalse);

    await gesture.up();
    await tester.pumpAndSettle();
    await tester.pump(const Duration(milliseconds: 400));
  });

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
      await deliverEvent('DropTarget/1/Drop/negotiationResult',
          VEvent()..detail = Dnd.dropMove..feedback = Dnd.feedbackSelect);
      await tester.pump();

      expect(_dropFeedbackOver(tester, find.text('Child B')), isTrue,
          reason: 'the nested item under the pointer must be highlighted, not its parent');
      expect(_dropFeedbackOver(tester, find.text('Parent')), isFalse);
      expect(_dropFeedbackOver(tester, find.text('Child A')), isFalse);
      expect(_dropFeedbackOver(tester, find.text('Other leaf')), isFalse);

      await gesture.up();
      await tester.pumpAndSettle();
      // Nothing answers the dragStart verdict here; let the fail-open timer run out.
      await tester.pump(const Duration(milliseconds: 400));
    },
  );
}
