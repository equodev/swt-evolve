// What the client allocates, and how long it takes, to apply one widget record Java sends: the frame
// read, the delivery gate asked, the record decoded into the value held and its listeners told. No
// widget is mounted, so no build is counted: this is the cost of the format and the registry alone.
//
// A measurement, printed rather than asserted. Needs the VM service:
//   flutter test --enable-vmservice --tags bench test/apply_path_bench_test.dart

@Tags(['bench'])
library;

import 'dart:typed_data';

import 'dart:developer';
import 'dart:isolate';

import 'package:flutter_test/flutter_test.dart';
import 'package:vm_service/vm_service.dart' hide Isolate;
import 'package:vm_service/vm_service_io.dart';
import 'package:swtflutter/src/comm/delivery_gate.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
import 'package:swtflutter/src/gen/widget.dart';
import 'package:swtflutter/src/gen/widgets.dart';

import 'delivery/support/deliver.dart';
import 'support/heap_bytes.dart';

const int _records = 2000;
const int _rounds = 5;

void main() {
  test('MEASURE: bytes and time to apply one widget record', timeout: Timeout.none, () async {
    final heap = await NewSpace.connect();
    if (heap == null) {
      print('MEASURE needs --enable-vmservice');
      return;
    }
    freshClient();
    var stamp = 1000;

    /// Frames for [n] records each, made ahead so making them is not counted.
    List<Uint8List> frames(int n, WireRecord Function(int stamp) record) =>
        [for (var i = 0; i < n; i++) wireFrameBody([record(++stamp)])];

    Future<void> report(String name, VWidget target, WireRecord Function(int stamp) record) async {
      final warm = frames(_records, record);
      for (final f in warm) {
        VRegistry.instance.applyWire(f);
      }
      final bytes = <double>[];
      final nanos = <double>[];
      for (var r = 0; r < _rounds; r++) {
        // Each run applied in the order it was made: a change fits only the state just before it.
        final runFrames = frames(_records, record);
        final quiet = await heap.bytesOf(() async {});
        final used = await heap.bytesOf(() async {
          for (final f in runFrames) {
            VRegistry.instance.applyWire(f);
          }
        });
        if (quiet != null && used != null) bytes.add((used - quiet) / _records);
        final timedFrames = frames(_records, record);
        final clock = Stopwatch()..start();
        for (final f in timedFrames) {
          VRegistry.instance.applyWire(f);
        }
        nanos.add(clock.elapsedMicroseconds * 1000 / _records);
      }
      print(measureLine(name, median(nanos), bytes.isEmpty ? null : median(bytes)));
      print('  held stamp after: ${deliveryGate.heldSeq(VRegistry.channelOf(target))}, last written $stamp');
      final byClass = frames(_records, record);
      final top = await _allocationsByClass(() {
        for (final f in byClass) {
          VRegistry.instance.applyWire(f);
        }
      });
      for (final (cls, count, size) in top.take(12)) {
        print('    ${cls.padRight(40)} ${(count / _records).toStringAsFixed(2).padLeft(6)} per record  ${(size / _records).toStringAsFixed(1).padLeft(7)} B');
      }
    }

    final label = VLabel()
      ..swt = 'Label'
      ..id = 810001
      ..text = 'start';
    applyRecords([WireRecord.whole(label..seq = ++stamp)]);
    await report('change: one Label text', label, (s) {
      label
        ..seq = s
        ..text = 'text $s';
      return WireRecord.change(label, keys: [WireKey.k_Label_text], base: s - 1);
    });

    final watched = VLabel()
      ..swt = 'Label'
      ..id = 810004
      ..text = 'start';
    applyRecords([WireRecord.whole(watched..seq = ++stamp)]);
    var told = 0;
    VRegistry.instance.watchAt(VRegistry.keyOf(watched), (_) => told++);
    await report('change: one Label text, watched as a mounted widget is', watched, (s) {
      watched
        ..seq = s
        ..text = 'text $s';
      return WireRecord.change(watched, keys: [WireKey.k_Label_text], base: s - 1);
    });
    print('  told $told times');

    final row = VTableItem()
      ..swt = 'TableItem'
      ..id = 810002;
    await report('whole: a TableItem of three texts', row, (s) {
      row
        ..seq = s
        ..texts = ['Name $s', 'Size', 'Modified'];
      return WireRecord.whole(row);
    });

    final children = <VControl>[
      for (var i = 0; i < 20; i++)
        VLabel()
          ..swt = 'Label'
          ..id = 811000 + i
          ..text = 'child $i',
    ];
    for (final c in children) {
      applyRecords([WireRecord.whole(c..seq = ++stamp)]);
    }
    final parent = VComposite()
      ..swt = 'Composite'
      ..id = 810003
      ..children = children;
    applyRecords([WireRecord.whole(parent..seq = ++stamp)]);
    await report('change: a Composite naming 20 held children', parent, (s) {
      parent.seq = s;
      return WireRecord.change(parent, keys: [WireKey.k_Composite_children], base: s - 1);
    });
  });
}

/// Instances and bytes allocated per class while [run] runs, largest first: what an empty run counts
/// (the profiler's own traffic) taken off, class by class.
Future<List<(String, int, int)>> _allocationsByClass(void Function() run) async {
  final uri = (await Service.getInfo()).serverWebSocketUri!;
  final vm = await vmServiceConnectUri(uri.toString());
  final isolate = Service.getIsolateId(Isolate.current)!;
  Future<Map<String, (int, int)>> sample(void Function() body) async {
    await vm.getAllocationProfile(isolate, reset: true, gc: true);
    body();
    final profile = await vm.getAllocationProfile(isolate);
    return {
      for (final m in profile.members ?? const <ClassHeapStats>[])
        if ((m.instancesAccumulated ?? 0) > 0)
          '${m.classRef?.library?.name ?? ''}:${m.classRef?.name ?? '?'}': (m.instancesAccumulated!, m.accumulatedSize ?? 0),
    };
  }

  final empty = await sample(() {});
  final used = await sample(run);
  await vm.dispose();
  final out = <(String, int, int)>[];
  used.forEach((cls, v) {
    final e = empty[cls] ?? (0, 0);
    final count = v.$1 - e.$1, size = v.$2 - e.$2;
    if (size > 0) out.add((cls.substring(cls.indexOf(':') + 1), count, size));
  });
  out.sort((a, b) => b.$3.compareTo(a.$3));
  return out;
}
