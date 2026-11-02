import 'dart:async';
import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/channel_header.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/tagged_values.dart';
import 'support/channels.dart' as channels;
import 'support/channels.dart' show NamedChannels;

/// Raw-bytes frames that arrive before `onBytes` registers are held as an ordered run per channel,
/// like tagged-values frames: a protocol's opening message is routinely followed by another before
/// the listener is up, and keeping only the newest one loses the opening message.
class _TestComm extends EquoCommBase {
  _TestComm() {
    markOpen();
  }

  @override
  void rawSend(Uint8List frame) {}

  /// Feeds an incoming raw-bytes frame as the transport would.
  void receiveRaw(String actionId, List<int> body) {
    receiveBinary(channels.frame(actionId, Uint8List.fromList(body)));
  }
}

Future<void> _drainMicrotasks() => Future<void>.delayed(Duration.zero);

void main() {
  // Bodies are not tagged values, so they can only be claimed by onBytes.
  const first = [0xFF, 0x01];
  const second = [0xFF, 0x02];

  test('raw frames received before onBytes are all replayed, in arrival order', () async {
    final comm = _TestComm();
    comm.receiveRaw('evolve.test.frameSync', first);
    comm.receiveRaw('evolve.test.frameSync', second);
    await _drainMicrotasks();

    final received = <List<int>>[];
    comm.onBytesNamed('evolve.test.frameSync', (bytes) => received.add(bytes.toList()));
    await _drainMicrotasks();

    expect(received, [first, second]);
  });

  test('replayed raw frames land ahead of frames that arrive after registration', () async {
    final comm = _TestComm();
    comm.receiveRaw('evolve.test.frameSync', first);
    comm.receiveRaw('evolve.test.frameSync', second);
    await _drainMicrotasks();

    final received = <List<int>>[];
    comm.onBytesNamed('evolve.test.frameSync', (bytes) => received.add(bytes.toList()));
    comm.receiveRaw('evolve.test.frameSync', const [0xFF, 0x03]);
    await _drainMicrotasks();

    expect(received, [first, second, const [0xFF, 0x03]]);
  });

  test('remove drops the held raw run', () async {
    final comm = _TestComm();
    comm.receiveRaw('evolve.test.frameSync', first);
    comm.receiveRaw('evolve.test.frameSync', second);
    await _drainMicrotasks();

    comm.removeNamed('evolve.test.frameSync');
    final received = <List<int>>[];
    comm.onBytesNamed('evolve.test.frameSync', (bytes) => received.add(bytes.toList()));
    await _drainMicrotasks();

    expect(received, isEmpty);
  });

  test('a raw run past the cap is dropped rather than trimmed', () async {
    final comm = _TestComm();
    for (var i = 0; i <= EquoCommBase.maxPendingPerChannel; i++) {
      comm.receiveRaw('evolve.test.frameSync', [0xFF, i & 0xFF]);
    }
    await _drainMicrotasks();

    final received = <List<int>>[];
    comm.onBytesNamed('evolve.test.frameSync', (bytes) => received.add(bytes.toList()));
    await _drainMicrotasks();

    expect(received, isEmpty);
  });

  test('a listener of decoded values claims the held frames from the raw run too', () async {
    final comm = _TestComm();
    // Tagged values are held both ways until a listener of either kind claims the channel.
    comm.receiveRaw('evolve.test.frameCost', TaggedValues.encode({'a': 1}));
    comm.receiveRaw('evolve.test.frameCost', TaggedValues.encode({'a': 2}));
    await _drainMicrotasks();

    final decoded = <dynamic>[];
    comm.onNamed('evolve.test.frameCost', decoded.add);
    final raw = <List<int>>[];
    comm.onBytesNamed('evolve.test.frameCost', (bytes) => raw.add(bytes.toList()));
    await _drainMicrotasks();

    expect(decoded, [
      {'a': 1},
      {'a': 2}
    ]);
    expect(raw, isEmpty);
  });
}
