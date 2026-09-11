// Frames are applied one at a time, in arrival order. A handler is free to await — decoding an
// image, resolving a surface — and the frame behind it must not start against half-applied state.

import 'dart:async';
import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  void deliver(String actionId, List<int> body) {
    final action = utf8.encode(actionId);
    final out = Uint8List(2 + action.length + body.length);
    out[0] = (action.length >> 8) & 0xFF;
    out[1] = action.length & 0xFF;
    out.setRange(2, 2 + action.length, action);
    out.setRange(2 + action.length, out.length, body);
    EquoCommService.commForTesting.receiveBinary(out);
  }

  Future<void> settle() async {
    for (var i = 0; i < 20; i++) {
      await Future<void>.delayed(Duration.zero);
    }
  }

  test('a frame that awaits holds the one behind it', () async {
    final applied = <String>[];
    final slow = Completer<void>();

    EquoCommService.onBytes('test/slow', (_) async {
      applied.add('slow:start');
      await slow.future;
      applied.add('slow:done');
    });
    EquoCommService.onBytes('test/fast', (_) {
      applied.add('fast');
    });

    deliver('test/slow', const [1]);
    deliver('test/fast', const [2]);
    await settle();

    expect(applied, ['slow:start'],
        reason: 'the second frame started while the first was still half-applied');

    slow.complete();
    await settle();

    expect(applied, ['slow:start', 'slow:done', 'fast']);
  });

  /// Which handler a frame belongs to is decided when the frame is applied, never when it arrives.
  /// Frames routinely register the handler the frame behind them needs — `GC/create` builds the
  /// drawer that owns the ops queued behind it — and a lookup at arrival time finds nothing, diverts
  /// the frame into the one-slot pending buffer, and replays it after the frames it was supposed to
  /// precede. Ordering then depends on how long the handler in front took to run.
  test('a frame registering the next frame handler still receives it in order', () async {
    final applied = <String>[];

    EquoCommService.onBytes('test/create', (_) async {
      // Yield first: the frame behind this one has already arrived by now.
      await Future<void>.delayed(Duration.zero);
      applied.add('create');
      EquoCommService.onBytes('test/op', (_) => applied.add('op'));
    });

    deliver('test/create', const [1]);
    deliver('test/op', const [2]);
    await settle();

    expect(applied, ['create', 'op'],
        reason: 'the op was routed before its handler existed, so it could only arrive late');
  });

  test('arrival order survives handlers of differing speed', () async {
    final applied = <int>[];
    for (var i = 0; i < 5; i++) {
      EquoCommService.onBytes('test/op$i', (_) async {
        // The odd ones yield; in arrival order they must still land in order.
        if (i.isOdd) await Future<void>.delayed(Duration.zero);
        applied.add(i);
      });
    }

    for (var i = 0; i < 5; i++) {
      deliver('test/op$i', [i]);
    }
    await settle();

    expect(applied, [0, 1, 2, 3, 4]);
  });
}
