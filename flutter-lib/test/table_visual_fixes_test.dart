// Regression tests for table visual fixes (issue #779).
//
// 1. calculateColumnWidths() always appends a FlexColumnWidth(1) filler at
//    index columns.length. The flex absorbs leftover space so Flutter does not
//    redistribute it among fixed columns (which would misalign header/body).
//    It also makes the Table fill the full container width so row backgrounds
//    extend to the right edge, matching native SWT.
//
// 2. Every TableRow (header and body) has columns.length + 1 children to match
//    the columnWidths map; a mismatch causes a Flutter assertion failure.
//
// 3. TableItem cells respect the per-column SWT alignment (LEFT/CENTER/RIGHT).
//    Checkbox-only cells (SWT.CHECK, no text or image) use the column alignment
//    rather than the previous hardcoded Alignment.centerLeft.

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/impl/table_evolve.dart';

VTableColumn _col(int id, {int? width, int alignment = SWT.LEFT}) =>
    VTableColumn()
      ..id = id
      ..width = width
      ..alignment = alignment
      ..text = 'Col$id';

VTableItem _item(int id, List<String> texts) => VTableItem()
  ..id = id
  ..texts = texts;

VTable _table({
  required List<VTableColumn> columns,
  required List<VTableItem> items,
  bool headerVisible = true,
  bool linesVisible = false,
  int style = SWT.NONE,
}) =>
    VTable()
      ..id = 1
      ..style = style
      ..headerVisible = headerVisible
      ..linesVisible = linesVisible
      ..columns = columns
      ..items = items
      ..bounds = (VRectangle()
        ..x = 0
        ..y = 0
        ..width = 800
        ..height = 400);

Widget _wrap(VTable value) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 800,
        height: 400,
        child: TableSwt<VTable>(value: value),
      ),
    );

void main() {
  group('calculateColumnWidths filler column', () {
    testWidgets('explicit widths: last entry is FlexColumnWidth', (tester) async {
      final cols = [_col(1, width: 200), _col(2, width: 300), _col(3, width: 150)];
      final value = _table(columns: cols, items: []);

      await tester.pumpWidget(_wrap(value));
      await tester.pump();

      final widths = tester.state<TableImpl>(find.byType(TableSwt<VTable>)).cachedColumnWidths!;

      expect(widths.length, equals(cols.length + 1),
          reason: 'columnWidths must have one extra filler entry');
      expect(widths[cols.length], isA<FlexColumnWidth>(),
          reason: 'filler entry must be FlexColumnWidth');
    });

    testWidgets('intrinsic widths: last entry is FlexColumnWidth', (tester) async {
      final cols = [_col(1), _col(2)];
      final value = _table(columns: cols, items: [_item(10, ['hello', 'world'])]);

      await tester.pumpWidget(_wrap(value));
      await tester.pump();

      final widths = tester.state<TableImpl>(find.byType(TableSwt<VTable>)).cachedColumnWidths!;

      expect(widths.length, equals(cols.length + 1));
      expect(widths[cols.length], isA<FlexColumnWidth>());
    });

    testWidgets('fixed columns keep their declared pixel widths', (tester) async {
      final cols = [_col(1, width: 100), _col(2, width: 250)];
      final value = _table(columns: cols, items: []);

      await tester.pumpWidget(_wrap(value));
      await tester.pump();

      final widths = tester.state<TableImpl>(find.byType(TableSwt<VTable>)).cachedColumnWidths!;

      expect((widths[0] as FixedColumnWidth).value, equals(100.0));
      expect((widths[1] as FixedColumnWidth).value, equals(250.0));
    });
  });

  group('TableRow child count', () {
    testWidgets('header row has columns.length + 1 children', (tester) async {
      final cols = [_col(1, width: 200), _col(2, width: 200), _col(3, width: 200)];
      final value = _table(columns: cols, items: [], headerVisible: true);

      await tester.pumpWidget(_wrap(value));
      await tester.pump();

      final tables = tester.widgetList<Table>(find.byType(Table)).toList();
      expect(tables, isNotEmpty);
      expect(
        tables.first.children.first.children!.length,
        equals(cols.length + 1),
        reason: 'header TableRow must include the SizedBox.shrink() filler',
      );
    });

    testWidgets('body rows each have columns.length + 1 children', (tester) async {
      final cols = [_col(1, width: 200), _col(2, width: 200)];
      final value = _table(
          columns: cols,
          items: [_item(10, ['a', 'b']), _item(11, ['c', 'd'])],
          headerVisible: false);

      await tester.pumpWidget(_wrap(value));
      await tester.pump();

      final bodyTable = tester.widgetList<Table>(find.byType(Table)).last;
      for (final row in bodyTable.children) {
        expect(
          row.children!.length,
          equals(cols.length + 1),
          reason: 'each body TableRow must include the SizedBox.shrink() filler',
        );
      }
    });
  });

  group('column text alignment', () {
    Future<TextAlign?> textAlign(WidgetTester tester, int alignment) async {
      final cols = [VTableColumn()
        ..id = 1
        ..width = 300
        ..alignment = alignment
        ..text = 'Header'];
      final value = _table(
          columns: cols, items: [_item(10, ['cell text'])], headerVisible: false);

      await tester.pumpWidget(_wrap(value));
      await tester.pump();

      return tester.widgetList<Text>(find.text('cell text')).firstOrNull?.textAlign;
    }

    testWidgets('SWT.LEFT renders TextAlign.left', (tester) async {
      expect(await textAlign(tester, SWT.LEFT), equals(TextAlign.left));
    });

    testWidgets('SWT.CENTER renders TextAlign.center', (tester) async {
      expect(await textAlign(tester, SWT.CENTER), equals(TextAlign.center));
    });

    testWidgets('SWT.RIGHT renders TextAlign.right', (tester) async {
      expect(await textAlign(tester, SWT.RIGHT), equals(TextAlign.right));
    });
  });

  group('checkbox-only cell alignment', () {
    Future<List<AlignmentGeometry?>> cellAlignments(
        WidgetTester tester, int columnAlignment) async {
      final cols = [VTableColumn()
        ..id = 1
        ..width = 100
        ..alignment = columnAlignment
        ..text = ''];
      final value = _table(
        columns: cols,
        items: [VTableItem()
          ..id = 10
          ..texts = ['']],
        headerVisible: false,
        style: SWT.CHECK,
      );

      await tester.pumpWidget(_wrap(value));
      await tester.pump();

      return tester.widgetList<Container>(find.byType(Container)).map((c) => c.alignment).toList();
    }

    testWidgets('SWT.CENTER uses Alignment.center', (tester) async {
      expect(await cellAlignments(tester, SWT.CENTER), contains(Alignment.center));
    });

    testWidgets('SWT.RIGHT uses Alignment.centerRight', (tester) async {
      expect(await cellAlignments(tester, SWT.RIGHT), contains(Alignment.centerRight));
    });

    testWidgets('SWT.LEFT uses Alignment.centerLeft', (tester) async {
      expect(await cellAlignments(tester, SWT.LEFT), contains(Alignment.centerLeft));
    });
  });
}
