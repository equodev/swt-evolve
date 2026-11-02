// What a frame on a widget's own channel costs on the Dart side, from its arrival to the production
// handler that applies it: time, and bytes allocated.
//
// A measurement, printed rather than asserted, for comparing the comm before and after a change. It
// only builds frames the way Java does and hands them to the comm, so the same file runs against
// any version of the client. Each scenario addresses 200 widgets in turn.
//
// Bytes allocated need the VM service: run with
//   flutter test --enable-vmservice --tags bench test/comm_widget_op_bench_test.dart
// They are read from new space (see NewSpace), less what the same run does with no frames. Without
// the service only time is printed.

@Tags(['bench'])
library;

import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/channel_header.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/tagged_values.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey, WireType;
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';
import 'package:swtflutter/src/impl/utils/veto_gate.dart';

import 'support/gc_frames.dart';
import 'support/heap_bytes.dart';
import 'support/channels.dart' as channels;

const int _widgets = 200;
const int _frames = 200000;
const int _allocFrames = 4000;
const int _rounds = 5;

EquoCommBase get _comm => EquoCommService.commForTesting;

List<int> get _ids => [for (var i = 0; i < _widgets; i++) 700001 + i * 7];

Uint8List _frame(String channel, Uint8List body) => channels.frame(channel, body);

/// [entries] in one batch, each as Java writes it: its header, its payload's length, its payload.
Uint8List _batch(List<Uint8List> entries) {
  final out = BytesBuilder(copy: false);
  for (final frame in entries) {
    final header = ChannelHeader.readAt(frame, 0)!.$2;
    out.add(Uint8List.sublistView(frame, 0, header));
    var length = frame.length - header;
    while (length >= 0x80) {
      out.addByte((length & 0x7F) | 0x80);
      length >>= 7;
    }
    out.addByte(length);
    out.add(Uint8List.sublistView(frame, header));
  }
  return _frame('swt.evolve.batch', out.toBytes());
}

Future<void> _report(String name, List<Uint8List> frames, {int perFrame = 1, NewSpace? heap}) async {
  // Warm up the JIT on the path being measured.
  await receiveFrames(_comm, frames, 20000 ~/ perFrame);
  final times = <double>[];
  final messages = _frames ~/ perFrame;
  for (var round = 0; round < _rounds; round++) {
    final sw = Stopwatch()..start();
    await receiveFrames(_comm, frames, messages);
    times.add(sw.elapsedMicroseconds * 1000 / (messages * perFrame));
  }
  double? bytes;
  if (heap != null) {
    final samples = <double>[];
    for (var round = 0; round < _rounds; round++) {
      final messages = _allocFrames ~/ perFrame;
      final quiet = await heap.bytesOf(() => idle(messages));
      final run = await heap.bytesOf(() => receiveFrames(_comm, frames, messages));
      if (quiet != null && run != null) samples.add((run - quiet) / (messages * perFrame));
    }
    if (samples.isNotEmpty) bytes = median(samples);
  }
  // ignore: avoid_print
  print(measureLine('$name, per frame', median(times), bytes));
}

/// A paint of [ops] ops, as an editor's ruler or a table's cells draw: a colour change, then filled
/// rectangles and lines, the second of each kind and on by their place in the frame.
Uint8List _paint(int id, int ops) {
  final frame = GcFrame().change(VGC.empty()..background = VColor.fromArgb(0xFF000000 | id), {WireKey.k_GC_background});
  for (var k = 0; k < ops; k++) {
    if (k.isEven) {
      frame.numbered('fillRectangleintintintint', (f) => f..i(k)..i(id % 97)..i(20)..i(14));
    } else {
      frame.numbered('drawLineintintintint', (f) => f..i(0)..i(k)..i(40)..i(k));
    }
  }
  return _frame('GC/$id/b', frame.bytes);
}

/// A control's whole paint: [_paint]'s ops, then the gcDispose that commits them, replacing what the
/// control showed.
Uint8List _wholePaint(int id, int ops) {
  final frame = GcFrame().change(VGC.empty()..background = VColor.fromArgb(0xFF000000 | id), {WireKey.k_GC_background});
  for (var k = 0; k < ops; k++) {
    if (k.isEven) {
      frame.numbered('fillRectangleintintintint', (f) => f..i(k)..i(id % 97)..i(20)..i(14));
    } else {
      frame.numbered('drawLineintintintint', (f) => f..i(0)..i(k)..i(40)..i(k));
    }
  }
  frame.escape('GC/$id/gcDispose', {'fullRepaint': true});
  return _frame('GC/$id/b', frame.bytes);
}

/// [_report] for whole paints into a control's drawer, as a canvas repaints: each commits and
/// replaces the last.
Future<void> _reportWholePaints(String name, List<int> ids, int ops, NewSpace? heap) async {
  const frames = 20000, allocFrames = 1000;
  final paints = [for (final id in ids) _wholePaint(id, ops)];
  final drawers = [
    for (final id in ids)
      GCDrawer.embedded(VGC()
        ..swt = 'GC'
        ..id = id, onShapesUpdated: (_) {}, onGCDispose: (_) {}),
  ];
  await receiveFrames(_comm, paints, frames);
  final times = <double>[];
  for (var round = 0; round < _rounds; round++) {
    final sw = Stopwatch()..start();
    await receiveFrames(_comm, paints, frames);
    times.add(sw.elapsedMicroseconds * 1000 / frames);
  }
  double? bytes;
  if (heap != null) {
    final samples = <double>[];
    for (var round = 0; round < _rounds; round++) {
      final quiet = await heap.bytesOf(() => idle(allocFrames));
      final ran = await heap.bytesOf(() => receiveFrames(_comm, paints, allocFrames));
      if (quiet != null && ran != null) samples.add((ran - quiet) / allocFrames);
    }
    if (samples.isNotEmpty) bytes = median(samples);
  }
  for (final drawer in drawers) {
    drawer.dispose();
  }
  // ignore: avoid_print
  print(measureLine('$name, per frame', median(times), bytes));
}

/// [_report] for paints, whose shapes the drawer keeps until the paint is disposed: fresh drawers
/// per round, and fewer frames.
Future<void> _reportPaints(String name, List<int> ids, int ops, NewSpace? heap) async {
  const frames = 20000, allocFrames = 1000;
  final paints = [for (final id in ids) _paint(id, ops)];
  Future<double> run(int count) async {
    final drawers = [
      for (final id in ids)
        GCDrawer.standalone(
          VGC()
            ..swt = 'GC'
            ..id = id,
        ),
    ];
    final sw = Stopwatch()..start();
    await receiveFrames(_comm, paints, count);
    final ns = sw.elapsedMicroseconds * 1000 / count;
    for (final drawer in drawers) {
      drawer.dispose();
    }
    return ns;
  }

  await run(frames);
  final times = [for (var round = 0; round < _rounds; round++) await run(frames)];
  double? bytes;
  if (heap != null) {
    final samples = <double>[];
    for (var round = 0; round < _rounds; round++) {
      final drawers = [
        for (final id in ids)
          GCDrawer.standalone(
            VGC()
              ..swt = 'GC'
              ..id = id,
          ),
      ];
      await receiveFrames(_comm, paints, ids.length);
      final quiet = await heap.bytesOf(() => idle(allocFrames));
      final ran = await heap.bytesOf(() => receiveFrames(_comm, paints, allocFrames));
      if (quiet != null && ran != null) samples.add((ran - quiet) / allocFrames);
      for (final drawer in drawers) {
        drawer.dispose();
      }
    }
    if (samples.isNotEmpty) bytes = median(samples);
  }
  // ignore: avoid_print
  print(measureLine('$name, per frame', median(times), bytes));
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('MEASURE: a frame on a widget\'s own channel, arrival to handler', () async {
    final heap = await NewSpace.connect();
    final ids = _ids;

    final gates = [
      for (final id in ids) VetoGate.key()..attach(WireType.text, id),
    ];
    final vetoable = [
      for (final id in ids) _frame('Text/$id/key/vetoable', TaggedValues.encode({'value': true})),
    ];
    final verdict = [
      for (final id in ids) _frame('Text/$id/key/verdict', TaggedValues.encode({'doit': true})),
    ];
    await _report('veto: arm (key/vetoable)', vetoable, heap: heap);
    await _report('veto: verdict (key/verdict)', verdict, heap: heap);
    expect(gates.every((g) => g.armed), isTrue);

    final batches = [
      for (var i = 0; i < _widgets; i += 20) _batch(vetoable.sublist(i, i + 20)),
    ];
    await _report('veto: arm, batches of 20', batches, perFrame: 20, heap: heap);
    for (final gate in gates) {
      gate.detach();
    }

    final drawers = [
      for (final id in ids)
        GCDrawer.standalone(
          VGC()
            ..swt = 'GC'
            ..id = id,
        ),
    ];
    final paints = [
      for (final id in ids)
        _frame('GC/$id/b',
            gcState(VGC.empty()..background = VColor.fromArgb(0xFF000000 | id), changed: {WireKey.k_GC_background})),
    ];
    await _report('gc: paint frame, one state change', paints, heap: heap);
    for (final drawer in drawers) {
      drawer.dispose();
    }
    await _reportPaints('gc: paint of 8 ops and a state change', ids, 8, heap);
    await _reportWholePaints('gc: whole paint of 8 ops, committed', ids, 8, heap);

    if (heap == null) {
      // ignore: avoid_print
      print('MEASURE bytes need --enable-vmservice');
    }
    await heap?.dispose();
  });
}
