// A Tracker only exists once Java has answered DragDetect, so every drag is already under way by
// the time there is anything to report it to, and the session replays what it recorded meanwhile.
// Those recorded positions arrive measured from the window and have to be reported measured from
// the Shell's content — and which Shell that is is only known once its Tracker opens.
//
// So the conversion cannot happen when a position is recorded. On the first drag of a session there
// is no Shell chosen yet, and everything recorded before the Tracker opened would be replayed
// unconverted: off by the Shell's own header, which is taller than a whole tab strip. The workbench
// resolves the drop from those positions, so it lands a tab strip away from where it was dropped.
//
// Its own file on purpose: the session's state is per-isolate, and "the first drag of a session" is
// only reachable in a test that is the first one to run.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/gestures.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/impl/utils/tracker_session.dart';

void _open(String host, int hostId, int trackerId) {
  final action = utf8.encode('$host/$hostId/Tracker/open');
  final body = utf8.encode(json.encode({'itemId': trackerId}));
  final frame = Uint8List(2 + action.length + body.length);
  frame[0] = (action.length >> 8) & 0xFF;
  frame[1] = action.length & 0xFF;
  frame.setRange(2, 2 + action.length, action);
  frame.setRange(2 + action.length, frame.length, body);
  EquoCommService.commForTesting.receiveBinary(frame);
}

void main() {
  testWidgets('the first drag of a session is replayed in the Shell\'s coordinates',
      (tester) async {
    final sent = <VEvent>[];
    TrackerSession.sendForTesting = (id, action, event) => sent.add(event);
    addTearDown(() => TrackerSession.sendForTesting = null);

    // What a Shell registers: a window position, less the frame it draws above its content.
    TrackerSession.attachHost('Shell', 9001,
        toDisplay: (p) => p - const Offset(0, 40));

    // The whole press-and-drag happens before Java has opened anything.
    final gesture = await tester.startGesture(const Offset(200, 100));
    await gesture.moveTo(const Offset(300, 110));
    await gesture.moveTo(const Offset(400, 120));
    await tester.pump();
    expect(sent, isEmpty, reason: 'the premise: nothing is reported until a Tracker exists');

    _open('Shell', 9001, 7001);
    await tester.pump();

    expect(sent, isNotEmpty, reason: 'the Tracker is given the drag it arrived in the middle of');
    expect(sent.map((e) => e.y).toList(), [60, 70, 80],
        reason: 'measured from the Shell\'s content, as the workbench measures everything it '
            'compares these against');

    await gesture.up();
    await tester.pump();
  });
}
