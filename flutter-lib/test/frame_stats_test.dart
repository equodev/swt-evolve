// FrameStats counts every frame drawn between start and stop, including those the engine reports
// only after stop: the web engine hands timings over in batches at most every 100 ms, so a short
// measured span is usually still waiting in the engine when it is stopped.

import 'dart:ui';

import 'package:flutter/scheduler.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/testing/frame_stats.dart';

FrameTiming _frame(int vsync, {int build = 1000, int raster = 2000}) => FrameTiming(
      vsyncStart: vsync,
      buildStart: vsync,
      buildFinish: vsync + build,
      rasterStart: vsync + build,
      rasterFinish: vsync + build + raster,
      rasterFinishWallTime: vsync + build + raster,
    );

int _now() => SchedulerBinding.instance.currentSystemFrameTimeStamp.inMicroseconds;

void _report(List<FrameTiming> batch) => PlatformDispatcher.instance.onReportTimings!(batch);

void main() {
  testWidgets('frames reported after stop still count when they were drawn before it', (tester) async {
    FrameStats.start();
    await tester.pump(const Duration(milliseconds: 16));
    final startedAt = _now();
    final drawn = [for (var i = 0; i < 4; i++) _frame(startedAt + (i + 1) * 16000)];

    var stopped = false;
    FrameStats.stop().then((_) => stopped = true);
    await tester.pump(const Duration(milliseconds: 100));
    final endedAt = _now();
    expect(stopped, isFalse, reason: 'the frames drawn before stop have not been reported yet');

    _report([_frame(startedAt - 16000), ...drawn, _frame(endedAt), _frame(endedAt + 16000)]);
    await tester.pump();
    expect(stopped, isTrue, reason: 'a batch holding a frame from past the window ends it');

    final stats = FrameStats.snapshot();
    expect(stats['frames'], drawn.length, reason: 'neither the frame start forces, one before it, nor one from stop on is counted');
    expect(stats['elapsedMicros'], drawn.last.timestampInMicroseconds(FramePhase.vsyncStart) - startedAt);
    await tester.pump(const Duration(seconds: 3));
  });

  testWidgets('stop answers with what was reported when no later batch comes', (tester) async {
    FrameStats.start();
    await tester.pump(const Duration(milliseconds: 16));
    _report([_frame(_now()), _frame(_now() + 16000)]);

    var stopped = false;
    FrameStats.stop().then((_) => stopped = true);
    await tester.pump(const Duration(milliseconds: 16));
    await tester.pump(const Duration(seconds: 2));
    expect(stopped, isTrue);
    expect(FrameStats.snapshot()['frames'], 1);
  });

  testWidgets('a stop left waiting does not end the next measurement', (tester) async {
    FrameStats.start();
    await tester.pump(const Duration(milliseconds: 16));
    FrameStats.stop();
    await tester.pump(const Duration(milliseconds: 16));

    FrameStats.start();
    await tester.pump(const Duration(milliseconds: 16));
    final startedAt = _now();
    await tester.pump(const Duration(seconds: 2));
    _report([_frame(startedAt + 16000), _frame(startedAt + 32000)]);
    expect(FrameStats.snapshot()['frames'], 2, reason: 'the first stop\'s fallback timer belongs to the first window');
    FrameStats.stop();
    await tester.pump(const Duration(milliseconds: 16));
    await tester.pump(const Duration(seconds: 3));
  });
}
