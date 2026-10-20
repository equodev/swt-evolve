import 'package:flutter/foundation.dart' show mapEquals;

import '../comm/comm.dart';

/// The viewport handshake a window sends Java: the area the app lays out in, the monitor it is on
/// and that monitor's zoom.
///
/// A report equal to the last one on its channel is not sent again: a change that moves the scale
/// reaches here from both the scale's listener and the config's.
class ClientReadyReporter {
  ClientReadyReporter([Future Function(String channel, Object payload)? send])
      : _send = send ?? EquoCommService.sendPayload;

  final Future Function(String channel, Object payload) _send;
  bool _firstSent = false;
  final Map<String, Map<String, Object>> _lastSent = {};

  /// The next report announces a fresh client, which makes Java resync it.
  void restart() {
    _firstSent = false;
    _lastSent.clear();
  }

  void report(String widgetName, int widgetId,
      {required int width,
      required int height,
      required int displayWidth,
      required int displayHeight,
      required int zoom}) {
    final isFirst = !_firstSent;
    _firstSent = true;
    final channel = '$widgetName/$widgetId/ClientReady';
    final Map<String, Object> payload = {
      'width': width,
      'height': height,
      'isFirst': isFirst,
      'displayWidth': displayWidth,
      'displayHeight': displayHeight,
      'zoom': zoom,
    };
    if (!isFirst && mapEquals(payload, _lastSent[channel])) return;
    _lastSent[channel] = payload;
    print('[ClientReady] $widgetName/$widgetId isFirst=$isFirst size=${width}x$height '
        'monitor=${displayWidth}x$displayHeight');
    _send(channel, payload);
  }
}
