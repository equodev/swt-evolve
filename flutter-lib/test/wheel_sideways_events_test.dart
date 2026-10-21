// A sideways wheel, touchpad swipe or Shift + wheel is SWT.MouseHorizontalWheel, counted positive
// to the left as SWT counts it; the vertical part of a scroll stays SWT.MouseWheel.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

String _describe(VEvent? e) =>
    '${e?.type == SWT.MouseHorizontalWheel ? 'horizontal' : 'vertical'}:${e?.count}';

class _WheelCanvasSwt extends CanvasSwt<VCanvas> {
  const _WheelCanvasSwt({required super.value, required this.wheels});

  final List<String> wheels;

  @override
  void sendMouseWheelMouseWheel(VCanvas val, VEvent? payload) => wheels.add(_describe(payload));
}

class _WheelCompositeSwt extends CompositeSwt<VComposite> {
  const _WheelCompositeSwt({required super.value, required this.wheels});

  final List<String> wheels;

  @override
  void sendMouseWheelMouseWheel(VComposite val, VEvent? payload) => wheels.add(_describe(payload));
}

VCanvas _canvas() => VCanvas()
  ..swt = 'Canvas'
  ..id = 3
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = _rect(0, 0, 200, 100);

/// The child sits in the bottom strip; the wheel lands on the composite's own area above it.
VComposite _composite() => VComposite()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = _rect(0, 0, 200, 100)
  ..children = [
    VLabel()
      ..id = 2
      ..text = 'child'
      ..enabled = true
      ..visible = true
      ..bounds = _rect(0, 85, 200, 15)
  ];

Future<void> _pump(WidgetTester tester, Widget subject) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(width: 200, height: 100, child: subject),
  ));
  await tester.pumpAndSettle();
}

Future<void> _wheel(WidgetTester tester, Offset delta) async {
  final pointer = TestPointer(1, PointerDeviceKind.mouse);
  await tester.sendEventToBinding(pointer.hover(const Offset(50, 30)));
  await tester.sendEventToBinding(pointer.scroll(delta));
  await tester.pump();
}

/// A two-finger trackpad swipe, which desktop embedders report as pan/zoom updates: the fingers
/// drag the content, so the pan is the wheel delta reversed.
Future<void> _swipe(WidgetTester tester, Offset delta) async {
  final gesture = await tester.createGesture(kind: PointerDeviceKind.trackpad);
  await gesture.panZoomStart(const Offset(50, 30));
  await gesture.panZoomUpdate(const Offset(50, 30), pan: -delta);
  await gesture.panZoomEnd();
  await tester.pump();
}

void main() {
  testWidgets('a horizontal wheel over a Canvas is SWT.MouseHorizontalWheel', (tester) async {
    final wheels = <String>[];
    await _pump(tester, _WheelCanvasSwt(value: _canvas(), wheels: wheels));

    await _wheel(tester, const Offset(120, 0));
    await _wheel(tester, const Offset(-120, 0));

    expect(wheels, ['horizontal:-3', 'horizontal:3']);
  });

  testWidgets('Shift turns a vertical wheel over a Canvas sideways', (tester) async {
    final wheels = <String>[];
    await _pump(tester, _WheelCanvasSwt(value: _canvas(), wheels: wheels));

    await tester.sendKeyDownEvent(LogicalKeyboardKey.shiftLeft);
    await _wheel(tester, const Offset(0, 120));
    await tester.sendKeyUpEvent(LogicalKeyboardKey.shiftLeft);

    expect(wheels, ['horizontal:-3']);
  });

  testWidgets('Ctrl + Shift + wheel over a Canvas stays SWT.MouseWheel', (tester) async {
    final wheels = <String>[];
    await _pump(tester, _WheelCanvasSwt(value: _canvas(), wheels: wheels));

    await tester.sendKeyDownEvent(LogicalKeyboardKey.controlLeft);
    await tester.sendKeyDownEvent(LogicalKeyboardKey.shiftLeft);
    await _wheel(tester, const Offset(0, 120));
    await tester.sendKeyUpEvent(LogicalKeyboardKey.shiftLeft);
    await tester.sendKeyUpEvent(LogicalKeyboardKey.controlLeft);

    expect(wheels, ['vertical:-3']);
  });

  testWidgets('control: a vertical wheel over a Canvas stays SWT.MouseWheel', (tester) async {
    final wheels = <String>[];
    await _pump(tester, _WheelCanvasSwt(value: _canvas(), wheels: wheels));

    await _wheel(tester, const Offset(0, 120));

    expect(wheels, ['vertical:-3']);
  });

  testWidgets('a diagonal swipe over a Canvas reports both axes', (tester) async {
    final wheels = <String>[];
    await _pump(tester, _WheelCanvasSwt(value: _canvas(), wheels: wheels));

    await _swipe(tester, const Offset(40, -40));

    expect(wheels, ['vertical:1', 'horizontal:-1']);
  });

  testWidgets("a horizontal wheel over a Composite's own area is SWT.MouseHorizontalWheel",
      (tester) async {
    final wheels = <String>[];
    await _pump(tester, _WheelCompositeSwt(value: _composite(), wheels: wheels));

    await _wheel(tester, const Offset(-120, 0));

    expect(wheels, ['horizontal:3']);
  });
}
