// Java owns where a Table is scrolled: Table.setTopIndex/showItem name the row to bring to the top, and
// the client reports how far the user scrolled, which Table.getTopIndex, item bounds and getItem(Point)
// are computed from. The coordinates of a cell click are the table's own, as getItem(Point) reads them.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tableitem.dart';

import 'package:swtflutter/src/gen/widgets.dart' show WireKey;

import 'delivery/support/deliver.dart';

VTable _table({int? topIndex, int seq = 1}) => VTable()
  ..id = 1
  ..seq = seq
  ..style = SWT.SINGLE
  ..enabled = true
  ..topIndex = topIndex
  ..items = [
    for (var i = 0; i < 60; i++)
      VTableItem()
        ..id = 100 + i
        ..seq = seq
        ..texts = ['row $i'],
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 300
    ..height = 120);

late Rect _tableRect;

Future<void> _pump(WidgetTester tester, VTable table) async {
  await tester.pumpWidget(const SizedBox.shrink());
  freshClient();
  final widget = TableSwt<VTable>(value: table);
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(width: 300, height: 120, child: widget),
    ),
  ));
  await tester.pumpAndSettle();
  _tableRect = tester.getRect(find.byWidget(widget));
}

/// Where row [row]'s text is, from the top of the table; a row at the top has its text at [_textInset].
double _rowTop(WidgetTester tester, int row) =>
    tester.getRect(find.text('row $row')).top - _tableRect.top;

/// How far below its row's top a cell's text is drawn (the cell's padding).
const double _textInset = 4.0;

List<VEvent> _sent(String suffix) => EquoCommService.commForTesting.sentFrames
    .where((frame) => EquoCommBase.decodeFrame(frame).$1.endsWith(suffix))
    .map(EquoCommBase.decodeEvent)
    .toList();

void main() {
  setUp(() => EquoCommBase.recordSentFrames = true);
  tearDown(() {
    EquoCommBase.recordSentFrames = false;
    EquoCommService.commForTesting.clearSentFrames();
  });

  testWidgets('a table that names a top index shows that row at the top', (tester) async {
    await _pump(tester, _table(topIndex: 20));

    expect(_rowTop(tester, 20), closeTo(_textInset, 1));
  });

  testWidgets('a new top index from Java scrolls the rows to it', (tester) async {
    final table = _table();
    await _pump(tester, table);
    expect(_rowTop(tester, 0), closeTo(_textInset, 1));

    await deliverChange(_table(topIndex: 30, seq: 10), keys: [WireKey.k_Table_topIndex], base: 1);
    await tester.pumpAndSettle();

    expect(_rowTop(tester, 30), closeTo(_textInset, 1));
  });

  testWidgets('a scroll by the user is reported as the distance the rows moved', (tester) async {
    await _pump(tester, _table());
    EquoCommService.commForTesting.clearSentFrames();

    await tester.drag(find.text('row 2'), const Offset(0, -100));
    await tester.pumpAndSettle();

    final scrolls = _sent('/Scroll/Scroll');
    expect(scrolls, isNotEmpty);
    expect(scrolls.last.y!.toDouble(), closeTo(_textInset - _rowTop(tester, 0), 1));
  });

  testWidgets('a click on a row of a scrolled table is in the table\'s own coordinates', (tester) async {
    await _pump(tester, _table(topIndex: 20));
    EquoCommService.commForTesting.clearSentFrames();

    await tester.tap(find.text('row 21'));
    await tester.pumpAndSettle();

    final downs = _sent('/Mouse/MouseDown');
    expect(downs, isNotEmpty);
    final y = downs.last.y!.toDouble();
    final row = tester.getRect(find.text('row 21'));
    expect(y, greaterThanOrEqualTo(_rowTop(tester, 20)));
    expect(y, lessThan(row.bottom - _tableRect.top + 8));
  });
}
