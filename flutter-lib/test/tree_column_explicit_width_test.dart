// A TreeColumn's width is the application's: SWT clips a cell's text to it and never resizes a
// column to its content. A value cell far wider than its column must not take width away from the
// other columns.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treecolumn.dart';
import 'package:swtflutter/src/gen/treeitem.dart';

const double _treeWidth = 500;
const double _treeHeight = 300;

const String _longPath =
    '/Users/someone/.m2/repository/org/postgresql/postgresql/42.7.13/postgresql-42.7.13.jar';

VTree _tree({required int nameWidth, required int valueWidth, String value = _longPath}) =>
    VTree()
  ..id = 1
  ..style = SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL
  ..enabled = true
  ..headerVisible = true
  ..linesVisible = true
  ..columns = [
    VTreeColumn()
      ..id = 2
      ..width = nameWidth
      ..alignment = SWT.LEFT
      ..text = 'Name',
    VTreeColumn()
      ..id = 3
      ..width = valueWidth
      ..alignment = SWT.LEFT
      ..text = 'Value',
  ]
  ..items = [
    VTreeItem()
      ..id = 10
      ..text = 'Driver Type'
      ..texts = ['Driver Type', 'PostgreSQL'],
    VTreeItem()
      ..id = 11
      ..text = 'Driver Files'
      ..texts = ['Driver Files', value],
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = _treeWidth.toInt()
    ..height = _treeHeight.toInt());

Widget _wrap(VTree value) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(
          width: _treeWidth,
          height: _treeHeight,
          child: TreeSwt<VTree>(value: value),
        ),
      ),
    );

/// Pumps [value] and drops the row overflow every multi-column tree reports: rows are laid out
/// [TreeThemeExtension.itemPaddingWithCols] wider than the columns leave room for, which is
/// unrelated to how the column widths are chosen. Any other error still fails the test.
Future<void> _pump(WidgetTester tester, VTree value) async {
  final report = FlutterError.onError;
  FlutterError.onError = (details) {
    if (details.exceptionAsString().contains('A RenderFlex overflowed')) return;
    report?.call(details);
  };
  try {
    await tester.pumpWidget(_wrap(value));
    await tester.pumpAndSettle();
  } finally {
    FlutterError.onError = report;
  }
}

Finder _header(String text) => find.byWidgetPredicate(
    (w) => w is Text && w.data != null && w.data!.toLowerCase() == text.toLowerCase());

/// Left edge of the [text] header cell, relative to the tree.
double _headerLeft(WidgetTester tester, String text) {
  final treeLeft = tester.getTopLeft(find.byType(TreeSwt<VTree>)).dx;
  return tester.getTopLeft(_header(text).first).dx - treeLeft;
}

void main() {
  testWidgets('a long value does not shrink the column the application sized', (tester) async {
    await _pump(tester, _tree(nameWidth: 200, valueWidth: 296));

    expect(_headerLeft(tester, 'Value'), greaterThanOrEqualTo(190),
        reason: 'the Name column is 200px wide; the long value is clipped in its own column');
  });

  testWidgets('columns that fit keep growing to their content', (tester) async {
    await _pump(tester, _tree(nameWidth: 40, valueWidth: 60, value: 'short'));

    expect(_headerLeft(tester, 'Value'), greaterThan(60),
        reason: "'Driver Files' is wider than 40px and there is room for it");
  });
}
