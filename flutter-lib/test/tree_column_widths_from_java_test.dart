// Java places a Tree's cells at the column widths it set (TreeItem.getBounds, SWT.PaintItem), so
// Flutter must not scale or widen them; the cell origin mirrors TreeSizes.getCellsLeft.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treecolumn.dart';
import 'package:swtflutter/src/gen/treeitem.dart';

const double _treeWidth = 300;
const double _treeHeight = 200;

const List<int> _widths = [150, 120, 110];

VTree _tree({List<int?> widths = _widths, int style = SWT.H_SCROLL | SWT.V_SCROLL}) => VTree()
  ..id = 1
  ..style = style
  ..enabled = true
  ..headerVisible = true
  ..linesVisible = false
  ..columns = [
    for (var i = 0; i < widths.length; i++)
      VTreeColumn()
        ..id = 2 + i
        ..width = widths[i]
        ..alignment = SWT.LEFT
        ..text = 'Column $i',
  ]
  ..items = [
    VTreeItem()
      ..id = 10
      ..text = 'alpha'
      ..texts = ['alpha', 'beta', 'gamma'],
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

double _horizontalScrollExtent(WidgetTester tester) {
  for (final scrollable in tester.widgetList<Scrollable>(find.byType(Scrollable))) {
    if (scrollable.axisDirection == AxisDirection.right) {
      return tester
          .state<ScrollableState>(find.byWidget(scrollable))
          .position
          .maxScrollExtent;
    }
  }
  return 0;
}

void main() {
  testWidgets('columns keep the widths Java set, even when they overflow the tree', (tester) async {
    await tester.pumpWidget(_wrap(_tree()));
    await tester.pumpAndSettle();

    final beta = tester.getTopLeft(find.text('beta')).dx;
    final gamma = tester.getTopLeft(find.text('gamma')).dx;
    expect(gamma - beta, _widths[1],
        reason: 'the second column is as wide as Java says, not scaled to fit the tree');
  });

  testWidgets('a cell starts where Java places it', (tester) async {
    await tester.pumpWidget(_wrap(_tree()));
    await tester.pumpAndSettle();

    final treeLeft = tester.getTopLeft(find.byType(TreeSwt<VTree>)).dx;
    // 1.5 edge gap (0.005 of 300) + 12 row padding + 150 first column + 8 text inset.
    expect(tester.getTopLeft(find.text('beta')).dx - treeLeft, closeTo(171.5, 1.0));
  });

  testWidgets('columns wider than the tree scroll horizontally', (tester) async {
    await tester.pumpWidget(_wrap(_tree()));
    await tester.pumpAndSettle();

    expect(_horizontalScrollExtent(tester), greaterThan(0));
  });

  testWidgets('the header scrolls with the rows', (tester) async {
    await tester.pumpWidget(_wrap(_tree()));
    await tester.pumpAndSettle();
    double headerToCell() =>
        tester.getTopLeft(find.text('beta')).dx - tester.getTopLeft(find.textContaining(
            RegExp('column 1', caseSensitive: false))).dx;
    final before = headerToCell();
    final cellBefore = tester.getTopLeft(find.text('beta')).dx;

    await tester.drag(find.text('beta'), const Offset(-60, 0));
    await tester.pumpAndSettle();

    expect(tester.getTopLeft(find.text('beta')).dx, lessThan(cellBefore),
        reason: 'the drag scrolled the rows');
    expect(headerToCell(), closeTo(before, 0.5));
  });

  testWidgets('a column Java hid keeps the others where Java places them', (tester) async {
    // A width of 0 is not sent: the middle column arrives without one.
    await tester.pumpWidget(_wrap(_tree(widths: [150, null, 110])));
    await tester.pumpAndSettle();

    expect(tester.takeException(), isNull);
    final treeLeft = tester.getTopLeft(find.byType(TreeSwt<VTree>)).dx;
    expect(tester.getTopLeft(find.text('gamma')).dx - treeLeft, closeTo(171.5, 1.0));
  });

  testWidgets('the last column is not widened to fill the tree', (tester) async {
    await tester.pumpWidget(_wrap(_tree(widths: [80, 60, 50])));
    await tester.pumpAndSettle();

    expect(_horizontalScrollExtent(tester), 0);
    // The last header's title area: its 50 minus the 8 left and 4 right insets.
    expect(tester.getSize(find.text('COLUMN 2')).width, closeTo(38, 1.0));
  });

  // Tree.checkStyle adds H_SCROLL | V_SCROLL to every tree unless it is created with NO_SCROLL.
  group('with NO_SCROLL', () {
    VTree clipped() => _tree(style: SWT.NO_SCROLL | SWT.FULL_SELECTION);

    testWidgets('columns that overflow the tree keep the widths Java set', (tester) async {
      await tester.pumpWidget(_wrap(clipped()));
      await tester.pumpAndSettle();

      expect(tester.takeException(), isNull);
      final treeLeft = tester.getTopLeft(find.byType(TreeSwt<VTree>)).dx;
      expect(tester.getTopLeft(find.text('beta')).dx - treeLeft, closeTo(171.5, 1.0));
      expect(tester.getTopLeft(find.text('gamma')).dx - tester.getTopLeft(find.text('beta')).dx,
          _widths[1]);
    });

    testWidgets('the overflow is clipped, not scrolled', (tester) async {
      await tester.pumpWidget(_wrap(clipped()));
      await tester.pumpAndSettle();

      expect(_horizontalScrollExtent(tester), 0);
      final treeRight = tester.getTopRight(find.byType(TreeSwt<VTree>)).dx;
      expect(tester.takeException(), isNull);
      expect(tester.getRect(find.text('gamma')).right, greaterThan(treeRight),
          reason: 'the third column runs past the tree and is not pulled back into view');
      final clips = find.ancestor(of: find.text('gamma'), matching: find.byType(ClipRect));
      expect(
          clips.evaluate().any((e) => tester.getRect(find.byWidget(e.widget)).right <= treeRight),
          isTrue,
          reason: 'the part of the row past the tree is clipped');
    });

    testWidgets('each header title sits over its own column', (tester) async {
      await tester.pumpWidget(_wrap(clipped()));
      await tester.pumpAndSettle();

      final title = tester.getTopLeft(find.textContaining(RegExp('column 1', caseSensitive: false)));
      expect(tester.getTopLeft(find.text('beta')).dx, closeTo(title.dx, 0.5));
    });
  });
}
