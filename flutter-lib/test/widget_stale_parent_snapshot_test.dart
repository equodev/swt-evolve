// A widget reaches Flutter twice: on its own channel, and nested inside an ancestor's
// serialized tree. Flutter rebuilds top-down, so an ancestor rebuild can hand a child a copy
// older than one the child already applied. Without the seq check the older copy wins and the
// widget renders one update behind — a row added to a table stays invisible until something
// unrelated repaints it.

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

const int _tableId = 4242;

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

VComposite _parent(List<VControl> children) => VComposite()
  ..id = 1
  ..seq = 1
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
    await tester.pumpWidget(host(_parent([_table(seq: 20, rows: ['GET', 'AAAA'])])));
    await tester.pumpAndSettle();
    expect(_renderedRows(tester), ['GET', 'AAAA']);

    // The ancestor now rebuilds carrying the snapshot it serialized *before* that add.
    await tester.pumpWidget(host(_parent([_table(seq: 12, rows: ['GET'])])));
    await tester.pumpAndSettle();

    expect(_renderedRows(tester), ['GET', 'AAAA'],
        reason: 'a lower seq is an older snapshot and must not replace newer state');
  });

  testWidgets('a newer parent-carried snapshot is still adopted',
      (WidgetTester tester) async {
    await tester.pumpWidget(host(_parent([_table(seq: 20, rows: ['GET', 'AAAA'])])));
    await tester.pumpAndSettle();

    await tester.pumpWidget(
        host(_parent([_table(seq: 31, rows: ['GET', 'AAAA', 'BBBBB'])])));
    await tester.pumpAndSettle();

    expect(_renderedRows(tester), ['GET', 'AAAA', 'BBBBB']);
  });

  testWidgets('unstamped payloads keep the previous adopt-always behaviour',
      (WidgetTester tester) async {
    // Nothing outside the Java serializer sets seq, so both sides stay at 0 and the
    // comparison must not start dropping legitimate parent-driven updates.
    await tester.pumpWidget(host(_parent([_table(seq: 0, rows: ['GET'])])));
    await tester.pumpAndSettle();

    await tester.pumpWidget(host(_parent([_table(seq: 0, rows: ['GET', 'AAAA'])])));
    await tester.pumpAndSettle();

    expect(_renderedRows(tester), ['GET', 'AAAA']);
  });
}
