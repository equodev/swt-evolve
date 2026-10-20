// A click on a Scale's track moves it to the value under the pointer, mapped across the track
// between the insets GTK and Cocoa leave at each end.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/scale.dart';
import 'package:swtflutter/src/gen/swt.dart';

class _RecordingScaleSwt extends ScaleSwt<VScale> {
  final List<int?> selections;
  const _RecordingScaleSwt({required VScale value, required this.selections})
      : super(value: value);

  @override
  void sendSelectionSelection(VScale val, VEvent? payload) =>
      selections.add(payload?.index);
}

VScale _scale() => VScale()
  ..id = 1
  ..style = SWT.HORIZONTAL
  ..enabled = true
  ..minimum = 0
  ..maximum = 100
  ..selection = 0
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 300
    ..height = 40);

void main() {
  testWidgets('a click at 90% across the track selects the value GTK and Cocoa select',
      (tester) async {
    final selections = <int?>[];
    final value = _scale();

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(
          width: 300,
          height: 40,
          child: _RecordingScaleSwt(value: value, selections: selections),
        ),
      ),
    ));
    await tester.pump();

    final origin = tester.getTopLeft(find.byType(_RecordingScaleSwt));
    await tester.tapAt(origin + const Offset(269, 20));
    await tester.pump();

    expect(value.selection, 93);
    expect(selections, isNotEmpty);
    expect(selections.last, 93);
  });

  testWidgets('a drag that starts beyond the end of the track moves nothing', (tester) async {
    final selections = <int?>[];
    final value = _scale()..selection = 25;

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(
          width: 300,
          height: 40,
          child: _RecordingScaleSwt(value: value, selections: selections),
        ),
      ),
    ));
    await tester.pump();

    final origin = tester.getTopLeft(find.byType(_RecordingScaleSwt));
    // 6 is 2% of 300, where the input-parity drag presses: beyond the track, and off the knob at 25.
    final gesture = await tester.startGesture(origin + const Offset(6, 20));
    for (var x = 30.0; x <= 295; x += 30) {
      await gesture.moveTo(origin + Offset(x, 20));
      await tester.pump(const Duration(milliseconds: 20));
    }
    await gesture.up();
    await tester.pump();

    expect(value.selection, 25);
    expect(selections, isEmpty);
  });

  Future<Offset> pumpScaleAt25(WidgetTester tester, List<int?> selections, VScale value) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(
          width: 300,
          height: 40,
          child: _RecordingScaleSwt(value: value, selections: selections),
        ),
      ),
    ));
    await tester.pump();
    return tester.getTopLeft(find.byType(_RecordingScaleSwt));
  }

  // The knob at 25 sits at 12 + 0.25 * 276 = 81; 84 is on it, off its centre.
  testWidgets('a click on the knob keeps its value and reports one Selection, as Cocoa does',
      (tester) async {
    final selections = <int?>[];
    final value = _scale()..selection = 25;
    final origin = await pumpScaleAt25(tester, selections, value);

    await tester.tapAt(origin + const Offset(84, 20));
    await tester.pump();

    expect(value.selection, 25);
    expect(selections, [25]);
  });

  // The knob at 0 is centred at 12 with a radius of 6, so it reaches into the padding: x=8 is on
  // its outer half, past the 6 px that are inert.
  testWidgets('a knob at the minimum can be dragged from its outer half', (tester) async {
    final selections = <int?>[];
    final value = _scale()..selection = 0;
    final origin = await pumpScaleAt25(tester, selections, value);

    final gesture = await tester.startGesture(origin + const Offset(8, 20));
    for (var x = 28.0; x <= 148; x += 20) {
      await gesture.moveTo(origin + Offset(x, 20));
      await tester.pump(const Duration(milliseconds: 20));
    }
    await gesture.up();
    await tester.pump();

    expect(value.selection, greaterThan(40));
    expect(selections, isNotEmpty);
  });

  testWidgets('on a vertical Scale too, a drag that starts beyond the end of the track moves nothing',
      (tester) async {
    final selections = <int?>[];
    final value = _scale()
      ..style = SWT.VERTICAL
      ..selection = 25
      ..bounds = (VRectangle()
        ..x = 0
        ..y = 0
        ..width = 40
        ..height = 300);
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(width: 40, height: 300, child: _RecordingScaleSwt(value: value, selections: selections)),
      ),
    ));
    await tester.pump();

    final origin = tester.getTopLeft(find.byType(_RecordingScaleSwt));
    for (final start in [4.0, 296.0]) {
      final gesture = await tester.startGesture(origin + Offset(20, start));
      for (var y = 30.0; y <= 270; y += 30) {
        await gesture.moveTo(origin + Offset(20, start < 150 ? y : 300 - y));
        await tester.pump(const Duration(milliseconds: 20));
      }
      await gesture.up();
      await tester.pump();
    }

    expect(value.selection, 25);
    expect(selections, isEmpty);
  });

  testWidgets('a double click on the knob reports one Selection, from its first press only',
      (tester) async {
    final selections = <int?>[];
    final value = _scale()..selection = 25;
    final origin = await pumpScaleAt25(tester, selections, value);

    await tester.tapAt(origin + const Offset(84, 20));
    await tester.pump(const Duration(milliseconds: 80));
    await tester.tapAt(origin + const Offset(84, 20));
    await tester.pump(const Duration(seconds: 1));

    expect(value.selection, 25);
    expect(selections, [25]);
  });

  testWidgets('a drag from the knob moves it by where it was grabbed', (tester) async {
    final selections = <int?>[];
    final value = _scale()..selection = 25;
    final origin = await pumpScaleAt25(tester, selections, value);

    final gesture = await tester.startGesture(origin + const Offset(84, 20));
    for (var x = 104.0; x <= 222; x += 20) {
      await gesture.moveTo(origin + Offset(x, 20));
      await tester.pump(const Duration(milliseconds: 20));
    }
    await gesture.moveTo(origin + const Offset(222, 20));
    await tester.pump(const Duration(milliseconds: 20));
    await gesture.up();
    await tester.pump();

    expect(value.selection, 75);
    expect(selections.last, 75);
  });
}
