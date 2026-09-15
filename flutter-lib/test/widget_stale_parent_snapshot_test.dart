// A widget reaches Flutter twice: on its own channel, and nested inside an ancestor's serialized
// tree. Which of the two descriptions counts is decided by when Java wrote it — every value carries
// the stamp of its own write — so an ancestor's copy of a child is taken only when it is newer than
// what the child already has. Without that, a table rewinds to the ancestor's snapshot and a row
// that was just added stays invisible until something unrelated repaints it.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
import 'package:swtflutter/src/impl/table_evolve.dart';

import 'delivery/support/deliver.dart';

const int _tableId = 4242;
const int _parentId = 1;

VTable _table({required int seq, required List<String> rows}) => VTable()
  ..id = _tableId
  ..seq = seq
  ..style = SWT.NONE
  ..headerVisible = false
  ..linesVisible = false
  ..columns = [
    VTableColumn()
      ..id = 1
      ..width = 200
      ..alignment = SWT.LEFT
      ..text = 'Method'
  ]
  ..items = [
    for (int i = 0; i < rows.length; i++)
      VTableItem()
        ..id = 100 + i
        ..texts = [rows[i]]
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 200
    ..height = 300);

VComposite _parent(List<VControl> children, {required int seq}) => VComposite()
  ..id = _parentId
  ..seq = seq
  ..style = SWT.NONE
  ..children = children;

List<String?> _renderedRows(WidgetTester tester) {
  final state = tester.state<TableImpl>(find.byType(TableSwt<VTable>));
  return state.getItems().map((i) => i.texts!.first).toList();
}

void main() {
  Widget host(VComposite value) => EvolveApp(
        theme: ThemeMode.light,
        contentWidget: SizedBox(
          width: 200,
          height: 300,
          child: CompositeSwt<VComposite>(value: value),
        ),
      );

  testWidgets('an older parent-carried snapshot does not rewind the table',
      (WidgetTester tester) async {
    // The table's own channel already delivered the row that was just added.
    await tester.pumpWidget(host(_parent([_table(seq: 20, rows: ['GET', 'AAAA'])], seq: 20)));
    await tester.pumpAndSettle();
    expect(_renderedRows(tester), ['GET', 'AAAA']);

    // The ancestor is described again for its own reasons, carrying the table as it was serialized
    // *before* that add.
    await deliverWhole(_parent([_table(seq: 12, rows: ['GET'])], seq: 25));
    await tester.pumpAndSettle();

    expect(_renderedRows(tester), ['GET', 'AAAA'],
        reason: 'a lower stamp is an older description and must not replace newer state');
  });

  testWidgets('a newer parent-carried snapshot is still adopted',
      (WidgetTester tester) async {
    await tester.pumpWidget(host(_parent([_table(seq: 20, rows: ['GET', 'AAAA'])], seq: 20)));
    await tester.pumpAndSettle();

    // The shape of a folded delivery: a dirty table sent inside the dirty ancestor above it rather
    // than on its own channel, so the ancestor's copy is the only description there is.
    await deliverWhole(_parent([_table(seq: 31, rows: ['GET', 'AAAA', 'BBBBB'])], seq: 30));
    await tester.pumpAndSettle();

    expect(_renderedRows(tester), ['GET', 'AAAA', 'BBBBB']);
  });

  testWidgets('the same stamp carries nothing new', (WidgetTester tester) async {
    await tester.pumpWidget(host(_parent([_table(seq: 20, rows: ['GET'])], seq: 20)));
    await tester.pumpAndSettle();

    await deliverWhole(_parent([_table(seq: 20, rows: ['GET', 'AAAA'])], seq: 25));
    await tester.pumpAndSettle();

    expect(_renderedRows(tester), ['GET'],
        reason: 'stamps come from one counter that moves on every write, so two descriptions '
            'stamped alike are the same description and the held one already is it');
  });
}
