// A short table builds every row only for the semantics tree, which needs a node per row. With
// semantics off it builds the rows around what is visible, as a long table does: the rest cannot be
// seen, and would only cost laying their text out.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tableitem.dart';

const int _rows = 100;

VTable _table() => VTable()
  ..id = 1
  ..seq = 1
  ..style = SWT.BORDER | SWT.FULL_SELECTION
  ..headerVisible = true
  ..itemCount = _rows
  ..columns = [
    VTableColumn()
      ..id = 2
      ..seq = 1
      ..width = 180
      ..text = 'Column 0'
  ]
  ..items = [
    for (var i = 0; i < _rows; i++)
      VTableItem()
        ..id = 100 + i
        ..seq = 1
        ..texts = ['row $i']
  ]
  ..bounds = (VRectangle()
    ..width = 200
    ..height = 300);

Widget _app() => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(width: 200, height: 300, child: TableSwt<VTable>(value: _table())),
    );

void main() {
  testWidgets('with semantics off a short table builds only the rows around what is visible', (tester) async {
    await tester.pumpWidget(_app());
    await tester.pumpAndSettle();
    expect(find.text('row 0'), findsOneWidget);
    expect(find.text('row ${_rows - 1}'), findsNothing);

    final position = tester.state<ScrollableState>(find.byType(Scrollable).last).position;
    position.jumpTo(position.maxScrollExtent);
    await tester.pumpAndSettle();
    expect(find.text('row ${_rows - 1}'), findsOneWidget, reason: 'the scroll extent still spans every row');
    expect(find.text('row 0'), findsNothing);
  }, semanticsEnabled: false);

  testWidgets('with semantics on every row of a short table has its node', (tester) async {
    final semantics = tester.ensureSemantics();
    await tester.pumpWidget(_app());
    await tester.pumpAndSettle();
    expect(find.text('row ${_rows - 1}'), findsOneWidget);
    expect(find.bySemanticsIdentifier('TableItem/${100 + _rows - 1}'), findsOneWidget);
    semantics.dispose();
  });

  testWidgets('semantics turned on later build the rows it left out', (tester) async {
    await tester.pumpWidget(_app());
    await tester.pumpAndSettle();
    expect(find.text('row ${_rows - 1}'), findsNothing);

    final semantics = tester.ensureSemantics();
    await tester.pumpAndSettle();
    expect(find.text('row ${_rows - 1}'), findsOneWidget);
    semantics.dispose();
  }, semanticsEnabled: false);
}
