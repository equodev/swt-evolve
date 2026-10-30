// A pixel read-back on the wasm renderer starts only once every frame rendered before it has been
// shown: one started while a frame is between sizing the shared surface and rasterising into it
// leaves that frame drawn at the image's size, and the whole view blank.

import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/utils/frame_presentation.dart';

void main() {
  late int presented;
  late Completer<void> next;

  void show() {
    presented++;
    final shown = next;
    next = Completer<void>();
    shown.complete();
  }

  setUp(() {
    presented = 0;
    next = Completer<void>();
  });
  tearDown(FramePresentation.debugReset);

  /// Tracks from here on, then renders exactly one frame.
  Future<void> renderOneFrame(WidgetTester tester) async {
    await tester.pumpWidget(const SizedBox());
    // Made in the test's zone, so its completion runs on the clock the test pumps.
    next = Completer<void>();
    FramePresentation.debugTrackWith(() => presented, () => next.future);
    tester.binding.scheduleFrame();
    await tester.pump();
  }

  testWidgets('a read-back waits for the frame rendered before it to be shown', (tester) async {
    await renderOneFrame(tester);
    var started = false;
    unawaited(FramePresentation.idle().then((_) => started = true));
    await tester.pump(Duration.zero);
    expect(started, isFalse, reason: 'the frame just rendered has not reached the screen');

    show();
    await tester.pump(Duration.zero);
    expect(started, isTrue);
  });

  testWidgets('with every rendered frame shown, a read-back starts at once', (tester) async {
    await renderOneFrame(tester);
    show();
    var started = false;
    unawaited(FramePresentation.idle().then((_) => started = true));
    await tester.pump(Duration.zero);
    expect(started, isTrue);
  });

  test('a frame that never shows holds a read-back for the timeout, and only once', () async {
    // A frame the engine skipped drawing is counted as rendered but never presented.
    final binding = TestWidgetsFlutterBinding.ensureInitialized();
    var waits = 0;
    FramePresentation.debugTrackWith(() => presented, () {
      waits++;
      return next.future;
    });
    binding.handleBeginFrame(Duration.zero);
    binding.handleDrawFrame();

    final watch = Stopwatch()..start();
    await FramePresentation.idle();
    expect(watch.elapsed, greaterThanOrEqualTo(FramePresentation.timeout * 0.9));
    expect(watch.elapsed, lessThan(FramePresentation.timeout * 3));

    final waited = waits;
    await FramePresentation.idle();
    expect(waits, waited, reason: 'the frame waited out is not waited for again');
  });
}
