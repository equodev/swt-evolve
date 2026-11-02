import 'dart:ui' show PlatformDispatcher;

import 'package:flutter/foundation.dart' show TargetPlatform, defaultTargetPlatform, kIsWeb, mapEquals;

import '../comm/widget_wire.dart' show sendWireWith;
import '../gen/widgets.dart' show WireChannel, wireFingerprint;
import '../comm/widget_key.dart' show WidgetKey;

/// The viewport handshake a window sends Java: the area the app lays out in, the monitor it is on,
/// that monitor's zoom, the height of the title strip the client draws above the shell, the OS the
/// client runs on, and on web the browser's locale, which Java formats numbers in. A desktop client
/// sends no locale: the JVM's applies.
///
/// A report equal to the last one on its channel is not sent again: a change that moves the scale
/// reaches here from both the scale's listener and the config's.
class ClientReadyReporter {
  ClientReadyReporter([Future Function(int type, int id, Object payload)? send, String? Function()? locale])
      : _send = send ?? _sendWire,
        _locale = locale ?? _platformLocale;

  final Future Function(int type, int id, Object payload) _send;
  final String? Function() _locale;

  static String? _platformLocale() => kIsWeb ? PlatformDispatcher.instance.locale.toLanguageTag() : null;
  bool _firstSent = false;
  final Map<int, Map<String, Object>> _lastSent = {};

  /// The next report announces a fresh client, which makes Java resync it.
  void restart() {
    _firstSent = false;
    _lastSent.clear();
  }

  void report(int type, int widgetId,
      {required int width,
      required int height,
      required int displayWidth,
      required int displayHeight,
      required int zoom,
      int insetTop = 0,
      TargetPlatform? platform}) {
    final isFirst = !_firstSent;
    _firstSent = true;
    final key = WidgetKey.of(type, widgetId);
    final locale = _locale();
    final Map<String, Object> payload = {
      'width': width,
      'height': height,
      'isFirst': isFirst,
      'displayWidth': displayWidth,
      'displayHeight': displayHeight,
      'zoom': zoom,
      'insetTop': insetTop,
      'wireFingerprint': wireFingerprint,
      if (locale != null) 'locale': locale,
      'targetPlatform': (platform ?? defaultTargetPlatform).name,
    };
    if (!isFirst && mapEquals(payload, _lastSent[key])) return;
    _lastSent[key] = payload;
    print('[ClientReady] $type/$widgetId isFirst=$isFirst size=${width}x$height '
        'monitor=${displayWidth}x$displayHeight');
    _send(type, widgetId, payload);
  }
}

/// Java's ClientReadyPayload numbers its fields 1 to 10, in the order of the report's keys.
Future _sendWire(int type, int id, Object payload) async {
  final p = payload as Map<String, Object>;
  sendWireWith(type, id, WireChannel.clientReady, (w) {
    w
      ..key(1)
      ..int32(p['width'] as int)
      ..key(2)
      ..int32(p['height'] as int)
      ..key(3)
      ..boolean(p['isFirst'] as bool)
      ..key(4)
      ..int32(p['displayWidth'] as int)
      ..key(5)
      ..int32(p['displayHeight'] as int)
      ..key(6)
      ..int32(p['zoom'] as int)
      ..key(7)
      ..int32(p['insetTop'] as int)
      ..key(8)
      ..int32(p['wireFingerprint'] as int);
    final locale = p['locale'] as String?;
    if (locale != null) w..key(9)..string(locale);
    w
      ..key(10)
      ..string(p['targetPlatform'] as String);
  });
}
