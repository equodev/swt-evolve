// A SWT Slider's `thumb` is the size of the visible portion of the content, and
// it consumes part of the range: the largest selection the widget will ever hold
// is `maximum - thumb`, not `maximum`. `DartSlider` enforces exactly that, in
// `setSelection` and `updateBar` alike (`Math.max(minimum, Math.min(maximum -
// thumb, value))`), and Cocoa's own `updateBar` maps that same value onto a full
// `NSScroller` — `fraction = (selection - minimum) / (maximum - thumb - minimum)`.
//
// So the rendered track has to span `minimum .. maximum - thumb`. Spanning
// `minimum .. maximum` instead strands the thumb short of the end when the
// content is fully scrolled, by exactly the fraction the page occupies, and lets
// a drag run past the reachable range — where Java's clamp pulls it back.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/slider.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/slider_evolve.dart';

const double _sliderWidth = 24;
const double _sliderHeight = 200;

// A 41-line file with 28 lines on screen: the shape of the reported case.
const int _maximum = 41;
const int _thumb = 28;
const int _maxSelection = _maximum - _thumb; // 13

VSlider _scrollbar({required int selection, int style = SWT.VERTICAL}) => VSlider()
  ..id = 1
  ..style = style
  ..enabled = true
  ..visible = true
  ..minimum = 0
  ..maximum = _maximum
  ..selection = selection
  ..thumb = _thumb
  ..increment = 1
  ..pageIncrement = _thumb
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

/// How far along its track the Material slider is actually drawn, 0.0 .. 1.0.
double _trackFraction(WidgetTester tester) {
  final slider = tester.widget<Slider>(find.byType(Slider));
  final span = slider.max - slider.min;
  return span <= 0 ? 1 : (slider.value - slider.min) / span;
}

int _selection(WidgetTester tester) =>
    tester.state<SliderImpl>(find.byType(SliderSwt<VSlider>)).state.selection!;

void main() {
  testWidgets('a fully scrolled Slider draws its thumb at the end of the track',
      (WidgetTester tester) async {
    await _pumpSlider(tester, _scrollbar(selection: _maxSelection));

    expect(_trackFraction(tester), closeTo(1.0, 0.001),
        reason: 'selection == maximum - thumb is as far as a Slider can go, so '
            'the thumb belongs at the end of the track');
  });

  testWidgets('a Slider cannot be dragged past its reachable selection',
      (WidgetTester tester) async {
    await _pumpSlider(tester, _scrollbar(selection: 0));

    // Drag well past the end of the track; the reachable range must still cap it.
    final gesture =
        await tester.startGesture(tester.getCenter(find.byType(Slider)));
    await tester.pump();
    for (var i = 0; i < 20; i++) {
      await gesture.moveBy(const Offset(0, 20));
      await tester.pump();
    }
    await gesture.up();
    await tester.pump();

    expect(_selection(tester), _maxSelection,
        reason: 'a drag past the end must settle on maximum - thumb, the value '
            'DartSlider would clamp it to, not overshoot to maximum');
  });

  testWidgets('a Slider with nothing to scroll stays at its minimum',
      (WidgetTester tester) async {
    // The page is the whole content, so `maximum - thumb` falls to the minimum
    // and the Slider has nowhere to travel. It still has to build.
    await _pumpSlider(
        tester,
        _scrollbar(selection: 0)
          ..maximum = _thumb
          ..thumb = _thumb);

    final slider = tester.widget<Slider>(find.byType(Slider));
    expect(slider.min, slider.max,
        reason: 'no reachable range when the page covers the content');
    expect(_selection(tester), 0);
    expect(tester.takeException(), isNull);
  });

  testWidgets('a horizontal Slider reserves the thumb the same way',
      (WidgetTester tester) async {
    await _pumpSlider(
        tester, _scrollbar(selection: _maxSelection, style: SWT.HORIZONTAL));

    expect(_trackFraction(tester), closeTo(1.0, 0.001),
        reason: 'the thumb consumes range regardless of orientation');
  });
}
