// A drop target reports where the pointer is relative to its control. Java maps that to display
// coordinates through the control, which is the only party that knows where the control sits in
// SWT terms: under a UI scale, or in a window away from the screen's origin, the pointer's global
// Flutter position is not an SWT coordinate.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/impl/utils/dnd_utils.dart';

import 'support/drag_gesture.dart';

void main() {
  testWidgets('a drop on a scaled, offset control reports the position local to that control',
      (tester) async {
    Offset? dropped;
    final state = VCanvas()
      ..id = 1
      ..dropTargetId = 1;

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(width: 800, height: 600, child: Stack(children: [
        Positioned(
          left: 0,
          top: 0,
          child: Draggable<DndDragPayload>(
            data: DndDragPayload(sourceControlId: 2),
            dragAnchorStrategy: pointerDragAnchorStrategy,
            feedback: const SizedBox(width: 1, height: 1),
            child: const ColoredBox(
              color: Colors.blue,
              child: SizedBox(key: Key('source'), width: 40, height: 40),
            ),
          ),
        ),
        Positioned(
          left: 300,
          top: 200,
          child: Transform.scale(
            scale: 0.5,
            alignment: Alignment.topLeft,
            child: wrapDropTarget<DndDragPayload>(
              state: state,
              onDrop: (_, __, ___, position) => dropped = position,
              child: const SizedBox(key: Key('target'), width: 200, height: 100),
            ),
          ),
        ),
      ])),
    ));

    await dragInSteps(tester, tester.getCenter(find.byKey(const Key('source'))),
        tester.getCenter(find.byKey(const Key('target'))));
    await tester.pump();

    expect(dropped, isNotNull);
    expect(dropped!.dx, closeTo(100, 1));
    expect(dropped!.dy, closeTo(50, 1));
  });
}
