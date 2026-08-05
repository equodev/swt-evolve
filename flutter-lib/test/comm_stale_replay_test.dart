import 'dart:async';
import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';

/// Regression: a widget-state payload buffered while
/// the widget was unmounted is ambiguous — replaying it can roll the widget
/// back to a stale snapshot (blank pane on the first reveal of a maximized-away
/// SashForm),
/// while silently dropping it can lose fresh content (empty Web/Mobile Recorder
/// dialogs). The comm must apply neither: it drops the buffer and asks Java to
/// re-serialize the widget, whose response is authoritative.
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
  test('a buffered widget-state payload is not replayed — a refresh is requested instead', () async {
    final comm = _TestComm();

    // Buffered while the widget is unmounted (no handler). Whether it is stale
    // or fresh (dialog content) cannot be known here.
    comm.receiveJson('Table/123', {'width': -1});

    final received = <dynamic>[];
    comm.on('Table/123', received.add);
    await _drainMicrotasks();

    // Not applied; Java is asked for the live state instead.
    expect(received, isEmpty);
    expect(comm.sent, [(EquoCommBase.widgetRefreshChannel, '"123"')]);

    // The refresh response (and any later update) flows normally.
    comm.receiveJson('Table/123', {'width': 744});
    await _drainMicrotasks();
    expect(received, [
      {'width': 744}
    ]);
  });

  test('a buffered non-widget payload is still replayed on registration', () async {
    final comm = _TestComm();
    comm.receiveJson('swt.evolve.properties', {'theme_name': 'dark'});
    comm.receiveJson('Display/7', {'bounds': null});

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

  test('no buffer means no replay and no refresh', () async {
    final comm = _TestComm();
    final received = <dynamic>[];
    comm.on('Shell/9', received.add);
    await _drainMicrotasks();

    expect(received, isEmpty);
    expect(comm.sent, isEmpty);
  });
}
