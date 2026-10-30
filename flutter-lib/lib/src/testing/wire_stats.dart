import 'package:flutter/foundation.dart';
import 'package:flutter/scheduler.dart';
import 'package:flutter/widgets.dart' show debugProfileBuildsEnabled;

import '../comm/comm_frame.dart' show EquoCommBase, ReceiveTimer;

/// Measurement-only: what receiving widget state costs on this side, from the bytes of a frame to the
/// Flutter frame that shows it. Off unless [start] is called; production pays one static read per frame.
///
/// - decode: bytes to the values a handler is given;
/// - apply: handing them to the widgets, up to and including their `setState`;
/// - build: the UI-thread work of every Flutter frame that follows an apply (build, layout, paint),
///   timed from its first frame callback to its post-frame callback, so it is known as soon as the
///   frame is done rather than when the engine reports its timings.
///
/// Outside release builds, the frames' phases too, from Flutter's own timeline: what `stop` reports as
/// `phases`, microseconds per phase name. Layout includes the builds that run inside it.
class WireStats {
  static bool on = false;
  static final Stopwatch _clock = Stopwatch()..start();

  static int frames = 0;
  static int bytes = 0;
  static int decodeMicros = 0;
  static int applyMicros = 0;
  static int builtFrames = 0;
  static int buildMicros = 0;
  static bool _framePending = false;
  static int _since = 0;

  static int now() => _clock.elapsedMicroseconds;

  /// Times every widget's build as its own phase, named by widget type (inclusive of what it builds).
  static const bool profileBuilds = bool.fromEnvironment('WIRE_STATS_PROFILE_BUILDS');

  static void start() {
    frames = bytes = decodeMicros = applyMicros = builtFrames = buildMicros = 0;
    _since = now();
    on = true;
    EquoCommBase.receiveTimer = _timer;
    if (!kReleaseMode) {
      FlutterTimeline.debugCollectionEnabled = true;
      FlutterTimeline.debugReset();
      debugProfileBuildsEnabled = profileBuilds;
    }
  }

  static Map<String, dynamic> stop() {
    on = false;
    EquoCommBase.receiveTimer = null;
    final phases = <String, int>{};
    if (!kReleaseMode && FlutterTimeline.debugCollectionEnabled) {
      final blocks = FlutterTimeline.debugCollect().timedBlocks;
      for (final block in blocks) {
        phases[block.name] = (phases[block.name] ?? 0) + block.duration.round();
      }
      if (profileBuilds) _selfTimes(blocks, phases);
      FlutterTimeline.debugCollectionEnabled = false;
      debugProfileBuildsEnabled = false;
    }
    return <String, dynamic>{
      'phases': phases,
      'frames': frames,
      'bytes': bytes,
      'decodeMicros': decodeMicros,
      'applyMicros': applyMicros,
      'builtFrames': builtFrames,
      'buildMicros': buildMicros,
    };
  }

  /// Counts one received frame: [decoded] and [applied] are [now] readings taken after each step.
  static final ReceiveTimer _timer = _WireStatsTimer();

  static void received(String channel, int length, int startedAt, int decoded, int applied) {
    // The frames that start and stop the count are not part of it.
    if (!on || startedAt < _since || channel.startsWith('evolve.test.')) return;
    frames++;
    bytes += length;
    decodeMicros += decoded - startedAt;
    applyMicros += applied - decoded;
    _timeNextFrame();
  }

  /// Each block's time less the blocks nested inside it, summed by name as `self NAME`.
  static void _selfTimes(List<TimedBlock> blocks, Map<String, int> into) {
    final sorted = [...blocks]..sort((a, b) {
        final byStart = a.start.compareTo(b.start);
        return byStart != 0 ? byStart : b.end.compareTo(a.end);
      });
    final open = <TimedBlock>[];
    final self = <TimedBlock, double>{};
    for (final block in sorted) {
      while (open.isNotEmpty && open.last.end <= block.start) {
        open.removeLast();
      }
      self[block] = block.duration;
      if (open.isNotEmpty) self[open.last] = self[open.last]! - block.duration;
      open.add(block);
    }
    self.forEach((block, micros) {
      final key = 'self ${block.name}';
      into[key] = (into[key] ?? 0) + micros.round();
    });
  }

  /// Times the frame the apply asked for. One that asked for none gets none: scheduling one here
  /// would draw a frame that nothing else would have.
  static void _timeNextFrame() {
    final scheduler = SchedulerBinding.instance;
    if (_framePending || !scheduler.hasScheduledFrame) return;
    _framePending = true;
    scheduler.scheduleFrameCallback((_) {
      final began = now();
      scheduler.addPostFrameCallback((_) {
        _framePending = false;
        if (!on) return;
        builtFrames++;
        buildMicros += now() - began;
      });
    });
  }
}

class _WireStatsTimer implements ReceiveTimer {
  @override
  int now() => WireStats.now();

  @override
  void received(String channel, int length, int startedAt, int decodedAt, int appliedAt) =>
      WireStats.received(channel, length, startedAt, decodedAt, appliedAt);
}
