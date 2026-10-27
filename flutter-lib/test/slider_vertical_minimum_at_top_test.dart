// A vertical SWT `Slider` has its minimum at the top: `selection == minimum`
// draws the thumb at the top of the track, and dragging the thumb down raises
// the selection. All three native backends agree — GTK builds a vertical
// `gtk_scrollbar` and only sets `gtk_range_set_inverted` for `SWT.RIGHT_TO_LEFT`,
// Win32 uses a bare `SB_VERT`, and Cocoa maps `selection == minimum` to an
// `NSScroller` `doubleValue` of 0, which is the knob at the top.
//
// `SliderImpl` builds the vertical variant by rotating the horizontal Material
// `Slider` (minimum at the left), so the rotation direction decides which end
// of the track lands at the top. `RotatedBox` rotates hit-testing along with the
// paint, so one wrong turn inverts both the rendering and the drag direction —
// the two symptoms always travel together.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/slider.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/slider_evolve.dart';

const double _sliderWidth = 24;
const double _sliderHeight = 200;

/// A vertical scrollbar-shaped Slider over 0..100.
VSlider _verticalSlider({required int selection}) => VSlider()
  ..id = 1
  ..style = SWT.VERTICAL
  ..enabled = true
  ..visible = true
  ..minimum = 0
  ..maximum = 100
  ..selection = selection
  ..thumb = 10
  ..increment = 1
  ..pageIncrement = 10
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = _sliderWidth.toInt()
    ..height = _sliderHeight.toInt());

Future<void> _pumpSlider(WidgetTester tester, VSlider value) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Center(
      child: SizedBox(
        width: _sliderWidth,
        height: _sliderHeight,
        child: SliderSwt<VSlider>(value: value),
      ),
    ),
  ));
  await tester.pumpAndSettle();
}

/// The two ends of the underlying horizontal track, in screen coordinates.
/// The Material `Slider` keeps its minimum at its local left edge, so these
/// report where the rotation actually put the minimum and the maximum.
({Offset minimum, Offset maximum}) _trackEnds(WidgetTester tester) {
  final box = tester.renderObject<RenderBox>(find.byType(Slider));
  final midHeight = box.size.height / 2;
  return (
    minimum: box.localToGlobal(Offset(0, midHeight)),
    maximum: box.localToGlobal(Offset(box.size.width, midHeight)),
  );
}

int _selection(WidgetTester tester) =>
    tester.state<SliderImpl>(find.byType(SliderSwt<VSlider>)).state.selection!;

void main() {
  testWidgets('a vertical Slider draws its minimum at the top',
      (WidgetTester tester) async {
    await _pumpSlider(tester, _verticalSlider(selection: 0));

    final ends = _trackEnds(tester);
    expect(ends.minimum.dy, lessThan(ends.maximum.dy),
        reason: 'the minimum end of a vertical Slider belongs above the maximum '
            'end; the thumb parks at the top when selection == minimum');
  });

  testWidgets('dragging a vertical Slider thumb up lowers the selection',
      (WidgetTester tester) async {
    // Mid-range, so the thumb sits at the centre of the track whichever way the
    // widget is oriented and the same upward drag is available either way.
    await _pumpSlider(tester, _verticalSlider(selection: 50));
    expect(_selection(tester), 50);

    final gesture =
        await tester.startGesture(tester.getCenter(find.byType(Slider)));
    await tester.pump();
    for (var i = 0; i < 8; i++) {
      await gesture.moveBy(const Offset(0, -10));
      await tester.pump();
    }
    await gesture.up();
    await tester.pump();

    expect(_selection(tester), lessThan(40),
        reason: 'dragging the thumb towards the minimum at the top must lower '
            'the selection, so the content scrolls up with the thumb');
  });
}
