// A desktop trackpad delivers a two-finger swipe as a pan/zoom gesture rather than as scroll
// signals, so the controls that scroll from the wheel must read that gesture too.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/composite.dart';
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

class _WheelCompositeSwt extends CompositeSwt<VComposite> {
  const _WheelCompositeSwt({required super.value, required this.wheels});

  final List<VEvent> wheels;

  @override
  void sendEvent(VComposite val, String ev, VEvent? payload) {
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

/// A Composite that draws its own rows and scrolls them from SWT.MouseWheel, with one child at the
/// bottom right.
VComposite _composite() => VComposite()
  ..swt = 'Composite'
  ..id = 9
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = _rect(300, 200)
  ..children = [
    VComposite()
      ..swt = 'Composite'
      ..id = 10
      ..style = SWT.NONE
      ..enabled = true
      ..visible = true
      ..bounds = (VRectangle()
        ..x = 200
        ..y = 120
        ..width = 100
        ..height = 80)
  ];

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

/// Two fingers moving up the trackpad by [distance], in [steps] updates, at [at].
Future<void> _swipeUp(WidgetTester tester,
        {double distance = 60, int steps = 3, Offset at = const Offset(150, 100)}) =>
    _swipe(tester, Offset(0, -distance), steps: steps, at: at);

/// Two fingers moving by [pan] on the trackpad, in [steps] updates, at [at].
Future<void> _swipe(WidgetTester tester, Offset pan,
    {int steps = 3, Offset at = const Offset(150, 100)}) async {
  final gesture = await tester.createGesture(kind: PointerDeviceKind.trackpad);
  await gesture.panZoomStart(at);
  for (var i = 1; i <= steps; i++) {
    await gesture.panZoomUpdate(at, pan: pan * (i / steps));
    await tester.pump();
  }
  await gesture.panZoomEnd();
  await tester.pump();
}

void main() {
  testWidgets('a two-finger swipe over a Canvas reaches Java as a mouse wheel', (tester) async {
    final wheels = <VEvent>[];
    await _pump(tester, _WheelCanvasSwt(value: _canvas(), wheels: wheels));

    await _swipeUp(tester);

    // One wheel per pan update; fingers moving up scroll the content down, as a wheel turned
    // towards the user does.
    expect(wheels.map((e) => e.count), [-1, -1, -1]);
  });

  testWidgets('a horizontal swipe over a Canvas does not scroll it vertically', (tester) async {
    final wheels = <VEvent>[];
    await _pump(tester, _WheelCanvasSwt(value: _canvas(), wheels: wheels));

    await _swipe(tester, const Offset(-60, 0));

    expect(wheels, isEmpty);
  });

  testWidgets('a two-finger swipe over a StyledText scrolls by the distance the fingers moved',
      (tester) async {
    final wheels = <VEvent>[];
    await _pump(tester, _WheelStyledTextSwt(value: _styledText(), wheels: wheels));

    await _swipeUp(tester, distance: 60, steps: 3);

    expect(wheels, isNotEmpty);
    expect(wheels.fold<int>(0, (sum, e) => sum + e.detail!), 60);
  });

  testWidgets('a two-finger swipe over a Composite reaches Java as a mouse wheel', (tester) async {
    final wheels = <VEvent>[];
    await _pump(tester, _WheelCompositeSwt(value: _composite(), wheels: wheels));

    await _swipeUp(tester);

    expect(wheels.map((e) => e.count), [-1, -1, -1]);
  });

  testWidgets('a mouse wheel over a Composite reaches Java', (tester) async {
    final wheels = <VEvent>[];
    await _pump(tester, _WheelCompositeSwt(value: _composite(), wheels: wheels));

    await tester.sendEventToBinding(
        const PointerScrollEvent(position: Offset(150, 100), scrollDelta: Offset(0, 120)));
    await tester.pump();

    expect(wheels.map((e) => e.count), [-3]);
  });

  testWidgets('a swipe over a Composite\'s child is the child\'s, not the Composite\'s',
      (tester) async {
    final wheels = <VEvent>[];
    await _pump(tester, _WheelCompositeSwt(value: _composite(), wheels: wheels));

    await _swipeUp(tester, at: const Offset(250, 160));

    expect(wheels, isEmpty);
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
}
