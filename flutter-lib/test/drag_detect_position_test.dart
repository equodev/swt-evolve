// SWT raises DragDetect only once the pointer has moved past the platform drag threshold, and fills
// the event's x/y with the press that began the drag. Two gaps here: the Draggable a control wraps
// itself in accepts the gesture on the press (Flutter's recognizer is immediate), so a plain click
// -- a mouse jitters a pixel -- was announced as a drag; and that announcement went out as a bare
// VEvent, which reaches Java as (0, 0). An application that resolves what is being dragged from
// those coordinates acted on whatever sits at the control's origin, and one that adds the dragged
// row to its selection kept two rows selected after a single click.
//
// The end-to-end wiring cannot be exercised here: the Draggable's feedback overlay lays this
// Composite's SizedBox.expand out unbounded and the test harness never settles. Both halves are
// covered directly instead, and the wiring is verified on the running application.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/composite_evolve.dart';
import 'package:swtflutter/src/impl/utils/dnd_utils.dart';

VComposite _surface() => VComposite()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 300
    ..height = 200);

void main() {
  group('DragDetectGate', () {
    test('a press with only jitter never announces a drag', () {
      final gate = DragDetectGate()..reset();
      expect(gate.passed(const Offset(1, 0)), isFalse);
      expect(gate.passed(const Offset(0, 1)), isFalse);
      expect(gate.passed(const Offset(-1, 0)), isFalse,
          reason: 'movement inside SWT.s drag threshold is not a drag');
    });

    test('accumulated movement past the threshold announces a drag exactly once', () {
      final gate = DragDetectGate()..reset();
      expect(gate.passed(const Offset(0, 2)), isFalse);
      expect(gate.passed(const Offset(0, 3)), isTrue,
          reason: 'the deltas accumulate: 5px clears the 4px threshold');
      expect(gate.passed(const Offset(0, 50)), isFalse,
          reason: 'SWT raises DragDetect once per drag, not per move');
    });

    test('a new drag starts from zero again', () {
      final gate = DragDetectGate()..reset();
      expect(gate.passed(const Offset(0, 10)), isTrue);
      gate.reset();
      expect(gate.passed(const Offset(0, 1)), isFalse);
      expect(gate.passed(const Offset(0, 10)), isTrue);
    });
  });

  testWidgets('DragDetect reports the press position, not the control origin', (tester) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(
          width: 300,
          height: 200,
          child: CompositeSwt<VComposite>(value: _surface()),
        ),
      ),
    ));
    await tester.pumpAndSettle();

    final impl = tester.state<CompositeImpl>(find.byType(CompositeSwt<VComposite>));

    final gesture = await tester.startGesture(const Offset(140, 120), kind: PointerDeviceKind.mouse);
    await tester.pump();

    final payload = impl.dragDetectPayload();
    expect([payload.x, payload.y], equals([140, 120]),
        reason: 'a bare VEvent reaches Java as (0, 0), which an application resolves to whatever '
            'sits at the control origin instead of the row the drag started on');

    await gesture.up();
    await tester.pump();
  });
}
