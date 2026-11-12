import 'dart:typed_data';

import 'package:flutter/foundation.dart';
import 'package:flutter/scheduler.dart';

/// How often the display shows a frame, measured from the frames themselves: the web engine
/// reports 60 Hz whatever the display runs at.
class FrameInterval {
  FrameInterval._();

  /// What is assumed until two frames have been seen back to back.
  static const int fallbackMicros = 1000000 ~/ 60;

  static final Int32List _recent = Int32List(8);
  static int _next = 0;
  static Duration? _last;
  static bool _tracking = false;

  /// The shortest of the recent intervals between back-to-back frames: a frame the engine skips
  /// makes an interval a multiple of the display's, never shorter.
  static int get micros {
    ensureTracking();
    var shortest = 0;
    for (final interval in _recent) {
      if (interval > 0 && (shortest == 0 || interval < shortest)) shortest = interval;
    }
    return shortest == 0 ? fallbackMicros : shortest;
  }

  static void ensureTracking() {
    if (_tracking) return;
    _tracking = true;
    SchedulerBinding.instance.addPersistentFrameCallback(_frame);
  }

  static void _frame(Duration timeStamp) {
    final last = _last;
    _last = timeStamp;
    if (last == null) return;
    final interval = (timeStamp - last).inMicroseconds;
    // Further apart than a 20 Hz display's: there was no frame to draw in between.
    if (interval < 2000 || interval > 50000) return;
    _recent[_next] = interval;
    _next = (_next + 1) % _recent.length;
  }

  @visibleForTesting
  static void reset() {
    _recent.fillRange(0, _recent.length, 0);
    _next = 0;
    _last = null;
  }
}
