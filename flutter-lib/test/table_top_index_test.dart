// Table.setTopIndex scrolls the rows on screen, and a scroll the user makes is told back to Java, so
// getTopIndex() answers what the screen shows.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart' show EquoCommBase;
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;

import 'delivery/support/deliver.dart';

const int _rows = 1000;
const double _viewportHeight = 300;

VTable _table({int topIndex = 0, int seq = 10}) => VTable()
  ..id = 1
  ..seq = seq
  ..style = 0
  ..headerVisible = false
  ..linesVisible = false
  ..itemCount = _rows
  ..topIndex = topIndex
  ..columns = [
    VTableColumn()
      ..id = 2
      ..width = 200
      ..text = 'Col1'
  ]
  ..items = [
    for (int i = 0; i < _rows; i++)
      VTableItem()
        ..id = 100 + i
        ..texts = ['r$i']
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 200
    ..height = _viewportHeight.toInt());

class _CapturingTableSwt extends TableSwt<VTable> {
  const _CapturingTableSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VTable val, String ev, VEvent? payload) => onEvent(ev, payload);
}

Widget _wrap(VTable value, List<VEvent?> reports) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 200,
        height: _viewportHeight,
        child: _CapturingTableSwt(
          value: value,
          onEvent: (ev, payload) {
            if (ev == 'SetData/SetData') reports.add(payload);
          },
        ),
      ),
    );

/// The row drawn at the top of the viewport.
int _topShownRow(WidgetTester tester) {
  final top = tester.getTopLeft(find.byType(Scrollable).last).dy;
  final shown = <int>[];
  for (final element in find.textContaining(RegExp(r'^r\d+$')).evaluate()) {
    final text = (element.widget as Text).data!;
    final box = element.renderObject! as RenderBox;
    final y = box.localToGlobal(Offset.zero).dy;
    if (y + box.size.height > top) shown.add(int.parse(text.substring(1)));
  }
  shown.sort();
  return shown.first;
}

void main() {
  setUp(freshClient);

  testWidgets('a table given a top index shows that row at the top', (tester) async {
    await tester.pumpWidget(_wrap(_table(topIndex: 500), []));
    await tester.pumpAndSettle();

    expect(_topShownRow(tester), 500);
  });

  testWidgets('a scroll by the user is reported as the row now at the top', (tester) async {
    final reports = <VEvent?>[];
    await tester.pumpWidget(_wrap(_table(), reports));
    await tester.pumpAndSettle();

    await tester.drag(find.byType(Scrollable).last, const Offset(0, -200));
    await tester.pumpAndSettle();

    expect(reports, isNotEmpty);
    expect(reports.last!.index, _topShownRow(tester));
    expect(reports.last!.index, greaterThan(0));
  });

  testWidgets('a rebuild after a scroll by the user keeps the row the user scrolled to',
      (tester) async {
    final reports = <VEvent?>[];
    final table = _table();
    await tester.pumpWidget(_wrap(table, reports));
    await tester.pumpAndSettle();
    await tester.drag(find.byType(Scrollable).last, const Offset(0, -200));
    await tester.pumpAndSettle();
    final scrolledTo = _topShownRow(tester);
    expect(scrolledTo, greaterThan(0));

    table
      ..linesVisible = true
      ..seq = 20;
    await deliverChange(table, keys: [WireKey.k_Table_linesVisible], base: 10);
    await tester.pumpAndSettle();

    expect(_topShownRow(tester), scrolledTo);
  });

  testWidgets('asking again for a top row the user scrolled away from scrolls back to it',
      (tester) async {
    final reports = <VEvent?>[];
    await tester.pumpWidget(_wrap(_table(topIndex: 500), reports));
    await tester.pumpAndSettle();
    await tester.drag(find.byType(Scrollable).last, const Offset(0, -200));
    await tester.pumpAndSettle();
    expect(_topShownRow(tester), isNot(500));

    await deliverWhole(_table(topIndex: 500, seq: 20));
    await tester.pumpAndSettle();

    expect(_topShownRow(tester), 500);
  });

  testWidgets('a scroll tells Java how far the rows moved', (tester) async {
    EquoCommBase.recordSentFrames = true;
    addTearDown(() => EquoCommBase.recordSentFrames = false);
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(width: 200, height: _viewportHeight, child: TableSwt<VTable>(value: _table())),
    ));
    await tester.pumpAndSettle();
    EquoCommService.commForTesting.clearSentFrames();

    final vertical = find.byWidgetPredicate((w) => w is Scrollable && w.axisDirection == AxisDirection.down);
    final position = tester.state<ScrollableState>(vertical.first).position;
    position.jumpTo(position.pixels + 40 * 100);
    await tester.pumpAndSettle();

    final reported = [
      for (final frame in EquoCommService.commForTesting.sentFrames)
        if (EquoCommBase.decodeFrame(frame).$1 == 'Table/1/Scroll/Scroll') EquoCommBase.decodeEvent(frame).y,
    ];
    expect(reported, isNotEmpty);
    expect(reported.last, position.pixels.round(),
        reason: 'Java works the first row out of how far the rows moved, so it is told exactly that');
  });
}
