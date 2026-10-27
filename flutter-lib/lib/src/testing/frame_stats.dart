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
/// The engine reports timings in batches (about once a second in a release build), so a batch that
/// arrives after [start] can hold frames rendered before it. The window opens at the first frame
/// after [start], on the clock the engine stamps frames with (which is not the same on every
/// platform as any Dart clock), and ends at the last frame reported.
class FrameStats {
  static bool _registered = false;
  static bool _collecting = false;
  /// The engine's timestamp of the first frame after [start]; null until that frame begins.
  static int? _startedAt;

  /// The vsync of the last frame counted.
  static int _lastFrameAt = 0;
  static final List<int> _buildMicros = <int>[];
  static final List<int> _rasterMicros = <int>[];

  static void start() {
    if (!_registered) {
      SchedulerBinding.instance.addTimingsCallback(_onTimings);
      _registered = true;
    }
    _buildMicros.clear();
    _rasterMicros.clear();
    _startedAt = null;
    _lastFrameAt = 0;
    final scheduler = SchedulerBinding.instance;
    scheduler.scheduleFrameCallback((_) {
      _startedAt = scheduler.currentSystemFrameTimeStamp.inMicroseconds;
      _lastFrameAt = _startedAt!;
    });
    scheduler.scheduleFrame();
    _collecting = true;
  }

  static void stop() => _collecting = false;

  static void _onTimings(List<FrameTiming> timings) {
    if (!_collecting) return;
    for (final timing in timings) {
      final startedAt = _startedAt;
      final vsync = timing.timestampInMicroseconds(FramePhase.vsyncStart);
      if (startedAt == null || vsync < startedAt) continue;
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
