import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';

/// A run of draw ops reaches Flutter fused into one frame, because a grid that paints a thousand
/// ops per repaint cannot afford a frame each. Each entry still has to arrive on its own channel,
/// in order, exactly as if it had come alone.

/// The wire name of a fused run; pinned here as a literal because it is a protocol contract
/// shared with Java's MessageBatch.
const batchEvent = 'swt.evolve.batch';

void _receive(String actionId, Object payload) {
  final actionBytes = utf8.encode(actionId);
  final body = utf8.encode(jsonEncode(payload));
  final frame = Uint8List(2 + actionBytes.length + body.length);
  frame[0] = (actionBytes.length >> 8) & 0xFF;
  frame[1] = actionBytes.length & 0xFF;
  frame.setRange(2, 2 + actionBytes.length, actionBytes);
  frame.setRange(2 + actionBytes.length, frame.length, body);
  EquoCommService.commForTesting.receiveBinary(frame);
}

void main() {
  test('a batch delivers every entry on its own channel, in order', () async {
    final seen = <String>[];
    EquoCommService.onRaw('GC/1/drawLine', (p) => seen.add('line:${(p as Map)["x1"]}'));
    EquoCommService.onRaw('GC/1/gcDispose', (_) => seen.add('dispose'));

    _receive(batchEvent, [
      ['GC/1/drawLine', {'x1': 7}],
      ['GC/1/drawLine', {'x1': 8}],
      ['GC/1/gcDispose', {'fullRepaint': true}],
    ]);
    await Future.delayed(Duration.zero);

    expect(seen, ['line:7', 'line:8', 'dispose']);
  });

  test('a batched entry whose handler is not up yet is not dropped', () async {
    _receive(batchEvent, [
      ['GC/2/drawLine', {'x1': 3}],
    ]);
    await Future.delayed(Duration.zero);

    final seen = <String>[];
    EquoCommService.onRaw('GC/2/drawLine', (p) => seen.add('line:${(p as Map)["x1"]}'));
    await Future.delayed(Duration.zero);

    expect(seen, ['line:3']);
  });

  test('a batch keeps its place among the frames around it', () async {
    final seen = <String>[];
    EquoCommService.onRaw('GC/3/before', (_) => seen.add('before'));
    EquoCommService.onRaw('GC/3/inside', (_) => seen.add('inside'));
    EquoCommService.onRaw('GC/3/after', (_) => seen.add('after'));

    _receive('GC/3/before', {});
    _receive(batchEvent, [
      ['GC/3/inside', {}],
    ]);
    _receive('GC/3/after', {});
    await Future.delayed(Duration.zero);

    expect(seen, ['before', 'inside', 'after']);
  });
}
