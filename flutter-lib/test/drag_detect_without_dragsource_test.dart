// Native SWT raises SWT.DragDetect on any drag gesture over a Control whose DRAG_DETECT state is
// set -- which every Control has by default -- and an application starts its own drag from there
// (addDragDetectListener, then a Tracker). It is not tied to org.eclipse.swt.dnd: a Control with no
// DragSource still gets the event. Evolve only raised it from inside the DnD Draggable, so a
// hand-drawn Composite that drives its own drag never heard anything and the drag never started.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

class _RecordingCompositeSwt extends CompositeSwt<VComposite> {
  const _RecordingCompositeSwt({required super.value, required this.calls});

  final List<String> calls;

  @override
  void sendDragDetectDragDetect(VComposite val, VEvent? payload) =>
      calls.add('dragDetect:${payload?.x}:${payload?.y}');
}

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

/// Childless: build() goes through ControlImpl.wrap().
VComposite _childless({bool? dragDetect}) => VComposite()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..dragDetect = dragDetect
  ..bounds = _rect(0, 0, 200, 60);

/// With a child: build() goes through the composite interaction chrome, which bypasses wrap().
/// The child sits in the bottom strip, clear of the gestures below.
VComposite _withChild() => VComposite()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = _rect(0, 0, 200, 60)
  ..children = [
    VLabel()
      ..id = 2
      ..text = 'child'
      ..enabled = true
      ..visible = true
      ..bounds = _rect(0, 50, 200, 10)
  ];

Future<List<String>> _pump(WidgetTester tester, VComposite value) async {
  final calls = <String>[];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 200,
      height: 60,
      child: _RecordingCompositeSwt(value: value, calls: calls),
    ),
  ));
  await tester.pumpAndSettle();
  return calls;
}

Future<TestGesture> _dragBy(WidgetTester tester, Offset from, Offset delta) async {
  final gesture = await tester.createGesture(buttons: kPrimaryButton);
  await gesture.down(from);
  await tester.pump(const Duration(milliseconds: 20));
  await gesture.moveBy(delta);
  await tester.pump(const Duration(milliseconds: 20));
  return gesture;
}

void main() {
  const onControl = Offset(100, 20);

  testWidgets('a drag on a Control with no DragSource raises SWT.DragDetect', (tester) async {
    final calls = await _pump(tester, _childless());

    final gesture = await _dragBy(tester, onControl, const Offset(30, 0));
    await gesture.up();
    await tester.pump(Duration.zero);

    expect(calls, equals(['dragDetect:100:20']),
        reason: 'native SWT raises DragDetect at the press position, DragSource or not');
  });

  testWidgets('DragDetect is raised once per press, not per pointer move', (tester) async {
    final calls = await _pump(tester, _childless());

    final gesture = await _dragBy(tester, onControl, const Offset(30, 0));
    await gesture.moveBy(const Offset(30, 0));
    await tester.pump(const Duration(milliseconds: 20));
    await gesture.moveBy(const Offset(30, 0));
    await tester.pump(const Duration(milliseconds: 20));
    await gesture.up();
    await tester.pump(Duration.zero);

    expect(calls, hasLength(1));
  });

  testWidgets('a click that never passes the drag threshold raises nothing', (tester) async {
    final calls = await _pump(tester, _childless());

    final gesture = await tester.createGesture(buttons: kPrimaryButton);
    await gesture.down(onControl);
    await tester.pump(const Duration(milliseconds: 20));
    await gesture.moveBy(const Offset(1, 1));
    await tester.pump(const Duration(milliseconds: 20));
    await gesture.up();
    await tester.pump(Duration.zero);

    expect(calls, isEmpty);
  });

  testWidgets('setDragDetect(false) suppresses it, as SWT clearing DRAG_DETECT does',
      (tester) async {
    final calls = await _pump(tester, _childless(dragDetect: false));

    final gesture = await _dragBy(tester, onControl, const Offset(30, 0));
    await gesture.up();
    await tester.pump(Duration.zero);

    expect(calls, isEmpty);
  });

  testWidgets('a Composite with children raises it too -- the chrome path bypasses wrap()',
      (tester) async {
    final calls = await _pump(tester, _withChild());

    final gesture = await _dragBy(tester, onControl, const Offset(30, 0));
    await gesture.up();
    await tester.pump(Duration.zero);

    expect(calls, equals(['dragDetect:100:20']));
  });
}
