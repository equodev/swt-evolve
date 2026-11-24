// A tree shown only for its header, whose rows another control paints at the widths Java gave the
// columns (a time-graph view: the tree has no items, a canvas below draws each entry's name, id
// and parent id at x = the sum of TreeColumn.getWidth() of the columns before it, in column order).
// Natively the header lays its columns on those same boundaries, from the tree's left edge, so each
// title sits over the values painted under it.

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart' show FontLoader, rootBundle;
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treecolumn.dart';
import 'package:swtflutter/src/impl/utils/bundled_assets.dart';

const double _treeWidth = 390;

const _columns = <(String, int)>[
  ('Process', 200),
  ('TID', 45),
  ('PID', 45),
  ('PTID', 45),
  ('Birth time', 150),
  ('Trace', 80),
];

VTree _tree() => VTree()
  ..id = 1
  ..style = SWT.SINGLE
  ..enabled = true
  ..headerVisible = true
  ..columns = [
    for (var i = 0; i < _columns.length; i++)
      VTreeColumn()
        ..id = 10 + i
        ..text = _columns[i].$1
        ..width = _columns[i].$2,
  ]
  ..items = []
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = _treeWidth.toInt()
    ..height = 80);

late Rect _treeRect;

Future<void> _pump(WidgetTester tester) async {
  final tree = TreeSwt<VTree>(value: _tree());
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(width: _treeWidth, height: 80, child: tree),
    ),
  ));
  await tester.pumpAndSettle();
  _treeRect = tester.getRect(find.byWidget(tree));
}

Finder _headerCell(int columnId) =>
    find.byWidgetPredicate((w) => w is TreeColumnSwt && w.value.id == columnId);

/// The header cell of the column with [columnId], in tree-local coordinates.
Rect _header(WidgetTester tester, int columnId) =>
    tester.getRect(_headerCell(columnId)).translate(-_treeRect.left, -_treeRect.top);

/// Where another control paints column [i]'s values: the sum of the widths before it.
double _edge(int i) => _columns.take(i).fold(0.0, (x, c) => x + c.$2);

void main() {
  setUpAll(() async {
    final inter = FontLoader(BundledAssets.family('Inter'))
      ..addFont(rootBundle.load('assets/fonts/Inter_18pt-SemiBold.ttf'));
    await inter.load();
  });

  testWidgets('each header cell starts at the sum of the widths of the columns before it',
      (tester) async {
    await _pump(tester);
    for (var i = 0; i < 4; i++) {
      expect(_header(tester, 10 + i).left, moreOrLessEquals(_edge(i), epsilon: 0.5),
          reason: '"${_columns[i].$1}" header left edge');
    }
  });

  testWidgets('each header cell is as wide as its column', (tester) async {
    await _pump(tester);
    for (var i = 0; i < 4; i++) {
      expect(_header(tester, 10 + i).width, moreOrLessEquals(_columns[i].$2.toDouble(), epsilon: 0.5),
          reason: '"${_columns[i].$1}" header width');
    }
  });

  testWidgets('the first column\'s header shows its title whole', (tester) async {
    await _pump(tester);
    final title = find.descendant(of: _headerCell(10), matching: find.text('PROCESS'));
    final paragraph = tester.renderObject<RenderBox>(title);
    final slack = tester.getSize(title).width - paragraph.getMaxIntrinsicWidth(double.infinity);
    expect(slack, greaterThanOrEqualTo(-0.5));
  });
}
