// What a benchmark needs to count the bytes a run allocates, and to receive frames the way the app
// does. Shared by the `bench` tests, which print measurements rather than assert them.

import 'dart:async';
import 'dart:developer';
import 'dart:isolate';
import 'dart:typed_data';

import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:vm_service/vm_service.dart' hide Isolate;
import 'package:vm_service/vm_service_io.dart';

/// Messages received between two turns of the event loop, as a socket delivers a burst.
const int burst = 64;

/// Bytes allocated in new space, read through the VM service: available when the test runs with
/// `--enable-vmservice`. A count is taken between two points with no scavenge in between, so it is
/// exact; one a scavenge interrupted is dropped.
class NewSpace {
  NewSpace._(this._vm, this._isolate);

  final VmService _vm;
  final String _isolate;

  /// The isolate's new space, or null when the VM service is not enabled.
  static Future<NewSpace?> connect() async {
    final uri = (await Service.getInfo()).serverWebSocketUri;
    if (uri == null) return null;
    final vm = await vmServiceConnectUri(uri.toString());
    return NewSpace._(vm, Service.getIsolateId(Isolate.current)!);
  }

  Future<(int, int)> _read() async {
    final heaps = (await _vm.getIsolate(_isolate)).json!['_heaps'] as Map;
    final space = heaps['new'] as Map;
    return ((space['used'] as num).toInt(), (space['collections'] as num).toInt());
  }

  /// Bytes [run] allocates, or null when a scavenge ran during it and the count is lost.
  Future<int?> bytesOf(Future<void> Function() run) async {
    await _vm.getAllocationProfile(_isolate, gc: true);
    final (before, collections) = await _read();
    await run();
    final (after, collectionsAfter) = await _read();
    return collectionsAfter == collections ? after - before : null;
  }

  Future<void> dispose() => _vm.dispose();
}

/// Has [comm] receive [count] of [frames] in turn, letting it apply them every [burst] messages.
///
/// In the root zone, as the app receives: the test's zone records a stack chain at every callback
/// scheduled in it, which would count against the comm.
Future<void> receiveFrames(EquoCommBase comm, List<Uint8List> frames, int count) =>
    Zone.root.run(() async {
      for (var i = 0; i < count; i++) {
        comm.receiveBinary(frames[i % frames.length]);
        if (i % burst == burst - 1) await Future<void>.delayed(Duration.zero);
      }
      await Future<void>.delayed(Duration.zero);
    });

/// Yields as [receiveFrames] does for [count] messages, receiving none: the run's own allocations.
Future<void> idle(int count) => Zone.root.run(() async {
      for (var i = 0; i < count; i++) {
        if (i % burst == burst - 1) await Future<void>.delayed(Duration.zero);
      }
      await Future<void>.delayed(Duration.zero);
    });

double median(List<double> values) => (List.of(values)..sort())[values.length ~/ 2];

/// One line of a benchmark's report: [name], time and bytes per unit.
String measureLine(String name, double ns, double? bytes) =>
    'MEASURE ${name.padRight(56)} ${ns.toStringAsFixed(0).padLeft(6)} ns '
    '${(bytes == null ? 'n/a' : bytes.toStringAsFixed(1)).padLeft(8)} B';
