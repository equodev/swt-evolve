import 'package:flutter/gestures.dart' show kDoubleTapTimeout;
import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/utils/double_tap_detector.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

void main() {
  tearDown(resetConfigFlags);

  group('unconfigured (production)', () {
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

  group('configured window (swt.evolve.double_click_timeout_ms)', () {
    test('a 400ms gap -- past the default -- pairs once the flags widen the window', () async {
      final detector = DoubleTapDetector();
      applyConfigFlags(ConfigFlags()..double_click_timeout_ms = 700);

      expect(detector.registerTap(), 1);
      await Future<void>.delayed(const Duration(milliseconds: 400));
      expect(detector.registerTap(), 2);
    });

    test('the window applies to detectors built before the flags arrived', () async {
      final detector = DoubleTapDetector();

      expect(detector.timeout, kDoubleTapTimeout);
      applyConfigFlags(ConfigFlags()..double_click_timeout_ms = 700);
      expect(detector.timeout, const Duration(milliseconds: 700));
    });

    test('an explicit per-widget timeout always wins', () async {
      applyConfigFlags(ConfigFlags()..double_click_timeout_ms = 700);
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
