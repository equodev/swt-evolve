// Natively, pressing an unselected Tree or Table item selects it on the mouse down, so by the time
// DragDetect is raised the selection already holds the item being dragged. A DragStart listener
// that builds the drag from the selection (a JFace viewer's drag adapter does) depends on that
// order: with the selection still holding the previous item, the first drag of an item carries the
// wrong data, or none.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';

import 'delivery/support/deliver.dart';
import 'support/drag_gesture.dart';

class _RecordingTreeSwt extends TreeSwt<VTree> {
  const _RecordingTreeSwt({required super.value, required this.calls});

  final List<String> calls;

  @override
  void sendSelectionSelection(VTree val, VEvent? payload) => calls.add('selection');

  @override
  void sendDragDetectDragDetect(VTree val, VEvent? payload) => calls.add('dragDetect');
}

class _RecordingTableSwt extends TableSwt<VTable> {
  const _RecordingTableSwt({required super.value, required this.calls});

  final List<String> calls;

  @override
  void sendSelectionSelection(VTable val, VEvent? payload) => calls.add('selection');

  @override
  void sendDragDetectDragDetect(VTable val, VEvent? payload) => calls.add('dragDetect');
}

VRectangle _bounds() => VRectangle()
  ..x = 0
  ..y = 0
  ..width = 400
  ..height = 300;

Widget _wrap(Widget child) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(width: 400, height: 300, child: child),
    );

Future<void> _dragFrom(WidgetTester tester, Finder source) async {
  final start = tester.getCenter(source);
  await dragInSteps(tester, start, start + const Offset(0, 120));
  await tester.pumpAndSettle();
  // Nothing answers the dragStart verdict here; let the fail-open timer run out.
  await tester.pump(const Duration(milliseconds: 400));
}

void main() {
  setUp(freshClient);

  testWidgets('dragging an unselected Tree item selects it before raising DragDetect',
      (tester) async {
    final calls = <String>[];
    final tree = VTree()
      ..id = 1
      ..style = SWT.NONE
      ..enabled = true
      ..dragSource = true
      ..items = [
        VTreeItem()
          ..id = 10
          ..text = 'Item A',
        VTreeItem()
          ..id = 11
          ..text = 'Item B',
      ]
      ..bounds = _bounds();
    await tester.pumpWidget(_wrap(_RecordingTreeSwt(value: tree, calls: calls)));
    await tester.pump();

    await _dragFrom(tester, find.text('Item B'));

    expect(calls.where((c) => c == 'selection' || c == 'dragDetect').take(2),
        equals(['selection', 'dragDetect']));
  });

  testWidgets('dragging an unselected Table row selects it before raising DragDetect',
      (tester) async {
    final calls = <String>[];
    final table = VTable()
      ..id = 1
      ..style = SWT.NONE
      ..enabled = true
      ..headerVisible = false
      ..linesVisible = false
      ..dragSource = true
      ..columns = [
        VTableColumn()
          ..id = 1
          ..width = 400
          ..text = 'Col1'
      ]
      ..items = [
        VTableItem()
          ..id = 10
          ..texts = ['Row A'],
        VTableItem()
          ..id = 11
          ..texts = ['Row B'],
      ]
      ..bounds = _bounds();
    await tester.pumpWidget(_wrap(_RecordingTableSwt(value: table, calls: calls)));
    await tester.pump();

    await _dragFrom(tester, find.text('Row B'));

    expect(calls.where((c) => c == 'selection' || c == 'dragDetect').take(2),
        equals(['selection', 'dragDetect']));
  });
}
