// A Selection event says the selection changed. A pointer crossing a 156-step
// track moves many times per step, so reporting on every move tells the
// application the same selection over and over, and each repeat makes it redo
// the scroll it has just done. Measured on a recorded session, 77 of 251
// events sent during five drags carried the selection already sent — and the
// application ended those drags 8 to 14 selections behind the pointer, which
// is the backlog the thumb then has to wait out once the pointer is up.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/slider.dart';
import 'package:swtflutter/src/gen/swt.dart';

/// Same as the generated SliderSwt, except sendEvent captures outgoing events
/// instead of handing them to EquoCommService, which needs a live transport.
class _CapturingSliderSwt extends SliderSwt<VSlider> {
  const _CapturingSliderSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VSlider val, String ev, VEvent? payload) =>
      onEvent(ev, payload);
}

VSlider _scrollbar() => VSlider()
  ..id = 1
  ..style = SWT.VERTICAL
  ..enabled = true
  ..visible = true
  ..minimum = 0
  ..maximum = 569
  ..selection = 0
  ..thumb = 413
  ..increment = 1
  ..pageIncrement = 413
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 24
    ..height = 400);

void main() {
  testWidgets('a drag reports each selection it crosses once',
      (WidgetTester tester) async {
    // Selection says the selection changed; the DefaultSelection that ends a
    // drag is a different event and repeats the last one by design.
    final reported = <int>[];
    final ended = <int>[];

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Center(
        child: SizedBox(
          width: 24,
          height: 400,
          child: _CapturingSliderSwt(
            value: _scrollbar(),
            onEvent: (ev, payload) {
              final index = payload?.index;
              if (index == null) return;
              if (ev == 'Selection/Selection') reported.add(index);
              if (ev == 'Selection/DefaultSelection') ended.add(index);
            },
          ),
        ),
      ),
    ));
    await tester.pumpAndSettle();

    // Many small moves, the way a pointer actually travels: several of them
    // land inside the same selection.
    final gesture =
        await tester.startGesture(tester.getCenter(find.byType(Slider)));
    await tester.pump();
    for (var i = 0; i < 40; i++) {
      await gesture.moveBy(const Offset(0, 2));
      await tester.pump();
    }
    await gesture.up();
    await tester.pump();

    expect(reported.length, greaterThan(5), reason: 'the drag crossed several');
    expect(reported.toSet().length, reported.length,
        reason: 'every reported selection should be one the drag newly '
            'crossed; repeats are work the application redoes for nothing');
    expect(reported, equals(List<int>.from(reported)..sort()),
        reason: 'a downward drag crosses selections in order');
    expect(ended, [reported.last],
        reason: 'the drag still ends on exactly one DefaultSelection, carrying '
            'the selection it committed');
  });
}
