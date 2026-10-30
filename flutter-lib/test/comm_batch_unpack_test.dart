import 'dart:async';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';

import 'support/batch.dart';
import 'support/channels.dart';

/// A run of draw ops reaches Flutter fused into one frame, because a grid that paints a thousand
/// ops per repaint cannot afford a frame each. Each entry still has to arrive on its own channel,
/// in order, exactly as if it had come alone.

void main() {
  test('a binary batch delivers every entry on its own channel, in order', () async {
    final seen = <String>[];
    EquoCommService.commForTesting.onNamed('GC/11/drawLineintintintint', (p) => seen.add('line:${(p as Map)["x1"]}'));
    EquoCommService.commForTesting.onNamed('GC/11/Mouse/MouseUp', (_) => seen.add('dispose'));
    EquoCommService.commForTesting.onNamed('Label/12/Paint/Paint', (p) => seen.add('paint:${(p as Map)["text"]}'));

    receiveBatch([
      ('GC/11/drawLineintintintint', {'x1': 7}),
      ('Label/12/Paint/Paint', {'text': 'déjà'}),
      ('GC/11/drawLineintintintint', {'x1': 8}),
      ('GC/11/Mouse/MouseUp', {'fullRepaint': true}),
    ]);
    await Future.delayed(Duration.zero);

    expect(seen, ['line:7', 'paint:déjà', 'line:8', 'dispose']);
  });

  test('a batch delivers every entry on its own channel, in order', () async {
    final seen = <String>[];
    EquoCommService.commForTesting.onNamed('GC/1/drawLineintintintint', (p) => seen.add('line:${(p as Map)["x1"]}'));
    EquoCommService.commForTesting.onNamed('GC/1/Mouse/MouseUp', (_) => seen.add('dispose'));

    receiveBatch([
      ('GC/1/drawLineintintintint', {'x1': 7}),
      ('GC/1/drawLineintintintint', {'x1': 8}),
      ('GC/1/Mouse/MouseUp', {'fullRepaint': true}),
    ]);
    await Future.delayed(Duration.zero);

    expect(seen, ['line:7', 'line:8', 'dispose']);
  });

  test('an entry whose handler fails does not drop the rest of the batch', () async {
    final seen = <String>[];
    final comm = EquoCommService.commForTesting;
    comm.onNamed('GC/41/drawLineintintintint', (p) => seen.add('line:${(p as Map)["x1"]}'));
    // Channels whose handlers a batch reaches whatever the widget, standing for any that fails.
    comm.onNamed('swt.evolve.timing.ping', (_) => throw StateError('broken handler'));
    final failedLater = Completer<void>();
    comm.onNamed('swt.evolve.timing.bare', (_) async {
      await Future<void>.delayed(Duration.zero);
      failedLater.complete();
      throw StateError('broken later');
    });
    addTearDown(() {
      comm.removeNamed('swt.evolve.timing.ping');
      comm.removeNamed('swt.evolve.timing.bare');
    });

    receiveBatch([
      ('GC/41/drawLineintintintint', {'x1': 1}),
      ('swt.evolve.timing.ping', {}),
      ('GC/41/drawLineintintintint', {'x1': 2}),
      ('swt.evolve.timing.bare', {}),
      ('GC/41/drawLineintintintint', {'x1': 3}),
    ]);
    // The entries after the late failure apply once it has thrown, in microtasks a zero timer outlasts.
    await failedLater.future;
    await Future<void>.delayed(Duration.zero);

    expect(seen, ['line:1', 'line:2', 'line:3']);
  });

  test('a batched entry a synchronous handler took is not kept for the next one', () async {
    final first = <int>[];
    EquoCommService.commForTesting.onNamed('GC/901/drawLineintintintint', (p) {
      first.add((p as Map)['x1'] as int);
    });
    receiveBatch([
      ('GC/901/drawLineintintintint', {'x1': 1}),
    ]);
    await Future.delayed(Duration.zero);

    // A new drawer on the same id registers the channel again.
    final second = <int>[];
    EquoCommService.commForTesting.onNamed('GC/901/drawLineintintintint', (p) {
      second.add((p as Map)['x1'] as int);
    });
    await Future.delayed(Duration.zero);

    expect(first, [1]);
    expect(second, isEmpty, reason: 'a frame already applied must not be replayed');
  });

  test('a batched entry whose handler is not up yet is not dropped', () async {
    receiveBatch([
      ('GC/2/drawLineintintintint', {'x1': 3}),
    ]);
    await Future.delayed(Duration.zero);

    final seen = <String>[];
    EquoCommService.commForTesting.onNamed('GC/2/drawLineintintintint', (p) => seen.add('line:${(p as Map)["x1"]}'));
    await Future.delayed(Duration.zero);

    expect(seen, ['line:3']);
  });

  test('a batch keeps its place among the frames around it', () async {
    final seen = <String>[];
    EquoCommService.commForTesting.onNamed('GC/3/Mouse/MouseDown', (_) => seen.add('before'));
    EquoCommService.commForTesting.onNamed('GC/3/imageResult', (_) => seen.add('inside'));
    EquoCommService.commForTesting.onNamed('GC/3/imageSnapshotResult', (_) => seen.add('after'));

    receiveTagged('GC/3/Mouse/MouseDown', {});
    receiveBatch([
      ('GC/3/imageResult', {}),
    ]);
    receiveTagged('GC/3/imageSnapshotResult', {});
    await Future.delayed(Duration.zero);

    expect(seen, ['before', 'inside', 'after']);
  });
}

