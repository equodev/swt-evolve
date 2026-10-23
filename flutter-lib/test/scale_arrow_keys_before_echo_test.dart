// Arrow keys step a Scale from the value it last reported, even when the application has not yet
// sent that value back: a key pressed before the round trip completes must not step from the
// value before it.

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

VScale _scale(int selection, {bool vertical = false}) => VScale()
  ..id = 1
  ..style = vertical ? SWT.VERTICAL : SWT.HORIZONTAL
  ..enabled = true
  ..minimum = 0
  ..maximum = 100
  ..selection = selection
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = vertical ? 40 : 300
    ..height = vertical ? 300 : 40);

Future<void> _pumpScale(WidgetTester tester, VScale value, List<int?> selections, {bool vertical = false}) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(
        width: vertical ? 40 : 300,
        height: vertical ? 300 : 40,
        child: _RecordingScaleSwt(value: value, selections: selections),
      ),
    ),
  ));
  await tester.pump();
}

void main() {
  for (final vertical in [false, true]) {
    testWidgets('arrows pressed before any echo step from the last reported value${vertical ? ' (vertical)' : ''}',
        (tester) async {
      final selections = <int?>[];
      final value = _scale(50, vertical: vertical);
      await _pumpScale(tester, value, selections, vertical: vertical);

      tester.widget<Slider>(find.byType(Slider)).focusNode!.requestFocus();
      await tester.pump();

      // Left, right, up, down: what the input parity plays after a click. They cancel out.
      for (final key in [
        LogicalKeyboardKey.arrowLeft,
        LogicalKeyboardKey.arrowRight,
        LogicalKeyboardKey.arrowUp,
        LogicalKeyboardKey.arrowDown,
      ]) {
        await tester.sendKeyEvent(key);
        await tester.pump();
      }

      expect(selections, hasLength(4));
      expect(value.selection, 50);
      expect(selections.last, 50);
    });
  }

  testWidgets('a value the application sets after the arrows is shown', (tester) async {
    final selections = <int?>[];
    final value = _scale(50);
    await _pumpScale(tester, value, selections);

    tester.widget<Slider>(find.byType(Slider)).focusNode!.requestFocus();
    await tester.pump();
    await tester.sendKeyEvent(LogicalKeyboardKey.arrowRight);
    await tester.pump();

    value.selection = 10;
    await _pumpScale(tester, value, selections);
    await tester.pump(const Duration(seconds: 1));

    expect(tester.widget<Slider>(find.byType(Slider)).value, 10);
  });
}
