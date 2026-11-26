// A trackpad swipe arrives as a stream of wheel signals of a pixel or two each: Firefox reports
// them as a mouse, Chrome, Edge and Safari as a trackpad, and a desktop embedder as pan updates.
// Native Cocoa SWT sends a MouseWheel only once the swipe has moved far enough to make a line, and
// one per mouse wheel notch. A chart that zooms one step per MouseWheel must zoom the same few
// steps here, not one per signal, and still one per notch.

import 'package:flutter/gestures.dart'
    show PointerDeviceKind, PointerPanZoomUpdateEvent, PointerScrollEvent;
import 'package:flutter/painting.dart' show Offset;
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/utils/wheel_input.dart';

PointerScrollEvent scroll(
  double dy, {
  double dx = 0,
  int ms = 0,
  PointerDeviceKind kind = PointerDeviceKind.mouse,
}) => PointerScrollEvent(
  timeStamp: Duration(milliseconds: ms),
  kind: kind,
  position: const Offset(10, 10),
  scrollDelta: Offset(dx, dy),
);

/// The wheel events one control reports for [events], in order.
List<VEvent> wheels(Iterable<PointerScrollEvent> events, [int? type]) {
  final gesture = WheelGesture();
  return events
      .expand((e) => wheelEvents(e, stateMask: 0, gesture: gesture))
      .where((e) => type == null || e.type == type)
      .toList();
}

/// A swipe of [n] signals of [dy] each, 8 ms apart.
List<PointerScrollEvent> swipe(
  int n,
  double dy, {
  PointerDeviceKind kind = PointerDeviceKind.mouse,
}) => List.generate(n, (i) => scroll(dy, ms: 8 * i, kind: kind));

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('a swipe of small mouse-kind deltas makes a few MouseWheel events, as natively', () {
    // The swipe measured in Firefox: 196 wheel events, about 320 px in all.
    final events = wheels(swipe(196, -1.6), SWT.MouseWheel);
    expect(events.length, inInclusiveRange(1, 10));
    expect(events.map((e) => e.count), everyElement(wheelLinesPerEvent));
  });

  test('a swipe of small trackpad-kind deltas makes a few MouseWheel events of a line each', () {
    final events = wheels(swipe(196, -1.6, kind: PointerDeviceKind.trackpad), SWT.MouseWheel);
    expect(events.length, inInclusiveRange(1, 10));
    expect(events.map((e) => e.count), everyElement(1));
  });

  test('mouse-kind deltas under a notch gather into one MouseWheel per line', () {
    // 120 px: the first signal, then one per 40 px of the remaining 118.
    expect(wheels(swipe(60, 2)).length, 3);
  });

  test('every mouse notch of a quick spin reports, however small', () {
    final notches = <double>[wheelNotchPixels, 6, 12, 25, 39, 100];
    final spin = [for (var i = 0; i < notches.length; i++) scroll(-notches[i], ms: 20 * i)];
    expect(wheels(spin).map((e) => e.count), List.filled(notches.length, wheelLinesPerEvent));
    expect(wheels(swipe(20, wheelNotchPixels)).length, 20);
  });

  test('one mouse wheel notch is still one MouseWheel event with the same count', () {
    expect(wheels([scroll(-100)]).map((e) => e.count), [wheelLinesPerEvent]);
    expect(wheels([scroll(100)]).map((e) => e.count), [-wheelLinesPerEvent]);
  });

  test('a small signal after a pause reports at once', () {
    expect(wheels([scroll(-2), scroll(-2, ms: 300), scroll(2, ms: 600)]).map((e) => e.count), [
      wheelLinesPerEvent,
      wheelLinesPerEvent,
      -wheelLinesPerEvent,
    ]);
  });

  test('a direction change drops the travel gathered the other way', () {
    // 39 px up after the first signal, 2 px down, 3 px up: a line only if the 39 px were kept.
    final events = wheels([...swipe(14, -3), scroll(2, ms: 112), scroll(-3, ms: 120)]);
    expect(events.length, 1);
  });

  test('the horizontal wheel gathers its own travel', () {
    final events = wheels(List.generate(30, (i) => scroll(0, dx: 2, ms: 8 * i)));
    expect(events.map((e) => e.type), everyElement(SWT.MouseHorizontalWheel));
    expect(events.length, 2);
    expect(events.first.count, -wheelLinesPerEvent);
  });

  test('a trackpad signal of several lines reports them in one event', () {
    final events = wheels([
      scroll(-2, kind: PointerDeviceKind.trackpad),
      scroll(-121, ms: 8, kind: PointerDeviceKind.trackpad),
    ]);
    expect(events.map((e) => e.count), [1, 3]);
  });

  test('a desktop trackpad pan gathers its updates into lines', () {
    final gesture = WheelGesture();
    final events = <VEvent>[];
    for (var i = 0; i < 30; i++) {
      panAsScroll(
        PointerPanZoomUpdateEvent(
          timeStamp: Duration(milliseconds: 8 * i),
          position: const Offset(10, 10),
          pan: Offset(0, 2.0 * (i + 1)),
          panDelta: const Offset(0, 2),
        ),
        (wheel) => events.addAll(wheelEvents(wheel, stateMask: 0, gesture: gesture)),
      );
    }
    // 60 px: the first update, then one per 40 px of the remaining 58.
    expect(events.map((e) => e.count), [1, 1]);
  });
}
