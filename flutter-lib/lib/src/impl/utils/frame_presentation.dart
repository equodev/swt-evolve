import 'dart:async';
import 'dart:math' as math;

import 'package:flutter/foundation.dart' show kIsWasm, visibleForTesting;
import 'package:flutter/scheduler.dart';

import 'frame_presentation_stub.dart' if (dart.library.js_interop) 'frame_presentation_web.dart' as page;

/// Whether the frames rendered so far have reached the screen.
///
/// On the wasm renderer a frame sizes the offscreen surface to the view, waits for that, then
/// rasterises into it; a pixel read-back sizes the same surface to its image. A read-back that
/// starts in between leaves the frame rasterised at the image's size, and the whole view blank
/// until the next one. A frame that starts during a read-back only costs the read-back, which
/// [ImageUtils.toBytes] does again. So frames never wait: a read-back waits for [idle].
class FramePresentation {
  FramePresentation._();

  /// How long a read-back waits for a frame that never shows (one the engine skips drawing).
  static const Duration timeout = Duration(milliseconds: 100);

  /// The presentation count at which every frame rendered so far has been shown.
  static int _shownAt = 0;
  static bool _tracking = false;

  static int? Function() _presentedFrames = page.presentedFrames;
  static Future<void> Function() _nextPresentedFrame = page.nextPresentedFrame;

  /// Starts counting rendered frames; a no-op where read-backs have a surface of their own.
  static void ensureTracking() {
    if (_tracking || !kIsWasm || _presentedFrames() == null) return;
    _track();
  }

  static bool _registered = false;

  static void _track() {
    _tracking = true;
    if (_registered) return;
    _registered = true;
    // Persistent callbacks run in the order registered; the rendering pipeline's comes first, so
    // this runs once the frame has been handed to the engine.
    SchedulerBinding.instance.addPersistentFrameCallback((_) {
      if (_tracking) _shownAt = math.max(_shownAt, _presentedFrames() ?? 0) + 1;
    });
  }

  /// Tracks against [presentedFrames] and [nextPresentedFrame] in place of the page's counter.
  @visibleForTesting
  static void debugTrackWith(int? Function() presentedFrames, Future<void> Function() nextPresentedFrame) {
    _presentedFrames = presentedFrames;
    _nextPresentedFrame = nextPresentedFrame;
    _shownAt = 0;
    _track();
  }

  /// The presentation count every frame rendered so far is shown at.
  @visibleForTesting
  static int get debugShownAt => _shownAt;

  @visibleForTesting
  static void debugReset() {
    _presentedFrames = page.presentedFrames;
    _nextPresentedFrame = page.nextPresentedFrame;
    _shownAt = 0;
    _tracking = false;
  }

  /// Completes once no frame is between being rendered and being shown.
  static Future<void> idle() async {
    if (!_tracking) return;
    final deadline = DateTime.now().add(timeout);
    while ((_presentedFrames() ?? _shownAt) < _shownAt) {
      final left = deadline.difference(DateTime.now());
      if (left <= Duration.zero) {
        _shownAt = _presentedFrames() ?? _shownAt;
        return;
      }
      await _nextPresentedFrame().timeout(left, onTimeout: () {});
    }
  }
}
