// Java owns a Slider's selection, but it only learns of a drag from the events
// Dart sends during it, it applies them one at a time, and it runs behind. So
// for a while after the pointer is up it is still reporting the middle of the
// drag. The replay below is a real one, recorded from the application: released
// on 103, and the selections that followed ran
//
//     103, 80, 82, 87, 90, 92, 93, 97, 99, 100, 101, 102, 103, 87, 97, 99
//
// — out of order, naming the committed 103 three times without being finished,
// and ending somewhere else entirely. So neither the release nor the first
// sight of the committed selection means Java has caught up; only the
// selections stopping does. The thumb holds the position the drag committed
// until then, and takes whatever Java ended on.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/slider.dart';
import 'package:swtflutter/src/gen/swt.dart';

const double _sliderWidth = 24;
const double _sliderHeight = 200;
const int _id = 1;

// The shape of the reported case: a long file, a page of it on screen.
VSlider _scrollbar() => VSlider()
  ..id = _id
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
    ..width = _sliderWidth.toInt()
    ..height = _sliderHeight.toInt());

double _rendered(WidgetTester tester) =>
    tester.widget<Slider>(find.byType(Slider)).value;

// Frames are ordered by sequence, never by the value they carry: Java's replay
// of a drag can report a lower selection than the frame before it.
int _seq = 0;

/// A selection arriving from Java, by the route it really travels.
Future<void> _javaReports(WidgetTester tester, int selection) async {
  final base = _seq;
  _seq += 1;
  VRegistry.instance.apply('Slider/$_id', <String, dynamic>{
    'id': _id,
    'swt': 'Slider',
    '_s': _seq,
    '_b': base,
    '_d': <String>['selection'],
    'selection': selection,
  });
  // A single frame, so the wait for the replay to go quiet is not advanced:
  // these arrive faster than that.
  await tester.pump();
}

Future<void> _pump(WidgetTester tester) async {
  VRegistry.instance.clear();
  _seq = 0;
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Center(
      child: SizedBox(
        width: _sliderWidth,
        height: _sliderHeight,
        child: SliderSwt<VSlider>(value: _scrollbar()),
      ),
    ),
  ));
  await tester.pumpAndSettle();
}

/// Drags the thumb down the track and returns where it was released.
Future<double> _dragAndRelease(WidgetTester tester) async {
  final gesture =
      await tester.startGesture(tester.getCenter(find.byType(Slider)));
  await tester.pump();
  for (var i = 0; i < 6; i++) {
    await gesture.moveBy(const Offset(0, 8));
    await tester.pump();
  }
  final held = _rendered(tester);
  await gesture.up();
  await tester.pump();
  return held;
}

void main() {
  testWidgets('the thumb settles on the selection the drag committed',
      (WidgetTester tester) async {
    await _pump(tester);
    final held = await _dragAndRelease(tester);

    expect(held, greaterThan(0), reason: 'the drag should have moved the thumb');
    expect(_rendered(tester), held.roundToDouble(),
        reason: 'a Slider selection is a whole number, so the thumb settles on '
            'the one that was committed rather than between two');
  });

  testWidgets('the thumb sits still through the whole replay of the drag',
      (WidgetTester tester) async {
    await _pump(tester);
    final committed = (await _dragAndRelease(tester)).roundToDouble();
    final c = committed.toInt();

    // The recorded replay, rebased on this drag: behind, ahead, and repeatedly
    // naming the committed selection itself without being finished.
    final replay = <int>[c, c - 23, c - 21, c - 16, c - 13, c - 11, c - 10, //
      c - 6, c - 4, c - 3, c - 2, c - 1, c, c - 16, c - 6, c - 4];

    for (final selection in replay) {
      await _javaReports(tester, selection);
      expect(_rendered(tester), committed,
          reason: 'selection $selection belongs to a drag that is over; the '
              'thumb must not be walked back through it');
    }
  });

  testWidgets('the thumb takes the selection Java ended on, once it stops',
      (WidgetTester tester) async {
    await _pump(tester);
    final committed = (await _dragAndRelease(tester)).roundToDouble();

    await _javaReports(tester, committed.toInt());
    await _javaReports(tester, committed.toInt() - 4);
    expect(_rendered(tester), committed, reason: 'still replaying');

    // The selections stop. Java ended 4 short of the committed one, and that is
    // where the content is, so that is where the thumb belongs.
    await tester.pump(const Duration(milliseconds: 400));
    expect(_rendered(tester), committed - 4,
        reason: 'once the replay is over Java is authoritative again');

    // And it stays authoritative for anything that happens afterwards.
    await _javaReports(tester, 7);
    await tester.pump(const Duration(milliseconds: 400));
    expect(_rendered(tester), 7);
  });

  testWidgets('a second drag is not disturbed by the first one\'s leftovers',
      (WidgetTester tester) async {
    await _pump(tester);
    await _dragAndRelease(tester);
    await _javaReports(tester, 5); // the first drag's replay, still arriving

    final gesture =
        await tester.startGesture(tester.getCenter(find.byType(Slider)));
    await tester.pump();
    for (var i = 0; i < 6; i++) {
      await gesture.moveBy(const Offset(0, 8));
      await tester.pump();
    }
    // A leftover from the first drag lands in the middle of the second one.
    await _javaReports(tester, 5);
    final held = _rendered(tester);
    expect(held, greaterThan(5),
        reason: 'the drag in progress owns the thumb, not the old replay');

    await gesture.up();
    await tester.pump();
    expect(_rendered(tester), held.roundToDouble());
  });
}
