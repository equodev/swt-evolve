import 'dart:async';
import 'dart:convert';
import 'dart:typed_data';

import 'src/comm/comm.dart';
import 'src/comm/tagged_values.dart';
import 'src/comm/v_registry.dart';
import 'src/comm/widget_wire.dart';
import 'src/gen/event.dart';

/// Bench handler — wired in from main.dart when widgetName == "BenchBridge".
///
/// Cross-language RTT echo handlers in both directions: the receiver runs the real production
/// codec (a widget frame applied on J→D, an event or a base64 image decoded on D→J) before acking,
/// so each timing is a true Java↔Dart round trip.
///
/// D→J shapes are real VEvent variants only (production Dart only sends VEvent).
void measureRequest(String bridge, int id) {
  final stopwatch = Stopwatch()..start();
  final prefix = "$bridge/$id";
  final catalog = _D2jCatalog.build();

  // ===== J→D codec — Dart receives a widget frame, applies it as production does, acks =====
  EquoCommService.onBytes("$prefix/bench/j2d/echo", (frame) {
    _apply(frame);
    EquoCommService.sendBytes("$prefix/bench/j2d/echoResponse", _ackBytes);
  });

  // ===== D→J — Dart writes a real VEvent each iteration, Java reads it field by field =====
  // Response handler is registered ONCE; the per-call completer is held in closure scope. Avoids
  // register/remove races where a fast probeResponse could land between iterations and be lost.
  Completer<int>? d2jCompleter;
  EquoCommService.onBytes("$prefix/bench/d2j/probeResponse", (resp) {
    final c = d2jCompleter;
    if (c != null && !c.isCompleted) c.complete(stopwatch.elapsedMicroseconds);
  });
  EquoCommService.onBytes("$prefix/bench/d2j/run", (runBytes) async {
    final (seq, shape) = _decodeRun(runBytes);
    final payload = catalog.forShape(shape).payload;
    d2jCompleter = Completer<int>();
    final t1 = stopwatch.elapsedMicroseconds;
    EquoCommService.sendPayload("$prefix/bench/d2j/probe", payload);
    final t2 = await d2jCompleter!.future;
    d2jCompleter = null;
    _sendResult("$prefix/bench/d2j/result", seq, t2 - t1);
  });
}

/// Decode the J-side run frame: [int32 BE seq][byte shape_len][shape utf-8].
(int, String) _decodeRun(Uint8List bytes) {
  final view = ByteData.sublistView(bytes);
  final seq = view.getInt32(0, Endian.big);
  final len = view.getUint8(4);
  final shape = utf8.decode(bytes.sublist(5, 5 + len));
  return (seq, shape);
}

/// Applies a widget frame the way the client applies one on arrival, into a registry of its own so
/// the client's widgets are untouched: for a real workbench root this builds the entire tree.
void _apply(Uint8List frame) {
  final registry = VRegistry();
  registry.applyWire(frame);
  registry.clear();
}

final Uint8List _ackBytes = Uint8List.fromList(const [0, 0, 0, 0]);

void _sendResult(String channel, int seq, int rttMicros) {
  final out = Uint8List(4 + 8);
  final v = ByteData.sublistView(out);
  v.setInt32(0, seq, Endian.big);
  _setInt64BE(v, 4, rttMicros);
  EquoCommService.sendBytes(channel, out);
}

/// Write a non-negative 64-bit value big-endian as two 32-bit halves. ByteData.setInt64 throws
/// "Int64 accessor not supported by dart2js" on web; splitting into hi/lo uint32 works everywhere
/// and Java reads it back unchanged via ByteBuffer.getLong. Values here (micros, counts) are well
/// within dart2js's 53-bit safe integer range.
void _setInt64BE(ByteData v, int offset, int value) {
  v.setUint32(offset, value ~/ 0x100000000, Endian.big);
  v.setUint32(offset + 4, value % 0x100000000, Endian.big);
}

/// D→J catalog of real production payloads. Event shapes are real VEvent variants (what
/// production Dart sends for widget events). Image shapes (IMG_*) are base64-encoded PNG
/// strings — exactly the form gcdrawer's `imageResult` sends back to Java, which Java
/// receives as a String and base64-decodes (see GCHelper.updateImageFromPng). No padding —
/// each shape's wire size is whatever its natural encoding produces.
class _D2jCatalog {
  final Map<String, _D2jEntry> _entries;
  _D2jCatalog._(this._entries);

  _D2jEntry forShape(String shape) {
    final e = _entries[shape];
    if (e == null) throw StateError('No D→J bench payload for shape "$shape"');
    return e;
  }

  static _D2jCatalog build() {
    final entries = <String, _D2jEntry>{};

    // Event shapes: sent via sendPayload as the event's fields; Java reads them into an Event.
    void addEvent(String name, VEvent ev) {
      entries[name] = _D2jEntry(ev);
      print('[bench.dart] D→J $name = ${WidgetWireWriter.encode(ev).length} bytes (event)');
    }

    // Image shapes: the base64 String (current production wire form) is sent via sendPayload;
    // Java receives it as a String and base64-decodes it.
    void addImage(String name, int rawSize) {
      final raw = _pseudoImageBytes(rawSize);
      final b64 = base64Encode(raw);
      entries[name] = _D2jEntry(b64);
      print('[bench.dart] D→J $name = base64 ${TaggedValues.encode(b64).length}B (tagged)');
    }

    addEvent('MOUSE_MOVE', _mouseMove());
    addEvent('KEY_DOWN', _keyDown());
    addEvent('SELECTION', _selection());
    addImage('IMG_SMALL', 1024);
    addImage('IMG_LARGE', 24576);
    return _D2jCatalog._(entries);
  }

  static VEvent _mouseMove() {
    final ev = VEvent.empty();
    ev.type = 5; // SWT.MouseMove
    ev.x = 100;
    ev.y = 200;
    ev.stateMask = 524288; // SWT.BUTTON1
    ev.count = 0;
    ev.time = 1234567;
    ev.button = 1;
    return ev;
  }

  static VEvent _keyDown() {
    final ev = VEvent.empty();
    ev.type = 1; // SWT.KeyDown
    ev.character = 65; // 'A'
    ev.keyCode = 65;
    ev.keyLocation = 0;
    ev.stateMask = 0;
    ev.doit = true;
    ev.time = 1234567;
    return ev;
  }

  static VEvent _selection() {
    final ev = VEvent.empty();
    ev.type = 13; // SWT.Selection
    ev.detail = 0;
    ev.stateMask = 0;
    ev.doit = true;
    ev.text = '';
    ev.time = 1234567;
    return ev;
  }
}

/// Deterministic, incompressible-ish byte buffer standing in for encoded image bytes. We only
/// measure the comm-layer cost (base64 encode + transport + decode), not PNG validity, so
/// synthetic bytes are faithful for that purpose.
Uint8List _pseudoImageBytes(int n) {
  final out = Uint8List(n);
  for (int i = 0; i < n; i++) {
    out[i] = (i * 31 + 7) & 0xFF;
  }
  return out;
}

class _D2jEntry {
  /// Object passed to sendPayload on the cross-language path: a VEvent for events, a base64 String
  /// for images (the real production wire forms Java decodes).
  final dynamic payload;
  _D2jEntry(this.payload);
}
