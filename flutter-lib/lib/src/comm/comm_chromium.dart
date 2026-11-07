import 'dart:async';
import 'dart:js_interop';
import 'dart:typed_data';

import 'package:web/web.dart' as web;

import '../gen/widget.dart';
import 'comm_api.dart';
import 'comm_frame.dart';
import '../testing/boot_trace.dart';
import 'server_replacement.dart';
import 'widget_wire.dart' show WidgetWireWriter;

/// Host-injected comm URL. An embedding host (any reverse-proxy deployment) sets
/// this so the browser connects back to the *gateway*, not to localhost — which
/// in a hosted deployment is the user's own machine, not the runtime. Absent for
/// local desktop-web, where browser and runtime share a machine.
@JS('window.equoCommUrl')
external JSString? get _equoCommUrl;

/// [_equoCommUrl] when a host injected one, null for local desktop-web.
String? _hostInjectedCommUrl() {
  final injected = _equoCommUrl?.toDart;
  return injected != null && injected.isNotEmpty && !injected.contains('{{') ? injected : null;
}

/// The instance of the server that served this page, empty when it was not served by one.
@JS('window.evolve.instance')
external JSString? get _servedBy;

/// Web comm: a direct browser WebSocket speaking the binary frame protocol. All
/// protocol logic lives in [EquoCommBase]; this class only owns the socket.
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
  bool _wasOpen = false;
  Timer? _replacementWatch;

  _BrowserComm(String url) : _url = url {
    BootTrace.markOnce('comm.connect');
    openSocket();
  }

  @override
  void openSocket() {
    final ws = web.WebSocket(_url);
    _ws = ws;
    ws.binaryType = 'arraybuffer';
    ws.onopen = ((web.Event _) {
      if (!identical(_ws, ws)) return;
      _wasOpen = true;
      _replacementWatch?.cancel();
      _replacementWatch = null;
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
    if (_wasOpen) _watchForReplacement();
  }

  /// The JVM this page talked to is gone. An application that relaunches itself comes back as
  /// another JVM on the same origin, with another comm port and other widget ids, so reconnecting
  /// the socket can never reach it: the page reloads into it instead.
  void _watchForReplacement() {
    if (_replacementWatch != null) return;
    final servedBy = _servedBy?.toDart ?? '';
    if (servedBy.isEmpty || _hostInjectedCommUrl() != null) return;
    _askServerAfter(servedBy, 0);
  }

  void _askServerAfter(String servedBy, int attempt) {
    _replacementWatch = Timer(replacementPollDelay(attempt), () async {
      if (await _isReplaced(servedBy)) {
        web.window.location.reload();
      } else if (_replacementWatch != null) {
        _askServerAfter(servedBy, attempt + 1);
      }
    });
  }

  static Future<bool> _isReplaced(String servedBy) async {
    try {
      final response = await web.window
          .fetch('/equo-instance'.toJS, web.RequestInit(cache: 'no-store'))
          .toDart;
      final answer = response.ok ? (await response.text().toDart).toDart : null;
      return isServedByAnotherInstance(servedBy, answer);
    } catch (_) {
      return false; // Nothing is serving the origin.
    }
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

  /// A browser socket copies what it is given before send returns, so a view of the buffer will do.
  @override
  void rawSendRange(Uint8List buffer, int length) {
    final ws = _ws;
    if (ws == null || ws.readyState != web.WebSocket.OPEN) {
      super.rawSendRange(buffer, length);
      return;
    }
    ws.send(Uint8List.sublistView(buffer, 0, length).toJS);
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
    final injected = _hostInjectedCommUrl();
    if (injected != null) {
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

  /// See [EquoCommBase.on].
  static Object on(int type, int id, int channel, CommCallback<dynamic> onSuccess) =>
      _comm.on(type, id, channel, onSuccess);

  /// See [EquoCommBase.onWidgetOp].
  static void onWidgetOp(int type, int channel, WidgetOpHandler handler) =>
      _comm.onWidgetOp(type, channel, handler);

  /// See [EquoCommBase.replayWidgetOps].
  static void replayWidgetOps(int id) => _comm.replayWidgetOps(id);

  /// See [EquoCommBase.forgetWidgetOps].
  static void forgetWidgetOps(int id) => _comm.forgetWidgetOps(id);

  /// See [EquoCommBase.ensureWidgetOps].
  static void ensureWidgetOps(void Function() register) => _comm.ensureWidgetOps(register);

  /// See [EquoCommBase.deliverEscape].
  static FutureOr<void> deliverEscape(Uint8List bytes, int headerAt, int start, int end) =>
      _comm.deliverEscape(bytes, headerAt, start, end);

  /// See [EquoCommBase.onFirstGcMessage].
  static void onFirstGcMessage(int id, void Function() arrived) => _comm.onFirstGcMessage(id, arrived);

  /// See [EquoCommBase.cancelFirstGcMessage].
  static void cancelFirstGcMessage(int id, void Function() arrived) =>
      _comm.cancelFirstGcMessage(id, arrived);

  /// See [EquoCommBase.sendTo].
  static void sendTo(int type, int id, int channel, [Object? payload]) =>
      _comm.sendTo(type, id, channel, payload);

  /// See [EquoCommBase.sendFrame].
  static void sendFrame(Uint8List frame) => _comm.sendFrame(frame);

  /// See [EquoCommBase.sendWritten].
  static void sendWritten(WidgetWireWriter w) => _comm.sendWritten(w);

  /// See [EquoCommBase.sendOn].
  static void sendOn(int channel, [Object? payload]) => _comm.sendOn(channel, payload);

  /// See [EquoCommBase.onBytesAt].
  static void onBytesAt(int channel, BytesAtHandler handler) => _comm.onBytesAt(channel, handler);

  /// See [EquoCommBase.onBytes].
  static void onBytes(int type, int id, int channel, FutureOr<void> Function(Uint8List) callback) =>
      _comm.onBytes(type, id, channel, callback);

  /// See [EquoCommBase.onArrival].
  static void onArrival(int type, int id, int channel, bool Function(Uint8List) handler) =>
      _comm.onArrival(type, id, channel, handler);

  /// See [EquoCommBase.remove].
  static void remove(int type, int id, int channel, [Object? token]) => _comm.remove(type, id, channel, token);

  /// Delivers [payload] on the channel these numbers name as if it had arrived in a batch: an entry
  /// of a GC's binary frame.
  static FutureOr<void> deliverEntry(int type, int id, int channel, Object? payload) =>
      _comm.deliverEntry(type, id, channel, payload);

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
