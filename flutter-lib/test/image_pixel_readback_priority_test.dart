// A pixel readback is answered by a caller blocked on it with a timeout, so it must not wait out a
// queue of unrelated frame work. When the picture it names is already rendered, the answer cannot
// be changed by anything still queued, so it goes out at arrival instead of taking its turn.
//
// What this guards is not a slow read but a wrong one: an unanswered readback leaves the Java side
// holding the buffer it already had, which for an image just drawn and never read is all zeros — a
// black image no caller can tell from a real one.
//
// Both cases are observed through the queued handler: production registers an arrival handler and a
// queued one for the same channel, so re-registering the queued one here reports exactly which of
// the two took the frame.

import 'dart:async';
import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/main.dart' show registerImagePixelsListener;
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/impl/utils/image_utils.dart';

Uint8List _frame(String actionId, List<int> body) {
  final action = utf8.encode(actionId);
  final out = Uint8List(2 + action.length + body.length);
  out[0] = (action.length >> 8) & 0xFF;
  out[1] = action.length & 0xFF;
  out.setRange(2, 2 + action.length, action);
  out.setRange(2 + action.length, out.length, body);
  return out;
}

Uint8List _refBytes(int ref) =>
    (ByteData(8)..setInt64(0, ref, Endian.big)).buffer.asUint8List();

Future<void> _settle() async {
  for (var i = 0; i < 30; i++) {
    await Future<void>.delayed(Duration.zero);
  }
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  late bool queuedPathTook;
  late Completer<void> blocker;

  setUp(() {
    registerImagePixelsListener();
    queuedPathTook = false;
    blocker = Completer<void>();
    // Replaces production's queued handler for this channel; the arrival handler is untouched.
    EquoCommService.onBytes('Image/requestPixels', (_) {
      queuedPathTook = true;
    });
    EquoCommService.onBytes('test/blocker', (_) async => blocker.future);
  });

  tearDown(() => blocker.isCompleted ? null : blocker.complete());

  test('an already-rendered picture is answered without waiting for the queue', () async {
    const ref = 994242;
    ImageUtils.registerRemoteImage(ref, await createTestImage(width: 4, height: 4));

    EquoCommService.commForTesting.receiveBinary(_frame('test/blocker', const []));
    EquoCommService.commForTesting
        .receiveBinary(_frame('Image/requestPixels', _refBytes(ref)));
    await _settle();

    // Draining the queue is what makes this discriminating: if the frame had merely been queued
    // behind the blocker, the queued handler would take it now. It must never take it at all.
    blocker.complete();
    await _settle();

    expect(queuedPathTook, isFalse,
        reason: 'the picture was already rendered, so nothing queued could change the answer — '
            'waiting behind the blocked frame is what lets the caller time out and read black');
  });

  test('a picture not yet rendered keeps its place in the queue', () async {
    const ref = 994243; // never registered: its render would itself be a queued frame

    EquoCommService.commForTesting.receiveBinary(_frame('test/blocker', const []));
    EquoCommService.commForTesting
        .receiveBinary(_frame('Image/requestPixels', _refBytes(ref)));
    await _settle();

    expect(queuedPathTook, isFalse,
        reason: 'the frame ahead of it has not finished applying');

    blocker.complete();
    await _settle();

    expect(queuedPathTook, isTrue,
        reason: 'answering before the render that produces this ref would race it');
  });
}
