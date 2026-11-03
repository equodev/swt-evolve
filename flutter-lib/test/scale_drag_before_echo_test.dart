// A dragged Scale shows the value it last reported until the application sends it back, and an
// answer to a value since replaced does not move the knob back.

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

VScale _scale(int selection) => VScale()
  ..id = 1
  ..style = SWT.HORIZONTAL
  ..enabled = true
  ..minimum = 0
  ..maximum = 100
  ..selection = selection
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 300
    ..height = 40);

Future<void> _pumpScale(WidgetTester tester, VScale value, List<int?> selections) async {
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
}

double _shown(WidgetTester tester) => tester.widget<Slider>(find.byType(Slider)).value;

void main() {
  testWidgets('a value the application sets after a drag is shown', (tester) async {
    final selections = <int?>[];
    final value = _scale(50);
    await _pumpScale(tester, value, selections);

    final gesture = await tester.startGesture(tester.getCenter(find.byType(Slider)));
    await tester.pump();
    await gesture.moveBy(const Offset(30, 0));
    await tester.pump();
    await gesture.up();
    await tester.pump();
    expect(selections, isNotEmpty);

    value.selection = 10;
    await _pumpScale(tester, value, selections);
    await tester.pump(const Duration(seconds: 1));

    expect(_shown(tester), 10);
  });

  // The application answers each reported value in turn, one move late. A rebuild of the Scale
  // between a move and its answer must not count as that answer, nor may an answer to a value since
  // replaced move the knob back.
  testWidgets('answers arriving a move late, around rebuilds, leave the knob on the last value', (tester) async {
    final selections = <int?>[];
    final value = _scale(50);
    late StateSetter rebuild;
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(
          width: 300,
          height: 40,
          child: StatefulBuilder(builder: (context, setState) {
            rebuild = setState;
            return _RecordingScaleSwt(value: value, selections: selections);
          }),
        ),
      ),
    ));
    await tester.pump();

    var answered = 0;
    Future<void> answerOne() async {
      rebuild(() => value.selection = selections[answered++]);
      await tester.pump();
    }

    final gesture = await tester.startGesture(tester.getCenter(find.byType(Slider)));
    await tester.pump();
    for (var i = 0; i < 4; i++) {
      await gesture.moveBy(const Offset(20, 0));
      await tester.pump();
      rebuild(() {});
      await tester.pump();
      if (i > 0) await answerOne();
      expect(_shown(tester).round(), selections.last);
    }
    await gesture.up();
    await tester.pump();
    while (answered < selections.length) {
      await answerOne();
    }
    await tester.pump(const Duration(seconds: 2));

    expect(selections, hasLength(4));
    expect(_shown(tester).round(), selections.last);
  });
}
