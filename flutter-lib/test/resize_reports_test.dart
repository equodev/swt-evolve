// A window being dragged to a new size tells Java as it goes, not only once it stops, and never
// faster than Java can lay it out; the size it ends at always reaches Java.

import 'package:fake_async/fake_async.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/utils/resize_reports.dart';

void main() {
  test('the first change is reported at once', () {
    fakeAsync((async) {
      var reports = 0;
      ResizeReports(() => reports++).changed();
      expect(reports, 1);
    });
  });

  test('a drag is reported once per window while it lasts, and where it stops', () {
    fakeAsync((async) {
      var reports = 0;
      final r = ResizeReports(() => reports++, every: const Duration(milliseconds: 50));
      // A frame's worth of change every 8 ms for 200 ms.
      for (var t = 0; t < 25; t++) {
        if (t > 0) async.elapse(const Duration(milliseconds: 8));
        r.changed();
      }
      expect(reports, 4, reason: 'one a window, not one a frame');
      final during = reports;
      async.elapse(const Duration(milliseconds: 100));
      expect(reports, during + 1, reason: 'the size it ends at');
      async.elapse(const Duration(seconds: 1));
      expect(reports, during + 1, reason: 'nothing once it is still');
    });
  });

  test('a client slow to apply a layout hears of the drag less often, until the drag ends', () {
    fakeAsync((async) {
      var reports = 0;
      final r = ResizeReports(() => reports++, every: const Duration(milliseconds: 50));
      r.changed();
      r.frameTook(const Duration(milliseconds: 16));
      r.frameTook(const Duration(milliseconds: 96));
      for (var t = 1; t < 60; t++) {
        async.elapse(const Duration(milliseconds: 8));
        r.changed();
      }
      // 480 ms of drag: the first window of 50 ms, then windows of 240 ms.
      expect(reports, 3);
      async.elapse(const Duration(seconds: 1));
      expect(reports, 4, reason: 'the size it ends at');

      r.changed();
      async.elapse(const Duration(milliseconds: 56));
      r.changed();
      expect(reports, 6, reason: 'a new drag starts at the fast pace');
    });
  });

  test('frames that are all slow do not slow the reports', () {
    fakeAsync((async) {
      var reports = 0;
      final r = ResizeReports(() => reports++, every: const Duration(milliseconds: 50));
      r.changed();
      for (var t = 1; t < 25; t++) {
        async.elapse(const Duration(milliseconds: 8));
        r.frameTook(const Duration(milliseconds: 35));
        r.changed();
      }
      expect(reports, 4, reason: 'one every 50 ms, as for a fast client');
    });
  });
}
