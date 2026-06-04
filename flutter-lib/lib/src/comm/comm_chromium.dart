import 'dart:js_interop';
import 'dart:typed_data';

import 'package:web/web.dart' as web;

import '../gen/widget.dart';
import 'comm_api.dart';
import 'comm_frame.dart';

/// Web comm: a direct browser WebSocket speaking the binary frame protocol. All
/// protocol logic lives in [EquoCommBase]; this class only owns the socket.
///
/// This replaces the legacy `equo-comm.js` text-JSON bridge (Dart→JS→`JSON.stringify`,
/// an `{actionId,payload,callbackId}` envelope, and a 5 ms `setTimeout` per send).
/// Owning the socket in Dart removes the JS-interop hops, the envelope, the double
/// JSON-encode, and the timer.
class _BrowserComm extends EquoCommBase {
  late final web.WebSocket _ws;

  _BrowserComm({required String host, required int port}) {
    _ws = web.WebSocket('ws://$host:$port');
    _ws.binaryType = 'arraybuffer';
    _ws.onopen = ((web.Event _) => markOpen()).toJS;
    _ws.onmessage = ((web.MessageEvent e) {
      final data = e.data;
      if (data.isA<JSArrayBuffer>()) {
        receiveBinary((data as JSArrayBuffer).toDart.asUint8List());
      }
    }).toJS;
    _ws.onerror = ((web.Event _) => print('[comm_chromium] WebSocket error')).toJS;
  }

  @override
  void rawSend(Uint8List frame) => _ws.send(frame.toJS);
}

/// Static facade over the web transport. Construction is the only part that differs
/// from the desktop facade; everything else delegates to [EquoCommBase].
class EquoCommService {
  static int port = 0;
  static EquoCommBase? _impl;

  static EquoCommBase get _comm => _impl ??= _create();

  static EquoCommBase _create() {
    final p = _getPort();
    return p != 0 ? _BrowserComm(host: 'localhost', port: p) : NoComm();
  }

  static void onRaw(String userEventActionId, CommCallback<dynamic> onSuccess) =>
      _comm.on(userEventActionId, onSuccess);

  static void on<V extends VWidget>(
          String userEventActionId, CommCallback<V> onSuccess) =>
      _comm.onWidget<V>(userEventActionId, onSuccess);

  static Future send(String userEventActionId) => _comm.send(userEventActionId);

  static Future sendPayload(String userEventActionId, Object payload) =>
      _comm.send(userEventActionId, payload);

  static Future sendBytes(String userEventActionId, Uint8List bytes) =>
      _comm.sendBytes(userEventActionId, bytes);

  static void onBytes(
          String userEventActionId, void Function(Uint8List) callback) =>
      _comm.onBytes(userEventActionId, callback);

  static void remove(eventName) => _comm.remove(eventName);

  static Future setPort(int p) async {
    port = p;
    if (p != 0) _comm; // trigger connection with the configured port
  }

  static int _getPort() => port != 0
      ? port
      : const int.fromEnvironment("equo.comm_port", defaultValue: 0);
}
