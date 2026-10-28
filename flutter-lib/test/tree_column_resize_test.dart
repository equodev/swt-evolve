// A tree column's header offers a resize grip on its right boundary, like native SWT: a TreeColumn is
// resizable unless setResizable(false) says otherwise, and dragging the boundary resizes the column.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treecolumn.dart';

VTreeColumn _column(int id, String text, {bool? resizable}) => VTreeColumn()
  ..id = id
  ..text = text
  ..width = 120
  ..resizable = resizable;

VTree _tree(List<VTreeColumn> columns) => VTree()
  ..id = 1
  ..style = SWT.SINGLE
  ..enabled = true
  ..headerVisible = true
  ..columns = columns
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 400
    ..height = 200);

Future<void> _pump(WidgetTester tester, List<VTreeColumn> columns) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(width: 400, height: 200, child: TreeSwt<VTree>(value: _tree(columns))),
  ));
  await tester.pumpAndSettle();
}

Rect _header(WidgetTester tester, int index) =>
    tester.getRect(find.byWidgetPredicate((w) => w is TreeColumnSwt).at(index));

/// The boundary at the right of the first column: where the second column's header starts.
Offset _firstBoundary(WidgetTester tester) {
  final second = _header(tester, 1);
  return Offset(second.left, second.center.dy);
}

const int _mouse = 1;

Future<void> _hover(WidgetTester tester, Offset position) async {
  final gesture = await tester.createGesture(kind: PointerDeviceKind.mouse, pointer: _mouse);
  await gesture.addPointer(location: Offset.zero);
  addTearDown(gesture.removePointer);
  await tester.pump();
  await gesture.moveTo(position);
  await tester.pumpAndSettle();
}

MouseCursor _cursor() => RendererBinding.instance.mouseTracker.debugDeviceActiveCursor(_mouse)!;

void main() {
  testWidgets('hovering a column boundary shows the resize cursor', (tester) async {
    await _pump(tester, [_column(2, 'Name'), _column(3, 'Value')]);

    await _hover(tester, _firstBoundary(tester));

    expect(_cursor(), SystemMouseCursors.resizeColumn);
  });

  testWidgets('dragging a column boundary resizes the column', (tester) async {
    await _pump(tester, [_column(2, 'Name'), _column(3, 'Value')]);
    final before = _firstBoundary(tester);

    final gesture = await tester.startGesture(before, kind: PointerDeviceKind.mouse);
    await gesture.moveTo(before + const Offset(60, 0));
    await tester.pump();
    await gesture.up();
    await tester.pumpAndSettle();

    expect(_firstBoundary(tester).dx, closeTo(before.dx + 60, 1));
  });

  testWidgets('a column that is not resizable offers no grip', (tester) async {
    await _pump(tester, [_column(2, 'Name', resizable: false), _column(3, 'Value')]);
    final before = _firstBoundary(tester);

    await _hover(tester, before);
    expect(_cursor(), isNot(SystemMouseCursors.resizeColumn));

    final gesture = await tester.startGesture(before, kind: PointerDeviceKind.mouse);
    await gesture.moveTo(before + const Offset(60, 0));
    await tester.pump();
    await gesture.up();
    await tester.pumpAndSettle();
    expect(_firstBoundary(tester).dx, closeTo(before.dx, 1));
  });
}
