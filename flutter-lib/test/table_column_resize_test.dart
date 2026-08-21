// Table column headers must offer a resize sash, like native SWT.
//
// A TableColumn is resizable by default (SWT sets resizable = true and only
// setResizable(false) clears it). Native SWT draws no sash widget: the header
// itself switches to the column-resize cursor within a few pixels of a column
// boundary and drags the boundary from there. JFace TableViewer tables built
// with TableViewerColumn inherit that for free, so an app that never touches
// resizing still gets resizable columns.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tableitem.dart';

const double _tableWidth = 600;
const double _tableHeight = 300;
const double _col0Width = 150;
const double _col1Width = 150;

VTableColumn _col(int id, String text, {int? width, bool? resizable}) =>
    VTableColumn()
      ..id = id
      ..text = text
      ..width = width
      ..resizable = resizable;

VTable _table({required List<VTableColumn> columns}) => VTable()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..headerVisible = true
  ..linesVisible = true
  ..columns = columns
  ..items = [
    VTableItem()
      ..id = 10
      ..texts = ['a', 'b'],
    VTableItem()
      ..id = 11
      ..texts = ['c', 'd'],
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = _tableWidth.toInt()
    ..height = _tableHeight.toInt());

Widget _wrap(VTable value) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: _tableWidth,
        height: _tableHeight,
        child: TableSwt<VTable>(value: value),
      ),
    );

/// The header is the first (topmost) Flutter [Table]; the body is the last.
Finder _headerTable() => find.byType(Table).first;

double _headerCellWidth(WidgetTester tester, int index) {
  final header = tester.widget<Table>(_headerTable());
  final cell = header.children.first.children[index];
  return tester.getSize(find.byWidget(cell).first).width;
}

double _bodyCellWidth(WidgetTester tester, int index) {
  final body = tester.widgetList<Table>(find.byType(Table)).last;
  final cell = body.children.first.children[index];
  return tester.getSize(find.byWidget(cell).first).width;
}

/// Global position of the boundary between column [index] and the next one.
Offset _boundary(WidgetTester tester, int index) {
  final header = tester.widget<Table>(_headerTable());
  final cell = header.children.first.children[index];
  final rect = tester.getRect(find.byWidget(cell).first);
  return Offset(rect.right, rect.center.dy);
}

const int _mousePointer = 1;

Future<TestGesture> _hover(WidgetTester tester, Offset position) async {
  final gesture = await tester.createGesture(
      kind: PointerDeviceKind.mouse, pointer: _mousePointer);
  await gesture.addPointer(location: Offset.zero);
  addTearDown(gesture.removePointer);
  await tester.pump();
  await gesture.moveTo(position);
  await tester.pumpAndSettle();
  return gesture;
}

MouseCursor _activeCursor() =>
    RendererBinding.instance.mouseTracker.debugDeviceActiveCursor(_mousePointer)!;

void main() {
  testWidgets('hovering a column boundary shows the resize cursor',
      (tester) async {
    await tester.pumpWidget(_wrap(_table(columns: [
      _col(1, 'Name', width: _col0Width.toInt()),
      _col(2, 'Value', width: _col1Width.toInt()),
    ])));
    await tester.pumpAndSettle();

    await _hover(tester, _boundary(tester, 0));

    expect(_activeCursor(), SystemMouseCursors.resizeColumn);
  });

  testWidgets('dragging a column boundary resizes header and body together',
      (tester) async {
    await tester.pumpWidget(_wrap(_table(columns: [
      _col(1, 'Name', width: _col0Width.toInt()),
      _col(2, 'Value', width: _col1Width.toInt()),
    ])));
    await tester.pumpAndSettle();

    expect(_headerCellWidth(tester, 0), _col0Width);

    final start = _boundary(tester, 0);
    final gesture = await tester.startGesture(start, kind: PointerDeviceKind.mouse);
    await gesture.moveTo(start + const Offset(60, 0));
    await tester.pump();
    await gesture.up();
    await tester.pumpAndSettle();

    expect(_headerCellWidth(tester, 0), closeTo(_col0Width + 60, 1));
    expect(_bodyCellWidth(tester, 0), closeTo(_col0Width + 60, 1));
    // The neighbour keeps its width: SWT resizes only the dragged column.
    expect(_headerCellWidth(tester, 1), closeTo(_col1Width, 1));
  });

  testWidgets('a drag survives a state push that swaps the column objects',
      (tester) async {
    await tester.pumpWidget(_wrap(_table(columns: [
      _col(1, 'Name', width: _col0Width.toInt()),
      _col(2, 'Value', width: _col1Width.toInt()),
    ])));
    await tester.pumpAndSettle();

    final start = _boundary(tester, 0);
    final gesture =
        await tester.startGesture(start, kind: PointerDeviceKind.mouse);
    await gesture.moveTo(start + const Offset(40, 0));
    await tester.pump();
    expect(_headerCellWidth(tester, 0), closeTo(_col0Width + 40, 1));

    // Java acknowledges the resize with a fresh payload: same ids, new V objects.
    // The handle closes over the column it was built with, so without a rebuild
    // reaching the live recognizer the rest of the drag would mutate an object
    // no longer in state.columns and the column would freeze mid-drag.
    final pushed = _table(columns: [
      _col(1, 'Name', width: (_col0Width + 40).toInt()),
      _col(2, 'Value', width: _col1Width.toInt()),
    ])..seq = 2;
    await tester.pumpWidget(_wrap(pushed));
    await tester.pump();

    await gesture.moveTo(start + const Offset(80, 0));
    await tester.pump();
    await gesture.up();
    await tester.pumpAndSettle();

    expect(_headerCellWidth(tester, 0), closeTo(_col0Width + 80, 1));
  });

  testWidgets('a column with resizable = false cannot be dragged',
      (tester) async {
    await tester.pumpWidget(_wrap(_table(columns: [
      _col(1, 'Name', width: _col0Width.toInt(), resizable: false),
      _col(2, 'Value', width: _col1Width.toInt()),
    ])));
    await tester.pumpAndSettle();

    await _hover(tester, _boundary(tester, 0));
    expect(_activeCursor(), isNot(SystemMouseCursors.resizeColumn));

    final start = _boundary(tester, 0);
    final drag = await tester.startGesture(start, kind: PointerDeviceKind.mouse);
    await drag.moveTo(start + const Offset(60, 0));
    await tester.pump();
    await drag.up();
    await tester.pumpAndSettle();

    expect(_headerCellWidth(tester, 0), _col0Width);
  });
}
