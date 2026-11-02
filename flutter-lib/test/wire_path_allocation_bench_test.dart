// What the Dart side allocates per message on each path of the wire, sending and receiving: the whole
// cost of one message, not the part that grows with its payload, and each step of a path on its own. The
// transport is left out: a sent frame stops where the socket would take it, and a received one starts
// where the socket hands it over.
//
// A measurement, printed rather than asserted. Needs the VM service:
//   flutter test --enable-vmservice --tags bench test/wire_path_allocation_bench_test.dart

@Tags(['bench'])
library;

import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/channel_header.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/tagged_values.dart';
import 'package:swtflutter/src/comm/widget_wire.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/widgets.dart';

import 'support/gc_frames.dart' show frame;
import 'support/heap_bytes.dart';
import 'support/channels.dart' as channels;
import 'support/channels.dart' show NamedChannels;

const int _messages = 4000;
const int _rounds = 5;

class _NullComm extends EquoCommBase {
  _NullComm() {
    markOpen();
  }

  int sent = 0;

  @override
  void rawSend(Uint8List frame) => sent += frame.length;

  // As a transport that copies the frame before the call returns, as a browser socket does.
  // ignore: override_on_non_overriding_member
  @override
  void rawSendRange(Uint8List buffer, int length) => sent += length;
}

void main() {
  test('MEASURE: bytes per message, each way and each step', timeout: Timeout.none, () async {
    final heap = await NewSpace.connect();
    if (heap == null) {
      print('MEASURE needs --enable-vmservice');
      return;
    }
    final comm = _NullComm();
    final event = VEvent()
      ..x = 10
      ..y = 4
      ..stateMask = 0;
    // Keys both sides number, as most payloads have; then keys spelled out, as a test's are.
    final map = {'id': 1, 'x': 2};
    final spelled = {'queryId': 1, 'targetId': 2};
    final bytes = Uint8List(8);

    Future<void> report(String name, Future<void> Function(int n) run) async {
      await run(_messages);
      final samples = <double>[];
      for (var r = 0; r < _rounds; r++) {
        final quiet = await heap.bytesOf(() => idle(_messages));
        final used = await heap.bytesOf(() => run(_messages));
        if (quiet != null && used != null) samples.add((used - quiet) / _messages);
      }
      print(measureLine(name, 0, samples.isEmpty ? null : median(samples)));
    }

    Future<void> sync(void Function() send, int n) async {
      for (var i = 0; i < n; i++) {
        send();
      }
    }

    await report('send: widget event, by numbers (sendTo)',
        (n) => sync(() => comm.sendTo(WireType.button, 700001, WireChannel.selectionSelection, event), n));
    await report('send: global tagged map, by number (sendOn)',
        (n) => sync(() => comm.sendOn(WireChannel.evolveTestQueryResponse, map), n));
    await report('send: raw bytes, by numbers (sendTo)',
        (n) => sync(() => comm.sendTo(WireType.image, 1, WireChannel.pixelsResult, bytes), n));

    var sink = 0;
    await report('  step: TaggedValues.encode(map)', (n) => sync(() => sink += TaggedValues.encode(map).length, n));
    await report('  step: TaggedValues.encode(spelled keys)',
        (n) => sync(() => sink += TaggedValues.encode(spelled).length, n));
    await report('  step: frameOn(channel, map)',
        (n) => sync(() => sink += WidgetWireWriter.frameOn(WireChannel.evolveTestQueryResponse, map).length, n));
    await report('  step: frameTo(numbers, event)',
        (n) => sync(() => sink += WidgetWireWriter.frameTo(WireType.button, 700001, WireChannel.selectionSelection, event).length, n));
    final encoded = TaggedValues.encode(map);
    await report('  step: TaggedValues.decode(map)', (n) => sync(() => sink += (TaggedValues.decode(encoded) as Map).length, n));
    final encodedSpelled = TaggedValues.encode(spelled);
    await report('  step: TaggedValues.decode(spelled keys)',
        (n) => sync(() => sink += (TaggedValues.decode(encodedSpelled) as Map).length, n));
    await report('  step: an empty map', (n) => sync(() => sink += <String, dynamic>{'id': n, 'x': 2}.length, n));

    var got = 0;
    comm.on(0, 0, WireChannel.evolveTestQuery, (_) => got++);
    comm.onBytes(0, 0, WireChannel.swtEvolveProperties, (_) => got++);
    final tagged = [frame('evolve.test.query', TaggedValues.encode(map))];
    final raw = [frame('swt.evolve.properties', bytes)];
    await report('receive: global tagged map (on)', (n) => receiveFrames(comm, tagged, n));
    await report('receive: raw bytes (onBytes)', (n) => receiveFrames(comm, raw, n));
    print('(sent ${comm.sent} bytes, handled $got, $sink)');
  });
}
