// A Canvas that draws its own content (a grid, a chart) is a DragSource over its whole area, the
// same as natively: SWT raises DragDetect wherever the press lands inside the control. A childless
// Canvas renders no hit-testable box of its own under the DnD Draggable, so a press there must
// still reach the Draggable or the drag never starts.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

class _RecordingCanvasSwt extends CanvasSwt<VCanvas> {
  const _RecordingCanvasSwt({required super.value, required this.calls});

  final List<String> calls;

  @override
  void sendDragDetectDragDetect(VCanvas val, VEvent? payload) => calls.add('dragDetect');
}

VCanvas _canvas() => VCanvas()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..dragSource = true
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 200
    ..height = 100);

void main() {
  testWidgets('a drag on a childless Canvas DragSource raises DragDetect', (tester) async {
    final calls = <String>[];
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 200,
        height: 100,
        child: _RecordingCanvasSwt(value: _canvas(), calls: calls),
      ),
    ));
    await tester.pumpAndSettle();

    final gesture = await tester.createGesture(buttons: kPrimaryButton);
    await gesture.down(const Offset(100, 50));
    await tester.pump(const Duration(milliseconds: 20));
    for (var i = 0; i < 5; i++) {
      await gesture.moveBy(const Offset(10, 0));
      await tester.pump(const Duration(milliseconds: 20));
    }
    await gesture.up();
    // Past the drag-start veto window, which Java never answers here.
    await tester.pump(const Duration(seconds: 1));

    expect(calls, equals(['dragDetect']));
  });
}
