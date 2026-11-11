import 'dart:ui' show FrameTiming, FramePhase;

import 'package:flutter/foundation.dart' show kIsWeb;
import 'package:flutter/scheduler.dart' show SchedulerBinding, TimingsCallback;
import 'package:flutter/services.dart' show MethodChannel;

import 'boot_trace_page_stub.dart'
    if (dart.library.js_interop) 'boot_trace_page_web.dart';

/// Where the client's start goes: named steps in microseconds since the epoch, the clock Java's
/// `dev.equo.swt.BootTrace` reads too, so both sides line up on one timeline. Read by the
/// `evolve.test.bootTrace` channel (see test_harness.dart).
class BootTrace {
  BootTrace._();

  static final List<Map<String, Object>> _steps = [];

  static int nowMicros() => DateTime.now().microsecondsSinceEpoch;

  /// A point in time: the first moment something happened.
  static void mark(String name) {
    final now = nowMicros();
    _steps.add({'name': name, 'depth': 0, 'startUs': now, 'endUs': now});
  }

  /// Like [mark], only the first time [name] is marked.
  static void markOnce(String name) {
    if (_steps.any((s) => s['name'] == name)) return;
    mark(name);
  }

  /// A step from [startMicros] until now.
  static void step(String name, int startMicros, {int depth = 0}) {
    _steps.add({'name': name, 'depth': depth, 'startUs': startMicros, 'endUs': nowMicros()});
  }

  /// The build and raster of the first [count] frames. The engine stamps them on a clock of its
  /// own, so they are placed by their distance from the newest frame's raster end, taken as the
  /// moment the engine reports it: a few milliseconds early at most.
  static void recordFirstFrames(int count) {
    if (kIsWeb) {
      _recordFirstPageFrames(count);
      return;
    }
    var recorded = 0;
    late final TimingsCallback callback;
    callback = (List<FrameTiming> timings) {
      final now = nowMicros();
      final anchor = timings.last.timestampInMicroseconds(FramePhase.rasterFinish);
      for (final t in timings) {
        if (recorded >= count) break;
        recorded++;
        int at(FramePhase phase) => now - (anchor - t.timestampInMicroseconds(phase));
        _steps.add({'name': 'frame $recorded vsync to build start', 'depth': 1,
            'startUs': at(FramePhase.vsyncStart), 'endUs': at(FramePhase.buildStart)});
        _steps.add({'name': 'frame $recorded build', 'depth': 1,
            'startUs': at(FramePhase.buildStart), 'endUs': at(FramePhase.buildFinish)});
        _steps.add({'name': 'frame $recorded raster', 'depth': 1,
            'startUs': at(FramePhase.rasterStart), 'endUs': at(FramePhase.rasterFinish)});
      }
      if (recorded >= count) SchedulerBinding.instance.removeTimingsCallback(callback);
    };
    SchedulerBinding.instance.addTimingsCallback(callback);
  }

  /// The web engine reports frame timings in batches 100 ms apart, which a page that paints a
  /// few frames and goes idle never reaches. Each frame is marked when its draw ends instead (the
  /// raster may finish after that). Its start is not known: the first frame is a warm-up frame,
  /// with no animation frame of its own.
  static void _recordFirstPageFrames(int count) {
    var recorded = 0;
    SchedulerBinding.instance.addPersistentFrameCallback((_) {
      if (recorded >= count) return;
      mark('frame ${++recorded} drawn');
    });
  }

  /// Every step so far: on the web what the page loaded before `main()` ran, on the desktop what
  /// the native window did to start the engine (where the platform reports it).
  static Future<List<Map<String, Object>>> snapshot() async => [...pageSteps(), ...await _nativeSteps(), ..._steps];

  static Future<List<Map<String, Object>>> _nativeSteps() async {
    if (kIsWeb) return const [];
    try {
      final steps = await const MethodChannel('dev.equo.swt/window').invokeListMethod<Map>('bootTrace');
      return [for (final s in steps ?? const <Map>[]) {...s.cast<String, Object>(), 'source': 'native'}];
    } catch (_) {
      // A platform whose window does not time its start.
      return const [];
    }
  }
}
