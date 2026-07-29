import 'dart:async';
import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';

/// Regression for #863: a payload buffered for a not-yet-registered channel must
/// not be replayed once newer information was already delivered — replaying it
/// rolled freshly-mounted widgets back to a stale pre-reveal snapshot (blank
/// data-binding section on the first "Show Data Binding" click).
class _TestComm extends EquoCommBase {
  final List<Uint8List> sent = [];

  _TestComm() {
    markOpen();
  }

  @override
  void rawSend(Uint8List frame) => sent.add(frame);

  /// Feeds an incoming JSON frame as the transport would.
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
  test('replays a buffered payload while it is still the newest information', () async {
    final comm = _TestComm();
    comm.receiveJson('Table/1', {'width': 744});

    final received = <dynamic>[];
    comm.on('Table/1', received.add);
    await _drainMicrotasks();

    expect(received, [
      {'width': 744}
    ]);
  });

  test('drops a buffered payload once newer information was delivered (#863)', () async {
    final comm = _TestComm();

    // Stale per-widget payload arrives while the widget is unmounted (no handler).
    comm.receiveJson('Table/1', {'width': -1});

    // A newer payload (e.g. the parent's reveal payload, carrying the widget's
    // fresh embedded state) is delivered to a registered handler afterwards.
    final parentReceived = <dynamic>[];
    comm.on('SashForm/2', parentReceived.add);
    comm.receiveJson('SashForm/2', {'maximizedControl': null});
    await _drainMicrotasks();
    expect(parentReceived, hasLength(1));

    // The widget now mounts and registers: the stale buffer must NOT be replayed.
    final received = <dynamic>[];
    comm.on('Table/1', received.add);
    await _drainMicrotasks();

    expect(received, isEmpty);

    // Live payloads keep flowing normally after the dropped replay.
    comm.receiveJson('Table/1', {'width': 744});
    await _drainMicrotasks();
    expect(received, [
      {'width': 744}
    ]);
  });
}
