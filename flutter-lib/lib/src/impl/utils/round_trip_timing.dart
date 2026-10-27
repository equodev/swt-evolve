import 'dart:convert';

import '../../comm/comm.dart';
import '../../comm/comm_frame.dart';
import 'perf_marks.dart';

/// Splits a drag's round trip into `RoundTrip.total/java/transport/bare` User Timing measures.
/// Off unless marks are on and Java runs with `-Ddev.equo.swt.echoTiming=true`.
class RoundTripTiming {
  static const String _ping = 'swt.evolve.timing.ping';
  static const String _pong = 'swt.evolve.timing.pong';
  static const String _bare = 'swt.evolve.timing.bare';

  static int _seq = 0;
  static final Map<int, double> _sentAt = {};
  static bool _listening = false;

  /// Stamps a ping for the move just sent. Cheap and silent when marks are off.
  static void trace() {
    if (!perfMarksEnabled) return;
    EquoCommBase.queueTracedActions.add(_pong);
    EquoCommBase.queueTracedActions.add(_bare);
    _listen();
    final seq = _seq++;
    final now = perfNow();
    _sentAt[seq] = now;
    // Bounded: a drag can outrun the echoes.
    if (_sentAt.length > 256) {
      _sentAt.remove(_sentAt.keys.first);
    }
    EquoCommService.sendPayload(_ping, {'seq': seq, 't0': now});
  }

  static void _listen() {
    if (_listening) return;
    _listening = true;
    EquoCommService.onRaw(_bare, (payload) {
      final body = payload is String ? jsonDecode(payload) : payload;
      if (body is! Map) return;
      final seq = (body['seq'] as num?)?.toInt();
      // Peeked, not taken: the ordinary echo for this seq is still on its way.
      final sent = seq == null ? null : _sentAt[seq];
      if (sent != null) perfSpan('RoundTrip.bare', sent, perfNow());
    });
    EquoCommService.onRaw(_pong, (payload) {
      final body = payload is String ? jsonDecode(payload) : payload;
      if (body is! Map) return;
      final seq = (body['seq'] as num?)?.toInt();
      final javaMs = (body['javaMs'] as num?)?.toDouble();
      if (seq == null || javaMs == null) return;
      final sent = _sentAt.remove(seq);
      if (sent == null) return;
      final now = perfNow();
      perfSpan('RoundTrip.total', sent, now);
      // Transport is what remains once Java's share is taken out.
      perfSpan('RoundTrip.java', now - javaMs, now);
      perfSpan('RoundTrip.transport', sent, now - javaMs);
    });
  }
}
