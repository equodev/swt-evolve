// A SWT.VIRTUAL table declares more rows than it has data for. Building every declared row would put
// the cost back where the windowing removed it.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tableitem.dart';

const int _declaredRows = 50000;
const int _loadedRows = 5000;
const double _viewportHeight = 300;

VTable _virtualTable() => VTable()
  ..id = 1
  ..style = SWT.VIRTUAL
  ..headerVisible = false
  ..linesVisible = false
  ..itemCount = _declaredRows
  ..columns = [
    VTableColumn()
      ..id = 2
      ..width = 200
      ..alignment = SWT.LEFT
      ..text = 'Col1'
  ]
  ..items = [
    for (int i = 0; i < _loadedRows; i++)
      VTableItem()
        ..id = 100 + i
        ..texts = ['r$i']
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 200
    ..height = _viewportHeight.toInt());

/// The generated TableSwt with sendEvent captured: EquoCommService needs a live transport.
class _CapturingTableSwt extends TableSwt<VTable> {
  const _CapturingTableSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VTable val, String ev, VEvent? payload) => onEvent(ev, payload);
}

Widget _wrap(VTable value,
        {void Function(String ev, VEvent? payload)? onEvent}) =>
    EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 200,
        height: _viewportHeight,
        child: onEvent == null
            ? TableSwt<VTable>(value: value)
            : _CapturingTableSwt(value: value, onEvent: onEvent),
      ),
    );

double _maxScrollExtent(WidgetTester tester) =>
    tester.widget<Scrollable>(find.byType(Scrollable).last).controller!.position.maxScrollExtent;

int _bodyRowCount(WidgetTester tester) =>
    tester.widgetList<Table>(find.byType(Table)).last.children.length;

void main() {
  testWidgets('a virtual table builds a window of rows, not every declared row',
      (tester) async {
    await tester.pumpWidget(_wrap(_virtualTable()));
    await tester.pumpAndSettle();

    // Anything near _loadedRows means every row Java sent was built.
    expect(_bodyRowCount(tester), lessThan(_loadedRows ~/ 10));
  });

  testWidgets('the scroll extent spans every declared row, not just the loaded ones',
      (tester) async {
    await tester.pumpWidget(_wrap(_virtualTable()..itemCount = _loadedRows));
    await tester.pumpAndSettle();
    final loadedExtent = _maxScrollExtent(tester);

    await tester.pumpWidget(_wrap(_virtualTable()));
    await tester.pumpAndSettle();

    expect(_maxScrollExtent(tester), greaterThan(loadedExtent * 5));
  });

  testWidgets('it asks Java to run SetData for the rows it is about to show',
      (tester) async {
    final requests = <VEvent?>[];
    await tester.pumpWidget(_wrap(
      _virtualTable(),
      onEvent: (ev, payload) {
        if (ev == 'SetData/SetData') requests.add(payload);
      },
    ));
    await tester.pumpAndSettle();

    expect(requests, isNotEmpty);
    final window = requests.last!;
    expect(window.end, greaterThan(0));
    expect(window.end, lessThan(_loadedRows ~/ 10));
  });
}
