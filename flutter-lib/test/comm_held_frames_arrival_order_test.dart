import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/channel_header.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'support/channels.dart' as channels;
import 'support/channels.dart' show NamedChannels;

/// Frames held because nothing listened for them yet apply in the order they arrived once their
/// handlers register, whichever registers first. A drawer that registers the channel of its ops
/// before the one of its image's description must still get the description first: drawn without
/// it, the image comes out 1x1.
class _TestComm extends EquoCommBase {
  _TestComm() {
    markOpen();
  }

  @override
  void rawSend(Uint8List frame) {}

  void receive(String channel, List<int> body) =>
      receiveBinary(channels.frame(channel, Uint8List.fromList(body)));
}

Future<void> _drainMicrotasks() => Future<void>.delayed(Duration.zero);

void main() {
  test('frames held on several channels replay in arrival order, not registration order', () async {
    final comm = _TestComm();
    final applied = <String>[];
    comm.receive('GC/create', [5]);
    comm.receive('GC/5/imageInit', [1]);
    comm.receive('GC/5/gcDispose', [2]);
    await _drainMicrotasks();

    comm.onBytesNamed('GC/create', (_) {
      applied.add('create');
      // Registered the other way round from how they were sent.
      comm.onBytesNamed('GC/5/gcDispose', (_) => applied.add('ops'));
      comm.onBytesNamed('GC/5/imageInit', (_) => applied.add('imageInit'));
    });
    await _drainMicrotasks();

    expect(applied, ['create', 'imageInit', 'ops']);
  });

  test('a replayed frame applies before a frame that arrived after it', () async {
    final comm = _TestComm();
    final applied = <String>[];
    comm.onBytesNamed('Image/releaseRemoteRef', (_) => applied.add('later'));
    comm.receive('Image/requestPixels', [1]);
    await _drainMicrotasks();

    comm.receive('Image/releaseRemoteRef', [2]);
    // Still queued: its handler registers before the queue drains.
    comm.onBytesNamed('Image/requestPixels', (_) => applied.add('earlier'));
    await _drainMicrotasks();

    expect(applied, ['earlier', 'later']);
  });
}
