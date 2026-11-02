// What a widget's state costs on the Dart side, from the binary frame Java flushes it in to the
// listener of the widget that shows it: time, and bytes allocated, per record.
//
// A measurement, printed rather than asserted, for comparing the client before and after a change.
// It builds frames as Java's WidgetWire writes them and hands them to the comm, and it reaches the
// registry only through what any version of it offers, so the same file runs against each one.
//
// Every record carries a newer write stamp than the last one for its widget, as Java's do, so a
// frame applies once: they are built before each timed round rather than cycled.
//
// Bytes allocated need the VM service: run with
//   flutter test --enable-vmservice --tags bench test/widget_wire_bench_test.dart

@Tags(['bench'])
library;

import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/channel_header.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/delivery_gate.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/comm/widget_wire.dart';
import 'package:swtflutter/src/gen/item.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
import 'package:swtflutter/src/gen/widgets.dart' show wireTypeNames;

import 'support/heap_bytes.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireChannel, WireKey;
import 'support/channels.dart' as channels;
import 'support/channels.dart' show NamedChannels;

const int _widgets = 200;
const int _records = 50000;
const int _allocRecords = 2000;
const int _rounds = 5;

EquoCommBase get _comm => EquoCommService.commForTesting;

int _typeNumber(String swt) => wireTypeNames.entries.firstWhere((e) => e.value == swt).key;

final List<String> _texts = [for (var i = 0; i < 16; i++) 'text $i'];

/// Writes flushes for one kind of widget, keeping each widget's last stamp as Java does.
class _Flushes {
  _Flushes(String swt, this.ids, this.textKey) : _type = _typeNumber(swt);

  final int _type;
  final List<int> ids;
  final int textKey;
  final Map<int, int> _last = {};
  int _stamp = 1000;
  int _next = 0;

  /// The stamp every widget's state starts at, as delivered before the measurement.
  static const int first = 1;

  /// [count] flushes of [perFlush] records each, over [ids] in turn: changes, or whole
  /// descriptions when [whole].
  List<Uint8List> build(int count, int perFlush, {bool whole = false}) => [
        for (var n = 0; n < count; n++) _flush([for (var k = 0; k < perFlush; k++) ids[_next++ % ids.length]], whole),
      ];

  Uint8List _flush(List<int> flushIds, bool whole) {
    final out = BytesBuilder();
    out.add((ByteData(4)..setUint32(0, flushIds.length, Endian.little)).buffer.asUint8List());
    var previous = 0;
    for (final id in flushIds) {
      final stamp = ++_stamp;
      final base = _last[id] ?? first;
      _last[id] = stamp;
      // The writer's buffer is reused by the next record: copied out.
      final record = Uint8List.fromList(WidgetWireWriter.encodeWith((w) {
        w.varint(_type * 2 + 1);
        w.varint(id * 2);
        w.varint(stamp - previous);
        if (!whole) w.varint(stamp - base);
        w.key(textKey);
        w.string(_texts[(stamp + id) % _texts.length]);
      }));
      previous = stamp;
      out.addByte(whole ? wireWhole : wireChange);
      var length = record.length;
      while (length >= 0x80) {
        out.addByte((length & 0x7F) | 0x80);
        length >>= 7;
      }
      out.addByte(length);
      out.add(record);
    }
    return channels.frame('swt.evolve.wire', out.toBytes());
  }
}

/// Has the comm hand the widget wire to [registry] as the app does: by number and in place.
void _listen(VRegistry registry) {
  _comm.onBytesAt(WireChannel.swtEvolveWire, registry.applyWire);
}

Future<void> _report(String name, _Flushes flushes, int perFlush, NewSpace? heap, {bool whole = false}) async {
  await receiveFrames(_comm, flushes.build(20000 ~/ perFlush, perFlush, whole: whole), 20000 ~/ perFlush);
  final times = <double>[];
  for (var round = 0; round < _rounds; round++) {
    final frames = flushes.build(_records ~/ perFlush, perFlush, whole: whole);
    final sw = Stopwatch()..start();
    await receiveFrames(_comm, frames, frames.length);
    times.add(sw.elapsedMicroseconds * 1000 / _records);
  }
  double? bytes;
  if (heap != null) {
    final samples = <double>[];
    for (var round = 0; round < _rounds; round++) {
      final frames = flushes.build(_allocRecords ~/ perFlush, perFlush, whole: whole);
      final quiet = await heap.bytesOf(() => idle(frames.length));
      final run = await heap.bytesOf(() => receiveFrames(_comm, frames, frames.length));
      if (quiet != null && run != null) samples.add((run - quiet) / _allocRecords);
    }
    if (samples.isNotEmpty) bytes = median(samples);
  }
  // ignore: avoid_print
  print(measureLine('$name, per record', median(times), bytes));
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('MEASURE: a widget record, arrival to the listener of the widget showing it', () async {
    final heap = await NewSpace.connect();
    final registry = VRegistry.instance;
    _listen(registry);

    final labels = [for (var i = 0; i < _widgets; i++) 800001 + i * 7];
    var told = 0;
    for (final id in labels) {
      registry.register(VLabel()
        ..swt = 'Label'
        ..id = id
        ..seq = _Flushes.first
        ..text = 'start');
      deliveryGate.applied('Label/$id', _Flushes.first);
      registry.watch('Label/$id', (_) => told++);
    }
    final labelFlushes = _Flushes('Label', labels, WireKey.k_Label_text);
    await _report('wire: one change per flush', labelFlushes, 1, heap);
    await _report('wire: 20 changes per flush', labelFlushes, 20, heap);
    await _report('wire: one whole description per flush', labelFlushes, 1, heap, whole: true);

    final rows = [for (var i = 0; i < _widgets; i++) 900001 + i * 7];
    final table = VTable()
      ..swt = 'Table'
      ..id = 899999
      ..seq = _Flushes.first
      ..style = SWT.NONE
      ..items = [
        for (final id in rows)
          VTableItem()
            ..swt = 'TableItem'
            ..id = id
            ..seq = _Flushes.first
            ..text = 'row',
      ];
    registry.register(table);
    registry.holds('Table/899999', table.items!);
    for (final id in rows) {
      deliveryGate.applied('TableItem/$id', _Flushes.first);
    }
    var tableTold = 0;
    registry.watch('Table/899999', (_) => tableTold++);
    await _report('wire: 20 row changes per flush, held', _Flushes('TableItem', rows, WireKey.k_Item_text), 20, heap);

    expect(told, greaterThan(0));
    expect(tableTold, greaterThan(0));
    expect(deliveryGate.recoveries, 0, reason: 'every record built on what the client held');
    if (heap == null) {
      // ignore: avoid_print
      print('MEASURE bytes need --enable-vmservice');
    }
    await heap?.dispose();
  });
}
