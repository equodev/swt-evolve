// A held button is SWT's implicit capture: no other control is entered or left until release, even
// when a drag moves controls under the pointer, although Flutter keeps re-evaluating hover.

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

/// A Composite whose right half is a Canvas: its left half is its own.
VComposite _parent() => VComposite()
  ..swt = 'Composite'
  ..id = 11
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = _rect(0, 0, 400, 200)
  ..children = [
    VCanvas()
      ..swt = 'Canvas'
      ..id = 12
      ..style = SWT.NONE
      ..enabled = true
      ..visible = true
      ..bounds = _rect(200, 0, 200, 200),
  ];

void main() {
  testWidgets('a drag holds hover on the control it started on until the button is released',
      (tester) async {
    final events = <String>[];
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 400,
        height: 200,
        child: _CapturingCompositeSwt(value: _parent(), onEvent: events.add),
      ),
    ));
    await tester.pumpAndSettle();

    final g = await tester.createGesture(kind: PointerDeviceKind.mouse);
    await g.addPointer(location: const Offset(50, 100));
    await g.moveTo(const Offset(60, 100));
    await tester.pumpAndSettle();
    expect(events, contains('MouseTrack/MouseEnter'), reason: 'hovering its own area enters it');
    events.clear();

    await g.down(const Offset(60, 100));
    for (var i = 1; i <= 8; i++) {
      await g.moveTo(Offset(60 + i * 30.0, 100));
      await tester.pump(const Duration(milliseconds: 16));
    }
    expect(events.where((e) => e.startsWith('MouseTrack/')), isEmpty,
        reason: 'the pressed control keeps the pointer: crossing into the Canvas while the button '
            'is held is not a MouseExit');

    await g.up();
    await tester.pumpAndSettle();
    expect(events, contains('MouseTrack/MouseExit'),
        reason: 'released over the Canvas, the pointer has left this control');
    await g.removePointer();
  });
}
