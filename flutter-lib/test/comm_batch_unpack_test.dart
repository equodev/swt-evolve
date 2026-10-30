import 'dart:async';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';

import 'support/batch.dart';

/// A run of draw ops reaches Flutter fused into one frame, because a grid that paints a thousand
/// ops per repaint cannot afford a frame each. Each entry still has to arrive on its own channel,
/// in order, exactly as if it had come alone.

void main() {
  test('a binary batch delivers every entry on its own channel, in order', () async {
    final seen = <String>[];
    EquoCommService.onRaw('GC/11/drawLine', (p) => seen.add('line:${(p as Map)["x1"]}'));
    EquoCommService.onRaw('GC/11/gcDispose', (_) => seen.add('dispose'));
    EquoCommService.onRaw('Label/12/Paint/Paint', (p) => seen.add('paint:${(p as Map)["text"]}'));

    receiveBatch([
      ('GC/11/drawLine', {'x1': 7}),
      ('Label/12/Paint/Paint', {'text': 'déjà'}),
      ('GC/11/drawLine', {'x1': 8}),
      ('GC/11/gcDispose', {'fullRepaint': true}),
    ]);
    await Future.delayed(Duration.zero);

    expect(seen, ['line:7', 'paint:déjà', 'line:8', 'dispose']);
  });

  test('a batch delivers every entry on its own channel, in order', () async {
    final seen = <String>[];
    EquoCommService.onRaw('GC/1/drawLine', (p) => seen.add('line:${(p as Map)["x1"]}'));
    EquoCommService.onRaw('GC/1/gcDispose', (_) => seen.add('dispose'));

    receiveBatch([
      ('GC/1/drawLine', {'x1': 7}),
      ('GC/1/drawLine', {'x1': 8}),
      ('GC/1/gcDispose', {'fullRepaint': true}),
    ]);
    await Future.delayed(Duration.zero);

    expect(seen, ['line:7', 'line:8', 'dispose']);
  });

  test('an entry whose handler fails does not drop the rest of the batch', () async {
    final seen = <String>[];
    EquoCommService.onRaw('GC/41/drawLine', (p) => seen.add('line:${(p as Map)["x1"]}'));
    EquoCommService.onRaw('GC/41/broken', (_) => throw StateError('broken handler'));
    final failedLater = Completer<void>();
    EquoCommService.onRaw('GC/41/failsLater', (_) async {
      await Future<void>.delayed(Duration.zero);
      failedLater.complete();
      throw StateError('broken later');
    });

    receiveBatch([
      ('GC/41/drawLine', {'x1': 1}),
      ('GC/41/broken', {}),
      ('GC/41/drawLine', {'x1': 2}),
      ('GC/41/failsLater', {}),
      ('GC/41/drawLine', {'x1': 3}),
    ]);
    // The entries after failsLater apply once it has thrown, in microtasks a zero timer outlasts.
    await failedLater.future;
    await Future<void>.delayed(Duration.zero);

    expect(seen, ['line:1', 'line:2', 'line:3']);
  });

  test('a batched entry a synchronous handler took is not kept for the next one', () async {
    final first = <int>[];
    EquoCommService.onRaw('GC/901/drawLine', (p) {
      first.add((p as Map)['x1'] as int);
    });
    receiveBatch([
      ('GC/901/drawLine', {'x1': 1}),
    ]);
    await Future.delayed(Duration.zero);

    // A new drawer on the same id registers the channel again.
    final second = <int>[];
    EquoCommService.onRaw('GC/901/drawLine', (p) {
      second.add((p as Map)['x1'] as int);
    });
    await Future.delayed(Duration.zero);

    expect(first, [1]);
    expect(second, isEmpty, reason: 'a frame already applied must not be replayed');
  });

  test('a batched entry whose handler is not up yet is not dropped', () async {
    receiveBatch([
      ('GC/2/drawLine', {'x1': 3}),
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

    receiveTagged('GC/3/before', {});
    receiveBatch([
      ('GC/3/inside', {}),
    ]);
    receiveTagged('GC/3/after', {});
    await Future.delayed(Duration.zero);

    expect(seen, ['before', 'inside', 'after']);
  });
}

