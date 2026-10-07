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
}
