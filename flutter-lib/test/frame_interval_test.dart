// The display's frame interval is measured from back-to-back frames, since the web engine reports
// 60 Hz whatever the display runs at: moves to Java are paced by it.

import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/utils/frame_interval.dart';

void main() {
  setUp(FrameInterval.reset);

  Future<void> frames(WidgetTester tester, List<int> gapsMs) async {
    for (final gap in gapsMs) {
      tester.binding.scheduleFrame();
      await tester.pump(Duration(milliseconds: gap));
    }
  }

  testWidgets('60 Hz is assumed until frames have been seen back to back', (tester) async {
    await tester.pumpWidget(const SizedBox());
    FrameInterval.reset();
    expect(FrameInterval.micros, FrameInterval.fallbackMicros);
  });

  testWidgets('a 120 Hz display is measured as one', (tester) async {
    await tester.pumpWidget(const SizedBox());
    FrameInterval.ensureTracking();
    await frames(tester, [8, 8, 8, 8]);
    expect(FrameInterval.micros, 8000);
  });

  testWidgets('a skipped frame or an idle gap does not lengthen the interval', (tester) async {
    await tester.pumpWidget(const SizedBox());
    FrameInterval.ensureTracking();
    await frames(tester, [8, 8, 16, 300, 8]);
    expect(FrameInterval.micros, 8000);
  });
}
