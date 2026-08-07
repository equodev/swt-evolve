import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tableitem.dart';

const double _tableWidth = 200;

VTable _table({required List<VTableColumn> columns}) => VTable()
  ..id = 1
  ..style = SWT.NONE
  ..headerVisible = false
  ..linesVisible = false
  ..columns = columns
  ..items = [
    VTableItem()
      ..id = 10
      ..texts = ['Current range'],
    VTableItem()
      ..id = 11
      ..texts = ['Range linked to current'],
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

double _firstCellWidth(WidgetTester tester) {
  final bodyTable = tester.widgetList<Table>(find.byType(Table)).last;
  final firstCell = bodyTable.children.first.children.first;
  return tester.getSize(find.byWidget(firstCell)).width;
}

void main() {
  testWidgets('a columnless table gives the whole row to the text cell', (tester) async {
    await tester.pumpWidget(_wrap(_table(columns: [])));
    await tester.pumpAndSettle();

    expect(_firstCellWidth(tester), greaterThan(_tableWidth * 0.9));
  });

  testWidgets('the trailing spacer of a columnless table takes no width', (tester) async {
    await tester.pumpWidget(_wrap(_table(columns: [])));
    await tester.pumpAndSettle();

    final bodyTable = tester.widgetList<Table>(find.byType(Table)).last;
    final spacer = bodyTable.children.first.children.last;

    expect(tester.getSize(find.byWidget(spacer).first).width, 0);
  });

  testWidgets('a table with columns still honours its column widths', (tester) async {
    await tester.pumpWidget(_wrap(_table(columns: [
      VTableColumn()
        ..id = 1
        ..width = 120
        ..alignment = SWT.LEFT
        ..text = 'Col1'
    ])));
    await tester.pumpAndSettle();

    expect(_firstCellWidth(tester), closeTo(120, 1));
  });
}
