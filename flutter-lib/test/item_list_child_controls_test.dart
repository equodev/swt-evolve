// A Tree or a Table draws the controls it holds besides its items at their own bounds: a TreeCursor or a
// TableCursor over the cell it covers, or any control an application put over the rows, as the child
// views a native list holds. A cursor itself is a plain Canvas that paints from its SWT.Paint listener.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tablecursor.dart';
import 'package:swtflutter/src/gen/tableeditor.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treecursor.dart';
import 'package:swtflutter/src/gen/treeeditor.dart';
import 'package:swtflutter/src/gen/treeitem.dart';
import 'package:swtflutter/src/impl/canvas_evolve.dart';

const double _width = 400;
const double _height = 300;

VRectangle _rect(int x, int y, int width, int height) => VRectangle()
  ..x = x
  ..y = y
  ..width = width
  ..height = height;

VTreeCursor _treeCursor(int id, VRectangle bounds) => VTreeCursor()
  ..id = id
  ..enabled = true
  ..visible = true
  ..bounds = bounds;

VTableCursor _tableCursor(int id, VRectangle bounds) => VTableCursor()
  ..id = id
  ..enabled = true
  ..visible = true
  ..bounds = bounds;

VText _text(int id, String text, VRectangle bounds) => VText()
  ..id = id
  ..enabled = true
  ..visible = true
  ..text = text
  ..bounds = bounds;

// No columns: a tree with columns overflows its row by a few pixels in the test window, which the
// framework reports as an error, and the columns play no part in where a child stands.
VTree _tree({List<VControl>? children, List<VTreeEditor>? editors, List<VTreeItem>? items}) => VTree()
  ..id = 1
  ..style = SWT.SINGLE
  ..enabled = true
  ..columns = []
  ..items = items ??
      [
        VTreeItem()
          ..id = 10
          ..text = 'a',
        VTreeItem()
          ..id = 11
          ..text = 'b',
      ]
  ..children = children
  ..editors = editors
  ..bounds = _rect(0, 0, _width.toInt(), _height.toInt());

VTable _table({List<VControl>? children, List<VTableEditor>? editors, List<VTableItem>? items}) => VTable()
  ..id = 1
  ..style = SWT.SINGLE
  ..enabled = true
  ..headerVisible = true
  ..columns = [
    VTableColumn()
      ..id = 2
      ..text = 'Name'
      ..width = 120,
    VTableColumn()
      ..id = 3
      ..text = 'Value'
      ..width = 120,
  ]
  ..items = items ??
      [
        VTableItem()
          ..id = 10
          ..texts = ['a', 'a1'],
        VTableItem()
          ..id = 11
          ..texts = ['b', 'b1'],
      ]
  ..children = children
  ..editors = editors
  ..selection = <int>[]
  ..bounds = _rect(0, 0, _width.toInt(), _height.toInt());

Future<void> _pump(WidgetTester tester, Widget list) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(width: _width, height: _height, child: list),
    ),
  ));
  await tester.pumpAndSettle();
}

Rect _relative(WidgetTester tester, Finder child, Finder list) {
  final origin = tester.getTopLeft(list);
  return tester.getRect(child).shift(-origin);
}

void main() {
  group('Tree', () {
    testWidgets('a TreeCursor child is drawn as a canvas over the cell at its bounds', (tester) async {
      await _pump(tester, TreeSwt<VTree>(value: _tree(children: [_treeCursor(50, _rect(120, 44, 120, 22))])));

      final cursor = find.byType(TreeCursorSwt<VTreeCursor>);
      expect(cursor, findsOneWidget);
      expect(tester.state(cursor), isA<CanvasImpl>());
      expect(find.text('TreeCursor'), findsNothing, reason: 'no placeholder text');
      expect(_relative(tester, cursor, find.byType(TreeSwt<VTree>)), const Rect.fromLTWH(120, 44, 120, 22));
    });

    testWidgets('a cell editor control in the children is drawn once, by its overlay', (tester) async {
      final text = _text(60, 'editing', _rect(0, 0, 120, 22));
      final item = VTreeItem()
        ..id = 10
        ..text = 'a';
      final editor = VTreeEditor()
        ..id = 70
        ..column = 0
        ..item = item
        ..editor = text;
      await _pump(
          tester,
          TreeSwt<VTree>(
              value: _tree(
                  items: [item],
                  children: [text, _treeCursor(50, _rect(0, 44, 120, 22))],
                  editors: [editor])));

      expect(tester.takeException(), isNull);
      expect(find.byType(TextSwt<VText>), findsOneWidget);
      expect(find.byType(TreeCursorSwt<VTreeCursor>), findsOneWidget);
    });

    testWidgets('a hidden cursor stands out of sight, as SWT parks it', (tester) async {
      await _pump(tester, TreeSwt<VTree>(value: _tree(children: [_treeCursor(50, _rect(-200, -200, 0, 0))])));

      final cursor = find.byType(TreeCursorSwt<VTreeCursor>);
      expect(cursor, findsOneWidget);
      expect(_relative(tester, cursor, find.byType(TreeSwt<VTree>)).size, Size.zero);
    });
  });

  group('Table', () {
    testWidgets('a TableCursor child is drawn as a canvas over the cell at its bounds', (tester) async {
      await _pump(tester, TableSwt<VTable>(value: _table(children: [_tableCursor(50, _rect(120, 44, 120, 22))])));

      final cursor = find.byType(TableCursorSwt<VTableCursor>);
      expect(cursor, findsOneWidget);
      expect(tester.state(cursor), isA<CanvasImpl>());
      expect(find.text('TableCursor'), findsNothing, reason: 'no placeholder text');
      expect(_relative(tester, cursor, find.byType(TableSwt<VTable>)), const Rect.fromLTWH(120, 44, 120, 22));
    });

    testWidgets('a cell editor control in the children is drawn once, by its overlay', (tester) async {
      final text = _text(60, 'editing', _rect(0, 0, 120, 22));
      final item = VTableItem()
        ..id = 10
        ..texts = ['a', 'a1'];
      final editor = VTableEditor()
        ..id = 70
        ..column = 1
        ..item = item
        ..editor = text;
      await _pump(
          tester,
          TableSwt<VTable>(
              value: _table(
                  items: [item],
                  children: [text, _tableCursor(50, _rect(0, 44, 120, 22))],
                  editors: [editor])));

      expect(tester.takeException(), isNull);
      expect(find.byType(TextSwt<VText>), findsOneWidget);
      expect(find.byType(TableCursorSwt<VTableCursor>), findsOneWidget);
    });
  });
}
