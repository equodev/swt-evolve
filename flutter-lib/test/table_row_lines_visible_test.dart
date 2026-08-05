// Row separators are grid lines: they must follow SWT's linesVisible, like the vertical ones.
// Painted unconditionally, the line under the last row of a table taller than its content reads
// as the start of a row that does not exist.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tableitem.dart';

VTable _table({required bool linesVisible}) => VTable()
  ..id = 1
  ..style = SWT.NONE
  ..headerVisible = false
  ..linesVisible = linesVisible
  ..columns = [
    VTableColumn()
      ..id = 1
      ..width = 200
      ..alignment = SWT.LEFT
      ..text = 'Col1'
  ]
  ..items = [
    VTableItem()
      ..id = 10
      ..texts = ['default'],
    VTableItem()
      ..id = 11
      ..texts = ['New Profile'],
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 200
    ..height = 300);

Widget _wrap(VTable value) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 200,
        height: 300,
        child: TableSwt<VTable>(value: value),
      ),
    );

/// Bottom border sides of every body row, in row order.
List<BorderSide> _rowBottomSides(WidgetTester tester) {
  final bodyTable = tester.widgetList<Table>(find.byType(Table)).last;
  return bodyTable.children
      .map((row) => ((row.decoration as BoxDecoration).border as Border).bottom)
      .toList();
}

void main() {
  testWidgets('linesVisible=false paints no row separators', (tester) async {
    await tester.pumpWidget(_wrap(_table(linesVisible: false)));
    await tester.pump();

    for (final side in _rowBottomSides(tester)) {
      expect(side, equals(BorderSide.none),
          reason: 'no row may paint a separator when linesVisible is false');
    }
  });

  testWidgets('linesVisible=true paints row separators', (tester) async {
    await tester.pumpWidget(_wrap(_table(linesVisible: true)));
    await tester.pump();

    for (final side in _rowBottomSides(tester)) {
      expect(side.width, greaterThan(0.0),
          reason: 'rows keep their separator when linesVisible is true');
    }
  });
}
