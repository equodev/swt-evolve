// A Canvas DragSource sees MouseDown, DragDetect and MouseUp of a drag, and no MouseMove between.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widgets.dart' show wireChannelNames;

class _RecordingCanvasSwt extends CanvasSwt<VCanvas> {
  const _RecordingCanvasSwt({required super.value, required this.calls});

  final List<String> calls;

  @override
  void sendEvent(VCanvas val, int channel, VEvent? payload) {
    final ev = wireChannelNames[channel]!;
    if (ev.startsWith('Mouse/') || ev.startsWith('MouseMove/') || ev.startsWith('DragDetect/')) {
      calls.add(ev);
    }
  }
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
    ..width = 400
    ..height = 300);

Future<List<String>> _pressDragRelease(WidgetTester tester) async {
  final calls = <String>[];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 400,
      height: 300,
      child: _RecordingCanvasSwt(value: _canvas(), calls: calls),
    ),
  ));
  await tester.pumpAndSettle();

  final gesture = await tester.createGesture(buttons: kPrimaryButton);
  await gesture.down(const Offset(100, 50));
  await tester.pump(const Duration(milliseconds: 10));
  for (var i = 0; i < 10; i++) {
    // MouseMove is throttled on the wall clock.
    await tester.runAsync(() => Future<void>.delayed(const Duration(milliseconds: 20)));
    await gesture.moveBy(const Offset(0, 20));
    await tester.pump(const Duration(milliseconds: 10));
  }
  await gesture.up();
  await tester.pump(const Duration(seconds: 1));
  return calls;
}

void main() {
  testWidgets('a press that starts a drag reaches Java as MouseDown before DragDetect', (tester) async {
    final calls = await _pressDragRelease(tester);
    final down = calls.indexOf('Mouse/MouseDown');
    final detect = calls.indexOf('DragDetect/DragDetect');
    expect(down, isNonNegative, reason: '$calls');
    expect(detect, greaterThan(down), reason: '$calls');
  });

  testWidgets('once the drag is out, the control sees no MouseMove until its MouseUp', (tester) async {
    final calls = await _pressDragRelease(tester);
    final detect = calls.indexOf('DragDetect/DragDetect');
    final afterDetect = calls.sublist(detect + 1).where((c) => !c.startsWith('DragDetect/')).toList();
    expect(afterDetect, equals(['Mouse/MouseUp']), reason: '$calls');
  });
}
