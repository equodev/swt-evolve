// Drift guard. JFace's ColumnViewer maps a click to a column with `TreeItem.getBounds(int)`, which
// Java answers on its own (`Sizes.treeCell`): column i starts at the sum of the widths of the
// columns before it, and column 0's indent, expander and checkbox sit inside column 0's width. A
// cell drawn anywhere else sends a click on it to another column: an inline editor on the first
// column has to open for a click on the right half of that cell.
//
// These tests pin the drawn cells to those numbers.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treecolumn.dart';
import 'package:swtflutter/src/gen/treeitem.dart';

import 'delivery/support/deliver.dart';

const double _treeWidth = 400;

// TreeSizes' constants for a tree with more than one column, mirrored from
// theme_settings/tree_theme_settings.dart.
const double _edgeGapFractionWithCols = 0.005;
const double _itemPaddingLeftWithCols = 12.0;
const double _itemIndent = 16.0;

VTreeColumn _column(int id, String text, int width) => VTreeColumn()
  ..id = id
  ..text = text
  ..width = width;

VTreeItem _item(int id, List<String> texts, {List<VTreeItem>? children}) => VTreeItem()
  ..id = id
  ..text = texts.first
  ..texts = texts
  ..expanded = children != null
  ..items = children;

VTree _tree(List<VTreeColumn> columns, {int style = SWT.SINGLE}) => VTree()
  ..id = 1
  ..style = style
  ..enabled = true
  ..headerVisible = true
  ..columns = columns
  ..items = [
    _item(10, columns.map((c) => '${c.text} root').toList(), children: [
      _item(20, columns.map((c) => '${c.text} child').toList()),
    ]),
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = _treeWidth.toInt()
    ..height = 300);

late Rect _treeRect;

Future<void> _pump(WidgetTester tester, VTree value) async {
  await tester.pumpWidget(const SizedBox.shrink());
  freshClient();
  final tree = TreeSwt<VTree>(value: value);
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(width: _treeWidth, height: 300, child: tree),
    ),
  ));
  await tester.pumpAndSettle();
  _treeRect = tester.getRect(find.byWidget(tree));
}

/// The cell of item [itemId] in column [column], in tree-local coordinates.
Rect _cell(WidgetTester tester, int itemId, int column) => tester
    .getRect(find.byKey(ValueKey('tree_cell_${itemId}_$column')))
    .translate(-_treeRect.left, -_treeRect.top);

/// Where the header draws [column]'s label, in tree-local coordinates.
Rect _header(WidgetTester tester, int column) => tester
    .getRect(find.byWidgetPredicate((w) => w is TreeColumnSwt).at(column))
    .translate(-_treeRect.left, -_treeRect.top);

/// The left of each column as `TreeItem.getBounds(i)` gives it: the sum of the widths before it.
List<double> _lefts(List<int> widths) {
  final lefts = <double>[];
  var x = 0.0;
  for (final w in widths) {
    lefts.add(x);
    x += w;
  }
  return lefts;
}

void main() {
  testWidgets('each cell spans the width Java gives its column, from the sum of those before it',
      (tester) async {
    const widths = [150, 120, 90];
    await _pump(tester, _tree([
      _column(2, 'Name', widths[0]),
      _column(3, 'Value', widths[1]),
      _column(4, 'Type', widths[2]),
    ]));

    final lefts = _lefts(widths);
    for (final itemId in [10, 20]) {
      for (var i = 0; i < widths.length; i++) {
        final cell = _cell(tester, itemId, i);
        expect(cell.left, moreOrLessEquals(lefts[i]),
            reason: 'item $itemId, column $i starts where getBounds($i) does');
        expect(cell.width, moreOrLessEquals(widths[i].toDouble()),
            reason: 'item $itemId, column $i is as wide as TreeColumn.getWidth()');
      }
    }
  });

  testWidgets('the indent and the expander are inside column 0, not ahead of it', (tester) async {
    await _pump(tester, _tree([_column(2, 'Name', 150), _column(3, 'Value', 120)]));

    final gap = _treeWidth * _edgeGapFractionWithCols;
    final rootArrow = tester
        .getRect(find.byIcon(Icons.keyboard_arrow_down))
        .translate(-_treeRect.left, -_treeRect.top);
    expect(rootArrow.left, moreOrLessEquals(gap + _itemPaddingLeftWithCols),
        reason: 'TreeSizes.getExpanderLeft for level 0');

    final childText = tester
        .getRect(find.text('Name child'))
        .translate(-_treeRect.left, -_treeRect.top);
    expect(childText.left, greaterThan(gap + _itemPaddingLeftWithCols + _itemIndent),
        reason: 'one level in, the text is indented');
    expect(childText.right, lessThanOrEqualTo(150),
        reason: 'the indented text still ends inside column 0');
    expect(_cell(tester, 20, 1).left, moreOrLessEquals(150),
        reason: 'the indent does not push the next column right');
  });

  testWidgets('the header lays its columns on the same boundaries as the rows', (tester) async {
    const widths = [150, 120];
    await _pump(tester, _tree([_column(2, 'Name', widths[0]), _column(3, 'Value', widths[1])]));

    expect(_header(tester, 0).right, moreOrLessEquals(150));
    expect(_header(tester, 1).left, moreOrLessEquals(150));
    expect(_header(tester, 1).width, moreOrLessEquals(120));
  });

  testWidgets('a single column also keeps the width Java gives it', (tester) async {
    await _pump(tester, _tree([_column(2, 'Name', 180)]));

    final cell = _cell(tester, 10, 0);
    expect(cell.left, moreOrLessEquals(0));
    expect(cell.width, moreOrLessEquals(180));
  });

  testWidgets('columns wider than the tree keep their widths and lay out without overflowing',
      (tester) async {
    const widths = [300, 250];
    await _pump(
        tester,
        _tree([_column(2, 'Name', widths[0]), _column(3, 'Value', widths[1])],
            style: SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL));

    expect(tester.takeException(), isNull);
    expect(_cell(tester, 10, 1).left, moreOrLessEquals(300));
    expect(_cell(tester, 10, 1).width, moreOrLessEquals(250));
    expect(_header(tester, 1).left, moreOrLessEquals(300));
  });

  testWidgets('a column narrower than its own indent clips instead of overflowing', (tester) async {
    await _pump(tester, _tree([_column(2, 'Name', 10), _column(3, 'Value', 0), _column(4, 'Type', 90)]));

    expect(tester.takeException(), isNull);
    expect(_cell(tester, 20, 0).width, moreOrLessEquals(10));
    expect(_cell(tester, 20, 2).left, moreOrLessEquals(10));
  });
}
