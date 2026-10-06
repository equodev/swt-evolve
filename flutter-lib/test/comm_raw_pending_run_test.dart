import 'dart:async';
import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';

/// Raw-bytes frames that arrive before `onBytes` registers are held as an ordered run per channel,
/// like JSON frames: a protocol's opening message is routinely followed by another before the
/// listener is up, and keeping only the newest one loses the opening message.
class _TestComm extends EquoCommBase {
  _TestComm() {
    markOpen();
  }

  @override
  void rawSend(Uint8List frame) {}

  /// Feeds an incoming raw-bytes frame as the transport would.
  void receiveRaw(String actionId, List<int> body) {
    final actionBytes = actionId.codeUnits;
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
  // Bodies are not valid JSON, so they can only be claimed by onBytes.
  const first = [0xFF, 0x01];
  const second = [0xFF, 0x02];

  test('raw frames received before onBytes are all replayed, in arrival order', () async {
    final comm = _TestComm();
    comm.receiveRaw('raw.channel', first);
    comm.receiveRaw('raw.channel', second);
    await _drainMicrotasks();

    final received = <List<int>>[];
    comm.onBytes('raw.channel', (bytes) => received.add(bytes.toList()));
    await _drainMicrotasks();

    expect(received, [first, second]);
  });

  test('replayed raw frames land ahead of frames that arrive after registration', () async {
    final comm = _TestComm();
    comm.receiveRaw('raw.channel', first);
    comm.receiveRaw('raw.channel', second);
    await _drainMicrotasks();

    final received = <List<int>>[];
    comm.onBytes('raw.channel', (bytes) => received.add(bytes.toList()));
    comm.receiveRaw('raw.channel', const [0xFF, 0x03]);
    await _drainMicrotasks();

    expect(received, [first, second, const [0xFF, 0x03]]);
  });

  test('remove drops the held raw run', () async {
    final comm = _TestComm();
    comm.receiveRaw('raw.channel', first);
    comm.receiveRaw('raw.channel', second);
    await _drainMicrotasks();

    comm.remove('raw.channel');
    final received = <List<int>>[];
    comm.onBytes('raw.channel', (bytes) => received.add(bytes.toList()));
    await _drainMicrotasks();

    expect(received, isEmpty);
  });

  test('a raw run past the cap is dropped rather than trimmed', () async {
    final comm = _TestComm();
    for (var i = 0; i <= EquoCommBase.maxPendingPerChannel; i++) {
      comm.receiveRaw('raw.channel', [0xFF, i & 0xFF]);
    }
    await _drainMicrotasks();

    final received = <List<int>>[];
    comm.onBytes('raw.channel', (bytes) => received.add(bytes.toList()));
    await _drainMicrotasks();

    expect(received, isEmpty);
  });

  test('a JSON listener claims the held frames from the raw run too', () async {
    final comm = _TestComm();
    // Valid JSON bodies are held both ways until a listener of either kind claims the channel.
    comm.receiveRaw('json.channel', '{"a":1}'.codeUnits);
    comm.receiveRaw('json.channel', '{"a":2}'.codeUnits);
    await _drainMicrotasks();

    final json = <dynamic>[];
    comm.on('json.channel', json.add);
    final raw = <List<int>>[];
    comm.onBytes('json.channel', (bytes) => raw.add(bytes.toList()));
    await _drainMicrotasks();

    expect(json, [
      {'a': 1},
      {'a': 2}
    ]);
    expect(raw, isEmpty);
  });
}
