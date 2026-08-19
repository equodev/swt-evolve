// Regression tests for table visual fixes.
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
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/impl/table_evolve.dart';
import 'package:swtflutter/src/impl/utils/image_utils.dart';

// Minimal 4×1 red RGB PNG for badge image rendering tests.
const _kBadgePng = <int>[
  137, 80, 78, 71, 13, 10, 26, 10,
  0, 0, 0, 13, 73, 72, 68, 82,
  0, 0, 0, 4, 0, 0, 0, 1, 8, 2, 0, 0, 0, 118, 94, 152, 154,
  0, 0, 0, 13, 73, 68, 65, 84, 120, 156, 99, 248, 207, 192,
  0, 71, 0, 29, 239, 3, 253, 230, 17, 102, 29,
  0, 0, 0, 0, 73, 69, 78, 68, 174, 66, 96, 130,
];

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

    testWidgets('mixed widths: a null-width column sizes to content, not 0',
        (tester) async {
      // A "Find Actions"-style shape: the category column has no explicit width
      // while the label column does. A null width must size to content, not
      // collapse to 0 (which hid the category column entirely).
      final cols = [_col(1), _col(2, width: 300)];
      final value = _table(
          columns: cols, items: [_item(10, ['Editors', 'Toggle Split Editor'])]);

      await tester.pumpWidget(_wrap(value));
      await tester.pump();

      final widths =
          tester.state<TableImpl>(find.byType(TableSwt<VTable>)).cachedColumnWidths!;

      expect((widths[0] as FixedColumnWidth).value, greaterThan(0.0),
          reason:
              'a column with no explicit width must size to content, not 0');
      expect((widths[1] as FixedColumnWidth).value, equals(300.0),
          reason: 'columns with an explicit width keep it');
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

  // A column-less Table (SWT List-style single column, e.g. JDT's "Defined
  // classpath variables" list) must let its one text cell use the full width and
  // clip only at the real edge — matching native SWT. Regression: buildBody set
  // only {0: FlexColumnWidth()} while buildRow still appends a trailing
  // SizedBox.shrink() cell, so the unlisted trailing column fell back to
  // Flutter's default FlexColumnWidth(1). Two equal-weight flex columns split
  // the width 50/50, so the text was ellipsized at ~half the list width with an
  // empty gap on the right (issue: item text truncated long before available width).
  group('column-less table fills the single cell', () {
    const longText =
        'JRE_LIB - /opt/example/very/long/installation/path/'
        'runtime/Contents/Eclipse/jre/lib/jrt-fs.jar';

    testWidgets('single text cell uses nearly the full width, not half',
        (tester) async {
      // Matches the reported list signature: 0 columns, no header, long item text.
      final value = _table(
        columns: const [],
        items: [_item(10, [longText])],
        headerVisible: false,
      );

      await tester.pumpWidget(_wrap(value));
      await tester.pump();

      // The Text is Expanded inside its cell, so its laid-out width equals the
      // cell (column 0) width. In an 800-wide list a healthy single column fills
      // almost all of it; the 50/50 bug caps it near 400.
      final textWidth = tester.getSize(find.text(longText)).width;
      expect(
        textWidth,
        greaterThan(640.0),
        reason: 'the lone text column must fill the list width (>0.8*800), not '
            'split it 50/50 with the empty trailing filler (~400)',
      );
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

  group('table item image rendering uses natural aspect ratio', () {
    setUp(() => ImageUtils.clearCache());

    test('buildVImageAsync with wide image and height-only constraint is non-square', () async {
      final img = VImage.empty()
        ..imageData = (VImageData.empty()..data = _kBadgePng);

      const iconHeight = 14.0;
      final widget = await ImageUtils.buildVImageAsync(
        img,
        height: iconHeight,
        constraints: BoxConstraints(
          maxWidth: iconHeight * 6,
          maxHeight: iconHeight,
        ),
        renderAsIcon: false,
        useBinaryImage: true,
      );

      expect(widget, isA<ConstrainedBox>(),
          reason: 'binary image with explicit constraints must return a ConstrainedBox');
      final box = widget as ConstrainedBox;
      expect(
        box.constraints.maxWidth,
        greaterThan(box.constraints.maxHeight),
        reason: 'maxWidth must exceed maxHeight so wide badge images render at their '
            'natural aspect ratio instead of being squeezed into a square icon box',
      );
    });

    testWidgets('image-only cell uses non-square ConstrainedBox after FutureBuilder resolves',
        (tester) async {
      final img = VImage.empty()
        ..imageData = (VImageData.empty()..data = _kBadgePng);

      final item = VTableItem.empty()
        ..id = 42
        ..texts = null
        ..images = [img];

      final value = _table(
        columns: [_col(1, width: 200)],
        items: [item],
        headerVisible: false,
      );

      await tester.pumpWidget(_wrap(value));
      await tester.pumpAndSettle();

      final boxes = tester.widgetList<ConstrainedBox>(find.byType(ConstrainedBox)).toList();
      final wideBox = boxes.any((b) => b.constraints.maxWidth > b.constraints.maxHeight);
      expect(
        wideBox,
        isTrue,
        reason: 'at least one ConstrainedBox in the tree must be wider than tall, '
            'proving the badge image is not constrained to a square icon box',
      );
    });
  });
}
