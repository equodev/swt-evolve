import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/channel_header.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';

/// Frames held for a channel nothing listens on are bounded: a widget that never mounts must not
/// keep them, nor the batch they were read from, for the life of the page.
class _TestComm extends EquoCommBase {
  _TestComm() {
    markOpen();
  }

  @override
  void rawSend(Uint8List frame) {}

  void receive(String channel, List<int> body) =>
      receiveBinary(ChannelHeader.frame(channel, Uint8List.fromList(body)));
}

Future<void> _drainMicrotasks() => Future<void>.delayed(Duration.zero);

void main() {
  test('frames held for a widget that is gone are let go', () async {
    final comm = _TestComm();
    final applied = <String>[];
    comm.receive('Spinner/9301/v', [1]);
    comm.receive('GC/9301/gcDispose', [2]);
    comm.receive('Spinner/93011/v', [3]);
    await _drainMicrotasks();

    comm.forgetHeld('Spinner/9301');
    comm.forgetHeld('GC/9301');
    comm.onBytes('Spinner/9301/v', (_) => applied.add('spinner'));
    comm.onBytes('GC/9301/gcDispose', (_) => applied.add('gc'));
    comm.onBytes('Spinner/93011/v', (_) => applied.add('another spinner'));
    await _drainMicrotasks();

    expect(applied, ['another spinner'], reason: 'only what was held for a widget still there replays');
  });

  test('frames are held for so many channels at most, the oldest let go first', () async {
    final comm = _TestComm();
    final applied = <int>[];
    const first = 100000;
    for (var i = 0; i <= EquoCommBase.maxHeldChannels; i++) {
      comm.receive('Spinner/${first + i}/v', [1]);
    }
    await _drainMicrotasks();

    for (final id in [first, first + EquoCommBase.maxHeldChannels]) {
      comm.onBytes('Spinner/$id/v', (_) => applied.add(id));
    }
    await _drainMicrotasks();

    expect(applied, [first + EquoCommBase.maxHeldChannels], reason: 'the oldest channel held for was let go');
  });

  test('a frame held out of a batch keeps its own bytes, not the batch', () async {
    final comm = _TestComm();
    Uint8List? replayed;
    final batch = Uint8List(64)..fillRange(0, 64, 7);
    comm.receive('Spinner/5/v', batch.sublist(0, 4));
    await _drainMicrotasks();

    comm.onBytes('Spinner/5/v', (bytes) => replayed = bytes);
    await _drainMicrotasks();

    expect(replayed, [7, 7, 7, 7]);
    expect(replayed!.buffer.lengthInBytes, 4, reason: 'held as a copy of its own bytes');
  });
}
