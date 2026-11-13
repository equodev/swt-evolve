// MouseEnter and MouseExit carry the pointer position, as on native SWT: JFace's hover closer
// reads it to decide whether the pointer is heading toward the hover.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
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
  void sendMouseTrackMouseEnter(VCanvas val, VEvent? payload) =>
      calls.add('enter:${payload?.x},${payload?.y}${_mask(payload)}');

  @override
  void sendMouseTrackMouseExit(VCanvas val, VEvent? payload) =>
      calls.add('exit:${payload?.x},${payload?.y}${_mask(payload)}');
}

String _mask(VEvent? payload) {
  final mask = payload?.stateMask ?? 0;
  return mask == 0 ? '' : ' mask:$mask';
}

VCanvas _canvas() => VCanvas()
  ..swt = 'Canvas'
  ..id = 7
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 200
    ..height = 100);

/// The recording Canvas at (50, 40), off the origin, so a position in the wrong coordinates cannot
/// pass for the right one.
Future<void> _pumpCanvas(WidgetTester tester, List<String> calls) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: Padding(
        padding: const EdgeInsets.only(left: 50, top: 40),
        child: SizedBox(
          width: 200,
          height: 100,
          child: _RecordingCanvasSwt(value: _canvas(), calls: calls),
        ),
      ),
    ),
  ));
  await tester.pump();
}

void main() {
  testWidgets('MouseEnter and MouseExit say where the pointer is, in the control\'s coordinates',
      (tester) async {
    final calls = <String>[];
    await _pumpCanvas(tester, calls);

    final mouse = await tester.createGesture(kind: PointerDeviceKind.mouse);
    await mouse.addPointer(location: const Offset(10, 10));
    await tester.pump();

    await mouse.moveTo(const Offset(80, 70));
    await tester.pump();
    await mouse.moveTo(const Offset(300, 70));
    await tester.pump();
    await mouse.removePointer();

    expect(calls, ['enter:30,30', 'exit:250,30']);
  });

  testWidgets('MouseEnter and MouseExit carry the modifier keys held while the pointer crosses',
      (tester) async {
    final calls = <String>[];
    await _pumpCanvas(tester, calls);

    final mouse = await tester.createGesture(kind: PointerDeviceKind.mouse);
    await mouse.addPointer(location: const Offset(10, 10));
    await tester.pump();

    await simulateKeyDownEvent(LogicalKeyboardKey.shiftLeft);
    await mouse.moveTo(const Offset(80, 70));
    await tester.pump();
    await mouse.moveTo(const Offset(300, 70));
    await tester.pump();
    await simulateKeyUpEvent(LogicalKeyboardKey.shiftLeft);
    await mouse.removePointer();

    expect(calls, ['enter:30,30 mask:${SWT.SHIFT}', 'exit:250,30 mask:${SWT.SHIFT}']);
  });
}
