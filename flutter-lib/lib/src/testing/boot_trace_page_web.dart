import 'dart:js_interop';

import 'package:web/web.dart' as web;

/// What the browser did before `main()`: the navigation and every resource the page fetched, from
/// the browser's own timing entries, in microseconds since the epoch.
List<Map<String, Object>> pageSteps() {
  final performance = web.window.performance;
  final origin = performance.timeOrigin;
  int epoch(num relativeMillis) => ((origin + relativeMillis) * 1000).round();
  final steps = <Map<String, Object>>[];

  final navigation = performance.getEntriesByType('navigation').toDart;
  if (navigation.isNotEmpty) {
    final nav = navigation.first as web.PerformanceNavigationTiming;
    steps.add({'name': 'page.navigation', 'depth': 0, 'startUs': epoch(0), 'endUs': epoch(nav.responseEnd)});
    steps.add({
      'name': 'page.domContentLoaded', 'depth': 0,
      'startUs': epoch(nav.domContentLoadedEventStart), 'endUs': epoch(nav.domContentLoadedEventEnd),
    });
    if (nav.loadEventEnd > 0) {
      steps.add({'name': 'page.load', 'depth': 0, 'startUs': epoch(nav.loadEventStart), 'endUs': epoch(nav.loadEventEnd)});
    }
  }
  for (final entry in performance.getEntriesByType('mark').toDart) {
    if (!entry.name.startsWith('evolve.')) continue;
    final at = epoch(entry.startTime);
    steps.add({'name': 'page.${entry.name.substring('evolve.'.length)}', 'depth': 0, 'startUs': at, 'endUs': at});
  }
  for (final entry in performance.getEntriesByType('resource').toDart) {
    final resource = entry as web.PerformanceResourceTiming;
    final path = Uri.tryParse(resource.name)?.path ?? resource.name;
    steps.add({
      'name': 'page.fetch $path (${resource.encodedBodySize} B${resource.transferSize == 0 ? ', cached' : ''})',
      'depth': 1,
      'startUs': epoch(resource.startTime),
      'endUs': epoch(resource.responseEnd),
    });
  }
  return steps;
}
