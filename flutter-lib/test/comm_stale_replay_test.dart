import 'dart:async';
import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';

/// A payload that arrives before anything is listening for it is held and replayed.
///
/// It used to be neither. Replaying it could roll a widget back to a stale snapshot (a blank pane
/// on the first reveal of a maximized-away SashForm) and dropping it could lose fresh content
/// (empty Web/Mobile Recorder dialogs), so the comm did neither and asked Java to re-serialize the
/// widget instead - a round trip on every widget described before it was mounted.
///
/// That ambiguity was never this layer's to settle: it knows nothing about what a widget holds.
/// Frames are dated now, and the delivery gate refuses one that describes a widget as it used to
/// be, so replaying is safe and the round trip is gone.
class _TestComm extends EquoCommBase {
  final List<(String, String)> sent = [];

  _TestComm() {
    markOpen();
  }

  @override
  void rawSend(Uint8List frame) {
    final nameLen = (frame[0] << 8) | frame[1];
    final actionId = utf8.decode(frame.sublist(2, 2 + nameLen));
    final body = utf8.decode(frame.sublist(2 + nameLen));
    sent.add((actionId, body));
  }

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
  test('a buffered widget-state payload is replayed on registration', () async {
    final comm = _TestComm();

    // Buffered while nothing was listening - a dialog's content sent right after the embed that
    // mounts it, which used to reach the widget only after a round trip through Java.
    //
    // A frame is routed when it is applied, not when it arrives, so let it apply: that is the
    // moment "no handler" is decided, and the moment the payload becomes a buffered one.
    comm.receiveJson('Table/123', {'width': 744, '_s': 10});
    await _drainMicrotasks();

    final received = <dynamic>[];
    comm.on('Table/123', received.add);
    await _drainMicrotasks();

    expect(received, [
      {'width': 744, '_s': 10}
    ]);
    expect(comm.sent, isEmpty,
        reason: 'nothing has to be asked for: the frame is here, and how it compares to what the '
            'widget holds is decided where that is known');
  });

  test('a buffered non-widget payload is still replayed on registration', () async {
    final comm = _TestComm();
    comm.receiveJson('swt.evolve.properties', {'theme_name': 'dark'});
    comm.receiveJson('Display/7', {'bounds': null});
    // As above: applied first, so both are genuinely buffered when the handlers register.
    await _drainMicrotasks();

    final props = <dynamic>[];
    final display = <dynamic>[];
    comm.on('swt.evolve.properties', props.add);
    comm.on('Display/7', display.add);
    await _drainMicrotasks();

    expect(props, [
      {'theme_name': 'dark'}
    ]);
    expect(display, [
      {'bounds': null}
    ]);
    expect(comm.sent, isEmpty);
  });

  test('no buffer means no replay', () async {
    final comm = _TestComm();
    final received = <dynamic>[];
    comm.on('Shell/9', received.add);
    await _drainMicrotasks();

    expect(received, isEmpty);
    expect(comm.sent, isEmpty);
  });

  test('changes held past an overflow are not replayed; the widget is asked for once, whole',
      () async {
    final comm = _TestComm();
    comm.receiveJson('GC/42', {'id': 42, '_s': 1});
    for (var seq = 2; seq <= EquoCommBase.maxPendingPerChannel + 40; seq++) {
      comm.receiveJson('GC/42', {'id': 42, '_s': seq, '_b': seq - 1});
    }
    await _drainMicrotasks();

    final received = <dynamic>[];
    comm.on('GC/42', received.add);
    await _drainMicrotasks();

    expect(received, isEmpty,
        reason: 'changes to a state that was dropped with the overflowing run cannot be applied');
    expect(comm.sent, [('swt.evolve.widget.refresh', '"42"')]);
  });

  test('a whole frame after an overflow starts the held run again', () async {
    final comm = _TestComm();
    for (var seq = 1; seq <= EquoCommBase.maxPendingPerChannel + 5; seq++) {
      comm.receiveJson('GC/43', {'id': 43, '_s': seq, '_b': seq - 1});
    }
    comm.receiveJson('GC/43', {'id': 43, '_s': 100});
    comm.receiveJson('GC/43', {'id': 43, '_s': 101, '_b': 100});
    await _drainMicrotasks();

    final received = <dynamic>[];
    comm.on('GC/43', received.add);
    await _drainMicrotasks();

    expect(received, [
      {'id': 43, '_s': 100},
      {'id': 43, '_s': 101, '_b': 100},
    ]);
    expect(comm.sent, isEmpty);
  });
}
