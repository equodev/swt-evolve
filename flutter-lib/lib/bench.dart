import 'dart:async';
import 'dart:convert';
import 'dart:typed_data';

import 'src/comm/comm.dart';
import 'src/gen/event.dart';
import 'src/gen/widgets.dart';

/// Bench handler — wired in from main.dart when widgetName == "BenchBridge".
///
/// Cross-language RTT echo handlers in both directions: the receiver runs the real production
/// codec (V*.fromJson/toJson on J→D, Event/base64-image decode on D→J) before acking, so each
/// timing is a true Java↔Dart round trip.
///
/// D→J shapes are real VEvent variants only (production Dart only sends VEvent).
void measureRequest(String bridge, int id) {
  final stopwatch = Stopwatch()..start();
  final prefix = "$bridge/$id";
  final catalog = _D2jCatalog.build();

  // ===== J→D codec — Dart receives, runs real V*.fromJson + toJson, acks =====
  EquoCommService.onRaw("$prefix/bench/j2d/json/echo", (payload) {
    _parseAndReencode(payload as Map<String, dynamic>);
    EquoCommService.sendBytes("$prefix/bench/j2d/json/echoResponse", _ackBytes);
  });

  // ===== D→J JSON — Dart re-encodes real VEvent each iteration, Java decodes via DSL-JSON =====
  // Response handler is registered ONCE; the per-call completer is held in closure scope. Avoids
  // register/remove races where a fast probeResponse could land between iterations and be lost
  // to comm_ws's JSON-decode fallback (4 zero ack bytes aren't valid JSON).
  Completer<int>? d2jJsonCompleter;
  EquoCommService.onBytes("$prefix/bench/d2j/json/probeResponse", (resp) {
    final c = d2jJsonCompleter;
    if (c != null && !c.isCompleted) c.complete(stopwatch.elapsedMicroseconds);
  });
  EquoCommService.onBytes("$prefix/bench/d2j/json/run", (runBytes) async {
    final (seq, shape) = _decodeRun(runBytes);
    final payload = catalog.forShape(shape).payload;
    d2jJsonCompleter = Completer<int>();
    final t1 = stopwatch.elapsedMicroseconds;
    EquoCommService.sendPayload("$prefix/bench/d2j/json/probe", payload);
    final t2 = await d2jJsonCompleter!.future;
    d2jJsonCompleter = null;
    _sendResult("$prefix/bench/d2j/json/result", seq, t2 - t1);
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

/// Decode the payload through the production polymorphic codec and re-encode it — exactly the J→D
/// first-paint path. `mapWidgetValue` dispatches on the `swt` field to the right `V*.fromJson`,
/// which recurses into `children`; for a real workbench root this rebuilds the entire tree
/// (Shell → CTabFolders, Trees, Tables, StyledText, …) before re-encoding.
void _parseAndReencode(Map<String, dynamic> json) {
  mapWidgetValue(json).toJson();
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
    final encoder = json.fuse(utf8);
    final entries = <String, _D2jEntry>{};

    // Event shapes: the Map is sent via sendPayload; Java decodes it into org.eclipse.swt.widgets.Event.
    void addEvent(String name, VEvent ev) {
      final payload = ev.toJson();
      final bytes = encoder.encode(payload) as Uint8List;
      entries[name] = _D2jEntry(payload);
      print('[bench.dart] D→J $name = ${bytes.length} bytes (event)');
    }

    // Image shapes: the base64 String (current production wire form) is sent via sendPayload;
    // Java receives it as a String and base64-decodes it.
    void addImage(String name, int rawSize) {
      final raw = _pseudoImageBytes(rawSize);
      final b64 = base64Encode(raw);
      entries[name] = _D2jEntry(b64);
      final jsonBytes = (encoder.encode(b64) as Uint8List).length;
      print('[bench.dart] D→J $name = base64 ${jsonBytes}B (json)');
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
/// measure the comm-layer cost (base64 encode + JSON transport + decode), not PNG validity, so
/// synthetic bytes are faithful for that purpose.
Uint8List _pseudoImageBytes(int n) {
  final out = Uint8List(n);
  for (int i = 0; i < n; i++) {
    out[i] = (i * 31 + 7) & 0xFF;
  }
  return out;
}

class _D2jEntry {
  /// Object passed to sendPayload on the cross-language path: a Map for events, a base64 String
  /// for images (the real production wire forms Java decodes).
  final dynamic payload;
  _D2jEntry(this.payload);
}
