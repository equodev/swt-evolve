import 'dart:js_interop';
import 'dart:js_interop_unsafe';

import 'package:web/web.dart' as web;

/// User Timing marks, which survive minification. Off unless the page asks
/// (`window.evolve.perf = true` or `?perf=true`): they are not free.
bool _enabled = _readFlag('perf');

bool _readFlag(String name) {
  try {
    if (Uri.base.queryParameters[name] == 'true') return true;
    final evolve = web.window.getProperty<JSAny?>('evolve'.toJS);
    if (evolve.isA<JSObject>()) {
      final flag = (evolve as JSObject).getProperty<JSAny?>(name.toJS);
      if (flag.isA<JSBoolean>()) return (flag as JSBoolean).toDart;
    }
  } catch (_) {
    // A page that does not let us look is a page that did not ask for this.
  }
  return false;
}

bool get perfMarksEnabled => _enabled;

/// The per-build and per-state-change marks; enabled separately because at their frequency they
/// distort the profile.
bool _detail = _readFlag('perfDetail');

bool get perfDetailEnabled => _enabled && _detail;

int _seq = 0;

void perfMark(String name, void Function() body) {
  if (!_enabled) {
    body();
    return;
  }
  final start = 'evolve-$name-${_seq++}';
  web.window.performance.mark(start);
  try {
    body();
  } finally {
    web.window.performance.measure(name, start.toJS);
    web.window.performance.clearMarks(start);
  }
}

/// Records that [name] happened once, without timing it.
void perfCount(String name) {
  if (!_enabled || !_detail) return;
  web.window.performance.mark('count:$name');
}

/// Records a span timed elsewhere; [startMs] and [endMs] are `performance.now()` values.
void perfSpan(String name, double startMs, double endMs) {
  if (!_enabled || endMs < startMs) return;
  final start = 'evolve-$name-${_seq++}';
  web.window.performance.mark(start,
      web.PerformanceMarkOptions(startTime: startMs));
  web.window.performance.measure(
      name, web.PerformanceMeasureOptions(start: start.toJS, end: endMs.toJS));
  web.window.performance.clearMarks(start);
}

int _sampled = 0;

/// As [perfSpan], recording one in [every]: marking every call costs more than it measures.
void perfSpanSampled(String name, double startMs, double endMs, {int every = 20}) {
  if (!_enabled) return;
  if (_sampled++ % every != 0) return;
  perfSpan(name, startMs, endMs);
}

/// `performance.now()`, for timing a span the caller closes later.
double perfNow() => web.window.performance.now();

/// As [perfMark], for a pass frequent enough that marking it distorts the profile.
void perfMarkDetail(String name, void Function() body) {
  if (!_enabled || !_detail) {
    body();
    return;
  }
  perfMark(name, body);
}

/// As [perfMarkValue], for a pass frequent enough that marking it distorts the profile.
T perfMarkValueDetail<T>(String name, T Function() body) {
  if (!_enabled || !_detail) return body();
  return perfMarkValue(name, body);
}

T perfMarkValue<T>(String name, T Function() body) {
  if (!_enabled) return body();
  final start = 'evolve-$name-${_seq++}';
  web.window.performance.mark(start);
  try {
    return body();
  } finally {
    web.window.performance.measure(name, start.toJS);
    web.window.performance.clearMarks(start);
  }
}
