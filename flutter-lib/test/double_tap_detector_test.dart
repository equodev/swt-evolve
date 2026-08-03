import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/impl/utils/double_tap_detector.dart';

void main() {
  tearDown(() {
    e2eTestMode = false;
  });

  group('production default (e2eTestMode off)', () {
    test('two taps within 300ms register as a double-click', () async {
      final detector = DoubleTapDetector();

      expect(detector.registerTap(), 1);
      await Future<void>.delayed(const Duration(milliseconds: 150));
      expect(detector.registerTap(), 2);
    });

    test('two taps past 300ms register as separate single taps', () async {
      final detector = DoubleTapDetector();

      expect(detector.registerTap(), 1);
      await Future<void>.delayed(const Duration(milliseconds: 400));
      expect(detector.registerTap(), 1);
    });
  });

  group('E2E test mode', () {
    test('a 400ms gap -- past prod, within the CI-worst-case margin -- still registers as a double-click', () async {
      e2eTestMode = true;
      final detector = DoubleTapDetector();

      expect(detector.registerTap(), 1);
      await Future<void>.delayed(const Duration(milliseconds: 400));
      expect(detector.registerTap(), 2);
    });

    test('an explicit timeout always wins over e2eTestMode', () async {
      e2eTestMode = true;
      final detector = DoubleTapDetector(timeout: const Duration(milliseconds: 100));

      expect(detector.registerTap(), 1);
      await Future<void>.delayed(const Duration(milliseconds: 200));
      expect(detector.registerTap(), 1);
    });
  });

  test('three taps within the window register a triple-click, then reset', () async {
    final detector = DoubleTapDetector();

    expect(detector.registerTap(), 1);
    await Future<void>.delayed(const Duration(milliseconds: 100));
    expect(detector.registerTap(), 2);
    await Future<void>.delayed(const Duration(milliseconds: 100));
    expect(detector.registerTap(), 3);

    expect(detector.registerTap(), 1);
  });

  test('a tap on a different key never pairs with the previous one', () async {
    final detector = DoubleTapDetector();

    expect(detector.registerTap(key: 'row-0'), 1);
    await Future<void>.delayed(const Duration(milliseconds: 50));
    expect(detector.registerTap(key: 'row-1'), 1);
  });

  test('a tap outside the slop radius never pairs with the previous one', () async {
    final detector = DoubleTapDetector();

    expect(detector.registerTap(position: const Offset(0, 0)), 1);
    await Future<void>.delayed(const Duration(milliseconds: 50));
    expect(detector.registerTap(position: const Offset(50, 50)), 1);
  });
}
