import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';

/// A frame a handler took is not held for the next handler of the same channel.
///
/// A synchronous handler returns null, the same value delivery reports when no handler is
/// registered. Confusing the two held every frame a synchronous handler had already applied, and a
/// handler registered later on the same channel — an owner-drawn row's GC, remounted when its row
/// moves — replayed the channel's whole history: draw ops from earlier paints, at positions the row
/// no longer has, landing in its next paint.
class _TestComm extends EquoCommBase {
  _TestComm() {
    markOpen();
  }

  @override
  void rawSend(Uint8List frame) {}

  void receiveJson(String actionId, Object payload) {
    final actionBytes = utf8.encode(actionId);
    final body = utf8.encode(json.encode(payload));
    final frame = Uint8List(2 + actionBytes.length + body.length);
    frame[0] = (actionBytes.length >> 8) & 0xFF;
    frame[1] = actionBytes.length & 0xFF;
    frame.setRange(2, 2 + actionBytes.length, actionBytes);
    frame.setRange(2 + actionBytes.length, frame.length, body);
    receiveBinary(frame);
  }
}

Future<void> _drainMicrotasks() => Future<void>.delayed(Duration.zero);

void main() {
  const op = 'GC/42/drawStringStringintintboolean';

  test('a frame a synchronous handler applied is not replayed to the next handler', () async {
    final comm = _TestComm();
    final first = <dynamic>[];
    comm.on(op, (payload) {
      first.add(payload);
    });
    comm.receiveJson(op, {'y': 350, 'string': 'row'});
    await _drainMicrotasks();

    final second = <dynamic>[];
    comm.on(op, (payload) {
      second.add(payload);
    });
    await _drainMicrotasks();

    expect(first, [
      {'y': 350, 'string': 'row'}
    ]);
    expect(second, isEmpty);
  });

  test('a batched frame a synchronous handler applied is not replayed to the next handler',
      () async {
    final comm = _TestComm();
    final first = <dynamic>[];
    comm.on(op, (payload) {
      first.add(payload);
    });
    comm.receiveJson(EquoCommBase.batchEvent, [
      [op, {'y': 350, 'string': 'row'}],
      [0, {'y': 112, 'string': 'row'}],
    ]);
    await _drainMicrotasks();

    final second = <dynamic>[];
    comm.on(op, (payload) {
      second.add(payload);
    });
    await _drainMicrotasks();

    expect(first, hasLength(2));
    expect(second, isEmpty);
  });

  test('a frame no handler took is still replayed when one registers', () async {
    final comm = _TestComm();
    comm.receiveJson(op, {'y': 112, 'string': 'row'});
    await _drainMicrotasks();

    final received = <dynamic>[];
    comm.on(op, (payload) {
      received.add(payload);
    });
    await _drainMicrotasks();

    expect(received, [
      {'y': 112, 'string': 'row'}
    ]);
  });
}
