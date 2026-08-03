// Overlapping GC drawers for the same id: a stale drawer's tokenless teardown
// unregistered the newer drawer's live handler, so the next draw op arrived with no subscriber
// and the icon never rendered. Teardown is now token-scoped; this locks that invariant.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';

/// Minimal concrete comm for driving [receiveBinary] deterministically; sends go nowhere.
class _TestComm extends EquoCommBase {
  _TestComm() {
    markOpen();
  }
  @override
  void rawSend(Uint8List frame) {}

  /// Build and deliver one framed message for [actionId] carrying [json] as its body,
  /// exactly as the wire transport would: [2-byte actionId length BE][actionId][body].
  void deliver(String actionId, Map<String, dynamic> json) {
    final action = utf8.encode(actionId);
    final body = utf8.encode(jsonEncode(json));
    final out = Uint8List(2 + action.length + body.length);
    out[0] = (action.length >> 8) & 0xFF;
    out[1] = action.length & 0xFF;
    out.setRange(2, 2 + action.length, action);
    out.setRange(2 + action.length, out.length, body);
    receiveBinary(out);
  }
}

void main() {
  const channel = 'GC/4242/drawImageImageintint';

  test('a stale drawer disposing by token does not unregister a newer drawer\'s handler', () async {
    final comm = _TestComm();

    var oldReceived = 0, newReceived = 0;

    // Old drawer subscribes, then a new drawer for the same GC id re-subscribes (overwriting
    // the channel's current handler) — this is the overlapping-lifecycle churn this test guards.
    final oldToken = comm.on(channel, (_) => oldReceived++);
    final newToken = comm.on(channel, (_) => newReceived++);

    // The stale (old) drawer's teardown removes strictly by ITS token. Because the channel is
    // now owned by the new drawer's token, this must be a no-op (the guard rejects the mismatch).
    comm.remove(channel, oldToken);

    comm.deliver(channel, {'x': 1, 'y': 2});
    await Future<void>.value(); // handlers dispatch on a microtask

    expect(newReceived, 1, reason: 'the live (new) drawer must still receive the paint op');
    expect(oldReceived, 0, reason: 'the removed (old) handler must not fire');

    // A genuine teardown by the current owner's token DOES remove it.
    comm.remove(channel, newToken);
    comm.deliver(channel, {'x': 3, 'y': 4});
    await Future<void>.value();
    expect(newReceived, 1, reason: 'after the owner removes by its own token, no handler remains');
  });

  test('regression guard: a TOKENLESS remove would clobber the newer handler (the original bug)', () async {
    // Documents exactly what the fix avoids: without a token, remove() deletes whatever handler
    // currently owns the channel — here the newer drawer's — reproducing the invisible-icon bug.
    final comm = _TestComm();

    var newReceived = 0;
    comm.on(channel, (_) {}); // old drawer
    comm.on(channel, (_) => newReceived++); // new drawer (current owner)

    comm.remove(channel); // tokenless — the pre-fix teardown

    comm.deliver(channel, {'x': 1, 'y': 2});
    await Future<void>.value();

    expect(newReceived, 0,
        reason: 'a tokenless remove clobbers the live handler — this is the failure the '
            'token-scoped teardown fixes');
  });
}
