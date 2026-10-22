// A click does not give a Slider the keyboard, as on every native: the arrows pressed afterwards do
// not move it.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/slider.dart';
import 'package:swtflutter/src/gen/swt.dart';

class _RecordingSliderSwt extends SliderSwt<VSlider> {
  final List<String> calls;
  const _RecordingSliderSwt({required VSlider value, required this.calls}) : super(value: value);

  @override
  void sendEvent(VSlider val, String ev, VEvent? payload) => calls.add(ev);
}

void main() {
  testWidgets('arrows after a click on a slider do not move it', (tester) async {
    final calls = <String>[];
    final value = VSlider()
      ..id = 1
      ..style = SWT.HORIZONTAL
      ..enabled = true
      ..minimum = 0
      ..maximum = 100
      ..thumb = 10
      ..selection = 45
      ..bounds = (VRectangle()
        ..x = 0
        ..y = 0
        ..width = 200
        ..height = 30);
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(width: 200, height: 30, child: _RecordingSliderSwt(value: value, calls: calls)),
      ),
    ));
    await tester.pump();

    await tester.tap(find.byType(_RecordingSliderSwt));
    await tester.pump(const Duration(seconds: 1));
    calls.clear();

    await tester.sendKeyEvent(LogicalKeyboardKey.arrowRight);
    await tester.sendKeyEvent(LogicalKeyboardKey.arrowUp);
    await tester.pump(const Duration(seconds: 1));

    expect(calls.where((c) => c.startsWith('Selection/')), isEmpty);
  });
}
