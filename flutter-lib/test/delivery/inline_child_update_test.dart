// A widget nothing renders in its own right.
//
// A TableItem has no State of its own: the Table draws every row from its own item list. So an
// update on the item's channel reaches its value and nothing redraws — which is why Java grew the
// habit of dirtying the Table whenever an item changed. That is expensive in the worst place: a
// one-cell edit re-serializes every row in the table.
//
// The client can tell these apart without being told which widgets they are. A value with nobody
// listening is a value nothing renders on its own, and then whatever holds it is what draws it.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tableitem.dart';

import 'support/deliver.dart';

const int _tableId = 500;

VTableItem _row(int id, String text, {required int seq}) => VTableItem()
  ..id = id
  ..seq = seq
  ..texts = [text];

VTable _table(List<VTableItem> items, {required int seq}) => VTable()
  ..id = _tableId
  ..seq = seq
  ..style = SWT.NONE
  ..headerVisible = false
  ..linesVisible = false
  ..columns = [
    VTableColumn()
      ..id = 1
      ..seq = seq
      ..width = 200
      ..alignment = SWT.LEFT
      ..text = 'Column'
  ]
  ..items = items
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 200
    ..height = 300);

Future<void> _mount(WidgetTester tester, VTable value) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 200,
      height: 300,
      child: TableSwt<VTable>(value: value),
    ),
  ));
  await tester.pumpAndSettle();
}

void main() {
  testWidgets('a row updated on its own channel is redrawn', (tester) async {
    await _mount(
        tester,
        _table([
          _row(100, 'first', seq: 1),
          _row(101, 'second', seq: 1),
        ], seq: 1));
    expect(find.text('second'), findsOneWidget);

    // Only the row is described. Nothing is sent about the table, which is the whole point: a row
    // changing must not cost a re-send of every other row.
    await deliverChange(_row(101, 'edited', seq: 10), changed: ['texts'], base: 1);
    await tester.pumpAndSettle();

    expect(find.text('edited'), findsOneWidget,
        reason: 'the table draws its rows, so the table is what has to redraw when one changes');
    expect(find.text('second'), findsNothing);
    expect(find.text('first'), findsOneWidget, reason: 'and the other rows are untouched');
  });

  testWidgets('a column updated on its own channel is redrawn', (tester) async {
    // The same again one level over: a Table draws its own headers, so a column has no renderer of
    // its own either.
    await _mount(tester, _table([_row(100, 'first', seq: 1)], seq: 1)..headerVisible = true);
    expect(find.text('Column'), findsOneWidget);

    await deliverChange(
        VTableColumn()
          ..id = 1
          ..seq = 10
          ..width = 200
          ..alignment = SWT.LEFT
          ..text = 'Renamed',
        changed: ['text'],
        base: 1);
    await tester.pumpAndSettle();

    expect(find.text('Renamed'), findsOneWidget);
  });

  testWidgets('the table keeps rendering rows it was told about as a whole', (tester) async {
    await _mount(tester, _table([_row(100, 'first', seq: 1)], seq: 1));

    await deliverWhole(_table([
      _row(100, 'first', seq: 10),
      _row(102, 'added', seq: 10),
    ], seq: 10));
    await tester.pumpAndSettle();

    expect(find.text('added'), findsOneWidget);
    expect(find.text('first'), findsOneWidget);
  });
}
