// A Table's rows are laid out and scrolled on the Dart side. Java resolves getItem(Point) and an
// item's bounds from the top of the model, so the table reports how far it has scrolled, and the
// coordinate lookups it answers for Java take the same distance off.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
import 'package:swtflutter/src/impl/table_evolve.dart';

import 'delivery/support/deliver.dart';

const double _viewportHeight = 200;
const double _tableWidth = 200;
const int _rows = 60;
const int _firstId = 100;
const double _scrollTo = 57;
const double _probeX = 10;
const double _probeY = 5;

/// Same as the generated TableSwt, except sendEvent captures outgoing events instead of handing
/// them to EquoCommService, which needs a transport a widget test does not have.
class _CapturingTableSwt extends TableSwt<VTable> {
  const _CapturingTableSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VTable val, String ev, VEvent? payload) => onEvent(ev, payload);
}

VTable _table({int rows = _rows, int seq = 1}) => VTable()
  ..id = 1
  ..seq = seq
  ..style = SWT.NONE
  ..headerVisible = false
  ..linesVisible = false
  ..itemCount = rows
  ..columns = [
    VTableColumn()
      ..id = 2
      ..seq = seq
      ..width = _tableWidth.toInt()
      ..alignment = SWT.LEFT
      ..text = 'Col1'
  ]
  ..items = [
    for (int i = 0; i < rows; i++)
      VTableItem()
        ..id = _firstId + i
        ..seq = seq
        ..texts = ['row $i']
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = _tableWidth.toInt()
    ..height = _viewportHeight.toInt());

Widget _wrap(Widget table) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: _tableWidth,
        height: _viewportHeight,
        child: table,
      ),
    );

ScrollController _scroll(WidgetTester tester) =>
    tester.widget<Scrollable>(find.byType(Scrollable).last).controller!;

/// The id of the row Flutter paints at [y] below the table's top edge.
int? _rowPaintedAt(WidgetTester tester, double y) {
  final top = tester.getTopLeft(find.byType(_CapturingTableSwt)).dy;
  for (int i = 0; i < _rows; i++) {
    final cell = find.text('row $i');
    if (cell.evaluate().isEmpty) continue;
    final rect = tester.getRect(cell);
    if (rect.top <= top + y && top + y < rect.bottom) return _firstId + i;
  }
  return null;
}

void main() {
  testWidgets('a scroll is reported to Java and the lookups it answers follow the scroll',
      (tester) async {
    final reported = <int?>[];
    await tester.pumpWidget(_wrap(_CapturingTableSwt(
      value: _table(),
      onEvent: (ev, payload) {
        if (ev == 'Scroll/Scroll') reported.add(payload?.y);
      },
    )));
    await tester.pumpAndSettle();
    final impl = tester.state<TableImpl>(find.byType(_CapturingTableSwt));

    expect(impl.findItemIdAtPosition(_probeX, _probeY), _firstId,
        reason: 'before any scroll the probe point is on the first row');
    expect(reported, isEmpty, reason: 'nothing to report until the user scrolls');

    _scroll(tester).jumpTo(_scrollTo);
    await tester.pumpAndSettle();

    expect(reported, [_scrollTo.round()]);
    final painted = _rowPaintedAt(tester, _probeY);
    expect(painted, isNot(_firstId), reason: 'the scroll must have moved another row under the probe');
    expect(impl.findItemIdAtPosition(_probeX, _probeY), painted,
        reason: 'the lookup must name the row Flutter paints under the point');

    _scroll(tester).jumpTo(0);
    await tester.pumpAndSettle();

    expect(reported, [_scrollTo.round(), 0]);
    expect(impl.findItemIdAtPosition(_probeX, _probeY), _firstId);
  });

  testWidgets('a refresh that empties the table lands at the top, and Java is told so',
      (tester) async {
    final reported = <int?>[];
    await tester.pumpWidget(_wrap(_CapturingTableSwt(
      value: _table(),
      onEvent: (ev, payload) {
        if (ev == 'Scroll/Scroll') reported.add(payload?.y);
      },
    )));
    await tester.pumpAndSettle();
    _scroll(tester).jumpTo(_scrollTo);
    await tester.pumpAndSettle();
    expect(reported, [_scrollTo.round()]);

    // What a viewer's setInput does: remove every row, then add them back.
    await deliverWhole(_table(rows: 0, seq: 2));
    await tester.pumpAndSettle();
    await deliverWhole(_table(seq: 3));
    await tester.pumpAndSettle();

    expect(_scroll(tester).offset, 0, reason: 'the rebuilt scroll view starts at the top');
    expect(reported.last, 0, reason: 'Java would otherwise keep the pre-refresh offset');
  });
}
