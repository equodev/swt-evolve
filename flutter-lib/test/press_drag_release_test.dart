// A press that travels and is released over the control: a button is still selected by it, and the
// hover that follows is reported where the pointer came to rest, as on Win32, GTK and Cocoa.

import 'package:flutter/gestures.dart' show PointerDeviceKind;
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

class _RecordingButtonSwt extends ButtonSwt<VButton> {
  final List<String> calls;
  const _RecordingButtonSwt({required VButton value, required this.calls}) : super(value: value);

  @override
  void sendEvent(VButton val, String ev, VEvent? payload) =>
      calls.add('$ev@${payload?.x},${payload?.y}');
}

VButton _button() => VButton()
  ..id = 1
  ..style = SWT.PUSH
  ..enabled = true
  ..text = 'OK'
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 160
    ..height = 40);

Future<Offset> _pump(WidgetTester tester, List<String> calls) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(width: 160, height: 40, child: _RecordingButtonSwt(value: _button(), calls: calls)),
    ),
  ));
  await tester.pump();
  return tester.getTopLeft(find.byType(_RecordingButtonSwt));
}

void main() {
  testWidgets('a press dragged across a button and released over it selects it once', (tester) async {
    final calls = <String>[];
    final origin = await _pump(tester, calls);

    final gesture = await tester.startGesture(origin + const Offset(3, 20));
    for (var x = 22.0; x <= 156; x += 19) {
      await gesture.moveTo(origin + Offset(x, 20));
      await tester.pump(const Duration(milliseconds: 20));
    }
    await gesture.up();
    await tester.pump();

    expect(calls.where((c) => c.startsWith('Selection/Selection')), hasLength(1));
    await tester.pump(const Duration(seconds: 2));
  });

  testWidgets('a plain click still selects a button once', (tester) async {
    final calls = <String>[];
    final origin = await _pump(tester, calls);

    await tester.tapAt(origin + const Offset(80, 20));
    await tester.pump();

    expect(calls.where((c) => c.startsWith('Selection/Selection')), hasLength(1));
    await tester.pump(const Duration(seconds: 2));
  });

  testWidgets('the hover after a drag is reported where the pointer was released', (tester) async {
    final calls = <String>[];
    final origin = await _pump(tester, calls);

    final mouse = await tester.createGesture(kind: PointerDeviceKind.mouse);
    await mouse.addPointer(location: origin + const Offset(3, 20));
    await tester.pump();
    await mouse.down(origin + const Offset(3, 20));
    for (var x = 22.0; x <= 156; x += 19) {
      await mouse.moveTo(origin + Offset(x, 20));
      await tester.pump(const Duration(milliseconds: 20));
    }
    await mouse.up();
    await tester.pump(const Duration(seconds: 2));

    final hovers = calls.where((c) => c.startsWith('MouseTrack/MouseHover')).toList();
    expect(hovers, isNotEmpty);
    expect(hovers.last, 'MouseTrack/MouseHover@155,20');
    await mouse.removePointer();
  });
}
