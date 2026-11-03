// The arrows step a Scale in Java, by its increment and only when no KeyDown listener vetoed them:
// the client's Slider must not step them as well, by a step of its own.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
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

VScale _scale({required bool vertical}) => VScale()
  ..id = 1
  ..style = vertical ? SWT.VERTICAL : SWT.HORIZONTAL
  ..enabled = true
  ..minimum = 0
  ..maximum = 100
  ..selection = 50
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = vertical ? 40 : 300
    ..height = vertical ? 300 : 40);

void main() {
  for (final vertical in [false, true]) {
    testWidgets('a focused ${vertical ? 'vertical' : 'horizontal'} Scale steps nothing on an arrow',
        (tester) async {
      final selections = <int?>[];
      await tester.pumpWidget(EvolveApp(
        theme: ThemeMode.light,
        contentWidget: Align(
          alignment: Alignment.topLeft,
          child: SizedBox(
            width: vertical ? 40 : 300,
            height: vertical ? 300 : 40,
            child: _RecordingScaleSwt(value: _scale(vertical: vertical), selections: selections),
          ),
        ),
      ));
      await tester.pump();
      final focusNode = tester.widget<Slider>(find.byType(Slider)).focusNode!;
      focusNode.requestFocus();
      await tester.pump();

      for (final key in [
        LogicalKeyboardKey.arrowRight,
        LogicalKeyboardKey.arrowLeft,
        LogicalKeyboardKey.arrowLeft,
        LogicalKeyboardKey.arrowUp,
        LogicalKeyboardKey.arrowDown,
        LogicalKeyboardKey.arrowDown,
      ]) {
        await tester.sendKeyEvent(key);
        await tester.pump();
      }

      expect(focusNode.hasFocus, isTrue);
      expect(selections, isEmpty);
      expect(tester.widget<Slider>(find.byType(Slider)).value, 50);
    });
  }
}
