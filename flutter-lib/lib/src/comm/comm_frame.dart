import 'dart:async';
import 'dart:convert';
import 'dart:typed_data';

import '../gen/widget.dart';
import '../gen/widgets.dart';
import 'comm_api.dart' show CommCallback;

/// Fused JSON→UTF-8 codec: encodes straight to bytes (and decodes from bytes)
/// in one pass, no intermediate String. Shared by every transport.
final _jsonBytes = json.fuse(utf8);

typedef OnSuccessCallback<T> = void Function(T response);
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
///     and flushed by [markOpen]).
///
/// A subclass supplies only the socket: it constructs it, calls [markOpen] when the
/// socket opens, calls [receiveBinary] with the bytes of each incoming binary frame,
/// and implements [rawSend] to put bytes on the wire.
abstract class EquoCommBase {
  final Map<String, UserEventCallback> _handlers = {};
  final Map<String, dynamic> _pending = {};
  final Map<String, void Function(Uint8List)> _rawHandlers = {};
  final Map<String, Uint8List> _rawPending = {};
  final List<Uint8List> _queue = [];
  bool _open = false;

  /// Puts an encoded frame on the wire. Only called while the socket is open.
  void rawSend(Uint8List frame);

  /// Subclasses call this once the socket is open; flushes any queued frames.
  void markOpen() {
    _open = true;
    for (final f in _queue) {
      rawSend(f);
    }
    _queue.clear();
  }

  void _enqueue(Uint8List frame) {
    if (_open) {
      rawSend(frame);
    } else {
      _queue.add(frame);
    }
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

    // Raw-bytes handlers skip JSON decode entirely.
    final rawHandler = _rawHandlers[actionId];
    if (rawHandler != null) {
      rawHandler(body ?? Uint8List(0));
      return;
    }

    dynamic payload;
    var jsonOk = true;
    if (body != null) {
      try {
        payload = _jsonBytes.decode(body);
      } catch (e) {
        jsonOk = false;
      }
    }

    final callback = _handlers[actionId];
    if (jsonOk && callback != null) {
      if (callback.args?.once ?? false) _handlers.remove(actionId);
      _deliver(actionId, callback.onSuccess, payload);
      return;
    }

    // Neither on() nor onBytes() has registered yet for this actionId (or the body isn't valid
    // JSON, meaning it's a raw-bytes payload). Buffer both ways so whichever registers first
    // can claim it.
    if (jsonOk) _pending[actionId] = payload;
    _rawPending[actionId] = body ?? Uint8List(0);
  }

  /// Runs a handler off the current call stack with its errors isolated.
  ///
  /// Uses [scheduleMicrotask], NOT `Future(...)`/`Timer`: on dart2js the latter
  /// compile to `setTimeout(_, 0)`, which the browser clamps to ~4.7 ms — that
  /// would re-impose a multi-millisecond delay on every received message (the exact
  /// floor that killed the old web comm). A microtask defers past the current stack
  /// (so a handler never runs synchronously inside [on]/[receiveBinary]) with no
  /// timer clamp.
  void _deliver(String actionId, OnSuccessCallback<dynamic> onSuccess, dynamic payload) {
    scheduleMicrotask(() {
      try {
        onSuccess(payload);
      } catch (e, st) {
        print('[comm] Handler error for "$actionId": $e\n$st');
      }
    });
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
    if (pending != null) _deliver(actionId, onSuccess, pending);
    return token;
  }

  /// Typed handler: decodes the payload into the widget value object before delivery.
  Object onWidget<V extends VWidget>(String actionId, CommCallback<V> onSuccess) {
    return on(actionId, (payload) => onSuccess(mapWidgetValue(payload) as V));
  }

  /// Raw-bytes receive: callback gets the raw frame body (no JSON decode).
  void onBytes(String actionId, void Function(Uint8List) callback) {
    _rawHandlers[actionId] = callback;
    final pending = _rawPending.remove(actionId);
    if (pending != null) {
      scheduleMicrotask(() {
        try {
          callback(pending);
        } catch (e, st) {
          print('[comm] Handler error for "$actionId": $e\n$st');
        }
      });
    }
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
