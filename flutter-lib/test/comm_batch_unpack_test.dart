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
  dictionaryTests();
  gcNameTests();
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

/// The sender names a repeated channel once and cites that entry's position afterwards.
void dictionaryTests() {
  test('a repeated channel travels as the index of the entry that named it', () async {
    final seen = <String>[];
    EquoCommService.onRaw('GC/7/drawStringStringintintboolean',
        (p) => seen.add('s:${(p as Map)["string"]}'));
    EquoCommService.onRaw('GC/7/gcDispose', (_) => seen.add('dispose'));

    _receive(batchEvent, [
      ['GC/7/drawStringStringintintboolean', {'string': '71'}],
      [0, {'string': '72'}],
      [0, {'string': '73'}],
      ['GC/7/gcDispose', <String, Object>{}],
    ]);
    await Future<void>.delayed(Duration.zero);

    expect(seen, ['s:71', 's:72', 's:73', 'dispose']);
  });

  test('an index is read against entry positions, so a bad entry does not shift the rest', () async {
    final seen = <String>[];
    EquoCommService.onRaw('GC/8/drawLine', (p) => seen.add('line:${(p as Map)["x1"]}'));

    _receive(batchEvent, [
      ['GC/8/drawLine', {'x1': 1}],
      'not an entry',
      [0, {'x1': 2}],
    ]);
    await Future<void>.delayed(Duration.zero);

    expect(seen, ['line:1', 'line:2'],
        reason: 'the index still names entry 0, not whatever survived parsing before it');
  });

  test('an index that names no earlier entry is dropped, not guessed', () async {
    final seen = <String>[];
    EquoCommService.onRaw('GC/9/drawLine', (p) => seen.add('line:${(p as Map)["x1"]}'));

    _receive(batchEvent, [
      [3, {'x1': 1}],
      ['GC/9/drawLine', {'x1': 2}],
    ]);
    await Future<void>.delayed(Duration.zero);

    expect(seen, ['line:2']);
  });
}

/// A GC description is spelled out once under `_gd` and cited by number (`_gr`) after that.
void gcNameTests() {
  test('a named description is filled back in for the GCs that cite it', () async {
    final seen = <Map<String, dynamic>>[];
    EquoCommService.onRaw('GC/11', (p) => seen.add((p as Map).cast<String, dynamic>()));
    EquoCommService.onRaw('GC/12', (p) => seen.add((p as Map).cast<String, dynamic>()));

    _receive(batchEvent, [
      ['GC/11', {'id': 11, 'swt': 'GC', 'style': 33554432, 'alpha': 255, '_gd': 0}],
      ['GC/12', {'id': 12, '_gr': 0}],
    ]);
    await Future.delayed(Duration.zero);

    expect(seen, hasLength(2));
    // The one that spelled it out keeps everything it said, without the bookkeeping key.
    expect(seen[0]['style'], 33554432);
    expect(seen[0].containsKey('_gd'), isFalse);
    // The one that cited it says exactly the same, under its own id.
    expect(seen[1]['id'], 12);
    expect(seen[1]['style'], 33554432);
    expect(seen[1]['alpha'], 255);
    expect(seen[1]['swt'], 'GC');
    expect(seen[1].containsKey('_gr'), isFalse);
  });

  test('a name is remembered across batches', () async {
    final seen = <Map<String, dynamic>>[];
    EquoCommService.onRaw('GC/21', (p) => seen.add((p as Map).cast<String, dynamic>()));
    EquoCommService.onRaw('GC/22', (p) => seen.add((p as Map).cast<String, dynamic>()));

    _receive(batchEvent, [
      ['GC/21', {'id': 21, 'swt': 'GC', 'style': 7, '_gd': 5}],
    ]);
    await Future.delayed(Duration.zero);
    _receive(batchEvent, [
      ['GC/22', {'id': 22, '_gr': 5}],
    ]);
    await Future.delayed(Duration.zero);

    expect(seen.last['style'], 7);
    expect(seen.last['id'], 22);
  });

  test('a name never given arrives as it is, rather than not at all', () async {
    final seen = <Map<String, dynamic>>[];
    EquoCommService.onRaw('GC/31', (p) => seen.add((p as Map).cast<String, dynamic>()));

    _receive(batchEvent, [
      ['GC/31', {'id': 31, '_gr': 999}],
    ]);
    await Future.delayed(Duration.zero);

    expect(seen, hasLength(1), reason: 'the frame still has to reach the GC it describes');
    expect(seen[0]['id'], 31);
  });
}
