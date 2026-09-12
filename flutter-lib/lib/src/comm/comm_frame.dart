import 'dart:async';
import 'dart:convert';
import 'dart:typed_data';

import '../gen/widget.dart';
import '../gen/widgets.dart';
import 'comm_api.dart' show CommCallback;

/// Fused JSON→UTF-8 codec: encodes straight to bytes (and decodes from bytes)
/// in one pass, no intermediate String. Shared by every transport.
final _jsonBytes = json.fuse(utf8);

// FutureOr, not void: a handler that awaits (an image decode, a surface being created) has to be
// awaitable, or the frame after it starts while it is still half-applied. See [_drainApplyQueue].
typedef OnSuccessCallback<T> = FutureOr<void> Function(T response);
typedef OnErrorCallback = void Function(SDKCommError error);
typedef Payload = dynamic;

class SendArgs {
  bool sequential;
  SendArgs({required this.sequential});
}

class CallbackArgs {
  bool once;
  CallbackArgs({required this.once});
}

class SDKCommError {
  int? code;
  String message;
  SDKCommError({this.code, required this.message});
}

class UserEventCallback {
  String? id;
  OnSuccessCallback<dynamic> onSuccess;
  OnErrorCallback? onError;
  CallbackArgs? args;
  final Object token;
  UserEventCallback({
    this.id,
    required this.onSuccess,
    this.onError,
    this.args,
    required this.token,
  });
}

/// Transport-agnostic binary comm protocol, shared by the desktop (`dart:io`) and
/// web (`package:web`) transports. It owns everything that does not depend on the
/// socket API:
///   - the frame codec `[2-byte actionId length BE][actionId UTF-8][body]`
///     (envelope-less, fire-and-forget — same wire format as the Java server),
///   - the handler / pending / raw-bytes maps and dispatch,
///   - a send-before-open queue (sends issued before the socket opens are buffered
///     and flushed by [markOpen]),
///   - the socket lifecycle: while the socket is down sends buffer instead of
///     reaching a dead wire, and [openSocket] is retried with a capped backoff.
///
/// A subclass supplies only the socket: it constructs it (in [openSocket] if it can
/// be reopened), calls [markOpen] when the socket opens and [markClosed] when it
/// closes or fails, calls [receiveBinary] with the bytes of each incoming binary
/// frame, and implements [rawSend] to put bytes on the wire.
abstract class EquoCommBase {
  final Map<String, UserEventCallback> _handlers = {};
  final Map<String, dynamic> _pending = {};
  final Map<String, FutureOr<void> Function(Uint8List)> _rawHandlers = {};
  final Map<String, Uint8List> _rawPending = {};
  final Map<String, bool Function(Uint8List)> _arrivalHandlers = {};
  final List<Uint8List> _queue = [];
  bool _open = false;
  bool _everOpened = false;
  bool _reopenScheduled = false;
  int _reopenAttempt = 0;

  /// Cap on frames buffered while the socket is down. An outage lasts as long as the
  /// other end is away while the UI keeps producing frames, so an uncapped buffer grows
  /// for the life of the page. Oldest (most likely already stale) frames are dropped.
  static const int maxQueuedFrames = 1024;

  /// Puts an encoded frame on the wire. Only called while the socket is believed open;
  /// a transport that finds its socket dead here re-buffers via [bufferUnsent].
  void rawSend(Uint8List frame);

  /// Opens a fresh socket, which must call [markOpen] once open and [markClosed] when it
  /// closes or fails. Implemented only by transports that can be reopened; one that never
  /// calls [markClosed] never needs it.
  void openSocket() {}

  /// Called after the socket comes back up following a drop — never on the first open.
  /// Whatever the other end pushed while the socket was down was lost, so the app resyncs
  /// from here.
  void Function()? onReconnected;

  /// Subclasses call this once the socket is open; flushes any queued frames.
  void markOpen() {
    final bool reconnected = _everOpened;
    _open = true;
    _everOpened = true;
    _reopenAttempt = 0;
    // Drain into a local first: a transport whose socket died again mid-flush re-buffers
    // through [bufferUnsent], which would otherwise mutate the list being iterated.
    final pending = List<Uint8List>.of(_queue);
    _queue.clear();
    for (final f in pending) {
      rawSend(f);
    }
    if (reconnected) onReconnected?.call();
  }

  /// Subclasses call this when the socket closed or failed. Sends buffer again — calling
  /// [rawSend] on a dead socket only loses the frame (and, in a browser, logs "WebSocket is
  /// already in CLOSING or CLOSED state" per attempt) — and the socket is reopened, so a
  /// drop the app never asked for (idle timeout, sleep/resume, a network blip) is transparent.
  void markClosed() {
    _open = false;
    _scheduleReopen();
  }

  /// Re-buffers a frame the transport could not put on the wire.
  void bufferUnsent(Uint8List frame) => _buffer(frame);

  void _scheduleReopen() {
    if (_open || _reopenScheduled) return;
    _reopenScheduled = true;
    // 50ms, 100, 200, … capped at 5s: fast enough that a transient drop is invisible, slow
    // enough that an end that stays away (a closed Java side) isn't polled hard for hours.
    final shift = _reopenAttempt < 7 ? _reopenAttempt : 7;
    final delayMs = (50 * (1 << shift)).clamp(50, 5000);
    _reopenAttempt++;
    Timer(Duration(milliseconds: delayMs), () {
      _reopenScheduled = false;
      if (_open) return;
      openSocket();
    });
  }

  void _enqueue(Uint8List frame) {
    if (_open) {
      rawSend(frame);
    } else {
      _buffer(frame);
    }
  }

  void _buffer(Uint8List frame) {
    if (_queue.length >= maxQueuedFrames) _queue.removeAt(0);
    _queue.add(frame);
  }

  Uint8List _frame(String actionId, Uint8List body) {
    final actionBytes = utf8.encode(actionId);
    final out = Uint8List(2 + actionBytes.length + body.length);
    out[0] = (actionBytes.length >> 8) & 0xFF;
    out[1] = actionBytes.length & 0xFF;
    out.setRange(2, 2 + actionBytes.length, actionBytes);
    if (body.isNotEmpty) out.setRange(2 + actionBytes.length, out.length, body);
    return out;
  }

  /// Subclasses call this with the raw bytes of each received binary frame.
  void receiveBinary(Uint8List data) {
    if (data.length < 2) return;
    final nameLen = (data[0] << 8) | data[1];
    if (data.length < 2 + nameLen) return;
    // No-copy frame split: utf8.decoder.convert takes start/end (vs sublist which copies), and
    // sublistView is a view onto `data` (vs sublist which copies the body). Each incoming WS
    // message is a fresh buffer, so a retained view stays valid — matters most on the raw-bytes/
    // image path where the body can be hundreds of KB. The JSON path consumes the body
    // synchronously below; onBytes consumers must not assume the view's buffer starts at offset 0.
    final actionId = utf8.decoder.convert(data, 2, 2 + nameLen);
    final bodyLen = data.length - 2 - nameLen;
    final body = bodyLen > 0 ? Uint8List.sublistView(data, 2 + nameLen) : null;

    // A channel that can answer from already-applied state skips the queue entirely; returning
    // false means it could not, and the frame takes its turn like any other.
    final arrival = _arrivalHandlers[actionId];
    if (arrival != null && arrival(body ?? Uint8List(0))) return;

    // Which handler a frame belongs to is resolved at apply time, never at arrival: a frame
    // routinely registers the handler the next frame needs (GC/create builds the drawer that owns
    // the ops behind it).
    _enqueueApply(actionId, () => _applyFrame(actionId, body));
  }

  FutureOr<void> _applyFrame(String actionId, Uint8List? body) {
    // Raw-bytes handlers skip JSON decode entirely.
    final rawHandler = _rawHandlers[actionId];
    if (rawHandler != null) return rawHandler(body ?? Uint8List(0));

    dynamic payload;
    var jsonOk = true;
    if (body != null) {
      try {
        payload = _jsonBytes.decode(body);
      } catch (e) {
        jsonOk = false;
      }
    }

    // A run of frames fused into one: each entry is delivered on its own channel, in order.
    if (jsonOk && actionId == batchEvent && payload is List) {
      return _applyBatch(payload);
    }

    if (jsonOk) {
      final delivered = _deliverDecoded(actionId, payload);
      if (delivered != null) return delivered;
    }

    // Neither on() nor onBytes() has registered yet for this actionId (or the body isn't valid
    // JSON, meaning it's a raw-bytes payload). Buffer both ways so whichever registers first
    // can claim it.
    if (jsonOk) _pending[actionId] = payload;
    _rawPending[actionId] = body ?? Uint8List(0);
    return null;
  }

  Future<void> _applyBatch(List entries) async {
    for (final entry in entries) {
      if (entry is! List || entry.length != 2 || entry[0] is! String) continue;
      final name = entry[0] as String;
      final delivered = _deliverDecoded(name, entry[1]);
      if (delivered == null) {
        _pending[name] = entry[1];
      } else {
        await delivered;
      }
    }
  }

  /// Channel a fused run of frames arrives on.
  static const batchEvent = 'swt.evolve.batch';

  /// Runs [actionId]'s handler on an already-decoded payload, or null when none is registered.
  FutureOr<void>? _deliverDecoded(String actionId, dynamic payload) {
    final callback = _handlers[actionId];
    if (callback == null) return null;
    if (callback.args?.once ?? false) _handlers.remove(actionId);
    return callback.onSuccess(payload);
  }

  // Frames are applied one at a time, in arrival order, and a frame that awaits holds the queue
  // until it is done. The transport already delivers in order; what did not was the applying — an
  // async handler returns the moment it awaits, letting the next frame start against half-applied
  // state.
  final List<Future<void>? Function()> _applyQueue = [];
  bool _applying = false;

  /// Runs a handler off the current call stack with its errors isolated, after every frame already
  /// queued has finished applying.
  ///
  /// [scheduleMicrotask], never `Future(...)`/`Timer`: those compile to `setTimeout(_, 0)` on
  /// dart2js, which the browser clamps to ~4.7 ms per received message.
  void _deliver(String actionId, OnSuccessCallback<dynamic> onSuccess, dynamic payload) {
    _enqueueApply(actionId, () => onSuccess(payload));
  }

  void _enqueueApply(String actionId, FutureOr<void> Function() apply) {
    _applyQueue.add(() {
      try {
        final applied = apply();
        if (applied is Future) {
          return applied.catchError((Object e, StackTrace st) {
            print('[comm] Handler error for "$actionId": $e\n$st');
          });
        }
      } catch (e, st) {
        print('[comm] Handler error for "$actionId": $e\n$st');
      }
      return null;
    });
    if (!_applying) {
      _applying = true;
      scheduleMicrotask(_drainApplyQueue);
    }
  }

  Future<void> _drainApplyQueue() async {
    try {
      while (_applyQueue.isNotEmpty) {
        // Only a genuinely async handler yields: awaiting unconditionally would spend a microtask
        // turn per frame, so a run of synchronous pushes would take a run of turns to settle.
        final applied = _applyQueue.removeAt(0)();
        if (applied != null) await applied;
      }
    } finally {
      _applying = false;
    }
  }

  Future send(String actionId, [Payload? payload]) {
    final body = payload != null ? (_jsonBytes.encode(payload) as Uint8List) : Uint8List(0);
    _enqueue(_frame(actionId, body));
    return Future.value();
  }

  /// Raw-bytes send: [bytes] become the frame body verbatim, no JSON. Receiver uses [onBytes].
  Future sendBytes(String actionId, Uint8List bytes) {
    _enqueue(_frame(actionId, bytes));
    return Future.value();
  }

  Object on(String actionId, OnSuccessCallback<dynamic> onSuccess,
      [OnErrorCallback? onError, CallbackArgs? args]) {
    final token = Object();
    _handlers[actionId] = UserEventCallback(
      onSuccess: onSuccess,
      onError: onError,
      args: args,
      token: token,
    );
    final pending = _pending.remove(actionId);
    if (pending != null) {
      if (_isWidgetStateChannel(actionId)) {
        // A widget-state payload buffered while the widget was unmounted is
        // ambiguous: it may be older than the state the widget just mounted
        // with (a stale pre-reveal snapshot — replaying it blanked the whole
        // subtree) or newer (a dialog Shell's content sent right after
        // the Display embed that mounted it — dropping it left the dialog
        // empty). Instead of guessing, ask Java to re-serialize the widget:
        // the response carries the live state and arrives after the mount, so
        // it is authoritative either way.
        send(widgetRefreshChannel, actionId.substring(actionId.indexOf('/') + 1));
      } else {
        _deliver(actionId, onSuccess, pending);
      }
    }
    return token;
  }

  /// Channel Java listens on for "re-serialize widget `<id>`" requests (see
  /// FlutterBridge.handleWidgetRefresh).
  static const widgetRefreshChannel = 'swt.evolve.widget.refresh';

  /// A per-widget state channel: `{SwtClass}/{id}` with a numeric id, e.g.
  /// "Table/123" or "Shell/9". Excludes `Display/*` (a Display is not in
  /// Java's widget registry and its single pre-subscribe payload is always the
  /// newest, so the plain replay stays correct) and multi-segment event
  /// channels like "Button/1/Selection".
  static bool _isWidgetStateChannel(String actionId) {
    final slash = actionId.indexOf('/');
    if (slash <= 0 || actionId.startsWith('Display/')) return false;
    final id = actionId.substring(slash + 1);
    if (id.isEmpty) return false;
    for (final c in id.codeUnits) {
      if (c < 0x30 || c > 0x39) return false;
    }
    return true;
  }

  /// Typed handler: decodes the payload into the widget value object before delivery.
  Object onWidget<V extends VWidget>(String actionId, CommCallback<V> onSuccess) {
    return on(actionId, (payload) => onSuccess(mapWidgetValue(payload) as V));
  }

  /// Answers [actionId] at arrival, ahead of the apply queue, when it can.
  ///
  /// The handler returns true once it has answered and false to let the frame queue normally. Only
  /// for a request whose answer, when it is available at all, cannot be changed by anything still
  /// queued — a read of state already applied. A request that depends on a queued frame must
  /// return false, or it races the very frame it needs.
  ///
  /// Exists because a blocking caller on the other side is timed: making it wait out a queue of
  /// unrelated frame work is what turns a read into a timeout.
  void onArrival(String actionId, bool Function(Uint8List) handler) {
    _arrivalHandlers[actionId] = handler;
  }

  /// Raw-bytes receive: callback gets the raw frame body (no JSON decode).
  void onBytes(String actionId, FutureOr<void> Function(Uint8List) callback) {
    _rawHandlers[actionId] = callback;
    final pending = _rawPending.remove(actionId);
    // A frame that arrived before anyone was listening replays through the queue, so it still
    // lands ahead of whatever has arrived since.
    if (pending != null) _enqueueApply(actionId, () => callback(pending));
  }

  void remove(String actionId, [Object? token]) {
    if (token != null && _handlers[actionId]?.token != token) return;
    _handlers.remove(actionId);
    _pending.remove(actionId);
    _rawHandlers.remove(actionId);
    _rawPending.remove(actionId);
  }
}

/// No-op transport used when no comm port is configured (port == 0): sends are
/// dropped, nothing is ever received. Keeps call sites null-free.
class NoComm extends EquoCommBase {
  NoComm() {
    markOpen(); // flush-to-/dev/null: queued frames go straight to the no-op rawSend
  }

  @override
  void rawSend(Uint8List frame) {}
}
