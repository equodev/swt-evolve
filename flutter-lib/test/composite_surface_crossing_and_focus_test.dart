// A Composite with no children is a surface an application draws on and clicks into, and a toolkit
// that owner-draws its own widgets there tracks the pointer from the events SWT sends it. Three of
// those did not match native SWT: MouseEnter/MouseExit carried no position, the first MouseMove
// arrived ahead of the MouseEnter it belongs to, and a press never produced SWT.FocusIn at all.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

/// Records the pointer-crossing, move, press and focus events in the order they are sent.
class _RecordingCompositeSwt extends CompositeSwt<VComposite> {
  const _RecordingCompositeSwt({required super.value, required this.calls});

  final List<String> calls;

  @override
  void sendMouseTrackMouseEnter(VComposite val, VEvent? payload) =>
      calls.add('enter:${payload?.x},${payload?.y}');

  @override
  void sendMouseTrackMouseExit(VComposite val, VEvent? payload) =>
      calls.add('exit:${payload?.x},${payload?.y}');

  @override
  void sendMouseMoveMouseMove(VComposite val, VEvent? payload) =>
      calls.add('move:${payload?.x},${payload?.y}');

  @override
  void sendMouseMouseDown(VComposite val, VEvent? payload) => calls.add('down');

  @override
  void sendFocusFocusIn(VComposite val, VEvent? payload) => calls.add('focusIn');

  @override
  void sendFocusFocusOut(VComposite val, VEvent? payload) => calls.add('focusOut');
}

VComposite _childlessComposite() => VComposite()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 200
    ..height = 60);

Future<List<String>> _pump(WidgetTester tester, List<String> calls) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(
        width: 200,
        height: 60,
        child: _RecordingCompositeSwt(value: _childlessComposite(), calls: calls),
      ),
    ),
  ));
  await tester.pumpAndSettle();
  return calls;
}

/// A mouse that starts well outside the composite, so moving onto it is a real border crossing.
Future<TestGesture> _mouseOutside(WidgetTester tester) async {
  final gesture = await tester.createGesture(kind: PointerDeviceKind.mouse);
  await gesture.addPointer(location: const Offset(600, 500));
  addTearDown(gesture.removePointer);
  await tester.pump();
  return gesture;
}

void main() {
  const onSurface = Offset(120, 30);

  testWidgets('MouseEnter carries the position the pointer crossed at', (tester) async {
    final calls = await _pump(tester, <String>[]);
    final gesture = await _mouseOutside(tester);

    await gesture.moveTo(onSurface);
    await tester.pump();

    expect(calls.first, equals('enter:120,30'),
        reason: 'native SWT fills x/y on MouseEnter; a null payload reaches Java as a blank Event, '
            'so an application reading it starts tracking at the control origin instead');
  });

  testWidgets('MouseEnter is reported before the move it belongs to', (tester) async {
    final calls = await _pump(tester, <String>[]);
    final gesture = await _mouseOutside(tester);

    await gesture.moveTo(onSurface);
    await tester.pump();

    expect(calls.where((c) => c.startsWith('enter') || c.startsWith('move')).toList(),
        equals(['enter:120,30', 'move:120,30']),
        reason: 'SWT delivers MouseEnter ahead of MouseMove; the arbiter resolved the crossing on a '
            'microtask, which put the move first');
  });

  testWidgets('a press reports FocusIn, ahead of the MouseDown', (tester) async {
    final calls = await _pump(tester, <String>[]);
    final gesture = await _mouseOutside(tester);
    await gesture.moveTo(onSurface);
    await tester.pump();
    calls.clear();

    await gesture.down(onSurface);
    await tester.pump();

    expect(calls, equals(['focusIn', 'down']),
        reason: 'SWT focuses the control a press lands on and delivers FocusIn before the press');
  });

  testWidgets('a second press on the same surface does not re-report FocusIn', (tester) async {
    final calls = await _pump(tester, <String>[]);
    final gesture = await _mouseOutside(tester);
    await gesture.moveTo(onSurface);
    await tester.pump();

    await gesture.down(onSurface);
    await gesture.up();
    await tester.pump();
    calls.clear();

    await gesture.down(onSurface);
    await tester.pump();

    expect(calls, equals(['down']),
        reason: 'the control already holds the focus, so there is no focus change to report');
  });
}
