import 'dart:developer' as developer;
import 'dart:io' show Platform;

/// Off unless the application runs with `EQUO_SWT_PERF=true` in its environment (and
/// `EQUO_SWT_PERF_DETAIL=true` for the per-build marks). Then the marks go to the Dart VM timeline,
/// next to the engine's own frame and raster events, for a profile build to be read over the VM
/// service. The web build sends them to the browser's profiler instead (perf_marks_web.dart).
final bool _perf = _readFlag('EQUO_SWT_PERF');
final bool _perfDetail = _perf && _readFlag('EQUO_SWT_PERF_DETAIL');

bool _readFlag(String name) {
  try {
    return Platform.environment[name] == 'true';
  } catch (_) {
    // An environment that cannot be read did not ask for this.
    return false;
  }
}

bool get perfMarksEnabled => _perf;

bool get perfDetailEnabled => _perfDetail;

void perfMark(String name, void Function() body) {
  if (!_perf) return body();
  developer.Timeline.timeSync(name, body);
}

void perfCount(String name) {
  if (_perf) developer.Timeline.instantSync(name);
}

void perfMarkDetail(String name, void Function() body) {
  if (!_perfDetail) return body();
  developer.Timeline.timeSync(name, body);
}

/// The timeline takes no past timestamps, so a span measured elsewhere is an instant event carrying
/// its start and end (ms on [perfNow]'s clock).
void perfSpan(String name, double startMs, double endMs) {
  if (_perf) developer.Timeline.instantSync(name, arguments: {'startMs': startMs, 'endMs': endMs});
}

int _sampled = 0;

void perfSpanSampled(String name, double startMs, double endMs, {int every = 20}) {
  if (_perf && _sampled++ % every == 0) perfSpan(name, startMs, endMs);
}

/// Milliseconds on the timeline's clock.
double perfNow() => _perf ? developer.Timeline.now / 1000.0 : 0;

T perfMarkValue<T>(String name, T Function() body) {
  if (!_perf) return body();
  return developer.Timeline.timeSync(name, body);
}

T perfMarkValueDetail<T>(String name, T Function() body) {
  if (!_perfDetail) return body();
  return developer.Timeline.timeSync(name, body);
}
