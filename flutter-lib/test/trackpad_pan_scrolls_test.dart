// A desktop trackpad delivers a two-finger swipe as a pan/zoom gesture rather than as scroll
// signals, so the controls that scroll from the wheel must read that gesture too.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/cursor.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/scrollbar.dart';
import 'package:swtflutter/src/gen/styledtext.dart';
import 'package:swtflutter/src/gen/swt.dart';

class _WheelCanvasSwt extends CanvasSwt<VCanvas> {
  const _WheelCanvasSwt({required super.value, required this.wheels});

  final List<VEvent> wheels;

  @override
  void sendEvent(VCanvas val, String ev, VEvent? payload) {
    if (ev == 'MouseWheel/MouseWheel') wheels.add(payload!);
  }
}

class _WheelStyledTextSwt extends StyledTextSwt<VStyledText> {
  const _WheelStyledTextSwt({required super.value, required this.wheels});

  final List<VEvent> wheels;

  @override
  void sendEvent(VStyledText val, String ev, VEvent? payload) {
    if (ev == 'MouseWheel/MouseWheel') wheels.add(payload!);
  }
}

VRectangle _rect(int w, int h) => VRectangle()
  ..x = 0
  ..y = 0
  ..width = w
  ..height = h;

VCanvas _canvas() => VCanvas()
  ..swt = 'Canvas'
  ..id = 7
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = _rect(300, 200);

VStyledText _styledText() => VStyledText()
  ..swt = 'StyledText'
  ..id = 42
  ..style = SWT.MULTI | SWT.V_SCROLL
  ..enabled = true
  ..visible = true
  ..editable = true
  ..text = List.generate(200, (i) => 'line $i').join('\n')
  ..bounds = _rect(300, 200)
  ..cursor = (VCursor()..cursorStyle = SWT.CURSOR_IBEAM)
  ..verticalBar = (VScrollBar()
    ..swt = 'ScrollBar'
    ..id = 11
    ..style = SWT.NONE
    ..visible = true
    ..enabled = true
    ..minimum = 0
    ..maximum = 1000
    ..thumb = 50
    ..selection = 0);

Future<void> _pump(WidgetTester tester, Widget control) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(width: 300, height: 200, child: control),
    ),
  ));
  await tester.pump();
}

/// Two fingers moving up the trackpad by [distance], in [steps] updates.
Future<void> _swipeUp(WidgetTester tester, {double distance = 60, int steps = 3}) async {
  final gesture = await tester.createGesture(kind: PointerDeviceKind.trackpad);
  await gesture.panZoomStart(const Offset(150, 100));
  for (var i = 1; i <= steps; i++) {
    await gesture.panZoomUpdate(const Offset(150, 100), pan: Offset(0, -distance * i / steps));
    await tester.pump();
  }
  await gesture.panZoomEnd();
  await tester.pump();
}

/// A swipe up of [distance] in [steps] updates [stepMs] apart, released while still moving.
Future<void> _flickUp(WidgetTester tester,
    {double distance = 120, int steps = 6, int stepMs = 16}) async {
  final gesture = await tester.createGesture(kind: PointerDeviceKind.trackpad);
  await gesture.panZoomStart(const Offset(150, 100));
  for (var i = 1; i <= steps; i++) {
    await gesture.panZoomUpdate(const Offset(150, 100),
        pan: Offset(0, -distance * i / steps), timeStamp: Duration(milliseconds: i * stepMs));
    await tester.pump(Duration(milliseconds: stepMs));
  }
  await gesture.panZoomEnd(timeStamp: Duration(milliseconds: steps * stepMs));
  await tester.pump();
}

Future<void> _frames(WidgetTester tester, int count) async {
  for (var i = 0; i < count; i++) {
    await tester.pump(const Duration(milliseconds: 16));
  }
}

int _travel(List<VEvent> wheels) => wheels.fold<int>(0, (sum, e) => sum + e.detail!);

void main() {
  testWidgets('a two-finger swipe over a Canvas reaches Java as a mouse wheel', (tester) async {
    final wheels = <VEvent>[];
    await _pump(tester, _WheelCanvasSwt(value: _canvas(), wheels: wheels));

    await _swipeUp(tester);

    expect(wheels, isNotEmpty);
    // Fingers moving up scroll the content down, as a wheel turned towards the user does.
    expect(wheels.every((e) => e.count! < 0), isTrue);
  });

  testWidgets('a two-finger swipe over a StyledText scrolls by the distance the fingers moved',
      (tester) async {
    final wheels = <VEvent>[];
    await _pump(tester, _WheelStyledTextSwt(value: _styledText(), wheels: wheels));

    await _swipeUp(tester, distance: 60, steps: 3);

    expect(wheels, isNotEmpty);
    expect(wheels.fold<int>(0, (sum, e) => sum + e.detail!), 60);
  });

  testWidgets('a pinch with no pan does not scroll', (tester) async {
    final wheels = <VEvent>[];
    await _pump(tester, _WheelCanvasSwt(value: _canvas(), wheels: wheels));

    final gesture = await tester.createGesture(kind: PointerDeviceKind.trackpad);
    await gesture.panZoomStart(const Offset(150, 100));
    await gesture.panZoomUpdate(const Offset(150, 100), scale: 1.5);
    await tester.pump();
    await gesture.panZoomEnd();
    await tester.pump();

    expect(wheels, isEmpty);
  });

  testWidgets('a flick keeps scrolling after the fingers lift, slowing to a stop', (tester) async {
    final wheels = <VEvent>[];
    await _pump(tester, _WheelStyledTextSwt(value: _styledText(), wheels: wheels));

    await _flickUp(tester, distance: 120);
    final atRelease = _travel(wheels);
    expect(atRelease, 120);

    await _frames(tester, 10);
    final early = _travel(wheels) - atRelease;
    await _frames(tester, 10);
    final later = _travel(wheels) - atRelease - early;
    expect(early, greaterThan(0), reason: 'the content glides on after the release');
    expect(later, lessThan(early), reason: 'and slows down');
    expect(wheels.every((e) => e.detail! > 0), isTrue, reason: 'in the direction of the swipe');

    await _frames(tester, 300);
    final stopped = _travel(wheels);
    await _frames(tester, 30);
    expect(_travel(wheels), stopped, reason: 'until it stops');
  });

  testWidgets('touching the trackpad again stops the glide', (tester) async {
    final wheels = <VEvent>[];
    await _pump(tester, _WheelStyledTextSwt(value: _styledText(), wheels: wheels));

    await _flickUp(tester, distance: 120);
    await _frames(tester, 3);
    tester.binding.handlePointerEvent(const PointerScrollInertiaCancelEvent(
        kind: PointerDeviceKind.trackpad, position: Offset(150, 100)));
    final stopped = _travel(wheels);
    await _frames(tester, 30);

    expect(_travel(wheels), stopped);
  });

  testWidgets('a swipe that comes to rest before the release does not glide', (tester) async {
    final wheels = <VEvent>[];
    await _pump(tester, _WheelStyledTextSwt(value: _styledText(), wheels: wheels));

    final gesture = await tester.createGesture(kind: PointerDeviceKind.trackpad);
    await gesture.panZoomStart(const Offset(150, 100));
    await gesture.panZoomUpdate(const Offset(150, 100),
        pan: const Offset(0, -60), timeStamp: const Duration(milliseconds: 16));
    await gesture.panZoomUpdate(const Offset(150, 100),
        pan: const Offset(0, -60), timeStamp: const Duration(milliseconds: 400));
    await gesture.panZoomEnd(timeStamp: const Duration(milliseconds: 400));
    await _frames(tester, 30);

    expect(_travel(wheels), 60);
  });

  testWidgets('a flick over a Canvas glides as further mouse wheel ticks', (tester) async {
    final wheels = <VEvent>[];
    await _pump(tester, _WheelCanvasSwt(value: _canvas(), wheels: wheels));

    await _flickUp(tester, distance: 120);
    final atRelease = wheels.length;
    await _frames(tester, 20);

    expect(wheels.length, greaterThan(atRelease));
    expect(wheels.every((e) => e.count! < 0), isTrue);
  });
}
