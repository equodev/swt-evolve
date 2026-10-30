// The decision table for arriving records. Every row is a way delivery can go wrong plus the one
// case where nothing is wrong, and the gate has to tell them apart without ever guessing.
//
// The bias throughout: when a change cannot be shown to fit the state a widget holds, ask for the
// widget again. Applying it on a hunch is silent corruption, and dropping it loses a change.

import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/comm/delivery_gate.dart';

const String channel = 'Label/1';

void main() {
  late DeliveryGate gate;

  setUp(() => gate = DeliveryGate());

  group('a whole description', () {
    test('is applied when nothing is held, whatever it is stamped', () {
      expect(gate.decideWhole(channel, 10), FrameAction.replace,
          reason: 'the first description of a widget is the only one there is');
    });

    test('is applied when it was written after the state held', () {
      gate.applied(channel, 50);

      expect(gate.decideWhole(channel, 51), FrameAction.replace);
    });

    test('is refused when it describes the widget as it was, not as it is', () {
      gate.applied(channel, 50);

      expect(gate.decideWhole(channel, 10), FrameAction.ignore,
          reason: 'a whole record describes the widget completely, but only as of when it was '
              'written - and one written before what is held is a replay, not news');
      expect(gate.duplicates, 1);
      expect(gate.recoveries, 0, reason: 'nothing is wrong here, so nothing is asked for again');
    });

    test('is refused when it is the state already held, delivered again', () {
      gate.applied(channel, 50);

      expect(gate.decideWhole(channel, 50), FrameAction.ignore);
    });
  });

  group('a change', () {
    test('merges when it was computed from the state held', () {
      gate.applied(channel, 10);
      expect(gate.decideChange(channel, 11, 10), FrameAction.merge);
      expect(gate.recoveries, 0);
    });

    test('recovers when it was computed from some other state', () {
      gate.applied(channel, 10);
      expect(gate.decideChange(channel, 12, 11), FrameAction.recover,
          reason: 'a change from a state we do not hold cannot be applied to the one we do');
      expect(gate.recoveries, 1);
    });

    test('recovers when nothing whole has ever arrived', () {
      expect(gate.decideChange(channel, 11, 10), FrameAction.recover,
          reason: 'there is nothing to merge into, and inventing a starting state is the corruption '
              'this gate exists to prevent');
      expect(gate.recoveries, 1);
    });
  });

  group('a change delivered twice', () {
    test('is ignored rather than recovered - it is redundant, not wrong', () {
      gate.applied(channel, 10);
      expect(gate.decideChange(channel, 11, 10), FrameAction.merge);
      gate.applied(channel, 11);

      expect(gate.decideChange(channel, 11, 10), FrameAction.ignore,
          reason: 'the change is already in the held state; re-applying it would be wrong for any '
              'property that is not idempotent, and re-fetching the widget would be waste');
      expect(gate.recoveries, 0, reason: 'a duplicate is not a delivery failure');
      expect(gate.duplicates, 1);
    });
  });

  group('bookkeeping', () {
    test('a declined change does not advance what we believe is held', () {
      gate.applied(channel, 10);
      gate.decideChange(channel, 12, 11); // recovers, caller applies nothing
      expect(gate.heldSeq(channel), 10,
          reason: 'the gate records what was applied, not what arrived - otherwise a rejected '
              'change would move the baseline and the next good one would be rejected too');
    });

    test('forgetting a channel makes the next change recover', () {
      gate.applied(channel, 10);
      gate.forget(channel);
      expect(gate.decideChange(channel, 11, 10), FrameAction.recover);
    });

    test('channels are independent', () {
      gate.applied('Label/1', 10);
      gate.applied('Label/2', 99);
      expect(gate.decideChange('Label/1', 11, 10), FrameAction.merge);
      expect(gate.heldSeq('Label/2'), 99);
    });
  });

  group('a change written before the state held', () {
    // A widget is described whole twice in one flush while a change to it, computed earlier, is
    // batched to the end of that flush - so the change arrives after descriptions that were written
    // after it. It fits nothing, but it is not news either: what is held was written later and
    // already says more.
    test('is stale, not unmergeable', () {
      gate.applied(channel, 124);

      expect(gate.decideChange(channel, 102, 89), FrameAction.ignore);
      expect(gate.recoveries, 0);
      expect(gate.duplicates, 1);
    });

    test('but one written after it, that still does not fit, is asked for', () {
      gate.applied(channel, 124);

      expect(gate.decideChange(channel, 130, 89), FrameAction.recover);
      expect(gate.recoveries, 1);
    });
  });
}
