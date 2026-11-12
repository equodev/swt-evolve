import 'dart:async';
import 'dart:ui';

import 'package:flutter/scheduler.dart';

/// Test-only: per-frame build and raster times straight from the engine's own frame pipeline.
///
/// This is the only measurement of Evolve's rendering that is comparable to a native toolkit's:
/// the Java side returns as soon as the draw ops are serialized, long before anything is on
/// screen, so timing it there measures serialization, not rendering.
///
/// The timings callback is registered on the first [start] rather than at boot, so a session that
/// never asks for stats pays nothing.
///
/// The engine reports timings in batches (on the web, at the first frame more than 100 ms after the
/// last batch), so a batch can hold frames from before [start] or arrive well after [stop]. The
/// window opens after the first frame after [start] and closes at the first frame after [stop], on the
/// clock the engine stamps frames with (which is not the same on every platform as any Dart clock);
/// [stop] completes once a batch has brought a frame from past the window, so every frame inside it
/// is counted.
class FrameStats {
  static bool _registered = false;
  static bool _collecting = false;
  /// The engine's timestamp of the first frame after [start]; null until that frame begins.
  static int? _startedAt;
  /// The engine's timestamp of the first frame after [stop]; null until that frame begins.
  static int? _endedAt;
  static Completer<void>? _stopped;
  static const _stopTimeout = Duration(seconds: 2);

  /// The vsync of the last frame counted.
  static int _lastFrameAt = 0;
  static final List<int> _buildMicros = <int>[];
  static final List<int> _rasterMicros = <int>[];

  static void start() {
    if (!_registered) {
      SchedulerBinding.instance.addTimingsCallback(_onTimings);
      _registered = true;
    }
    final pending = _stopped;
    if (pending != null && !pending.isCompleted) pending.complete();
    _buildMicros.clear();
    _rasterMicros.clear();
    _startedAt = null;
    _endedAt = null;
    _stopped = null;
    _lastFrameAt = 0;
    final scheduler = SchedulerBinding.instance;
    scheduler.scheduleFrameCallback((_) {
      _startedAt = scheduler.currentSystemFrameTimeStamp.inMicroseconds;
      _lastFrameAt = _startedAt!;
    });
    scheduler.scheduleFrame();
    _collecting = true;
  }

  /// Closes the window at the next frame; completes once every frame inside it has been reported, or
  /// after [_stopTimeout] with what was.
  static Future<void> stop() {
    if (!_collecting) return Future<void>.value();
    final stopped = _stopped ??= Completer<void>();
    final scheduler = SchedulerBinding.instance;
    void frame(Duration _) {
      if (stopped.isCompleted) return;
      _endedAt ??= scheduler.currentSystemFrameTimeStamp.inMicroseconds;
      // Frames until the engine hands over the batch holding one from past the window.
      scheduler.scheduleFrameCallback(frame);
      scheduler.scheduleFrame();
    }

    scheduler.scheduleFrameCallback(frame);
    scheduler.scheduleFrame();
    Timer(_stopTimeout, () {
      if (identical(_stopped, stopped)) _finish();
    });
    return stopped.future;
  }

  static void _finish() {
    _collecting = false;
    final stopped = _stopped;
    if (stopped != null && !stopped.isCompleted) stopped.complete();
  }

  static void _onTimings(List<FrameTiming> timings) {
    if (!_collecting) return;
    for (final timing in timings) {
      final startedAt = _startedAt;
      final vsync = timing.timestampInMicroseconds(FramePhase.vsyncStart);
      // The frame [start] forces only marks where the window opens.
      if (startedAt == null || vsync <= startedAt) continue;
      final endedAt = _endedAt;
      if (endedAt != null && vsync >= endedAt) {
        _finish();
        return;
      }
      if (vsync > _lastFrameAt) _lastFrameAt = vsync;
      _buildMicros.add(timing.buildDuration.inMicroseconds);
      _rasterMicros.add(timing.rasterDuration.inMicroseconds);
    }
  }

  /// Frames counted since [start], with build/raster distributions. Percentiles rather than a mean
  /// alone: an average hides the stutter that makes a UI feel slow.
  static Map<String, dynamic> snapshot() {
    final startedAt = _startedAt;
    final elapsedMicros = startedAt == null ? 0 : _lastFrameAt - startedAt;
    return <String, dynamic>{
      'frames': _buildMicros.length,
      'elapsedMicros': elapsedMicros,
      'build': _distribution(_buildMicros),
      'raster': _distribution(_rasterMicros),
    };
  }

  static int _percentileIndex(int length, int percentile) {
    final index = (length * percentile) ~/ 100;
    return index >= length ? length - 1 : index;
  }

  static Map<String, dynamic> _distribution(List<int> samples) {
    if (samples.isEmpty) {
      return <String, dynamic>{'mean': 0, 'p50': 0, 'p95': 0, 'max': 0};
    }
    final sorted = List<int>.from(samples)..sort();
    var total = 0;
    for (final value in sorted) {
      total += value;
    }
    return <String, dynamic>{
      'mean': total ~/ sorted.length,
      'p50': sorted[sorted.length ~/ 2],
      'p95': sorted[_percentileIndex(sorted.length, 95)],
      'max': sorted[sorted.length - 1],
    };
  }
}
