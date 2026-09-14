// Native SWT delivers SWT.MouseMove to the single control under the pointer, never to that
// control's ancestors as well -- the same exclusivity HoverExclusivityArbiter already enforces
// for MouseEnter/MouseExit, and that onPointerDown already enforces with its _hitsAnyChild
// guard. The move paths had no such guard, so one pointer move became one MouseMove per
// composite in the chain, measured at 12x on a real workbench (274 pointer moves during a drag
// arrived as 3822 events). That flood starves the UI thread of the asyncExec turns draw2d's
// DeferredUpdateManager needs, which is what leaves a diagram's drag feedback unpainted.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

class _CapturingCompositeSwt extends CompositeSwt<VComposite> {
  const _CapturingCompositeSwt({required super.value, required this.onEvent});

  final void Function(String ev) onEvent;

  @override
  void sendEvent(VComposite val, String ev, VEvent? payload) => onEvent(ev);
}

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

/// A Composite filling the area with one Canvas child covering all of it, which is the shape of
/// every layer between a Shell and the editor canvas an SWT workbench stacks up.
VComposite _parentWithCanvasChild() => VComposite()
  ..swt = 'Composite'
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = _rect(0, 0, 400, 200)
  ..children = [
    VCanvas()
      ..swt = 'Canvas'
      ..id = 2
      ..style = SWT.NONE
      ..enabled = true
      ..visible = true
      ..bounds = _rect(0, 0, 400, 200),
  ];

Future<List<String>> _parentEventsDuring(
    WidgetTester tester, Future<void> Function(WidgetTester) gesture) async {
  final events = <String>[];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 400,
      height: 200,
      child: _CapturingCompositeSwt(
        value: _parentWithCanvasChild(),
        onEvent: events.add,
      ),
    ),
  ));
  await tester.pumpAndSettle();
  events.clear();
  await gesture(tester);
  await tester.pumpAndSettle();
  return events;
}

void main() {
  testWidgets('a drag over a child does not also send the parent MouseMove', (tester) async {
    final events = await _parentEventsDuring(tester, (t) async {
      final g = await t.startGesture(const Offset(100, 100));
      for (var i = 1; i <= 8; i++) {
        await g.moveTo(Offset(100 + i * 20.0, 100));
        await t.pump(const Duration(milliseconds: 40));
      }
      await g.up();
    });

    expect(events.where((e) => e.contains('MouseMove')), isEmpty,
        reason: 'the Canvas under the pointer is the control SWT delivers the move to; the '
            'composites above it must stay silent');
  });

  testWidgets('a hover over a child does not also send the parent MouseMove', (tester) async {
    final events = await _parentEventsDuring(tester, (t) async {
      final pointer = TestPointer(1, PointerDeviceKind.mouse);
      await t.sendEventToBinding(pointer.hover(const Offset(100, 100)));
      for (var i = 1; i <= 8; i++) {
        await t.sendEventToBinding(pointer.hover(Offset(100 + i * 20.0, 100)));
        await t.pump(const Duration(milliseconds: 40));
      }
    });

    expect(events.where((e) => e.contains('MouseMove')), isEmpty,
        reason: 'hover has the same exclusivity as a drag: only the deepest control reports it');
  });

  testWidgets('a childless Composite still reports its own moves', (tester) async {
    final events = <String>[];
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 400,
        height: 200,
        child: _CapturingCompositeSwt(
          value: VComposite()
            ..swt = 'Composite'
            ..id = 3
            ..style = SWT.NONE
            ..enabled = true
            ..visible = true
            ..bounds = _rect(0, 0, 400, 200),
          onEvent: events.add,
        ),
      ),
    ));
    await tester.pumpAndSettle();
    events.clear();

    final g = await tester.startGesture(const Offset(100, 100));
    for (var i = 1; i <= 4; i++) {
      await g.moveTo(Offset(100 + i * 20.0, 100));
      await tester.pump(const Duration(milliseconds: 40));
    }
    await g.up();
    await tester.pumpAndSettle();

    expect(events.where((e) => e.contains('MouseMove')), isNotEmpty,
        reason: 'with nothing below it, this composite is the control under the pointer');
  });
}
