// A drag that starts on a Canvas DragSource (a self-drawn grid such as a NatTable) is the same SWT
// drag as one that starts on a Tree: the DropTarget under the pointer is entered, dragged over and
// dropped on.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';

import 'delivery/support/deliver.dart';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VCanvas _canvas({required bool dragSource}) => VCanvas()
  ..id = 1
  ..style = SWT.BORDER
  ..enabled = true
  ..visible = true
  ..dragSource = dragSource
  ..bounds = _rect(0, 0, 200, 200);

VTree _tree() => VTree()
  ..id = 2
  ..style = SWT.BORDER
  ..enabled = true
  ..visible = true
  ..dropTargetId = 9
  ..items = [VTreeItem()
    ..id = 20
    ..text = 'Inventory']
  ..bounds = _rect(300, 0, 200, 200);

Widget _placed(VRectangle b, Widget child) => Positioned(
      left: b.x.toDouble(),
      top: b.y.toDouble(),
      width: b.width.toDouble(),
      height: b.height.toDouble(),
      child: child,
    );

List<String> _dropEvents() => EquoCommService.commForTesting.sentFrames
    .map(EquoCommBase.decodeFrame)
    .map((f) => f.$1)
    .where((action) => action.startsWith('DropTarget/9/Drop/'))
    .map((action) => action.substring('DropTarget/9/Drop/'.length))
    .toList();

void main() {
  for (final attachedLater in [false, true]) {
  testWidgets('a drag from a Canvas DragSource enters and drops on a Tree DropTarget'
      '${attachedLater ? ', the DragSource attached once the Canvas is shown' : ''}', (tester) async {
    EquoCommBase.recordSentFrames = true;
    addTearDown(() => EquoCommBase.recordSentFrames = false);
    final canvas = _canvas(dragSource: !attachedLater);
    final tree = _tree();
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 500,
        height: 200,
        child: Stack(children: [
          _placed(canvas.bounds!, CanvasSwt<VCanvas>(value: canvas)),
          _placed(tree.bounds!, TreeSwt<VTree>(value: tree)),
        ]),
      ),
    ));
    await tester.pumpAndSettle();
    if (attachedLater) {
      await deliverWhole(_canvas(dragSource: true));
      await tester.pumpAndSettle();
    }
    EquoCommService.commForTesting.clearSentFrames();

    const start = Offset(100, 20);
    final end = tester.getCenter(find.text('Inventory'));
    final gesture = await tester.startGesture(start);
    await tester.pump(const Duration(milliseconds: 50));
    const steps = 8;
    for (var i = 1; i <= steps; i++) {
      await gesture.moveTo(Offset.lerp(start, end, i / steps)!);
      await tester.pump(const Duration(milliseconds: 20));
    }
    await gesture.up();
    await tester.pumpAndSettle();
    // Nothing answers the dragStart verdict here; let the fail-open timer run out.
    await tester.pump(const Duration(milliseconds: 400));

    final events = _dropEvents();
    expect(events, contains('dragEnter'));
    expect(events, contains('drop'));
  });
  }
}
