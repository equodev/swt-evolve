import 'dart:async';
import 'dart:js_interop';
import 'dart:typed_data';

import 'package:web/web.dart' as web;

import '../gen/widget.dart';
import 'comm_api.dart';
import 'comm_frame.dart';

/// Host-injected comm URL. An embedding host (any reverse-proxy deployment) sets
/// this so the browser connects back to the *gateway*, not to localhost — which
/// in a hosted deployment is the user's own machine, not the runtime. Absent for
/// local desktop-web, where browser and runtime share a machine.
@JS('window.equoCommUrl')
external JSString? get _equoCommUrl;

/// Web comm: a direct browser WebSocket speaking the binary frame protocol. All
/// protocol logic lives in [EquoCommBase]; this class only owns the socket.
///
/// This replaces the legacy `equo-comm.js` text-JSON bridge (Dart→JS→`JSON.stringify`,
/// an `{actionId,payload,callbackId}` envelope, and a 5 ms `setTimeout` per send).
/// Owning the socket in Dart removes the JS-interop hops, the envelope, the double
/// JSON-encode, and the timer.
///
/// The very first connect attempt can fail transiently (the Java WS server may not
/// have finished binding when the page loads, or a cold headless-Chrome network stack
/// can refuse the upgrade once). A single fire-and-forget socket would then strand the
/// queued `ClientReady` frame forever — [markOpen] is never called, so the buffer in
/// [EquoCommBase] never flushes and the Java side times out waiting for the client.
/// To make boot deterministic we retry the *initial* connect with a capped backoff
/// until the socket opens; the send-before-open queue already makes the late flush safe.
class _BrowserComm extends EquoCommBase {
  final String _url;
  web.WebSocket? _ws;
  bool _connected = false;
  bool _reconnectScheduled = false;
  int _attempt = 0;

  _BrowserComm(String url) : _url = url {
    _connect();
  }

  void _connect() {
    final ws = web.WebSocket(_url);
    _ws = ws;
    ws.binaryType = 'arraybuffer';
    ws.onopen = ((web.Event _) {
      _connected = true;
      _attempt = 0;
      markOpen();
    }).toJS;
    ws.onmessage = ((web.MessageEvent e) {
      final data = e.data;
      if (data.isA<JSArrayBuffer>()) {
        receiveBinary((data as JSArrayBuffer).toDart.asUint8List());
      }
    }).toJS;
    // onerror and onclose both signal a dead socket; retry the initial connect so a
    // transient first-attempt failure can't permanently strand the queued ClientReady.
    ws.onerror = ((web.Event _) => _scheduleReconnect()).toJS;
    ws.onclose = ((web.CloseEvent _) => _scheduleReconnect()).toJS;
  }

  /// Retry the initial connect with a capped backoff, until the socket opens once.
  /// Once connected we stop retrying — a later drop (e.g. Java teardown) must not
  /// resurrect the socket, matching the prior no-reconnect behavior post-boot.
  void _scheduleReconnect() {
    if (_connected || _reconnectScheduled) return;
    _reconnectScheduled = true;
    // 50ms, 100, 200, ... capped at 1s.
    final shift = _attempt < 5 ? _attempt : 5;
    final delayMs = (50 * (1 << shift)).clamp(50, 1000);
    _attempt++;
    Timer(Duration(milliseconds: delayMs), () {
      _reconnectScheduled = false;
      if (_connected) return;
      _connect();
    });
  }

  @override
  void rawSend(Uint8List frame) => _ws?.send(frame.toJS);
}

/// Static facade over the web transport. Construction is the only part that differs
/// from the desktop facade; everything else delegates to [EquoCommBase].
class EquoCommService {
  static int port = 0;
  static EquoCommBase? _impl;

  static EquoCommBase get _comm => _impl ??= _create();

  static EquoCommBase _create() {
    final url = _resolveUrl();
    return url != null ? _BrowserComm(url) : NoComm();
  }

  /// Resolves the comm WebSocket URL.
  ///
  /// A host-injected `window.equoCommUrl` wins (reverse-proxy / embedding host): it
  /// may be an absolute `ws(s)://…` URL or a path like `/s/<id>/comm`. A path is
  /// resolved against the current page, so the host and TLS scheme follow it
  /// (`https` → `wss`). With nothing injected — local desktop-web, where the
  /// browser and runtime share a machine — it falls back to
  /// `ws://localhost:<port>` using the configured port.
  static String? _resolveUrl() {
    final injected = _equoCommUrl?.toDart;
    if (injected != null && injected.isNotEmpty && !injected.contains('{{')) {
      if (injected.startsWith('ws://') || injected.startsWith('wss://')) {
        return injected;
      }
      final loc = web.window.location;
      final scheme = loc.protocol == 'https:' ? 'wss' : 'ws';
      final path = injected.startsWith('/') ? injected : '/$injected';
      return '$scheme://${loc.host}$path';
    }
    // Only a real, positive port yields a localhost URL; 0 (unset) or -1
    // (comm not ready) degrade to no comm rather than building an invalid
    // `ws://localhost:-1`, which throws a SyntaxError in the WebSocket ctor.
    final p = _getPort();
    return p > 0 ? 'ws://localhost:$p' : null;
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
