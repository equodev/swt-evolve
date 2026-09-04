// A Tree declared with SWT.H_SCROLL scrolls horizontally when its content is wider than the
// client area. A tree with no TreeColumns has no column widths to derive that content width
// from, so the width has to come from the items themselves.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treecolumn.dart';
import 'package:swtflutter/src/gen/treeitem.dart';

const double _treeWidth = 200;
const double _treeHeight = 300;

const String _longLabel =
    '01_Seabed_Decimate_100, 639, NEW, 01STA, TRACE_HEADER_MAPPING_DEFAULT';

VTree _tree({required List<VTreeColumn> columns, required int style}) => VTree()
  ..id = 1
  ..style = style
  ..enabled = true
  ..headerVisible = columns.isNotEmpty
  ..linesVisible = false
  ..columns = columns
  ..items = [
    VTreeItem()
      ..id = 10
      ..text = _longLabel
      ..texts = [_longLabel],
    VTreeItem()
      ..id = 11
      ..text = 'Probe 01 Inline;Far_offset:fusion10'
      ..texts = ['Probe 01 Inline;Far_offset:fusion10'],
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = _treeWidth.toInt()
    ..height = _treeHeight.toInt());

Widget _wrap(VTree value) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: _treeWidth,
        height: _treeHeight,
        child: TreeSwt<VTree>(value: value),
      ),
    );

/// The scroll extent of the tree's horizontal viewport, or `null` when the tree built no
/// horizontal scrollable at all.
double? _horizontalScrollExtent(WidgetTester tester) {
  for (final scrollable in tester.widgetList<Scrollable>(find.byType(Scrollable))) {
    if (scrollable.axisDirection == AxisDirection.right ||
        scrollable.axisDirection == AxisDirection.left) {
      return tester
          .state<ScrollableState>(find.byWidget(scrollable))
          .position
          .maxScrollExtent;
    }
  }
  return null;
}

void main() {
  testWidgets('a columnless H_SCROLL tree scrolls to its widest item', (tester) async {
    await tester.pumpWidget(_wrap(_tree(columns: [], style: SWT.H_SCROLL | SWT.V_SCROLL)));
    await tester.pumpAndSettle();

    final extent = _horizontalScrollExtent(tester);
    expect(extent, isNotNull, reason: 'no horizontal viewport was built at all');
    expect(extent, greaterThan(0),
        reason: 'the widest label is wider than the tree, so it must be reachable');
  });

  testWidgets('the widest label is laid out at its full width, not ellipsized', (tester) async {
    await tester.pumpWidget(_wrap(_tree(columns: [], style: SWT.H_SCROLL | SWT.V_SCROLL)));
    await tester.pumpAndSettle();

    expect(tester.getSize(find.text(_longLabel)).width, greaterThan(_treeWidth));
  });

  testWidgets('a columnless tree without H_SCROLL does not scroll horizontally', (tester) async {
    await tester.pumpWidget(_wrap(_tree(columns: [], style: SWT.V_SCROLL)));
    await tester.pumpAndSettle();

    expect(_horizontalScrollExtent(tester), anyOf(isNull, 0.0));
  });

  testWidgets('turning the scroll path on does not shrink the tree itself', (tester) async {
    await tester.pumpWidget(_wrap(_tree(columns: [], style: SWT.V_SCROLL)));
    await tester.pumpAndSettle();
    final notScrolling = tester.getSize(find.byType(TreeSwt<VTree>));

    await tester.pumpWidget(_wrap(_tree(columns: [], style: SWT.H_SCROLL | SWT.V_SCROLL)));
    await tester.pumpAndSettle();
    final scrolling = tester.getSize(find.byType(TreeSwt<VTree>));

    expect(scrolling, notScrolling);
    expect(scrolling, const Size(_treeWidth, _treeHeight));
  });

  testWidgets('a tree with columns keeps deriving its width from them', (tester) async {
    await tester.pumpWidget(_wrap(_tree(
      columns: [
        VTreeColumn()
          ..id = 2
          ..width = 400
          ..alignment = SWT.LEFT
          ..text = 'Name',
      ],
      style: SWT.H_SCROLL | SWT.V_SCROLL,
    )));
    await tester.pumpAndSettle();

    expect(_horizontalScrollExtent(tester), isNotNull);
  });
}
