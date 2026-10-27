// The echo must tolerate being off, out-of-order answers, and answers for unstamped pings.

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/utils/round_trip_timing.dart';
import 'package:swtflutter/src/impl/utils/perf_marks.dart';

void main() {
  test('tracing is silent when the page did not ask for marks', () {
    expect(perfMarksEnabled, isFalse, reason: 'marks are off outside the web build');
    // A production page pays nothing: no send, no throw.
    RoundTripTiming.trace();
    RoundTripTiming.trace();
  });

  test('perfSpan ignores a span that ends before it starts', () {
    // What an echo for an unstamped ping would produce.
    perfSpan('RoundTrip.total', 10, 5);
  });
}
