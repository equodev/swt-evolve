// The sorted column of a Table (Table.setSortColumn / setSortDirection) shows an arrow in its header
// pointing the way it is sorted, as the Tree's does: a JFace viewer sorted by a column marks it so.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tableitem.dart';

VTableColumn _col(int id) => VTableColumn()
  ..id = id
  ..width = 150
  ..text = 'Col$id';

VTable _table({required int direction}) {
  final columns = [_col(1), _col(2)];
  return VTable()
    ..swt = 'Table'
    ..id = 1
    ..style = SWT.NONE
    ..enabled = true
    ..headerVisible = true
    ..columns = columns
    ..sortColumn = columns[1]
    ..sortDirection = direction
    ..items = [VTableItem()
      ..id = 10
      ..texts = ['a', 'b']]
    ..bounds = (VRectangle()
      ..x = 0
      ..y = 0
      ..width = 300
      ..height = 120);
}

Future<void> _pump(WidgetTester tester, VTable table) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(width: 300, height: 120, child: TableSwt<VTable>(value: table)),
  ));
  await tester.pumpAndSettle();
}

void main() {
  testWidgets('the sort column shows an arrow up when sorted up', (tester) async {
    await _pump(tester, _table(direction: SWT.UP));

    expect(find.byIcon(Icons.keyboard_arrow_up), findsOneWidget);
    expect(find.byIcon(Icons.keyboard_arrow_down), findsNothing);
    final arrow = tester.getCenter(find.byIcon(Icons.keyboard_arrow_up));
    expect(arrow.dx, greaterThan(150), reason: 'in the second column, the sorted one');
  });

  testWidgets('the sort column shows an arrow down when sorted down', (tester) async {
    await _pump(tester, _table(direction: SWT.DOWN));

    expect(find.byIcon(Icons.keyboard_arrow_down), findsOneWidget);
  });

  testWidgets('no arrow without a sort direction', (tester) async {
    await _pump(tester, _table(direction: SWT.NONE));

    expect(find.byIcon(Icons.keyboard_arrow_up), findsNothing);
    expect(find.byIcon(Icons.keyboard_arrow_down), findsNothing);
  });
}
