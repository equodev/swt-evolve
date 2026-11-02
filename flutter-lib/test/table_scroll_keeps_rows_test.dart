// A long Table builds only a window of rows around what is visible, and a scroll moves that window.
// The rows that stay in it must keep their cells: handed the next item's content instead, every
// cell in the window would lay its text out again on every scroll.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tableitem.dart';

VTable _table() => VTable()
  ..id = 1
  ..seq = 1
  ..style = SWT.BORDER | SWT.FULL_SELECTION
  ..headerVisible = true
  ..linesVisible = true
  ..itemCount = 1000
  ..columns = [
    for (var c = 0; c < 2; c++)
      VTableColumn()
        ..id = 2 + c
        ..seq = 1
        ..width = 140
        ..text = 'Column $c'
  ]
  ..items = [
    for (var i = 0; i < 1000; i++)
      VTableItem()
        ..id = 100 + i
        ..seq = 1
        ..texts = ['row $i / 0', 'row $i / 1']
  ]
  ..bounds = (VRectangle()
    ..width = 300
    ..height = 300);

void main() {
  testWidgets('a scroll keeps the cells of the rows that stay in the window', (tester) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(width: 300, height: 300, child: TableSwt<VTable>(value: _table())),
    ));
    await tester.pumpAndSettle();
    const probe = 'row 8 / 1';
    final before = tester.renderObject(find.text(probe));

    final position = tester.state<ScrollableState>(find.byType(Scrollable).last).position;
    position.jumpTo(position.pixels + 5 * 24);
    await tester.pumpAndSettle();

    expect(find.text(probe), findsOneWidget);
    expect(tester.renderObject(find.text(probe)), same(before));
    expect(find.text('row 0 / 0'), findsNothing, reason: 'the window must have moved');
  });
}
