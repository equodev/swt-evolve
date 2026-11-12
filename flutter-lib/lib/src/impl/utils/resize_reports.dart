import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/scheduler.dart';

/// When a window being resized tells Java its size: the first change at once, then at most one
/// every [every] while changes keep coming, and the size it ends at once they stop. Java lays the
/// window out while it is dragged, as a native one is, without a relayout for every frame of the
/// drag queueing up behind the ones before it.
///
/// A client slow to apply a layout hears of the drag less often: a window lasts at least
/// [slowFrameShare] times what the drag's slowest frame took beyond its fastest one, so applying
/// new layouts takes at most that share of the drag's frames, not all of them. Frames that are
/// all slow, whether or not a layout arrives, do not slow the reports.
class ResizeReports {
  ResizeReports(this._report, {this.every = const Duration(milliseconds: 50)});

  /// Reports paced by the frames this client draws while the window is resized.
  factory ResizeReports.pacedByFrames(void Function() report) {
    final reports = ResizeReports(report);
    final binding = SchedulerBinding.instance;
    final clock = Stopwatch()..start();
    var start = -1;
    void frameStarts(Duration _) => start = clock.elapsedMicroseconds;
    // Runs after the rendering pipeline's own persistent callback, registered at binding init.
    binding.addPersistentFrameCallback((_) {
      if (start >= 0) {
        reports.frameTook(Duration(microseconds: clock.elapsedMicroseconds - start));
        start = -1;
      }
      if (reports.resizing) binding.scheduleFrameCallback(frameStarts);
    });
    reports._reported = () => binding.scheduleFrameCallback(frameStarts);
    return reports;
  }

  static const slowFrameShare = 3;

  /// Whether the window is being dragged: true from its first report until a window of reports ends
  /// with no change.
  static final ValueNotifier<bool> windowDragged = ValueNotifier(false);

  final void Function() _report;
  final Duration every;
  Timer? _window;
  bool _changedSince = false;
  Duration _slowestFrame = Duration.zero;
  Duration? _fastestFrame;
  void Function()? _reported;

  /// Whether a report went out and its window has not yet ended without a change.
  bool get resizing => _window != null;

  /// The window's size changed.
  void changed() {
    if (_window != null) {
      _changedSince = true;
      return;
    }
    _report();
    windowDragged.value = true;
    final paced = (_slowestFrame - (_fastestFrame ?? _slowestFrame)) * slowFrameShare;
    _window = Timer(paced > every ? paced : every, _windowEnds);
    _reported?.call();
  }

  /// The client took [cost] to draw a frame.
  void frameTook(Duration cost) {
    if (!resizing) return;
    if (cost > _slowestFrame) _slowestFrame = cost;
    if (_fastestFrame == null || cost < _fastestFrame!) _fastestFrame = cost;
  }

  void _windowEnds() {
    _window = null;
    if (!_changedSince) {
      _slowestFrame = Duration.zero;
      _fastestFrame = null;
      windowDragged.value = false;
      return;
    }
    _changedSince = false;
    changed();
  }
}
