import 'dart:async';
import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/channel_header.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/tagged_values.dart';

/// A payload that arrives before anything is listening for it is held and replayed, in arrival
/// order, once something listens: a dialog's content can arrive right after the embed that mounts
/// it, and asking Java for it again would cost a round trip.
class _TestComm extends EquoCommBase {
  final List<(String, String)> sent = [];

  _TestComm() {
    markOpen();
  }

  @override
  void rawSend(Uint8List frame) => sent.add(EquoCommBase.decodeFrame(frame));

  /// Feeds an incoming message of tagged values as the transport would.
  void receiveTagged(String channel, Object payload) =>
      receiveBinary(ChannelHeader.frame(channel, TaggedValues.encode(payload)));
}

Future<void> _drainMicrotasks() => Future<void>.delayed(Duration.zero);

void main() {
  test('a buffered payload is replayed on registration', () async {
    final comm = _TestComm();

    // A message is routed when it is applied, not when it arrives, so let it apply: that is the
    // moment "no handler" is decided, and the moment the payload becomes a buffered one.
    comm.receiveTagged('Table/123/GetItemBoundsResponse', {'width': 744});
    await _drainMicrotasks();

    final received = <dynamic>[];
    comm.on('Table/123/GetItemBoundsResponse', received.add);
    await _drainMicrotasks();

    expect(received, [
      {'width': 744}
    ]);
    expect(comm.sent, isEmpty, reason: 'nothing has to be asked for: the message is here');
  });

  test('payloads buffered on several channels each reach their own handler', () async {
    final comm = _TestComm();
    comm.receiveTagged('swt.evolve.timing.ping', {'theme_name': 'dark'});
    comm.receiveTagged('Display/7/close', {'bounds': null});
    // As above: applied first, so both are genuinely buffered when the handlers register.
    await _drainMicrotasks();

    final ping = <dynamic>[];
    final close = <dynamic>[];
    comm.on('swt.evolve.timing.ping', ping.add);
    comm.on('Display/7/close', close.add);
    await _drainMicrotasks();

    expect(ping, [
      {'theme_name': 'dark'}
    ]);
    expect(close, [
      {'bounds': null}
    ]);
    expect(comm.sent, isEmpty);
  });

  test('no buffer means no replay', () async {
    final comm = _TestComm();
    final received = <dynamic>[];
    comm.on('Shell/9/close', received.add);
    await _drainMicrotasks();

    expect(received, isEmpty);
    expect(comm.sent, isEmpty);
  });

  test('a run past its cap is dropped, and what arrives after it is held again', () async {
    final comm = _TestComm();
    for (var n = 0; n <= EquoCommBase.maxPendingPerChannel; n++) {
      comm.receiveTagged('Shell/42/close', {'n': n});
    }
    comm.receiveTagged('Shell/42/close', {'n': 'after'});
    await _drainMicrotasks();

    final received = <dynamic>[];
    comm.on('Shell/42/close', received.add);
    await _drainMicrotasks();

    expect(received, [
      {'n': 'after'}
    ], reason: 'a listener that never comes must not make the client hold its messages forever');
  });
}
