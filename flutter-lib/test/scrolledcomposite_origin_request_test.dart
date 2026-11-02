// What the application does with setOrigin and showControl reaches a view the user has already scrolled,
// and what the user does with it still reaches the application. Java counts its own requests (never the
// user's scroll, which arrives as the selection of a scroll bar), so the client moves the view when the
// count is not the one it acted on, once: a rebuild, or the widget described again, leaves the view where
// it is. The live state's origin is where the view is.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/point.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/scrollbar.dart';
import 'package:swtflutter/src/gen/scrolledcomposite.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/swt.dart';

import 'package:swtflutter/src/gen/widgets.dart' show WireKey;

import 'delivery/support/deliver.dart';

const int _hBar = 10;
const int _vBar = 11;

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VScrollBar _bar(int id) => VScrollBar()
  ..id = id
  ..selection = 0
  ..maximum = 500
  ..thumb = 150;

VPoint _point(int x, int y) => VPoint()
  ..x = x
  ..y = y;

VScrolledComposite _sc({int seq = 1, int request = 0, int x = 0, int y = 0}) => VScrolledComposite()
  ..id = 1
  ..seq = seq
  ..style = SWT.H_SCROLL | SWT.V_SCROLL
  ..enabled = true
  ..bounds = _rect(0, 0, 150, 150)
  ..horizontalBar = _bar(_hBar)
  ..verticalBar = _bar(_vBar)
  ..origin = _point(x, y)
  ..originRequest = request
  ..content = (VComposite()
    ..id = 2
    ..style = SWT.NONE
    ..enabled = true
    ..bounds = _rect(0, 0, 500, 500)
    ..children = [
      VComposite()
        ..id = 3
        ..style = SWT.NONE
        ..enabled = true
        ..bounds = _rect(0, 0, 10, 10),
    ]);

/// What Java sends when the application calls `setOrigin`: the origin and the count of requests.
Future<void> _setOrigin(int seq, int request, int x, int y) => deliverChange(
      _sc(seq: seq, request: request, x: x, y: y),
      keys: [WireKey.k_ScrolledComposite_origin, WireKey.k_ScrolledComposite_originRequest],
      base: seq - 1,
    );

class _Harness {
  _Harness(this.tester, this.rebuild, this.value);

  final WidgetTester tester;
  final VoidCallback rebuild;
  final VScrolledComposite value;

  (int?, int?) get liveOrigin => (value.origin?.x, value.origin?.y);

  ScrollPosition _position(Axis axis) => tester
      .stateList<ScrollableState>(find.byType(Scrollable))
      .map((s) => s.position)
      .firstWhere((p) => p.axis == axis);

  double get vertical => _position(Axis.vertical).pixels;
  double get horizontal => _position(Axis.horizontal).pixels;

  Future<void> userDrags(double dy) async {
    await tester.drag(find.byType(Scrollable).first, Offset(0, dy));
    await tester.pumpAndSettle();
  }

  Future<void> rebuilt() async {
    rebuild();
    await tester.pumpAndSettle();
  }

  Future<void> settled() => tester.pumpAndSettle();
}

Future<_Harness> _pump(WidgetTester tester) async {
  await tester.pumpWidget(const SizedBox.shrink());
  freshClient();
  late StateSetter setter;
  final value = _sc();
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 150,
      height: 150,
      child: StatefulBuilder(builder: (context, setState) {
        setter = setState;
        return ScrolledCompositeSwt<VScrolledComposite>(value: value);
      }),
    ),
  ));
  await tester.pumpAndSettle();
  return _Harness(tester, () => setter(() {}), value);
}

/// The offsets the client has told Java, as the selection of [bar], in the order they were sent.
List<int> _reported(int bar) => EquoCommService.commForTesting.sentFrames
    .where((frame) => EquoCommBase.decodeFrame(frame).$1 == 'ScrollBar/$bar/Selection/Selection')
    .map((frame) => EquoCommBase.decodeEvent(frame).index ?? 0)
    .toList();

void main() {
  setUp(() => EquoCommBase.recordSentFrames = true);
  tearDown(() {
    EquoCommBase.recordSentFrames = false;
    EquoCommService.commForTesting.clearSentFrames();
  });

  testWidgets('an origin the application sets moves a view the user has scrolled',
      (tester) async {
    final h = await _pump(tester);
    await h.userDrags(-120);
    expect(h.vertical, greaterThan(0), reason: 'sanity: the user scrolled');

    await _setOrigin(2, 1, 40, 300);
    await h.settled();

    expect(h.vertical, 300);
    expect(h.horizontal, 40);
  });

  testWidgets('the view stays at the origin the application set when it is rebuilt',
      (tester) async {
    final h = await _pump(tester);
    await h.userDrags(-120);
    await _setOrigin(2, 1, 0, 300);
    await h.settled();

    await h.rebuilt();

    expect(h.vertical, 300);
  });

  testWidgets('the scroll the application made is not reported back to it',
      (tester) async {
    final h = await _pump(tester);
    await h.userDrags(-120);
    EquoCommService.commForTesting.clearSentFrames();

    await _setOrigin(2, 1, 0, 300);
    await h.settled();

    expect(h.vertical, 300);
    expect(_reported(_vBar), isEmpty);
    expect(_reported(_hBar), isEmpty);
  });

  testWidgets('a scroll by the user after the application moved the view is reported',
      (tester) async {
    final h = await _pump(tester);
    await h.userDrags(-120);
    await _setOrigin(2, 1, 0, 300);
    await h.settled();
    EquoCommService.commForTesting.clearSentFrames();

    await h.userDrags(100);

    expect(h.vertical, inInclusiveRange(150, 290));
    expect(_reported(_vBar), isNotEmpty);
    expect(_reported(_vBar).last, h.vertical.round());
  });

  testWidgets('a rebuild does not take the view back to an origin the user has left',
      (tester) async {
    final h = await _pump(tester);
    await _setOrigin(2, 1, 0, 300);
    await h.settled();
    await h.userDrags(100);
    final scrolled = h.vertical;
    expect(scrolled, lessThan(300), reason: 'sanity: the user moved away from the origin');

    await h.rebuilt();
    expect(h.vertical, scrolled);

    // The widget described again, whole, with the request it already acted on.
    await deliverWhole(_sc(seq: 3, request: 1, x: 0, y: 300));
    await h.settled();
    expect(h.vertical, scrolled);
  });

  testWidgets('asking for the origin the application asked for before moves the view there again',
      (tester) async {
    final h = await _pump(tester);
    await _setOrigin(2, 1, 0, 300);
    await h.settled();
    await h.userDrags(100);
    expect(h.vertical, lessThan(300), reason: 'sanity: the user moved away from the origin');

    // The same origin: Java does not send it again, only that it was asked for once more.
    await deliverChange(_sc(seq: 3, request: 2, x: 0, y: 300), keys: [WireKey.k_ScrolledComposite_originRequest], base: 2);
    await h.settled();

    expect(h.vertical, 300);
  });

  testWidgets('a delivery that names something else leaves the view where the user put it',
      (tester) async {
    final h = await _pump(tester);
    await h.userDrags(-120);
    final scrolled = h.vertical;

    await deliverChange(_sc(seq: 2)..minHeight = 400, keys: [WireKey.k_ScrolledComposite_minHeight], base: 1);
    await h.settled();

    expect(h.vertical, scrolled);
  });

  group('live origin', () {
    testWidgets('follows a scroll by the user', (tester) async {
      final h = await _pump(tester);

      await h.userDrags(-120);
      expect(h.vertical, greaterThan(0), reason: 'sanity: the user scrolled');
      expect(h.liveOrigin, (0, h.vertical.round()));

      await h.userDrags(50);
      expect(h.liveOrigin, (0, h.vertical.round()));
    });

    testWidgets('is where an origin the application set put the view', (tester) async {
      final h = await _pump(tester);

      await _setOrigin(2, 1, 40, 1000);
      await h.settled();

      expect(h.vertical, 350, reason: 'sanity: the view stops at the end of the content');
      expect(h.liveOrigin, (40, 350));
    });

    testWidgets('survives the widget described again with the origin Java holds', (tester) async {
      final h = await _pump(tester);
      await h.userDrags(-120);
      final scrolled = h.vertical.round();

      await deliverWhole(_sc(seq: 2));
      await h.settled();

      expect(h.liveOrigin, (0, scrolled));
    });

    testWidgets('is not the origin a repeated request moves the view to', (tester) async {
      final h = await _pump(tester);
      await _setOrigin(2, 1, 0, 300);
      await h.settled();
      await h.userDrags(100);
      expect(h.liveOrigin, (0, h.vertical.round()));
      EquoCommService.commForTesting.clearSentFrames();

      await deliverChange(_sc(seq: 3, request: 2, x: 0, y: 300), keys: [WireKey.k_ScrolledComposite_originRequest], base: 2);
      await h.settled();

      expect(h.vertical, 300);
      expect(h.liveOrigin, (0, 300));
      expect(_reported(_vBar), isEmpty, reason: 'a move the application asked for is not reported back');
    });
  });
}
