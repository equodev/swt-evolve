// With compactRows (the application turned native item heights off) a Table's and a Tree's rows are
// one text line plus the compact padding, with no theme minimum; otherwise they keep the theme's row.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';
import 'package:swtflutter/src/impl/tree_evolve.dart';
import 'package:swtflutter/src/theme/theme_extensions/table_theme_extension.dart';
import 'package:swtflutter/src/theme/theme_extensions/tree_theme_extension.dart';

const _labels = ['Open file', 'Save all', 'Show log'];

VTable _table(bool compact) => VTable()
  ..id = compact ? 1 : 2
  ..seq = 1
  ..style = SWT.FULL_SELECTION
  ..compactRows = compact
  ..itemCount = _labels.length
  ..items = [
    for (var i = 0; i < _labels.length; i++)
      VTableItem()
        ..id = (compact ? 100 : 200) + i
        ..seq = 1
        ..texts = [_labels[i]]
  ]
  ..bounds = (VRectangle()
    ..width = 300
    ..height = 300);

VTree _tree(bool compact) => VTree()
  ..id = compact ? 1 : 2
  ..style = SWT.SINGLE
  ..enabled = true
  ..compactRows = compact
  ..items = [
    for (var i = 0; i < _labels.length; i++)
      VTreeItem()
        ..id = (compact ? 100 : 200) + i
        ..text = _labels[i]
  ]
  ..bounds = (VRectangle()
    ..width = 300
    ..height = 300);

/// Pumps [content] in a new app, so that no layout state carries over from a previous pump.
Future<void> _pump(WidgetTester tester, Widget content) async {
  await tester.pumpWidget(const SizedBox());
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(width: 300, height: 300, child: content),
  ));
  for (var i = 0; i < 3; i++) {
    await tester.runAsync(() => Future<void>.delayed(const Duration(milliseconds: 50)));
    await tester.pump();
  }
  await tester.pumpAndSettle();
}

/// The distance between two consecutive rows' labels: the height each row is laid out at.
double _rowPitch(WidgetTester tester) =>
    tester.getTopLeft(find.text(_labels[1])).dy - tester.getTopLeft(find.text(_labels[0])).dy;

double _lineHeight(WidgetTester tester) => tester.getSize(find.text(_labels[0])).height;

void main() {
  testWidgets('a compact table lays its rows out one text line plus the compact padding tall', (tester) async {
    await _pump(tester, TableSwt<VTable>(value: _table(true)));
    final theme = Theme.of(tester.element(find.byType(TableSwt<VTable>))).extension<TableThemeExtension>()!;
    final compact = _rowPitch(tester);
    expect(compact, closeTo(_lineHeight(tester) + theme.compactCellPadding.vertical, 1.0));

    await _pump(tester, TableSwt<VTable>(value: _table(false)));
    final regular = _rowPitch(tester);
    expect(regular, greaterThan(compact), reason: 'the default row keeps the theme height');
    expect(regular, closeTo(_lineHeight(tester) + theme.cellPadding.vertical, 1.0));
  });

  testWidgets('a compact tree lays its rows out one text line plus the row padding tall', (tester) async {
    await _pump(tester, TreeSwt<VTree>(value: _tree(true)));
    final theme = Theme.of(tester.element(find.byType(TreeSwt<VTree>))).extension<TreeThemeExtension>()!;
    final compact = tester.state<TreeImpl>(find.byType(TreeSwt<VTree>)).cachedItemHeight!;
    expect(compact, (_lineHeight(tester) + theme.itemPadding.vertical).ceilToDouble());
    expect(_rowPitch(tester), compact);

    await _pump(tester, TreeSwt<VTree>(value: _tree(false)));
    final regular = tester.state<TreeImpl>(find.byType(TreeSwt<VTree>)).cachedItemHeight!;
    expect(regular, greaterThanOrEqualTo(theme.itemHeight + theme.itemPadding.vertical),
        reason: 'the default row keeps the theme height as its floor');
    expect(regular, greaterThan(compact));
  });
}
