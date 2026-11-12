// What the client sends in one turn leaves as one message, a batch laid out as Java's MessageBatch
// lays one out, so a keystroke or a frame that sends several costs one socket write; a turn that
// sends one frame sends it as it is.

import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/channel_header.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';

import 'support/channels.dart' as channels;

class _Batching extends EquoCommBase {
  _Batching() {
    markOpen();
  }

  final List<Uint8List> sent = [];

  @override
  bool get batchesSends => true;

  @override
  void rawSend(Uint8List frame) => sent.add(frame);
}

Uint8List _bytes(String s) => Uint8List.fromList(s.codeUnits);

/// The entries of a batch, each as its channel and its payload as text.
List<String> _entries(Uint8List batch) {
  final (name, start) = ChannelHeader.read(batch)!;
  expect(name, 'swt.evolve.batch');
  final out = <String>[];
  var pos = start;
  while (pos < batch.length) {
    final (channel, at) = ChannelHeader.readAt(batch, pos)!;
    var length = 0, shift = 0, p = at;
    while (true) {
      final b = batch[p++];
      length |= (b & 0x7F) << shift;
      if (b < 0x80) break;
      shift += 7;
    }
    out.add('$channel ${String.fromCharCodes(batch.sublist(p, p + length))}');
    pos = p + length;
  }
  return out;
}

void main() {
  test('the frames of one turn leave as one batch, in the order they were sent', () async {
    final comm = _Batching();
    comm.sendFrame(channels.frame('Button/1/Selection/Selection', _bytes('a')));
    comm.sendFrame(channels.frame('TreeItem/12/Selection/Selection', _bytes('bc')));
    comm.sendFrame(channels.frame('Button/-2/Selection/Selection', _bytes('')));
    expect(comm.sent, isEmpty, reason: 'nothing leaves before the turn ends');

    await Future<void>.microtask(() {});

    expect(comm.sent, hasLength(1));
    expect(_entries(comm.sent.single), [
      'Button/1/Selection/Selection a',
      'TreeItem/12/Selection/Selection bc',
      'Button/-2/Selection/Selection ',
    ]);
  });

  test('a turn that sends one frame sends it as it is', () async {
    final comm = _Batching();
    final frame = channels.frame('Button/1/Selection/Selection', _bytes('only'));
    comm.sendFrame(frame);

    await Future<void>.microtask(() {});

    expect(comm.sent.single, frame);
  });

  test('each turn is a message of its own', () async {
    final comm = _Batching();
    comm.sendFrame(channels.frame('Button/1/Selection/Selection', _bytes('first')));
    await Future<void>.microtask(() {});
    comm.sendFrame(channels.frame('Button/1/Selection/Selection', _bytes('x' * 5000)));
    comm.sendFrame(channels.frame('Button/2/Selection/Selection', _bytes('y')));
    await Future<void>.microtask(() {});

    expect(comm.sent, hasLength(2));
    expect(_entries(comm.sent[1]), ['Button/1/Selection/Selection ${'x' * 5000}', 'Button/2/Selection/Selection y']);
  });
}
