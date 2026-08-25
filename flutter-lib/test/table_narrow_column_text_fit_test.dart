import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tableitem.dart';

/// A preference page that lays its table out with weighted columns hands the
/// narrow ones the minimum width it calculated from the platform font's
/// metrics. This theme's font is wider, so at that width the label and the cell
/// value both used to ellipsize (`Provider` -> `Prov...`, `Manual` -> `Man...`)
/// while the native control showed them whole.
const double _tableWidth = 400;
const double _pinnedNarrowWidth = 50;

VTable _table() => VTable()
  ..id = 1
  ..style = SWT.NONE
  ..headerVisible = true
  ..linesVisible = true
  ..columns = [
    VTableColumn()
      ..id = 1
      ..width = _pinnedNarrowWidth.toInt()
      ..alignment = SWT.LEFT
      ..text = 'Provider',
    VTableColumn()
      ..id = 2
      ..width = 130
      ..alignment = SWT.LEFT
      ..text = 'Host',
  ]
  ..items = [
    VTableItem()
      ..id = 10
      ..texts = ['Manual', 'example.org'],
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = _tableWidth.toInt()
    ..height = 300);

Widget _wrap(VTable value) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: _tableWidth,
        height: 300,
        child: TableSwt<VTable>(value: value),
      ),
    );

/// How much room the laid-out [text] has beyond what its glyphs need. Negative
/// means it is being cut off with an ellipsis.
double _slack(WidgetTester tester, String text) {
  final finder = find.text(text);
  final paragraph = tester.renderObject<RenderParagraph>(finder);
  return tester.getSize(finder).width -
      paragraph.getMaxIntrinsicWidth(double.infinity);
}

void main() {
  testWidgets('a column pinned narrower than its header still shows the label',
      (tester) async {
    await tester.pumpWidget(_wrap(_table()));
    await tester.pumpAndSettle();

    expect(_slack(tester, 'Provider'), greaterThanOrEqualTo(-0.5));
  });

  testWidgets('a cell in a column pinned that narrow still shows its value',
      (tester) async {
    await tester.pumpWidget(_wrap(_table()));
    await tester.pumpAndSettle();

    expect(_slack(tester, 'Manual'), greaterThanOrEqualTo(-0.5));
  });

  testWidgets('a column wide enough for its content keeps its full padding',
      (tester) async {
    await tester.pumpWidget(_wrap(_table()));
    await tester.pumpAndSettle();

    // 'Host' is nowhere near 130px, so nothing about that column moves.
    final host = tester.renderObject<RenderParagraph>(find.text('Host'));
    final available = tester.getSize(find.text('Host')).width;
    expect(available, lessThan(130));
    expect(available,
        greaterThan(host.getMaxIntrinsicWidth(double.infinity)));
  });

  testWidgets('a column the application pinned to zero stays hidden',
      (tester) async {
    final value = _table();
    value.columns![0].width = 0;

    await tester.pumpWidget(_wrap(value));
    await tester.pumpAndSettle();

    final headerTable = tester.widgetList<Table>(find.byType(Table)).first;
    final firstHeaderCell = headerTable.children.first.children.first;
    expect(tester.getSize(find.byWidget(firstHeaderCell)).width, 0);
  });

  testWidgets('a column the user drags narrower keeps the width they chose',
      (tester) async {
    await tester.pumpWidget(_wrap(_table()));
    await tester.pumpAndSettle();

    final grip = find
        .byWidgetPredicate((w) =>
            w is MouseRegion && w.cursor == SystemMouseCursors.resizeColumn)
        .first;
    final wide = tester.getSize(find.text('Provider')).width;

    await tester.drag(grip, const Offset(-40, 0));
    await tester.pumpAndSettle();

    // The floor compensates for a width the *application* computed; a width the
    // user dragged is explicit and must be honoured, ellipsis and all.
    expect(tester.getSize(find.text('Provider')).width, lessThan(wide));
  });
}
