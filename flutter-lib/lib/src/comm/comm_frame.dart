import 'dart:async';
import 'dart:convert';

import 'package:meta/meta.dart';
import 'dart:typed_data';

import '../impl/utils/perf_marks.dart';

import '../gen/widget.dart';
import '../gen/widgets.dart';
import 'channel_header.dart';
import 'tagged_values.dart';
import 'comm_api.dart' show ChannelRouter;
import 'gc_wire.dart';
import 'widget_wire.dart' show widgetWireChannel, WireWritable, WidgetWireWriter, WidgetWireReader;
import '../gen/event.dart';

// FutureOr, not void: a handler that awaits (an image decode, a surface being created) has to be
// awaitable, or the frame after it starts while it is still half-applied. See [_drainApplyQueue].
typedef OnSuccessCallback<T> = FutureOr<void> Function(T response);
typedef OnErrorCallback = void Function(SDKCommError error);
typedef Payload = dynamic;

/// Told what receiving each frame cost, while something measures it ([EquoCommBase.receiveTimer]).
abstract class ReceiveTimer {
  /// The clock the readings passed to [received] are taken on, in microseconds.
  int now();

  /// A frame of [length] bytes on [channel] started decoding at [startedAt], was decoded at
  /// [decodedAt] and was handed to its handler by [appliedAt].
  void received(String channel, int length, int startedAt, int decodedAt, int appliedAt);
}

void _logHandlerError(String actionId, Object error, StackTrace stack) {
  print('[comm] Handler error for "$actionId": $error\n$stack');
}

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
///   - the frame codec: the channel as [ChannelHeader] writes it, then the body
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
  /// What times received frames; null unless something is measuring them.
  static ReceiveTimer? receiveTimer;

  final Map<String, UserEventCallback> _handlers = {};
  final Map<String, List<(int, dynamic)>> _pending = {};

  /// The channels a handler is registered on.
  @visibleForTesting
  Iterable<String> get handledChannels => _handlers.keys;

  final Map<String, FutureOr<void> Function(Uint8List)> _rawHandlers = {};

  /// What waits for a GC's first message, by GC id: see [onFirstGcMessage].
  final Map<int, void Function()> _gcWaiters = {};

  /// Calls [arrived], once, when something arrives for the GC [id] that nothing is listening for
  /// yet. What arrived is held as anything without a listener is, and replayed in order once the
  /// GC registers.
  void onFirstGcMessage(int id, void Function() arrived) => _gcWaiters[id] = arrived;

  /// Undoes [onFirstGcMessage], unless [arrived] has been replaced by another wait for the same id.
  void cancelFirstGcMessage(int id, void Function() arrived) {
    if (identical(_gcWaiters[id], arrived)) _gcWaiters.remove(id);
  }

  void _heldOn(String actionId) {
    if (_gcWaiters.isEmpty || !actionId.startsWith('GC/')) return;
    final end = actionId.indexOf('/', 3);
    final id = int.tryParse(end < 0 ? actionId.substring(3) : actionId.substring(3, end));
    if (id != null) _gcWaiters.remove(id)?.call();
  }

  /// Routers by base channel, with the token [remove] checks: see [onRouted].
  final Map<String, (ChannelRouter, Object)> _routers = {};
  final Map<String, List<(int, Uint8List)>> _rawPending = {};

  /// A GC's binary frames held for a drawer that does not exist yet. A run, as [_hold] keeps one:
  /// each frame is a paint, and a later one may only add to what an earlier one drew.
  final Map<String, List<(int, Uint8List)>> _gcPending = {};
  final Map<String, bool Function(Uint8List)> _arrivalHandlers = {};
  final List<Uint8List> _queue = [];
  bool _open = false;
  bool _everOpened = false;
  bool _reopenScheduled = false;
  int _reopenAttempt = 0;

  /// Cap on frames held for one channel nothing is listening on yet. Reached only by a channel
  /// that is written to repeatedly and never claimed, which a whole frame would have emptied - so
  /// the run is dropped rather than trimmed: half a run of partial frames is not applicable, and
  /// the far side answers a widget it cannot place by asking for it again.
  static const int maxPendingPerChannel = 64;

  /// Cap on channels frames are held for, the oldest let go first: a widget that never mounts must
  /// not keep what was sent to it for the life of the page.
  static const int maxHeldChannels = 1024;

  /// The run held for [actionId] in [held], made room for among [maxHeldChannels].
  static List<T> _runIn<T>(Map<String, List<T>> held, String actionId) {
    final run = held[actionId];
    if (run != null) return run;
    if (held.length >= maxHeldChannels) held.remove(held.keys.first);
    return held[actionId] = [];
  }

  /// Lets go of what is held for [owner] (`Type/id`) and its channels: it is gone, and will never
  /// listen for them.
  void forgetHeld(String owner) {
    bool owned(String channel) =>
        channel.startsWith(owner) && (channel.length == owner.length || channel.codeUnitAt(owner.length) == 0x2F);
    _pending.removeWhere((channel, _) => owned(channel));
    _rawPending.removeWhere((channel, _) => owned(channel));
    _gcPending.removeWhere((channel, _) => owned(channel));
  }

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

  /// Pushes deferred until after the frame, flushed before any other send so Java never sees an
  /// event ahead of the state it was made against.
  static final Set<void Function()> _owed = {};
  static bool _paying = false;

  static void owe(void Function() push) => _owed.add(push);

  static void payOwed() {
    if (_paying || _owed.isEmpty) return;
    _paying = true;
    try {
      final due = _owed.toList();
      _owed.clear();
      for (final push in due) {
        push();
      }
    } finally {
      _paying = false;
    }
  }

  void _enqueue(Uint8List frame) {
    payOwed();
    if (recordSentFrames) _sent.add(frame);
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

  Uint8List _frame(String actionId, Uint8List body) => ChannelHeader.frame(actionId, body);

  /// Frames this client has sent, recorded only while [recordSentFrames] is on.
  ///
  /// Reading a widget's rendered position only covers what the client draws; a test that never looks
  /// at what it reports cannot see the two drifting apart -- which is how a coordinate conversion
  /// came to be right in one direction and wrong in the other. Off by default, so production
  /// records nothing.
  final List<Uint8List> _sent = [];

  @visibleForTesting
  static bool recordSentFrames = false;

  @visibleForTesting
  List<Uint8List> get sentFrames => List.unmodifiable(_sent);

  /// Forgets the frames recorded so far, so a test can assert on one interaction at a time.
  @visibleForTesting
  void clearSentFrames() => _sent.clear();

  /// Splits a frame from [sentFrames] into its channel and its body as text (a binary body reads
  /// as garbage; see [decodeEvent]).
  @visibleForTesting
  static (String, String) decodeFrame(Uint8List frame) {
    final (channel, start) = ChannelHeader.read(frame)!;
    return (channel, utf8.decode(frame.sublist(start), allowMalformed: true));
  }

  /// The event a frame from [sentFrames] carries.
  @visibleForTesting
  static VEvent decodeEvent(Uint8List frame) {
    final (_, start) = ChannelHeader.read(frame)!;
    return WidgetWireReader(Uint8List.sublistView(frame, start)).fields(VEvent());
  }

  /// Subclasses call this with the raw bytes of each received binary frame.
  /// Microseconds on [clock] at which the socket last handed a frame over, before it was queued.
  static int lastReceivedMicros = 0;
  static final Stopwatch clock = Stopwatch()..start();

  void receiveBinary(Uint8List data) {
    lastReceivedMicros = clock.elapsedMicroseconds;
    final header = ChannelHeader.read(data);
    if (header == null) return;
    // The body is a view onto `data`, not a copy: each incoming message is a fresh buffer, so a
    // retained view stays valid, which matters most on the raw-bytes and image paths where the body
    // can be hundreds of KB. onBytes consumers must not assume the view's buffer starts at offset 0.
    final (actionId, start) = header;
    final body = data.length > start ? Uint8List.sublistView(data, start) : null;

    // A channel that can answer from already-applied state skips the queue entirely; returning
    // false means it could not, and the frame takes its turn like any other.
    final arrival = _arrivalHandlers[actionId];
    if (arrival != null && arrival(body ?? Uint8List(0))) return;

    // Which handler a frame belongs to is resolved at apply time, never at arrival: a frame
    // routinely registers the handler the next frame needs (GC/create builds the drawer that owns
    // the ops behind it).
    _enqueueApply(actionId, () => _applyFrame(actionId, body), ++_arrived);
  }

  FutureOr<void> _applyFrame(String actionId, Uint8List? body) {
    // A raw-bytes handler gets the body as it came.
    final rawHandler = _rawHandlers[actionId];
    if (rawHandler != null) {
      // A binary frame of changes decodes as it applies, so its whole cost counts as applying.
      final timer = receiveTimer;
      if (timer != null && body != null && actionId == widgetWireChannel) {
        final startedAt = timer.now();
        final applied = rawHandler(body);
        timer.received(actionId, body.length, startedAt, startedAt, timer.now());
        return applied;
      }
      return rawHandler(body ?? Uint8List(0));
    }
    if (isGcFrame(actionId)) {
      final frame = body ?? Uint8List(0);
      final handler = _handlers[actionId];
      if (handler != null) {
        return handler.onSuccess(frame);
      }
      _holdGcFrame(actionId, frame);
      return null;
    }

    final timer = body != null ? receiveTimer : null;
    final startedAt = timer != null ? timer.now() : 0;
    if (actionId == batchEvent && body != null) return _applyBinaryBatch(body);
    // Tagged values decode to the maps, lists and scalars they hold; any other body is bytes, for a
    // handler that reads them as such.
    dynamic payload;
    var decoded = true;
    if (body != null && body.isNotEmpty) {
      if (TaggedValues.isTagged(body)) {
        payload = TaggedValues.decode(body);
      } else {
        decoded = false;
      }
    }
    if (timer != null && decoded) {
      final decodedAt = timer.now();
      final applied = _applyDecoded(actionId, payload, body);
      timer.received(actionId, body!.length, startedAt, decodedAt, timer.now());
      return applied;
    }
    return _applyDecoded(actionId, payload, body, decoded: decoded);
  }

  /// A run of frames sent as one (Java's `MessageBatch`): per entry, its channel as a header names
  /// it, its payload's length and its payload, each applied in turn as if it had come on its own.
  Future<void> _applyBinaryBatch(Uint8List body) async {
    var pos = 0;
    while (pos < body.length) {
      final header = ChannelHeader.readAt(body, pos);
      if (header == null) return;
      final (channel, lengthAt) = header;
      var at = lengthAt;
      var length = 0;
      var scale = 1;
      while (true) {
        final x = body[at++];
        length += (x & 0x7F) * scale;
        if (x < 0x80) break;
        scale *= 128;
      }
      final payload = length == 0 ? null : Uint8List.sublistView(body, at, at + length);
      pos = at + length;
      // An entry that fails is that entry's failure alone: the rest of the run applies, as it would
      // have arriving in frames of its own.
      try {
        final applied = _applyFrame(channel, payload);
        if (applied is Future) await applied;
      } catch (e, st) {
        _logHandlerError(channel, e, st);
      }
    }
  }

  FutureOr<void> _applyDecoded(String actionId, dynamic payload, Uint8List? body, {bool decoded = true}) {
    if (decoded) {
      final delivered = _deliverDecoded(actionId, payload);
      if (!identical(delivered, _noHandler)) return delivered;
    }

    // Neither on() nor onBytes() has registered yet for this actionId (or the body is bytes rather
    // than tagged values). Buffer both ways so whichever registers first can claim it.
    if (decoded) _hold(actionId, payload);
    _holdRaw(actionId, body ?? Uint8List(0));
    return null;
  }

  /// [_hold] for a raw-bytes channel: a run in arrival order, capped the same way. A protocol's
  /// opening message is routinely followed by another before its listener is up, and a slot would
  /// keep only the later one.
  void _holdRaw(String actionId, Uint8List body) {
    _heldOn(actionId);
    final run = _runIn(_rawPending, actionId);
    if (run.length >= maxPendingPerChannel) {
      run.clear();
      return;
    }
    // Its own bytes only: a frame read out of a batch would otherwise keep the whole batch.
    run.add((_applyingSeq, Uint8List.fromList(body)));
  }

  /// Keeps a decoded payload for a channel nothing is listening on yet, to be replayed in arrival
  /// order when something is: a run, capped as [_holdRaw]'s is.
  void _hold(String actionId, dynamic payload) {
    _heldOn(actionId);
    final run = _runIn(_pending, actionId);
    if (run.length >= maxPendingPerChannel) {
      run.clear();
      return;
    }
    run.add((_applyingSeq, payload));
  }

  void _holdGcFrame(String actionId, Uint8List frame) {
    _heldOn(actionId);
    final run = _runIn(_gcPending, actionId);
    if (run.length >= maxPendingPerChannel) {
      run.clear();
    }
    run.add((_applyingSeq, Uint8List.fromList(frame)));
  }

  /// Delivers one entry of a GC's binary frame that travels on a channel of its own, as a batch
  /// delivers its entries.
  FutureOr<void> deliverEntry(String channel, Object? payload) {
    final delivered = _deliverDecoded(channel, payload);
    if (identical(delivered, _noHandler)) {
      _hold(channel, payload);
      return null;
    }
    return delivered;
  }

  /// Channel a fused run of frames arrives on.
  static const batchEvent = 'swt.evolve.batch';

  /// What [_deliverDecoded] returns when no handler is registered. Not null: a synchronous handler
  /// returns null too, and a frame it already applied must not be held for the next handler.
  static final Future<void> _noHandler = Future<void>.value();

  /// Runs [actionId]'s handler on an already-decoded payload, or returns [_noHandler].
  FutureOr<void>? _deliverDecoded(String actionId, dynamic payload) {
    final callback = _handlers[actionId];
    if (callback == null) {
      final slash = actionId.lastIndexOf('/');
      final router = slash < 0 ? null : _routers[actionId.substring(0, slash)];
      final handler = router?.$1(actionId.substring(slash + 1));
      return handler == null ? _noHandler : handler(payload);
    }
    if (callback.args?.once ?? false) _handlers.remove(actionId);
    return callback.onSuccess(payload);
  }

  // Frames are applied one at a time, in arrival order, and a frame that awaits holds the queue
  // until it is done. The transport already delivers in order; what did not was the applying — an
  // async handler returns the moment it awaits, letting the next frame start against half-applied
  // state.
  final List<(int, Future<void>? Function())> _applyQueue = [];
  bool _applying = false;

  /// How many frames have arrived; each frame's number is its place in the apply order.
  int _arrived = 0;

  /// The number of the frame being applied, which a frame held for a later handler keeps.
  int _applyingSeq = 0;

  /// Runs a handler off the current call stack with its errors isolated, after every frame already
  /// queued has finished applying.
  ///
  /// [scheduleMicrotask], never `Future(...)`/`Timer`: those compile to `setTimeout(_, 0)` on
  /// dart2js, which the browser clamps to ~4.7 ms per received message.
  void _deliver(String actionId, OnSuccessCallback<dynamic> onSuccess, dynamic payload, [int? seq]) {
    _enqueueApply(actionId, () => onSuccess(payload), seq);
  }

  /// Action ids whose queue wait is measured every time rather than sampled.
  static final Set<String> queueTracedActions = {};

  /// Queues [apply] at frame number [seq]'s place: after everything that arrived before it, ahead of
  /// everything that arrived after. A frame held until its handler registers is replayed at the
  /// place it arrived in, so one drawer's frames on several channels apply in the order they were
  /// sent whichever channel registers first. No [seq]: after everything that has arrived.
  void _enqueueApply(String actionId, FutureOr<void> Function() apply, [int? seq]) {
    final at = seq ?? _arrived;
    final queuedAt = perfNow();
    var index = _applyQueue.length;
    while (index > 0 && _applyQueue[index - 1].$1 > at) {
      index--;
    }
    _applyQueue.insert(index, (at, () {
      _applyingSeq = at;
      if (queueTracedActions.contains(actionId)) {
        perfSpan('queueWait:$actionId', queuedAt, perfNow());
      } else {
        perfSpanSampled('Comm.queueWait', queuedAt, perfNow());
      }
      try {
        final applied = apply();
        if (applied is Future) {
          return applied.catchError((Object e, StackTrace st) {
            _logHandlerError(actionId, e, st);
          });
        }
      } catch (e, st) {
        _logHandlerError(actionId, e, st);
      }
      return null;
    }));
    if (!_applying) {
      _applying = true;
      _applyingCount++;
      scheduleMicrotask(_drainApplyQueue);
    }
  }

  /// How many connections have frames received and not yet applied. Static because what waits on
  /// it, the widget registry, is one for the whole client.
  static int _applyingCount = 0;
  static final List<void Function()> _whenApplied = [];

  /// Runs [callback] once every frame received so far has been applied: when the apply queue runs
  /// dry, or in a microtask when nothing is waiting to be applied.
  ///
  /// A batch can await a handler halfway through, so the end of the current microtask is not the
  /// end of the frames that arrived together.
  static void whenApplied(void Function() callback) {
    _whenApplied.add(callback);
    if (_applyingCount == 0 && _whenApplied.length == 1) {
      scheduleMicrotask(() {
        if (_applyingCount == 0) _runWhenApplied();
      });
    }
  }

  static void _runWhenApplied() {
    while (_whenApplied.isNotEmpty) {
      final callbacks = List.of(_whenApplied);
      _whenApplied.clear();
      for (final callback in callbacks) {
        try {
          callback();
        } catch (e, st) {
          _logHandlerError('whenApplied', e, st);
        }
      }
    }
  }

  Future<void> _drainApplyQueue() async {
    try {
      while (_applyQueue.isNotEmpty) {
        // Only a genuinely async handler yields: awaiting unconditionally would spend a microtask
        // turn per frame, so a run of synchronous pushes would take a run of turns to settle.
        final applied = _applyQueue.removeAt(0).$2();
        if (applied != null) await applied;
      }
    } finally {
      _applying = false;
      if (--_applyingCount == 0) _runWhenApplied();
    }
  }

  /// Sees every event the client sends to Java, so a widget test can check what controls built
  /// from state report.
  @visibleForTesting
  static void Function(String actionId, Payload? payload)? sendObserverForTesting;

  /// Tells [sendObserverForTesting] about an event about to be sent in the wire's encoding.
  static void observeSend(String actionId, Payload? payload) =>
      sendObserverForTesting?.call(actionId, payload);

  Future send(String actionId, [Payload? payload]) {
    sendObserverForTesting?.call(actionId, payload);
    // A value in the wire's encoding, a string as its UTF-8 bytes: what Java reads them as.
    final body = switch (payload) {
      null => Uint8List(0),
      WireWritable() => WidgetWireWriter.encode(payload),
      String() => utf8.encode(payload),
      _ => TaggedValues.encode(payload),
    };
    _enqueue(_frame(actionId, body));
    return Future.value();
  }

  /// Raw-bytes send: [bytes] become the frame body verbatim. Receiver uses [onBytes].
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
    // Replayed in arrival order. Whether a buffered payload is older or newer than what the widget
    // holds is not something this layer can tell - and does not have to: every frame is dated, and
    // the delivery gate refuses one that describes the widget as it used to be. Asking Java to
    // re-serialize instead, which this did while that could not be told apart, was a round trip for
    // every widget described before it was mounted.
    // The same frames were held as raw bytes too, for an onBytes that will not come now.
    _rawPending.remove(actionId);
    for (final (seq, pending) in _pending.remove(actionId) ?? const <(int, dynamic)>[]) {
      _deliver(actionId, onSuccess, pending, seq);
    }
    for (final (seq, frame) in _gcPending.remove(actionId) ?? const <(int, Uint8List)>[]) {
      _deliver(actionId, onSuccess, frame, seq);
    }
    return token;
  }

  /// Handles every channel `<base>/<name>` that has no handler of its own: [resolve] gives the
  /// handler for a name, or null for a name it does not know. One registration in place of one per
  /// name, for a receiver that answers many names but rarely receives any.
  Object onRouted(String base, ChannelRouter resolve) {
    final token = Object();
    _routers[base] = (resolve, token);
    final prefix = '$base/';
    final held = [
      for (final channel in _pending.keys)
        if (channel.startsWith(prefix) && !_handlers.containsKey(channel)) channel,
    ];
    for (final channel in held) {
      final handler = resolve(channel.substring(prefix.length));
      if (handler == null) continue;
      _rawPending.remove(channel);
      for (final (seq, pending) in _pending.remove(channel)!) {
        _enqueueApply(channel, () => handler(pending), seq);
      }
    }
    return token;
  }

  /// Channel Java listens on for "re-serialize widget `<id>`" requests (see
  /// FlutterBridge.handleWidgetRefresh).
  static const widgetRefreshChannel = 'swt.evolve.widget.refresh';


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

  /// Raw-bytes receive: callback gets the raw frame body, undecoded.
  void onBytes(String actionId, FutureOr<void> Function(Uint8List) callback) {
    _rawHandlers[actionId] = callback;
    // A body that decoded as tagged values was held for on() too; this channel is raw now.
    _pending.remove(actionId);
    // Frames that arrived before anyone was listening replay through the queue, in arrival order,
    // so they still land ahead of whatever has arrived since.
    for (final (seq, pending) in _rawPending.remove(actionId) ?? const <(int, Uint8List)>[]) {
      _enqueueApply(actionId, () => callback(pending), seq);
    }
  }

  void remove(String actionId, [Object? token]) {
    if (token != null && identical(_routers[actionId]?.$2, token)) {
      _routers.remove(actionId);
      return;
    }
    if (token != null && _handlers[actionId]?.token != token) return;
    _handlers.remove(actionId);
    _pending.remove(actionId);
    _rawHandlers.remove(actionId);
    _rawPending.remove(actionId);
    _gcPending.remove(actionId);
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
