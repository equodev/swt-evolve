import 'dart:ui' show PlatformDispatcher;

import 'package:flutter/foundation.dart' show mapEquals;

import '../comm/comm.dart';

/// The viewport handshake a window sends Java: the area the app lays out in, the monitor it is on,
/// that monitor's zoom, the height of the title strip the client draws above the shell, and the
/// user's locale (the browser's on web, the system's on desktop), which Java formats numbers in.
///
/// A report equal to the last one on its channel is not sent again: a change that moves the scale
/// reaches here from both the scale's listener and the config's.
class ClientReadyReporter {
  ClientReadyReporter([Future Function(String channel, Object payload)? send, String Function()? locale])
      : _send = send ?? EquoCommService.sendPayload,
        _locale = locale ?? _platformLocale;

  final Future Function(String channel, Object payload) _send;
  final String Function() _locale;

  static String _platformLocale() => PlatformDispatcher.instance.locale.toLanguageTag();
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
      required int zoom,
      int insetTop = 0}) {
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
      'insetTop': insetTop,
      'locale': _locale(),
    };
    if (!isFirst && mapEquals(payload, _lastSent[channel])) return;
    _lastSent[channel] = payload;
    print('[ClientReady] $widgetName/$widgetId isFirst=$isFirst size=${width}x$height '
        'monitor=${displayWidth}x$displayHeight');
    _send(channel, payload);
  }
}
