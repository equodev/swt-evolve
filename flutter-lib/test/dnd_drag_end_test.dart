// Natively a DragSource hears every drag end: DND.DragEnd with the operation a drop performed, or
// with DROP_NONE when the drag was cancelled or released over nothing that takes it. The client
// tells the DragSource each time a drag ends; a drop reaches its DropTarget first, so Java can tell
// a drag a drop has ended from one nothing was dropped on.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart' show EquoCommBase;
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';

import 'delivery/support/deliver.dart';

const int _dragSourceId = 9;
const int _dropTargetId = 7;

VTree _tree() => VTree()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..dragSource = true
  ..dragSourceId = _dragSourceId
  ..dropTargetId = _dropTargetId
  ..items = [for (final text in ['Source', 'Middle', 'Target']) VTreeItem()..id = text.hashCode..text = text]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 400
    ..height = 300);

/// The channels the client sent on, in order, that are about the drag's end or its drop.
List<String> _dragEndChannels() => [
      for (final frame in EquoCommService.commForTesting.sentFrames)
        EquoCommBase.decodeFrame(frame).$1,
    ].where((c) => c.endsWith('/Drag/dragFinished') || c.endsWith('/Drop/drop')).toList();

Future<TestGesture> _dragFrom(WidgetTester tester, Offset start, Offset end) async {
  final gesture = await tester.createGesture(kind: PointerDeviceKind.mouse, buttons: kPrimaryButton);
  await gesture.addPointer(location: start);
  await gesture.down(start);
  await tester.pump(const Duration(milliseconds: 50));
  for (var i = 1; i <= 6; i++) {
    await gesture.moveTo(Offset.lerp(start, end, i / 6)!);
    await tester.pump(const Duration(milliseconds: 20));
  }
  return gesture;
}

void main() {
  setUp(() {
    freshClient();
    EquoCommBase.recordSentFrames = true;
    EquoCommService.commForTesting.clearSentFrames();
  });
  tearDown(() => EquoCommBase.recordSentFrames = false);

  Future<void> pumpTree(WidgetTester tester) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(width: 400, height: 300, child: TreeSwt<VTree>(value: _tree())),
    ));
    await tester.pump();
  }

  testWidgets('a drag released over nothing that takes it tells its DragSource it is over',
      (tester) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Row(children: [
        SizedBox(width: 400, height: 300, child: TreeSwt<VTree>(value: _tree()..dropTargetId = null)),
        const SizedBox(width: 200, height: 300),
      ]),
    ));
    await tester.pump();

    final gesture = await _dragFrom(tester, tester.getCenter(find.text('Source')), const Offset(500, 150));
    await gesture.up();
    await tester.pumpAndSettle();

    expect(_dragEndChannels(), ['DragSource/$_dragSourceId/Drag/dragFinished']);
    await tester.pump(const Duration(milliseconds: 400));
  });

  testWidgets('a drag dropped on a drop target tells its DragSource after the drop', (tester) async {
    await pumpTree(tester);

    final gesture = await _dragFrom(
        tester, tester.getCenter(find.text('Source')), tester.getCenter(find.text('Target')));
    await gesture.up();
    await tester.pumpAndSettle();

    expect(_dragEndChannels(), [
      'DropTarget/$_dropTargetId/Drop/drop',
      'DragSource/$_dragSourceId/Drag/dragFinished',
    ]);
    await tester.pump(const Duration(milliseconds: 400));
  });
}
