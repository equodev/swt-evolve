// What Java sets on a Tree reaches the screen: the selection it changes after a click, the item it
// scrolls to the top, and the vertical position a re-layout must not lose.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treecolumn.dart';
import 'package:swtflutter/src/gen/treeitem.dart';
import 'package:swtflutter/src/impl/utils/widget_utils.dart' show SwtColorScope;

import 'delivery/support/deliver.dart';

const double _width = 300;
const double _height = 300;

VTreeItem _item(int id, String text, {int seq = 1, bool? expanded, List<VTreeItem>? items}) =>
    VTreeItem()
      ..id = id
      ..seq = seq
      ..text = text
      ..expanded = expanded
      ..items = items;

List<VTreeItem> _rows(int count, {int seq = 1}) =>
    [for (var i = 0; i < count; i++) _item(100 + i, 'item $i', seq: seq)];

VTree _tree(int id, List<VTreeItem> items,
        {int seq = 1, int style = SWT.MULTI | SWT.V_SCROLL, List<VTreeItem>? selection, VTreeItem? topItem}) =>
    VTree()
      ..id = id
      ..seq = seq
      ..style = style
      ..enabled = true
      ..items = items
      ..selection = selection
      ..topItem = topItem
      ..bounds = (VRectangle()
        ..x = 0
        ..y = 0
        ..width = _width.toInt()
        ..height = _height.toInt());

Future<void> _mount(WidgetTester tester, VTree value) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(width: _width, height: _height, child: TreeSwt<VTree>(value: value)),
  ));
  await tester.pumpAndSettle();
}

List<int> _selectedIds(VTree value) => (value.selection ?? []).map((item) => item.id).toList();

ScrollPosition _vertical(WidgetTester tester) {
  final scrollable = tester.widgetList<Scrollable>(find.byType(Scrollable)).firstWhere(
      (s) => s.axisDirection == AxisDirection.down || s.axisDirection == AxisDirection.up);
  return tester.state<ScrollableState>(find.byWidget(scrollable)).position;
}

double _rowHeight(WidgetTester tester) =>
    tester.getTopLeft(find.text('item 1')).dy - tester.getTopLeft(find.text('item 0')).dy;

// A second click within the double-click window is a DefaultSelection; the detector reads the
// wall clock, not the fake test clock.
Future<void> _leaveDoubleClickWindow(WidgetTester tester) =>
    tester.runAsync(() => Future<void>.delayed(const Duration(milliseconds: 400)));

void main() {
  group('selection', () {
    testWidgets('a selection Java sets after a click replaces the clicked one', (tester) async {
      final value = _tree(701, _rows(4));
      await _mount(tester, value);

      await tester.tap(find.text('item 0'));
      await tester.pump();
      expect(_selectedIds(value), [100]);

      final rows = _rows(4, seq: 10);
      await deliverChange(_tree(701, rows, seq: 10, selection: [rows[2]]),
          changed: ['selection'], base: 1);
      await tester.pumpAndSettle();

      expect(_selectedIds(value), [102],
          reason: 'Java owns the selection; what it sets has to be painted');
    });

    testWidgets('Java clearing the selection clears what a click selected', (tester) async {
      final value = _tree(702, _rows(4));
      await _mount(tester, value);

      await tester.tap(find.text('item 1'));
      await tester.pump();
      expect(_selectedIds(value), [101]);

      await deliverChange(_tree(702, _rows(4, seq: 10), seq: 10, selection: []),
          changed: ['selection'], base: 1);
      await tester.pumpAndSettle();

      expect(_selectedIds(value), isEmpty);
    });

    testWidgets('a delivery about something else keeps the clicked row selected', (tester) async {
      final value = _tree(703, _rows(4));
      await _mount(tester, value);

      await tester.tap(find.text('item 3'));
      await tester.pump();

      await deliverChange(_tree(703, _rows(4, seq: 10), seq: 10)..linesVisible = true,
          changed: ['linesVisible'], base: 1);
      await tester.pumpAndSettle();

      expect(_selectedIds(value), [103],
          reason: 'that delivery may have been written before Java heard of the click');
    });

    testWidgets('after Java confirms a click, its next selection is painted', (tester) async {
      final value = _tree(704, _rows(4));
      await _mount(tester, value);

      await tester.tap(find.text('item 0'));
      await tester.pump();
      var rows = _rows(4, seq: 10);
      await deliverChange(_tree(704, rows, seq: 10, selection: [rows[0]]),
          changed: ['selection'], base: 1);
      await tester.pumpAndSettle();
      await _leaveDoubleClickWindow(tester);

      rows = _rows(4, seq: 20);
      await deliverChange(_tree(704, rows, seq: 20, selection: [rows[1], rows[3]]),
          changed: ['selection'], base: 10);
      await tester.pumpAndSettle();

      expect(_selectedIds(value), [101, 103]);
    });
  });

  group('top item', () {
    testWidgets('the item Java puts at the top is the first row shown', (tester) async {
      await _mount(tester, _tree(711, _rows(100)));
      final rowHeight = _rowHeight(tester);

      final rows = _rows(100, seq: 10);
      await deliverChange(_tree(711, rows, seq: 10, topItem: rows[40]),
          changed: ['topItem'], base: 1);
      await tester.pumpAndSettle();

      expect(_vertical(tester).pixels, closeTo(40 * rowHeight, 0.5));
      expect(find.text('item 40'), findsOneWidget);
      expect(find.text('item 0'), findsNothing);
    });

    testWidgets('an item near the end stops the list at its last row', (tester) async {
      await _mount(tester, _tree(712, _rows(100)));

      final rows = _rows(100, seq: 10);
      await deliverChange(_tree(712, rows, seq: 10, topItem: rows[99]),
          changed: ['topItem'], base: 1);
      await tester.pumpAndSettle();

      final position = _vertical(tester);
      expect(position.pixels, position.maxScrollExtent);
      expect(find.text('item 99'), findsOneWidget);
    });

    testWidgets('a top item given before the tree is shown is honoured on first layout', (tester) async {
      final rows = _rows(100);
      await _mount(tester, _tree(713, rows, topItem: rows[30]));

      expect(find.text('item 30'), findsOneWidget);
      expect(find.text('item 0'), findsNothing);
    });

    testWidgets('a delivery that does not name the top item leaves the scroll alone', (tester) async {
      final rows = _rows(100);
      await _mount(tester, _tree(714, rows, topItem: rows[20]));
      final position = _vertical(tester);
      position.jumpTo(position.pixels + 150);
      await tester.pumpAndSettle();
      final scrolled = position.pixels;

      await deliverChange(_tree(714, _rows(100, seq: 10), seq: 10)..linesVisible = true,
          changed: ['linesVisible'], base: 1);
      await tester.pumpAndSettle();

      expect(_vertical(tester).pixels, scrolled);
    });
  });

  group('vertical position', () {
    testWidgets('a columnless tree that starts scrolling horizontally keeps its vertical position',
        (tester) async {
      final rows = _rows(60);
      await _mount(tester, _tree(721, rows, style: SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL));
      final position = _vertical(tester);
      position.jumpTo(5 * _rowHeight(tester));
      await tester.pumpAndSettle();
      final before = _vertical(tester).pixels;
      expect(before, greaterThan(0));

      // Opening an item whose child is wider than the tree turns the horizontal scroll on.
      await deliverChange(
          _item(106, 'item 6', seq: 10, expanded: true, items: [
            _item(900, 'a label far wider than the three hundred pixels this tree is given ' * 2,
                seq: 10),
          ]),
          changed: ['expanded', 'items'],
          base: 1);
      await tester.pumpAndSettle();

      expect(_vertical(tester).pixels, before,
          reason: 'expanding a node must not throw the tree back to its first row');
    });
  });

  group('header', () {
    VTreeColumn column(int id, String text, {int seq = 1}) => VTreeColumn()
      ..id = id
      ..seq = seq
      ..text = text
      ..width = 120;

    VTree sortedTree(int id, {int seq = 1, VTreeColumn? sortColumn, int? sortDirection, VColor? foreground}) =>
        _tree(id, const [], seq: seq)
          ..headerVisible = true
          ..columns = [column(id * 10 + 1, 'Name', seq: seq), column(id * 10 + 2, 'Size', seq: seq)]
          ..sortColumn = sortColumn
          ..sortDirection = sortDirection
          ..headerForeground = foreground
          ..bounds = (VRectangle()
            ..x = 0
            ..y = 0
            ..width = 800
            ..height = _height.toInt());

    Future<void> mountWide(WidgetTester tester, VTree value, {bool appColors = false}) async {
      final Widget tree = SizedBox(width: 800, height: _height, child: TreeSwt<VTree>(value: value));
      await tester.pumpWidget(EvolveApp(
        theme: ThemeMode.light,
        contentWidget: appColors ? SwtColorScope(child: tree) : tree,
      ));
      await tester.pumpAndSettle();
    }

    testWidgets('the sort column shows the direction the application set', (tester) async {
      await mountWide(tester, sortedTree(731));
      expect(find.byIcon(Icons.keyboard_arrow_up), findsNothing);
      expect(find.byIcon(Icons.keyboard_arrow_down), findsNothing);

      var next = sortedTree(731, seq: 10);
      next
        ..sortColumn = next.columns![1]
        ..sortDirection = SWT.UP;
      await deliverChange(next, changed: ['sortColumn', 'sortDirection'], base: 1);
      await tester.pumpAndSettle();
      expect(find.byIcon(Icons.keyboard_arrow_up), findsOneWidget);
      final arrow = tester.getCenter(find.byIcon(Icons.keyboard_arrow_up));
      expect(arrow.dx, greaterThan(tester.getTopLeft(find.textContaining(RegExp('size', caseSensitive: false))).dx),
          reason: 'the indicator belongs to the sort column');

      next = sortedTree(731, seq: 20);
      next
        ..sortColumn = next.columns![1]
        ..sortDirection = SWT.DOWN;
      await deliverChange(next, changed: ['sortDirection'], base: 10);
      await tester.pumpAndSettle();
      expect(find.byIcon(Icons.keyboard_arrow_down), findsOneWidget);
      expect(find.byIcon(Icons.keyboard_arrow_up), findsNothing);

      next = sortedTree(731, seq: 30)..sortDirection = SWT.NONE;
      await deliverChange(next, changed: ['sortDirection'], base: 20);
      await tester.pumpAndSettle();
      expect(find.byIcon(Icons.keyboard_arrow_down), findsNothing);
    });

    testWidgets('the header text takes the foreground the application set', (tester) async {
      final red = VColor()
        ..red = 200
        ..green = 10
        ..blue = 20
        ..alpha = 255;
      await mountWide(tester, sortedTree(732, foreground: red), appColors: true);

      final text = tester.widget<Text>(find.textContaining(RegExp('name', caseSensitive: false)));
      expect(text.style?.color, const Color.fromARGB(255, 200, 10, 20));
    });
  });
}
