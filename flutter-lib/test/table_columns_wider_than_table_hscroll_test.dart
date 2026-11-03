// A Table whose columns are wider than the table scrolls sideways, as a native one does: the
// header and the rows move together, and Java is told how far, so its coordinate lookups follow.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
import 'package:swtflutter/src/gen/widgets.dart' show wireChannelNames;

// The Installed JREs page: Name, Location and Type in a table narrower than the three.
const List<int> _columnWidths = [195, 1479, 106];
const List<String> _columnNames = ['Name', 'Location', 'Type'];
const double _tableWidth = 587;
const double _tableHeight = 200;
const int _rows = 3;

class _CapturingTableSwt extends TableSwt<VTable> {
  const _CapturingTableSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VTable val, int channel, VEvent? payload) =>
      onEvent(wireChannelNames[channel]!, payload);
}

VTable _table({int rows = _rows}) => VTable()
  ..id = 1
  ..seq = 1
  ..style = SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION
  ..headerVisible = true
  ..linesVisible = true
  ..itemCount = rows
  ..columns = [
    for (int c = 0; c < _columnWidths.length; c++)
      VTableColumn()
        ..id = 10 + c
        ..seq = 1
        ..width = _columnWidths[c]
        ..alignment = SWT.LEFT
        ..text = _columnNames[c]
  ]
  ..items = [
    for (int i = 0; i < rows; i++)
      VTableItem()
        ..id = 100 + i
        ..seq = 1
        ..texts = ['jre $i', '/opt/jre$i', 'Standard VM $i']
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = _tableWidth.toInt()
    ..height = _tableHeight.toInt());

Widget _wrap(Widget table, {double width = _tableWidth}) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(width: width, height: _tableHeight, child: table),
      ),
    );

Finder _horizontalScrollables() => find.descendant(
      of: find.byType(_CapturingTableSwt),
      matching: find.byWidgetPredicate(
          (w) => w is Scrollable && axisDirectionToAxis(w.axisDirection) == Axis.horizontal),
    );

ScrollPosition _horizontalPosition(WidgetTester tester) =>
    tester.state<ScrollableState>(_horizontalScrollables().first).position;

ScrollPosition _verticalPosition(WidgetTester tester) => tester
    .state<ScrollableState>(find.descendant(
      of: find.byType(_CapturingTableSwt),
      matching: find.byWidgetPredicate(
          (w) => w is Scrollable && axisDirectionToAxis(w.axisDirection) == Axis.vertical),
    ).first)
    .position;

/// Lets the table be widened past the default 800 px test surface.
void _wideSurface(WidgetTester tester) {
  tester.view.physicalSize = const Size(2400, 600);
  tester.view.devicePixelRatio = 1;
  addTearDown(tester.view.reset);
}

Future<void> _scrollSideways(WidgetTester tester, double dx, {double dy = 0}) async {
  final pointer = TestPointer(1, PointerDeviceKind.mouse);
  final inside = tester.getRect(find.byType(_CapturingTableSwt)).center;
  await tester.sendEventToBinding(pointer.hover(inside));
  await tester.sendEventToBinding(pointer.scroll(Offset(dx, dy)));
  await tester.pumpAndSettle();
}

void main() {
  testWidgets('columns wider than the table: header and rows scroll sideways together',
      (tester) async {
    final reportedX = <int?>[];
    await tester.pumpWidget(_wrap(_CapturingTableSwt(
      value: _table(),
      onEvent: (ev, payload) {
        if (ev == 'Scroll/Scroll') reportedX.add(payload?.x);
      },
    )));
    await tester.pumpAndSettle();

    final tableRect = tester.getRect(find.byType(_CapturingTableSwt));
    expect(_horizontalScrollables(), findsWidgets,
        reason: 'the columns are wider than the table, so it must scroll sideways');

    final headerBefore = tester.getRect(find.text('Type'));
    final cellBefore = tester.getRect(find.text('Standard VM 0'));
    expect(headerBefore.left, greaterThan(tableRect.right),
        reason: 'the Type column starts past the right edge before any scroll');

    // A trackpad swipe / shift+wheel to the right.
    final pointer = TestPointer(1, PointerDeviceKind.mouse);
    final inside = tableRect.center;
    await tester.sendEventToBinding(pointer.hover(inside));
    await tester.sendEventToBinding(pointer.scroll(const Offset(2000, 0)));
    await tester.pumpAndSettle();

    final headerAfter = tester.getRect(find.text('Type'));
    final cellAfter = tester.getRect(find.text('Standard VM 0'));
    expect(headerAfter.left, lessThan(headerBefore.left), reason: 'the header must scroll');
    expect(cellAfter.left - cellBefore.left, closeTo(headerAfter.left - headerBefore.left, 0.5),
        reason: 'the rows must move by as much as the header');
    expect(headerAfter.right, lessThanOrEqualTo(tableRect.right),
        reason: 'scrolled to the end, the Type column is in view');
    expect(reportedX.whereType<int>().where((x) => x > 0), isNotEmpty,
        reason: 'Java must be told the sideways offset');
  });

  testWidgets('columns that fit the table do not scroll sideways', (tester) async {
    final table = _table()
      ..columns = [
        for (final c in _table().columns!) c..width = 150,
      ];
    await tester.pumpWidget(_wrap(_CapturingTableSwt(value: table, onEvent: (_, __) {})));
    await tester.pumpAndSettle();

    expect(_horizontalPosition(tester).maxScrollExtent, 0);
  });

  testWidgets('the horizontal scroll bar shows at rest, as Tree\'s does', (tester) async {
    await tester.pumpWidget(_wrap(_CapturingTableSwt(value: _table(), onEvent: (_, __) {})));
    await tester.pumpAndSettle();

    final bars = find.ancestor(
      of: _horizontalScrollables().first,
      matching: find.byWidgetPredicate((w) =>
          w is Scrollbar &&
          w.thumbVisibility == true &&
          w.controller?.position.axis == Axis.horizontal),
    );
    expect(bars, findsOneWidget, reason: 'an always-visible bar under the rows');
  });

  testWidgets('Java is told x 0 once the table widens until its columns fit', (tester) async {
    _wideSurface(tester);
    final reported = <VEvent>[];
    // The same widget, so only the layout changes: the table itself is not rebuilt.
    final swt = _CapturingTableSwt(
      value: _table(),
      onEvent: (ev, payload) {
        if (ev == 'Scroll/Scroll' && payload != null) reported.add(payload);
      },
    );
    Widget table(double width) => _wrap(swt, width: width);

    await tester.pumpWidget(table(_tableWidth));
    await tester.pumpAndSettle();
    await _scrollSideways(tester, 500);
    expect(reported.last.x, greaterThan(0));

    await tester.pumpWidget(table(2200));
    await tester.pumpAndSettle();

    expect(reported.last.x, 0, reason: 'the columns fit, nothing is scrolled sideways');
  });

  testWidgets('Java is told the clamped x when the table widens while its columns overflow',
      (tester) async {
    _wideSurface(tester);
    final reported = <VEvent>[];
    // The same widget, so only the layout changes: the table itself is not rebuilt.
    final swt = _CapturingTableSwt(
      value: _table(),
      onEvent: (ev, payload) {
        if (ev == 'Scroll/Scroll' && payload != null) reported.add(payload);
      },
    );
    Widget table(double width) => _wrap(swt, width: width);

    await tester.pumpWidget(table(_tableWidth));
    await tester.pumpAndSettle();
    await _scrollSideways(tester, 3000);
    final atEnd = reported.last.x!;

    await tester.pumpWidget(table(1200));
    await tester.pumpAndSettle();

    final shown = _horizontalPosition(tester).pixels.round();
    expect(shown, lessThan(atEnd), reason: 'a wider viewport has less to scroll');
    expect(reported.last.x, shown);
  });

  testWidgets('the rows keep their vertical position when the columns stop overflowing',
      (tester) async {
    _wideSurface(tester);
    Widget table(double width) =>
        _wrap(_CapturingTableSwt(value: _table(rows: 40), onEvent: (_, __) {}), width: width);

    await tester.pumpWidget(table(_tableWidth));
    await tester.pumpAndSettle();
    await _scrollSideways(tester, 0, dy: 150);
    final scrolledDown = _verticalPosition(tester).pixels;
    expect(scrolledDown, greaterThan(0));

    await tester.pumpWidget(table(2200));
    await tester.pumpAndSettle();

    expect(_verticalPosition(tester).pixels, scrolledDown);
  });
}
