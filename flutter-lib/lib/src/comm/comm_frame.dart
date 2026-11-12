import 'dart:async';
import 'dart:convert';

import 'package:meta/meta.dart';
import 'dart:typed_data';

import '../impl/utils/perf_marks.dart';
import '../testing/boot_trace.dart';

import '../gen/widget.dart';
import '../gen/widgets.dart';
import 'channel_header.dart';
import 'widget_key.dart';
import 'tagged_values.dart';
import 'comm_api.dart' show BytesAtHandler, WidgetOpHandler;
import 'gc_wire.dart';
import 'widget_wire.dart' show WidgetWireWriter, WidgetWireReader;
import '../gen/event.dart';

// FutureOr, not void: a handler that awaits (an image decode, a surface being created) has to be
// awaitable, or the frame after it starts while it is still half-applied. See [_drainNow].
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

  EquoCommBase() {
    onBytesAt(WireChannel.swtEvolveBatch, _applyBinaryBatch);
  }

  /// What listens on each channel that is not answered per kind of widget, by the channel's number
  /// and then by its widget's key (see [WidgetKey]; 0 for a global channel): see [_Named].
  List<Map<int, _Named>?> _named = List.filled(0, null);

  /// The channels a handler is registered on.
  @visibleForTesting
  Iterable<String> get handledChannels => [
        for (final byKey in _named)
          if (byKey != null)
            for (final n in byKey.values)
              if (n.handler != null) n.name,
      ];

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

  /// The listener of the channel numbered [channel] of the widget of type [type] numbered [id]
  /// (type [_noType] for a global channel), made on first use: a frame that arrives before anyone
  /// listens is held in it.
  _Named _namedAt(int type, int id, int channel) {
    if (channel >= _named.length) _named = List.of(_named)..length = channel + 1;
    final byKey = _named[channel] ??= {};
    final key = type <= 0 ? 0 : WidgetKey.of(type, id);
    return byKey[key] ??= _Named(this, type <= 0 ? _noType : type, id, channel);
  }

  /// [_namedAt], for a listener already made; null when there is none.
  _Named? _namedIfAny(int type, int id, int channel) {
    if (channel >= _named.length) return null;
    return _named[channel]?[type <= 0 ? 0 : WidgetKey.of(type, id)];
  }

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

  /// Cap on frames buffered while the socket is down. An outage lasts as long as the
  /// other end is away while the UI keeps producing frames, so an uncapped buffer grows
  /// for the life of the page. Oldest (most likely already stale) frames are dropped.
  static const int maxQueuedFrames = 1024;

  /// Puts an encoded frame on the wire. Only called while the socket is believed open;
  /// a transport that finds its socket dead here re-buffers via [bufferUnsent].
  void rawSend(Uint8List frame);

  /// Sends the first [length] bytes of [buffer], which the caller reuses once this returns. A
  /// transport that copies a frame before returning takes them as they are; this one gets a copy.
  void rawSendRange(Uint8List buffer, int length) => rawSend(Uint8List(length)..setRange(0, length, buffer));

  /// Whether the frames sent in one turn of the event loop leave as one message, as Java's
  /// `MessageBatch` sends them: a message costs a socket write (and on a client socket a mask)
  /// whatever its size, and one input or frame often sends several. A turn that sends one frame
  /// sends it as it is. The far side must read [WireChannel.swtEvolveBatch]. Not for a browser,
  /// where a turn that draws a frame ends after the frame is rendered, so what it sent would wait
  /// for the render.
  bool get batchesSends => false;

  /// The frames of this turn, as the entries of a batch after room for its header: per frame, its
  /// header, its payload's length and its payload. Reused from turn to turn.
  Uint8List _turn = Uint8List(4096);
  int _turnSize = _batchHeaderRoom;
  int _turnFrames = 0;

  /// Where the one frame of a turn that sent only one starts in [_turn], and where its header ends.
  int _soleHeaderEnd = 0;
  int _solePayloadAt = 0;

  /// Room left before the first entry for the batch's header (a global channel's, a varint).
  static const int _batchHeaderRoom = 4;
  static final Uint8List _batchHeader = () {
    final w = BytesBuilder();
    var v = WireChannel.swtEvolveBatch << 1;
    while (v >= 0x80) {
      w.addByte((v & 0x7F) | 0x80);
      v >>= 7;
    }
    w.addByte(v);
    return w.toBytes();
  }();

  late final void Function() _sendTurn = _sendTurnNow;

  /// Puts the first [length] bytes of [frame] on the wire, with the rest of this turn's when the
  /// transport [batchesSends].
  void _send(Uint8List frame, int length) {
    if (!batchesSends) {
      rawSendRange(frame, length);
      return;
    }
    final headerEnd = ChannelHeader.skip(frame, 0);
    final payload = length - headerEnd;
    _reserve(headerEnd + 5 + payload);
    if (_turnFrames++ == 0) scheduleMicrotask(_sendTurn);
    final at = _turnSize;
    _turn.setRange(at, at + headerEnd, frame);
    var size = at + headerEnd;
    if (_turnFrames == 1) _soleHeaderEnd = size;
    var v = payload;
    while (v >= 0x80) {
      _turn[size++] = (v & 0x7F) | 0x80;
      v >>= 7;
    }
    _turn[size++] = v;
    if (_turnFrames == 1) _solePayloadAt = size;
    _turn.setRange(size, size + payload, frame, headerEnd);
    _turnSize = size + payload;
  }

  void _reserve(int more) {
    if (_turnSize + more <= _turn.length) return;
    var capacity = _turn.length * 2;
    while (capacity < _turnSize + more) {
      capacity *= 2;
    }
    _turn = Uint8List(capacity)..setRange(0, _turnSize, _turn);
  }

  void _sendTurnNow() {
    final frames = _turnFrames;
    final size = _turnSize;
    _turnFrames = 0;
    _turnSize = _batchHeaderRoom;
    if (frames == 0) return;
    if (frames == 1) {
      // The frame as it was sent: its header moved up against its payload, over its length.
      final headerStart = _batchHeaderRoom;
      final headerLength = _soleHeaderEnd - headerStart;
      final start = _solePayloadAt - headerLength;
      _turn.setRange(start, _solePayloadAt, _turn, headerStart);
      rawSendRange(Uint8List.sublistView(_turn, start, size), size - start);
      return;
    }
    final start = _batchHeaderRoom - _batchHeader.length;
    _turn.setRange(start, _batchHeaderRoom, _batchHeader);
    rawSendRange(Uint8List.sublistView(_turn, start, size), size - start);
  }

  /// Opens a fresh socket, which must call [markOpen] once open and [markClosed] when it
  /// closes or fails. Implemented only by transports that can be reopened; one that never
  /// calls [markClosed] never needs it.
  void openSocket() {}

  /// Called after the socket comes back up following a drop — never on the first open.
  /// Whatever the other end pushed while the socket was down was lost, so the app resyncs
  /// from here.
  void Function()? onReconnected;

  /// Subclasses call this once the socket is open; flushes any queued frames.
  bool _everReceived = false;

  final Completer<void> _firstOpen = Completer<void>();

  /// Completes when the socket first opens.
  Future<void> get firstOpen => _firstOpen.future;

  void markOpen() {
    BootTrace.markOnce('comm.open');
    if (!_firstOpen.isCompleted) _firstOpen.complete();
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
      _send(frame, frame.length);
    } else {
      _buffer(frame);
    }
  }

  void _buffer(Uint8List frame) {
    if (_queue.length >= maxQueuedFrames) _queue.removeAt(0);
    _queue.add(frame);
  }


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

  /// Splits a frame from [sentFrames] into its channel and its body as text: a body that is one
  /// string as the widget wire writes it reads as that string, any other as its bytes taken for
  /// UTF-8 (a binary body reads as garbage; see [decodeEvent]).
  @visibleForTesting
  static (String, String) decodeFrame(Uint8List frame) {
    final (channel, start) = ChannelHeader.read(frame)!;
    final body = frame.sublist(start);
    final r = WidgetWireReader(body);
    try {
      final text = r.string();
      if (text != null && !r.hasMore) return (channel, text);
    } catch (_) {
      // Not a string: read as bytes below.
    }
    return (channel, utf8.decode(body, allowMalformed: true));
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
    if (!_everReceived) {
      _everReceived = true;
      BootTrace.mark('comm.firstMessage');
    }
    lastReceivedMicros = clock.elapsedMicroseconds;
    // A frame for a channel answered per kind of widget goes to its handler as numbers: no channel
    // name, no view of its body, no closure to queue it.
    final seq = ++_arrived;
    final op = _widgetOpOf(data, 0);
    if (op != null) {
      _applyQueue.addOp(op, _opType, _opChannel, _opId, data, _opStart, data.length, seq);
      _scheduleDrain();
      return;
    }
    if (!_headerAt(data, 0)) {
      print('[comm] dropped a frame with no channel number');
      return;
    }
    // A number this client's table does not have: Java was generated from another table, so nothing
    // it sends can be trusted to mean what it says.
    if (wireChannelNames[_opChannel] == null || (_opType > 0 && wireTypeNames[_opType] == null)) {
      throw StateError('Java sent channel $_opChannel, type $_opType, which the wire table has no number for: '
          'Java and this client were not generated together');
    }
    final named = _namedAt(_opType, _opId, _opChannel);
    // A channel that can answer from already-applied state skips the queue entirely; returning
    // false means it could not, and the frame takes its turn like any other.
    final arrival = named.arrival;
    if (arrival != null && arrival(Uint8List.sublistView(data, _opStart))) return;
    // Which handler a frame belongs to is resolved at apply time, never at arrival: a frame
    // routinely registers the handler the next frame needs (GC/create builds the drawer that owns
    // the ops behind it).
    if (perfMarksEnabled) {
      final start = _opStart;
      _enqueueApply(named, () => named.applyFrame(data, start, data.length), seq);
      return;
    }
    _applyQueue.addOp(named.apply, named.type, named.channel, named.id, data, _opStart, data.length, seq);
    _scheduleDrain();
  }

  /// Reads the header at [at] in [data] into [_opType] ([_noType] for a global channel),
  /// [_opChannel], [_opId] and [_opStart]; false when it carries no channel number.
  bool _headerAt(Uint8List data, int at) {
    var v = 0, scale = 1, x = 0;
    do {
      if (at >= data.length) return false;
      x = data[at++];
      v += (x & 0x7F) * scale;
      scale *= 128;
    } while (x >= 0x80);
    if (v == 0) return false;
    _opChannel = v ~/ 2;
    if (v.isEven) {
      _opType = _noType;
      _opId = 0;
      _opStart = at;
      return true;
    }
    v = 0;
    scale = 1;
    do {
      if (at >= data.length) return false;
      x = data[at++];
      v += (x & 0x7F) * scale;
      scale *= 128;
    } while (x >= 0x80);
    _opType = v;
    v = 0;
    scale = 1;
    do {
      if (at >= data.length) return false;
      x = data[at++];
      v += (x & 0x7F) * scale;
      scale *= 128;
    } while (x >= 0x80);
    _opId = v.isEven ? v ~/ 2 : -(v ~/ 2) - 1;
    _opStart = at;
    return true;
  }

  /// Applies a frame on [n]'s channel, its payload in `data[start, end)`: to its raw-bytes handler,
  /// else decoded to its handler, else held for whichever registers first. Always answered, so the
  /// apply queue never holds it as an unanswered widget op.
  FutureOr<bool> _applyNamed(_Named n, Uint8List data, int start, int end) {
    final raw = n.raw;
    if (raw != null) {
      // A binary frame of changes decodes as it applies, so its whole cost counts as applying.
      final timer = end > start && n.channel == _wireChannel ? receiveTimer : null;
      final startedAt = timer != null ? timer.now() : 0;
      final applied = raw(Uint8List.sublistView(data, start, end));
      if (timer != null) timer.received(n.name, end - start, startedAt, startedAt, timer.now());
      return applied is Future ? applied.then(_handled) : true;
    }
    final timer = end > start ? receiveTimer : null;
    final startedAt = timer != null ? timer.now() : 0;
    // Tagged values decode to the maps, lists and scalars they hold, read in place; any other body
    // is bytes, for a handler that reads them as such.
    dynamic payload;
    var decoded = true;
    if (end > start) {
      if (data[start] == TaggedValues.mark) {
        payload = TaggedValues.decodeAt(data, start);
      } else {
        decoded = false;
      }
    }
    final decodedAt = timer != null ? timer.now() : 0;
    final handler = decoded ? n.handler : null;
    if (handler == null) {
      // Neither on() nor onBytes() has registered yet (or the body is bytes rather than tagged
      // values). Held both ways, so whichever registers first can claim it.
      _hold(n, decoded, payload, Uint8List.sublistView(data, start, end));
      return true;
    }
    if (handler.args?.once ?? false) n.handler = null;
    final applied = handler.onSuccess(payload);
    if (timer != null) timer.received(n.name, end - start, startedAt, decodedAt, timer.now());
    return applied is Future ? applied.then(_handled) : true;
  }

  static const int _wireChannel = WireChannel.swtEvolveWire;

  /// Keeps a frame for [n]'s channel nothing is listening on yet, to be replayed in arrival order
  /// when something is. A run, not a slot: a protocol's opening message is routinely followed by
  /// another before its listener is up.
  void _hold(_Named n, bool decoded, dynamic payload, Uint8List body) {
    if (n.type == _gcType) _gcWaiters.remove(n.id)?.call();
    final raw = n.heldRaw ??= [];
    if (raw.length >= maxPendingPerChannel) {
      raw.clear();
      n.heldRawAt?.clear();
      n.held?.clear();
      n.heldAt?.clear();
      return;
    }
    raw.add(body);
    (n.heldRawAt ??= []).add(_applyingSeq);
    if (decoded) {
      (n.held ??= []).add(payload);
      (n.heldAt ??= []).add(_applyingSeq);
    }
  }

  /// A run of frames sent as one (Java's `MessageBatch`): per entry, its channel as a header names
  /// it, its payload's length and its payload, each applied in turn as if it had come on its own.
  ///
  /// Synchronous while its entries are: only an entry whose handler returns a future makes the rest
  /// of the run wait for it (see [_batchAfterOp] and [_batchAfter]).
  FutureOr<void> _applyBinaryBatch(Uint8List body, int pos, int end) {
    while (pos < end) {
      final op = _widgetOpOf(body, pos);
      if (op != null) {
        // As [receiveBinary] queues one, read in place up to the entry's end.
        final type = _opType, channel = _opChannel, id = _opId;
        var at = _opStart;
        var length = 0;
        var scale = 1;
        while (true) {
          final x = body[at++];
          length += (x & 0x7F) * scale;
          if (x < 0x80) break;
          scale *= 128;
        }
        pos = at + length;
        try {
          final handled = op(id, body, at, pos);
          if (handled is! bool) return _batchAfterOp(handled, type, channel, id, body, at, pos, end);
          if (!handled) _holdWidgetOp(type, channel, id, body, at, pos);
        } catch (e, st) {
          _logHandlerError(wireChannelNames[channel] ?? '$channel', e, st);
        }
        continue;
      }
      if (!_headerAt(body, pos)) return null;
      final named = _namedAt(_opType, _opId, _opChannel);
      var at = _opStart;
      var length = 0;
      var scale = 1;
      while (true) {
        final x = body[at++];
        length += (x & 0x7F) * scale;
        if (x < 0x80) break;
        scale *= 128;
      }
      final from = at;
      pos = at + length;
      // An entry that fails is that entry's failure alone: the rest of the run applies, as it would
      // have arriving in frames of its own.
      try {
        final applied = _applyNamed(named, body, from, pos);
        if (applied is Future<bool>) return _batchAfter(named, applied, body, pos, end);
      } catch (e, st) {
        _logHandlerError(named.name, e, st);
      }
    }
    return null;
  }

  /// The rest of a batch, up to [batchEnd], from the end of the widget op's entry at [at] to [end].
  Future<void> _batchAfterOp(Future<bool> handled, int type, int channel, int id, Uint8List body, int at,
      int end, int batchEnd) async {
    try {
      if (!await handled) _holdWidgetOp(type, channel, id, body, at, end);
    } catch (e, st) {
      _logHandlerError(wireChannelNames[channel] ?? '$channel', e, st);
    }
    await _applyBinaryBatch(body, end, batchEnd);
  }

  /// The rest of a batch from [pos] to [end], once the entry on [n]'s channel before it has applied.
  Future<void> _batchAfter(_Named n, Future<bool> applied, Uint8List body, int pos, int end) async {
    try {
      await applied;
    } catch (e, st) {
      _logHandlerError(n.name, e, st);
    }
    await _applyBinaryBatch(body, pos, end);
  }

  /// Delivers an entry of a GC's binary frame that travels on a channel of its own: its header
  /// starts at [headerAt] in [bytes], and its payload lies from [start] to [end]. A channel answered
  /// per kind of widget is read in place, as [receiveBinary] reads one; any other is named and
  /// delivered by [deliverEntry].
  FutureOr<void> deliverEscape(Uint8List bytes, int headerAt, int start, int end) {
    final op = _widgetOpOf(bytes, headerAt);
    if (op == null) {
      if (!_headerAt(bytes, headerAt)) return null;
      return _deliverEntry(_namedAt(_opType, _opId, _opChannel),
          start == end ? null : TaggedValues.decode(Uint8List.sublistView(bytes, start, end)));
    }
    final type = _opType, channel = _opChannel, id = _opId;
    final handled = op(id, bytes, start, end);
    if (handled is bool) {
      if (!handled) _holdWidgetOp(type, channel, id, bytes, start, end);
      return null;
    }
    return handled.then((done) {
      if (!done) _holdWidgetOp(type, channel, id, bytes, start, end);
    });
  }

  /// Delivers one entry of a GC's binary frame that travels on a channel of its own, as a batch
  /// delivers its entries.
  FutureOr<void> deliverEntry(int type, int id, int channel, Object? payload) =>
      _deliverEntry(_namedAt(type, id, channel), payload);

  FutureOr<void> _deliverEntry(_Named n, Object? payload) {
    final handler = n.handler;
    if (handler == null) {
      if (n.type == _gcType) _gcWaiters.remove(n.id)?.call();
      final held = n.held ??= [];
      final heldAt = n.heldAt ??= [];
      if (held.length >= maxPendingPerChannel) {
        held.clear();
        heldAt.clear();
        return null;
      }
      held.add(payload);
      heldAt.add(_applyingSeq);
      return null;
    }
    if (handler.args?.once ?? false) n.handler = null;
    return handler.onSuccess(payload);
  }

  // Frames are applied one at a time, in arrival order, and a frame that awaits holds the queue
  // until it is done. The transport already delivers in order; what did not was the applying — an
  // async handler returns the moment it awaits, letting the next frame start against half-applied
  // state.
  final _ApplyQueue _applyQueue = _ApplyQueue();
  bool _applying = false;

  /// How many frames have arrived; each frame's number is its place in the apply order.
  int _arrived = 0;

  /// The number of the frame being applied, which a frame held for a later handler keeps: replayed,
  /// it applies at the place it arrived in, so one drawer's frames on several channels apply in the
  /// order they were sent whichever channel registers first.
  int _applyingSeq = 0;

  /// Runs a handler off the current call stack with its errors isolated, after every frame already
  /// queued has finished applying.
  ///
  /// [scheduleMicrotask], never `Future(...)`/`Timer`: those compile to `setTimeout(_, 0)` on
  /// dart2js, which the browser clamps to ~4.7 ms per received message.
  void _deliver(_Named n, OnSuccessCallback<dynamic> onSuccess, dynamic payload, int seq) {
    _enqueueApply(n, () => onSuccess(payload), seq);
  }

  /// Channels whose queue wait is measured every time rather than sampled, by number.
  static final Set<int> queueTracedActions = {};

  /// Queues [apply] at frame number [seq]'s place; without one, after everything that has arrived.
  void _enqueueApply(_Named n, FutureOr<void> Function() apply, [int? seq]) {
    final queuedAt = perfNow();
    _applyQueue.addClosure(() {
      if (queueTracedActions.contains(n.channel)) {
        perfSpan('queueWait:${n.name}', queuedAt, perfNow());
      } else {
        perfSpanSampled('Comm.queueWait', queuedAt, perfNow());
      }
      try {
        final applied = apply();
        if (applied is Future) {
          return applied.catchError((Object e, StackTrace st) {
            _logHandlerError(n.name, e, st);
          });
        }
      } catch (e, st) {
        _logHandlerError(n.name, e, st);
      }
      return null;
    }, seq ?? _arrived);
    _scheduleDrain();
  }

  void _scheduleDrain() {
    if (!_applying) {
      _applying = true;
      _applyingCount++;
      scheduleMicrotask(_drain);
    }
  }

  /// How many connections have frames received and not yet applied. Static because what waits on
  /// it, the widget registry, is one for the whole client.
  static int _applyingCount = 0;
  static final List<void Function()> _whenApplied = [];

  /// As a new client starts: no frame is being applied, nothing waits for one, and no push is owed. A
  /// test that ends with a drain still scheduled in its fake-async zone would otherwise leave every
  /// later test in the isolate waiting for that drain.
  @visibleForTesting
  static void resetForTesting() {
    _applyingCount = 0;
    _whenApplied.clear();
    _owed.clear();
    _paying = false;
  }

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

  /// [_drainNow], torn off once: scheduling a drain makes no closure.
  late final void Function() _drain = _drainNow;

  /// Applies what is queued, in order. Synchronous for as long as the handlers are: only a handler
  /// that returns a future hands the rest of the queue to [_drainAfter] or [_drainAfterOp], which
  /// wait for it and carry on. Awaiting every frame would spend a microtask turn per frame, so a run
  /// of synchronous pushes would take a run of turns to settle.
  void _drainNow() {
    final queue = _applyQueue;
    var handedOff = false;
    try {
      while (queue.length > 0) {
        queue.take();
        _applyingSeq = queue.seq;
        final op = queue.op;
        if (op == null) {
          final applied = queue.closure!();
          if (applied != null) {
            handedOff = true;
            _drainAfter(applied);
            return;
          }
          continue;
        }
        final type = queue.type, channel = queue.channel, id = queue.id;
        final frame = queue.frame!, start = queue.start, end = queue.end;
        try {
          final handled = op(id, frame, start, end);
          if (handled is bool) {
            if (!handled) _holdWidgetOp(type, channel, id, frame, start, end);
          } else {
            handedOff = true;
            _drainAfterOp(handled, type, channel, id, frame, start, end);
            return;
          }
        } catch (e, st) {
          _logHandlerError(wireChannelNames[channel] ?? '$channel', e, st);
        }
      }
    } finally {
      if (!handedOff) _drainEnded();
    }
  }

  /// Resumes the queue once [applied] completes; one that fails stops the drain, as it always did.
  Future<void> _drainAfter(Future<void> applied) async {
    try {
      await applied;
    } catch (_) {
      _drainEnded();
      rethrow;
    }
    _drainNow();
  }

  /// The queue ran dry, or a drain stopped on a failure: what waits for every frame to be applied runs.
  void _drainEnded() {
    _applying = false;
    // The last frame applied is not kept past the drain that applied it.
    _applyQueue.frame = null;
    if (--_applyingCount == 0) _runWhenApplied();
  }

  /// Resumes the queue once the widget op [handled] answers, holding the frame when it declined.
  Future<void> _drainAfterOp(
      Future<bool> handled, int type, int channel, int id, Uint8List frame, int start, int end) async {
    try {
      if (!await handled) _holdWidgetOp(type, channel, id, frame, start, end);
    } catch (e, st) {
      _logHandlerError(wireChannelNames[channel] ?? '$channel', e, st);
    }
    _drainNow();
  }

  /// Handlers by channel number, each a chain of one handler per kind of widget that answers the
  /// channel: see [onWidgetOp].
  List<_WidgetOp?> _widgetOps = List.filled(0, null);

  /// Frames for widgets that did not listen yet, by widget id: see [replayWidgetOps].
  final Map<int, List<_HeldOp>> _heldOps = {};

  /// Handles [channel] for every widget of kind [type] (both as the wire numbers them), as
  /// [WidgetOpHandler] describes: a frame on `<type>/<id>/<channel>` goes to [handler] with the id,
  /// without a channel name being built or a handler being registered per widget. One handler per
  /// kind and channel; registering again replaces it.
  void onWidgetOp(int type, int channel, WidgetOpHandler handler) {
    if (channel >= _widgetOps.length) {
      _widgetOps = List<_WidgetOp?>.filled(channel + 64, null)
        ..setRange(0, _widgetOps.length, _widgetOps);
    }
    for (var op = _widgetOps[channel]; op != null; op = op.next) {
      if (op.type == type) {
        op.handler = handler;
        return;
      }
    }
    _widgetOps[channel] = _WidgetOp(type, handler, _widgetOps[channel]);
  }

  WidgetOpHandler? _handlerOf(int type, int channel) {
    if (channel >= _widgetOps.length) return null;
    for (var op = _widgetOps[channel]; op != null; op = op.next) {
      if (op.type == type) return op.handler;
    }
    return null;
  }

  bool _widgetOpsRegistered = false;

  /// Runs [register], which registers every kind's [onWidgetOp] handlers, once for this comm: a
  /// kind's handlers have to be in place before the first frame for one of its widgets, which can
  /// arrive before any of them exists.
  void ensureWidgetOps(void Function() register) {
    if (_widgetOpsRegistered) return;
    _widgetOpsRegistered = true;
    register();
  }

  /// Applies again, in arrival order, the frames held for widget [id] because nothing answered for
  /// it when they arrived. A widget calls this once it can answer.
  /// Lets go of the frames held for widget [id], which will never answer for them: it is gone.
  void forgetWidgetOps(int id) => _heldOps.remove(id);

  /// How many widgets frames are held for at most; see [_holdWidgetOp].
  static const int maxHeldWidgets = 1024;

  void replayWidgetOps(int id) {
    final held = _heldOps.remove(id);
    if (held == null) return;
    for (final h in held) {
      final op = _handlerOf(h.type, h.channel);
      if (op != null) _applyQueue.addOp(op, h.type, h.channel, id, h.frame, h.start, h.end, h.seq);
    }
    _scheduleDrain();
  }

  void _holdWidgetOp(int type, int channel, int id, Uint8List frame, int start, int end) {
    // A widget that never mounts must not grow this for the life of the page: so many runs are kept,
    // the oldest let go first, and each run so many frames.
    if (!_heldOps.containsKey(id) && _heldOps.length >= maxHeldWidgets) _heldOps.remove(_heldOps.keys.first);
    final run = _heldOps.putIfAbsent(id, () => []);
    if (run.length >= maxPendingPerChannel) run.removeAt(0);
    // Its own bytes only: a frame read out of a batch would otherwise keep the whole batch.
    run.add(_HeldOp(type, channel, Uint8List.fromList(Uint8List.sublistView(frame, start, end)), 0, end - start,
        _applyingSeq));
    // A GC is mounted by the first thing that arrives for it: see [onFirstGcMessage].
    if (type == _gcType) _gcWaiters.remove(id)?.call();
  }

  static const int _gcType = WireType.gc;

  /// The handler for a frame addressed to a widget whose kind answers its channel, read from the
  /// header without building anything: sets [_opType], [_opChannel], [_opId] and [_opStart] and
  /// returns it, or returns null.
  WidgetOpHandler? _widgetOpOf(Uint8List data, int at) {
    final ops = _widgetOps;
    // The header (see ChannelHeader): the channel's number, odd when a widget's type and id follow.
    var v = 0, scale = 1, x = 0;
    do {
      if (at >= data.length) return null;
      x = data[at++];
      v += (x & 0x7F) * scale;
      scale *= 128;
    } while (x >= 0x80);
    if (v & 1 == 0) {
      // A channel addressing no widget; 0 is a name spelled out.
      final channel = v >> 1;
      if (channel == 0 || channel >= _channelOps.length) return null;
      final op = _channelOps[channel];
      if (op == null) return null;
      _opType = _noType;
      _opChannel = channel;
      _opId = 0;
      _opStart = at;
      return op;
    }
    final channel = v >> 1;
    if (channel >= ops.length) return null;
    var op = ops[channel];
    if (op == null) return null;
    v = 0;
    scale = 1;
    do {
      if (at >= data.length) return null;
      x = data[at++];
      v += (x & 0x7F) * scale;
      scale *= 128;
    } while (x >= 0x80);
    final type = v;
    while (op != null && op.type != type) {
      op = op.next;
    }
    if (op == null) return null;
    v = 0;
    scale = 1;
    do {
      if (at >= data.length) return null;
      x = data[at++];
      v += (x & 0x7F) * scale;
      scale *= 128;
    } while (x >= 0x80);
    _opType = type;
    _opChannel = channel;
    _opId = v.isEven ? v ~/ 2 : -(v ~/ 2) - 1;
    _opStart = at;
    return op.handler;
  }

  int _opType = 0, _opChannel = 0, _opId = 0, _opStart = 0;

  /// Sees every event the client sends to Java, so a widget test can check what controls built
  /// from state report.
  @visibleForTesting
  static void Function(String actionId, Payload? payload)? sendObserverForTesting;

  /// Tells [sendObserverForTesting] about an event about to be sent in the wire's encoding.
  static void observeSend(String actionId, Payload? payload) =>
      sendObserverForTesting?.call(actionId, payload);

  /// Told the channel of every event [sendTo] has sent (see `TraversalOrder`).
  static void Function(int channel)? afterSend;

  /// Sends [payload] on the channel numbered [channel] (see [WireChannel]) of the widget of type
  /// [type] numbered [id], the header written from the numbers: no channel name is built.
  void sendTo(int type, int id, int channel, [Payload? payload]) {
    final observer = sendObserverForTesting;
    if (observer != null) {
      observer('${WidgetKey.typeName(type)}/$id/${wireChannelNames[channel] ?? channel}', payload);
    }
    // Owed pushes write with the same writer, so they go first.
    payOwed();
    _sendWritten(WidgetWireWriter.writeTo(type, id, channel, payload));
    afterSend?.call(channel);
  }

  /// Sends the frame [w] holds: lent to the transport when the socket is open, so nothing is
  /// allocated for it; copied when it has to wait.
  void _sendWritten(WidgetWireWriter w) {
    if (_open && !recordSentFrames) {
      _send(w.buffer, w.size);
    } else {
      _enqueue(w.finish());
    }
  }

  /// Sends what [w], begun with [WidgetWireWriter.startTo] or [WidgetWireWriter.startOn], holds:
  /// lent to the transport where it can be, so the frame is never copied out of the writer.
  void sendWritten(WidgetWireWriter w) {
    if (sendObserverForTesting != null || _owed.isNotEmpty) {
      // Owed pushes write with the same writer, so this frame leaves it before they run.
      sendFrame(w.finish());
      return;
    }
    _sendWritten(w);
  }

  /// Sends [frame], one [WidgetWireWriter.startTo] or [WidgetWireWriter.startOn] finished.
  void sendFrame(Uint8List frame) {
    final observer = sendObserverForTesting;
    if (observer != null) {
      final (name, start) = ChannelHeader.read(frame)!;
      observer(name, Uint8List.sublistView(frame, start));
    }
    _enqueue(frame);
  }

  /// Sends [payload] on the global channel numbered [channel] (see [WireChannel]).
  void sendOn(int channel, [Payload? payload]) {
    final observer = sendObserverForTesting;
    if (observer != null) observer(wireChannelNames[channel] ?? '$channel', payload);
    payOwed();
    _sendWritten(WidgetWireWriter.writeOn(channel, payload));
  }

  /// Handles the channel numbered [channel] of the widget of type [type] numbered [id], or the global
  /// channel numbered [channel] when [type] is 0, with each payload decoded.
  Object on(int type, int id, int channel, OnSuccessCallback<dynamic> onSuccess,
      [OnErrorCallback? onError, CallbackArgs? args]) {
    final n = _namedAt(type, id, channel);
    final token = Object();
    n.handler = UserEventCallback(
      onSuccess: onSuccess,
      onError: onError,
      args: args,
      token: token,
    );
    // Replayed in arrival order. Whether a buffered payload is older or newer than what the widget
    // holds is not something this layer can tell - and does not have to: every frame is dated, and
    // the delivery gate refuses one that describes the widget as it used to be.
    // The same frames were held as raw bytes too, for an onBytes that will not come now.
    n.heldRaw = null;
    n.heldRawAt = null;
    final held = n.held, heldAt = n.heldAt;
    n.held = null;
    n.heldAt = null;
    for (var k = 0; k < (held?.length ?? 0); k++) {
      _deliver(n, onSuccess, held![k], heldAt![k]);
    }
    return token;
  }

  /// Channel Java listens on for "re-serialize widget `<id>`" requests (see
  /// FlutterBridge.handleWidgetRefresh).
  static const widgetRefreshChannel = 'swt.evolve.widget.refresh';


  /// Answers the channel these numbers name (see [on]) at arrival, ahead of the apply queue, when it can.
  ///
  /// The handler returns true once it has answered and false to let the frame queue normally. Only
  /// for a request whose answer, when it is available at all, cannot be changed by anything still
  /// queued — a read of state already applied. A request that depends on a queued frame must
  /// return false, or it races the very frame it needs.
  ///
  /// Exists because a blocking caller on the other side is timed: making it wait out a queue of
  /// unrelated frame work is what turns a read into a timeout.
  void onArrival(int type, int id, int channel, bool Function(Uint8List) handler) {
    _namedAt(type, id, channel).arrival = handler;
  }

  /// Handles the global channel numbered [number]'s frames, read in place: [handler] gets the frame
  /// and where its payload lies in it, with no view, name or closure made for it.
  void onBytesAt(int number, BytesAtHandler handler) {
    if (number >= _channelOps.length) {
      _channelOps = List.of(_channelOps)..length = number + 1;
    }
    final timed = number == WireChannel.swtEvolveWire;
    final op = _channelOps[number] = (int _, Uint8List data, int start, int end) {
      final timer = timed ? receiveTimer : null;
      if (timer != null) {
        // A binary frame of changes decodes as it applies, so its whole cost counts as applying.
        final startedAt = timer.now();
        final applied = handler(data, start, end);
        timer.received(wireChannelNames[number]!, end - start, startedAt, startedAt, timer.now());
        return applied is Future ? applied.then(_handled) : true;
      }
      final applied = handler(data, start, end);
      return applied is Future ? applied.then(_handled) : true;
    };
    final held = _namedIfAny(_noType, 0, number);
    if (held != null) {
      _named[number]!.remove(0);
      final raw = held.heldRaw ?? const <Uint8List>[];
      for (var k = 0; k < raw.length; k++) {
        _applyQueue.addOp(op, _noType, number, 0, raw[k], 0, raw[k].length, held.heldRawAt![k]);
      }
    }
    _scheduleDrain();
  }

  static bool _handled(void _) => true;

  /// The type a frame on a channel answered by number is queued under: it addresses no widget.
  static const int _noType = -1;

  /// Handlers by channel number for channels that address no widget: see [onBytesAt].
  List<WidgetOpHandler?> _channelOps = List.filled(0, null);

  /// Handles the channel these numbers name (see [on]) with each payload's bytes as they arrived.
  void onBytes(int type, int id, int channel, FutureOr<void> Function(Uint8List) callback) {
    final n = _namedAt(type, id, channel);
    n.raw = callback;
    // A body that decoded as tagged values was held for on() too; this channel is raw now.
    n.held = null;
    n.heldAt = null;
    // Frames that arrived before anyone was listening replay at the places they arrived in, so they
    // still land ahead of whatever has arrived since.
    final held = n.heldRaw, heldAt = n.heldRawAt;
    n.heldRaw = null;
    n.heldRawAt = null;
    for (var k = 0; k < (held?.length ?? 0); k++) {
      final pending = held![k];
      _enqueueApply(n, () => callback(pending), heldAt![k]);
    }
  }

  /// Stops handling the channel these numbers name (see [on]); only [token]'s handler, when given.
  void remove(int type, int id, int channel, [Object? token]) {
    final n = _namedIfAny(type, id, channel);
    if (n == null) return;
    if (token != null && n.handler?.token != token) return;
    n
      ..handler = null
      ..raw = null
      ..held = null
      ..heldAt = null
      ..heldRaw = null
      ..heldRawAt = null;
    if (n.arrival == null) _named[channel]!.remove(type <= 0 ? 0 : WidgetKey.of(type, id));
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

/// What listens on one channel - of one widget, or a global one - found by its numbers, and what
/// arrived on it before anything did.
class _Named {
  _Named(EquoCommBase comm, this.type, this.id, this.channel) {
    apply = (_, data, start, end) => comm._applyNamed(this, data, start, end);
  }

  final int type;
  final int id;
  final int channel;
  late final WidgetOpHandler apply;
  UserEventCallback? handler;
  FutureOr<void> Function(Uint8List)? raw;
  bool Function(Uint8List)? arrival;

  /// Payloads decoded for [handler], and the same frames' bodies for [raw], held until one registers,
  /// with the numbers they arrived with.
  List<dynamic>? held;
  List<int>? heldAt;
  List<Uint8List>? heldRaw;
  List<int>? heldRawAt;

  /// The channel's name, for logs: built only when one asks.
  late final String name = () {
    final rest = wireChannelNames[channel] ?? '$channel';
    if (type <= 0) return rest;
    final owner = '${WidgetKey.typeName(type)}/$id';
    return rest.isEmpty ? owner : '$owner/$rest';
  }();

  FutureOr<void> applyFrame(Uint8List data, int start, int end) => apply(id, data, start, end);
}

/// A frame held for a widget that did not answer yet: see [EquoCommBase.replayWidgetOps].
class _HeldOp {
  final int type;
  final int channel;
  final Uint8List frame;
  final int start;
  final int end;

  /// The number the frame arrived with: see [EquoCommBase._applyingSeq].
  final int seq;
  _HeldOp(this.type, this.channel, this.frame, this.start, this.end, this.seq);
}

/// One kind of widget's handler for a channel, chained to the next kind's.
class _WidgetOp {
  final int type;
  WidgetOpHandler handler;
  final _WidgetOp? next;
  _WidgetOp(this.type, this.handler, this.next);
}

/// The frames waiting to apply, in arrival order: a ring of plain fields, so queueing a frame
/// allocates nothing. An entry is either a closure, for a frame resolved by channel name, or a
/// widget op with what it is applied to.
class _ApplyQueue {
  var _closures = List<Future<void>? Function()?>.filled(64, null);
  var _ops = List<WidgetOpHandler?>.filled(64, null);
  var _frames = List<Uint8List?>.filled(64, null);
  var _ids = List<int>.filled(64, 0);
  var _starts = List<int>.filled(64, 0);
  var _ends = List<int>.filled(64, 0);
  var _channels = List<int>.filled(64, 0);
  var _types = List<int>.filled(64, 0);
  var _seqs = List<int>.filled(64, 0);
  int _head = 0;
  int length = 0;

  /// What [take] took.
  Future<void>? Function()? closure;
  WidgetOpHandler? op;
  Uint8List? frame;
  int id = 0, start = 0, end = 0, channel = 0, type = 0, seq = 0;

  void addClosure(Future<void>? Function() apply, int seq) {
    final i = _slot(seq);
    _closures[i] = apply;
    _ops[i] = null;
    // The slot can hold what shifting entries copied into it: no frame is kept for a closure.
    _frames[i] = null;
  }

  void addOp(WidgetOpHandler op, int type, int channel, int id, Uint8List frame, int start, int end, int seq) {
    final i = _slot(seq);
    _types[i] = type;
    _closures[i] = null;
    _ops[i] = op;
    _frames[i] = frame;
    _ids[i] = id;
    _starts[i] = start;
    _ends[i] = end;
    _channels[i] = channel;
  }

  /// The slot for an entry numbered [seq]: after every entry numbered up to it. A frame arriving
  /// takes the last slot; only a replayed one, numbered when it first arrived, moves entries along.
  int _slot(int seq) {
    if (length == _closures.length) _grow();
    final mask = _closures.length - 1;
    var at = length;
    while (at > 0 && _seqs[(_head + at - 1) & mask] > seq) {
      at--;
    }
    for (var k = length; k > at; k--) {
      final to = (_head + k) & mask, from = (_head + k - 1) & mask;
      _closures[to] = _closures[from];
      _ops[to] = _ops[from];
      _frames[to] = _frames[from];
      _ids[to] = _ids[from];
      _starts[to] = _starts[from];
      _ends[to] = _ends[from];
      _channels[to] = _channels[from];
      _types[to] = _types[from];
      _seqs[to] = _seqs[from];
    }
    final i = (_head + at) & mask;
    _seqs[i] = seq;
    length++;
    return i;
  }

  /// Takes the oldest entry into [closure] or [op] and the fields it is applied to.
  void take() {
    final i = _head;
    closure = _closures[i];
    op = _ops[i];
    frame = _frames[i];
    id = _ids[i];
    start = _starts[i];
    end = _ends[i];
    channel = _channels[i];
    type = _types[i];
    seq = _seqs[i];
    _closures[i] = null;
    _ops[i] = null;
    _frames[i] = null;
    _head = (i + 1) & (_closures.length - 1);
    length--;
  }

  void _grow() {
    final capacity = _closures.length * 2;
    List<T> unrolled<T>(List<T> from, T empty) {
      final to = List<T>.filled(capacity, empty);
      for (var k = 0; k < length; k++) {
        to[k] = from[(_head + k) & (from.length - 1)];
      }
      return to;
    }

    _closures = unrolled(_closures, null);
    _ops = unrolled(_ops, null);
    _frames = unrolled(_frames, null);
    _ids = unrolled(_ids, 0);
    _starts = unrolled(_starts, 0);
    _ends = unrolled(_ends, 0);
    _channels = unrolled(_channels, 0);
    _types = unrolled(_types, 0);
    _seqs = unrolled(_seqs, 0);
    _head = 0;
  }
}
