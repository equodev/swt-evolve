import 'dart:convert';
import 'dart:typed_data';

import 'package:fake_async/fake_async.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';

/// Regression: a comm socket that dropped long after boot was never reopened, and the
/// transport kept believing it was open — so every later frame was handed to a dead socket
/// (in a browser: one "WebSocket is already in CLOSING or CLOSED state" per attempt) and
/// nothing reached Java again. The app stayed frozen until the page was refreshed.
class _TestComm extends EquoCommBase {
  final List<String> sent = [];
  int openAttempts = 0;

  /// Whether the next [openSocket] succeeds; false models an end that is still away.
  bool socketAvailable = true;

  /// Mirrors a real transport: a connect that fails reports the dead socket, exactly as a
  /// browser WebSocket fires onerror/onclose when the upgrade is refused.
  @override
  void openSocket() {
    openAttempts++;
    if (socketAvailable) {
      markOpen();
    } else {
      markClosed();
    }
  }

  @override
  void rawSend(Uint8List frame) {
    final nameLen = (frame[0] << 8) | frame[1];
    sent.add(utf8.decode(frame.sublist(2, 2 + nameLen)));
  }
}

void main() {
  test('a frame sent while the socket is down is buffered, not handed to a dead socket',
      () {
    fakeAsync((async) {
      final comm = _TestComm()..openSocket();
      comm.send('Button/1/Selection');
      expect(comm.sent, ['Button/1/Selection']);

      comm.socketAvailable = false;
      comm.markClosed();
      comm.send('Button/2/Selection');

      // The dead socket is never written to; the frame waits instead of being lost.
      expect(comm.sent, ['Button/1/Selection']);

      comm.socketAvailable = true;
      async.elapse(const Duration(seconds: 1));
      expect(comm.sent, ['Button/1/Selection', 'Button/2/Selection']);
    });
  });

  test('a socket that drops after boot is reopened', () {
    fakeAsync((async) {
      final comm = _TestComm()..openSocket();
      expect(comm.openAttempts, 1);

      comm.socketAvailable = false;
      comm.markClosed();
      async.elapse(const Duration(seconds: 30));

      // Kept trying while the other end was away, and backed off rather than spinning.
      expect(comm.openAttempts, greaterThan(1));
      expect(comm.openAttempts, lessThan(30));

      comm.socketAvailable = true;
      async.elapse(const Duration(seconds: 10));
      comm.send('Button/1/Selection');
      expect(comm.sent, ['Button/1/Selection']);
    });
  });

  test('onReconnected fires on a reopen, not on the first open', () {
    fakeAsync((async) {
      var reconnects = 0;
      final comm = _TestComm()..onReconnected = () => reconnects++;

      comm.openSocket();
      expect(reconnects, 0);

      comm.markClosed();
      async.elapse(const Duration(seconds: 1));
      expect(reconnects, 1);
    });
  });

  test('the buffer is capped so a long outage cannot grow it without bound', () {
    fakeAsync((async) {
      final comm = _TestComm()..openSocket();
      comm.socketAvailable = false;
      comm.markClosed();

      final overflow = EquoCommBase.maxQueuedFrames + 100;
      for (var i = 0; i < overflow; i++) {
        comm.send('Button/$i/Selection');
      }

      comm.socketAvailable = true;
      async.elapse(const Duration(seconds: 10));

      // Oldest dropped, newest kept, order preserved.
      expect(comm.sent.length, EquoCommBase.maxQueuedFrames);
      expect(comm.sent.first, 'Button/100/Selection');
      expect(comm.sent.last, 'Button/${overflow - 1}/Selection');
    });
  });
}
