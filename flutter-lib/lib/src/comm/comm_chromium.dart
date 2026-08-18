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
/// The socket is opened through [EquoCommBase.openSocket], so its whole lifecycle —
/// retrying a connect that never opened, and reopening one that dropped — is the base's
/// capped-backoff loop, and sends buffer instead of reaching a dead wire in between.
///
/// Both ends of that matter here. The very first connect attempt can fail transiently (the
/// Java WS server may not have finished binding when the page loads, or a cold headless-Chrome
/// network stack can refuse the upgrade once), which without a retry strands the queued
/// `ClientReady` frame forever. And a socket that dropped long after boot — an idle timeout,
/// sleep/resume, a network blip — must come back, or every later interaction is silently lost
/// on a closed socket and the app is frozen for good.
class _BrowserComm extends EquoCommBase {
  final String _url;
  web.WebSocket? _ws;

  _BrowserComm(String url) : _url = url {
    openSocket();
  }

  @override
  void openSocket() {
    final ws = web.WebSocket(_url);
    _ws = ws;
    ws.binaryType = 'arraybuffer';
    ws.onopen = ((web.Event _) {
      if (!identical(_ws, ws)) return;
      markOpen();
    }).toJS;
    ws.onmessage = ((web.MessageEvent e) {
      final data = e.data;
      if (data.isA<JSArrayBuffer>()) {
        receiveBinary((data as JSArrayBuffer).toDart.asUint8List());
      }
    }).toJS;
    // onerror and onclose both signal a dead socket. A superseded socket's late close must
    // not tear down the one that replaced it, hence the identity check.
    ws.onerror = ((web.Event _) => _onDead(ws)).toJS;
    ws.onclose = ((web.CloseEvent _) => _onDead(ws)).toJS;
  }

  void _onDead(web.WebSocket ws) {
    if (!identical(_ws, ws)) return;
    markClosed();
  }

  @override
  void rawSend(Uint8List frame) {
    final ws = _ws;
    // A socket can be CLOSING/CLOSED before its close event has been delivered; sending then
    // throws away the frame and logs an error per attempt. Treat it as the drop it is.
    if (ws == null || ws.readyState != web.WebSocket.OPEN) {
      bufferUnsent(frame);
      markClosed();
      return;
    }
    ws.send(frame.toJS);
  }
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

  static Object onRaw(String userEventActionId, CommCallback<dynamic> onSuccess) =>
      _comm.on(userEventActionId, onSuccess);

  static Object on<V extends VWidget>(
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

  static void remove(eventName, [Object? token]) => _comm.remove(eventName, token);

  /// Registers the callback fired when the socket comes back after a drop, so the app can
  /// resync the state the other end pushed while it was down. See [EquoCommBase.onReconnected].
  static void onReconnect(void Function() callback) =>
      _comm.onReconnected = callback;

  static Future setPort(int p) async {
    port = p;
    if (p != 0) _comm; // trigger connection with the configured port
  }

  static int _getPort() => port != 0
      ? port
      : const int.fromEnvironment("equo.comm_port", defaultValue: 0);
}
