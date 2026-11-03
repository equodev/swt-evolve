// A press on a Scale beside its track, near the control's edge across the track, moves nothing and
// reports no Selection, as on GTK and Win32; it still focuses the Scale. A press on the track moves
// the value. Flutter's Slider would take a press anywhere in its box as one on the track.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/scale.dart';
import 'package:swtflutter/src/gen/swt.dart';

// The input parity fixture of a vertical Scale; a horizontal one is the same box turned.
const double _long = 130;
const double _across = 40;

class _RecordingScaleSwt extends ScaleSwt<VScale> {
  final List<int?> selections;
  const _RecordingScaleSwt({required VScale value, required this.selections})
      : super(value: value);

  @override
  void sendSelectionSelection(VScale val, VEvent? payload) =>
      selections.add(payload?.index);
}

Size _size({required bool vertical}) =>
    vertical ? const Size(_across, _long) : const Size(_long, _across);

VScale _scale({required bool vertical}) {
  final size = _size(vertical: vertical);
  return VScale()
    ..id = 1
    ..style = vertical ? SWT.VERTICAL : SWT.HORIZONTAL
    ..enabled = true
    ..minimum = 0
    ..maximum = 100
    ..selection = 25
    ..bounds = (VRectangle()
      ..x = 0
      ..y = 0
      ..width = size.width.toInt()
      ..height = size.height.toInt());
}

Future<List<int?>> _pumpScale(WidgetTester tester, {required bool vertical}) async {
  final selections = <int?>[];
  final size = _size(vertical: vertical);
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(
        width: size.width,
        height: size.height,
        child: _RecordingScaleSwt(value: _scale(vertical: vertical), selections: selections),
      ),
    ),
  ));
  await tester.pump();
  return selections;
}

/// A point halfway along the track, [acrossFraction] of the way across it.
Offset _at({required bool vertical, required double acrossFraction}) => vertical
    ? Offset(_across * acrossFraction, _long * 0.5)
    : Offset(_long * 0.5, _across * acrossFraction);

void main() {
  for (final vertical in [true, false]) {
    final name = vertical ? 'vertical' : 'horizontal';

    testWidgets('a press beside a $name Scale track moves nothing and reports nothing', (tester) async {
      final selections = await _pumpScale(tester, vertical: vertical);

      await tester.tapAt(_at(vertical: vertical, acrossFraction: 0.9));
      await tester.pump();

      expect(selections, isEmpty);
      expect(tester.widget<Slider>(find.byType(Slider)).focusNode!.hasFocus, isTrue);
    });

    testWidgets('a press on a $name Scale track moves the value', (tester) async {
      final selections = await _pumpScale(tester, vertical: vertical);

      await tester.tapAt(_at(vertical: vertical, acrossFraction: 0.5));
      await tester.pump();

      expect(selections, isNotEmpty);
      expect(selections.last, isNot(25));
    });
  }
}
