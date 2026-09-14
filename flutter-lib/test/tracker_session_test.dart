// A Tracker blocks the Java UI thread in its own event loop until Flutter tells it the gesture
// ended, so these pin the two things that keep that safe: it becomes active when Java opens it,
// and it never stays active with nothing to end it — a Tracker opened with no button held would
// otherwise hang the whole workbench, because no pointer-up is ever coming.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/gestures.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/impl/utils/tracker_session.dart';

void _deliver(String actionId, Map<String, dynamic> body) {
  final actionBytes = utf8.encode(actionId);
  final bodyBytes = utf8.encode(json.encode(body));
  final frame = Uint8List(2 + actionBytes.length + bodyBytes.length);
  frame[0] = (actionBytes.length >> 8) & 0xFF;
  frame[1] = actionBytes.length & 0xFF;
  frame.setRange(2, 2 + actionBytes.length, actionBytes);
  frame.setRange(2 + actionBytes.length, frame.length, bodyBytes);
  EquoCommService.commForTesting.receiveBinary(frame);
}

void _open(String host, int hostId, int trackerId) =>
    _deliver('$host/$hostId/Tracker/open', {'itemId': trackerId});

void _close(String host, int hostId) => _deliver('$host/$hostId/Tracker/close', {});

/// Presses a pointer so the session sees a gesture in flight, the way a real drag would.
Future<TestGesture> _holdPointer(WidgetTester tester) =>
    tester.startGesture(const Offset(10, 10));

void main() {
  testWidgets('opening a Tracker takes the pointer', (tester) async {
    final gesture = await _holdPointer(tester);
    TrackerSession.attachHost('Shell', 8001);

    _open('Shell', 8001, 4242);
    await tester.pump();

    expect(TrackerSession.isTracking, isTrue);

    await gesture.up();
    await tester.pump();
  });

  testWidgets('releasing the pointer ends the tracking', (tester) async {
    final gesture = await _holdPointer(tester);
    TrackerSession.attachHost('Shell', 8002);

    _open('Shell', 8002, 4243);
    await tester.pump();
    expect(TrackerSession.isTracking, isTrue);

    await gesture.up();
    await tester.pump();

    expect(TrackerSession.isTracking, isFalse,
        reason: 'the Java loop only ends when Flutter says the gesture is over');
  });

  testWidgets('Java closing the Tracker releases it on this side too', (tester) async {
    final gesture = await _holdPointer(tester);
    TrackerSession.attachHost('Shell', 8003);

    _open('Shell', 8003, 4244);
    await tester.pump();
    expect(TrackerSession.isTracking, isTrue);

    _close('Shell', 8003);
    await tester.pump();

    expect(TrackerSession.isTracking, isFalse);

    await gesture.up();
    await tester.pump();
  });

  testWidgets('a Tracker that arrives after the gesture ended does not wait for it', (tester) async {
    // The Tracker only exists once Java has answered DragDetect, which is a round trip. A quick
    // drag is over by then, so nothing more will ever reach the route: holding the session open
    // means Java's loop sits idle until its silence timeout, and a Tracker that times out is
    // reported as cancelled — the workbench then throws the drop away.
    TrackerSession.attachHost('Shell', 8005);

    final gesture = await tester.startGesture(const Offset(10, 10));
    await gesture.moveTo(const Offset(120, 60));
    await gesture.up();
    await tester.pump();

    _open('Shell', 8005, 4246);
    await tester.pump();

    expect(TrackerSession.isTracking, isFalse,
        reason: 'a gesture already released must end the Tracker at once, not time it out');
  });

  testWidgets('a drag that outlasts any fixed budget is not released under the pointer',
      (tester) async {
    // Crossing several stacks takes as long as it takes, and longer still while the drop feedback
    // is slow to come back. A gesture cut short mid-drag drops the view where it was never meant to
    // go, so only a pointer that has gone quiet releases the Tracker -- never the clock alone.
    TrackerSession.attachHost('Shell', 8010);
    final gesture = await tester.startGesture(const Offset(100, 100));
    _open('Shell', 8010, 4250);
    await tester.pump();
    expect(TrackerSession.isTracking, isTrue);

    for (var i = 0; i < 12; i++) {
      await gesture.moveTo(Offset(100.0 + i * 60, 100));
      await tester.pump(const Duration(seconds: 5));
    }

    expect(TrackerSession.isTracking, isTrue,
        reason: 'a drag still moving after a minute is still a drag');

    await gesture.up();
    await tester.pump();
    expect(TrackerSession.isTracking, isFalse);
  });

  group('the pointer is sampled, not mirrored', () {
    late List<String> sent;

    setUp(() {
      sent = [];
      TrackerSession.sendForTesting =
          (id, action, event) => sent.add('$action ${event.x},${event.y}');
    });

    tearDown(() => TrackerSession.sendForTesting = null);

    testWidgets('a move too small to reach another target is not reported',
        (tester) async {
      TrackerSession.attachHost('Shell', 8006);
      final gesture = await tester.startGesture(const Offset(100, 100));
      _open('Shell', 8006, 4247);
      await tester.pump();
      sent.clear();

      // Java answers every position with a Display snapshot carrying every Shell, and it re-runs
      // the same drop resolution each time, so a crawl across a few points cannot change its answer.
      for (final dx in [4.0, 8.0, 12.0, 16.0]) {
        await gesture.moveTo(Offset(100 + dx, 100));
      }
      await tester.pump();

      expect(sent, isEmpty);

      await gesture.up();
      await tester.pump();
    });

    testWidgets('a move far enough to land somewhere else is reported',
        (tester) async {
      TrackerSession.attachHost('Shell', 8007);
      final gesture = await tester.startGesture(const Offset(100, 100));
      _open('Shell', 8007, 4248);
      await tester.pump();
      sent.clear();

      await gesture.moveTo(const Offset(400, 100));
      await tester.pump();

      expect(sent, ['Control/Move 400,100']);

      await gesture.up();
      await tester.pump();
    });

    testWidgets('the position the gesture ended on is always reported',
        (tester) async {
      TrackerSession.attachHost('Shell', 8008);
      final gesture = await tester.startGesture(const Offset(100, 100));
      _open('Shell', 8008, 4249);
      await tester.pump();
      sent.clear();

      // Released a few points from where it was last reported. Sampling that away would resolve the
      // drop on a stale position -- the workbench decides where the view lands from this one.
      await gesture.moveTo(const Offset(105, 100));
      await gesture.up();
      await tester.pump();

      expect(sent, contains('Control/Move 105,100'));
    });
  });
}
