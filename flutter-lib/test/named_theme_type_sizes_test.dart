// A named theme can carry its own type sizes, so a product whose layouts were sized against
// native SWT gets type that fits the boxes it reserved -- without the product being patched
// widget by widget.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/theme/named_themes.dart';
import 'package:swtflutter/src/theme/theme.dart';

void main() {
  const scheme = ColorScheme.light();
  TextTheme shared() => createMaterialTextTheme(scheme);

  group('NamedTheme.typeScale', () {
    test('a theme that declares none leaves the shared scale alone', () {
      final base = shared();
      for (final name in ['nondefault', 'equo', 'green', 'hb']) {
        final theme = kNamedThemes[name]!;
        expect(theme.typeScale, isNull, reason: name);
        expect(theme.applyTypeSizes(base).bodyLarge!.fontSize,
            base.bodyLarge!.fontSize,
            reason: name);
      }
    });

    test('every existing theme is untouched by this, sizes included', () {
      // Guards the default: adding the hook must not have moved anyone's type.
      final base = shared();
      expect(base.bodyLarge!.fontSize, 14.0);
      expect(base.bodySmall!.fontSize, 12.0);
      expect(base.titleMedium!.fontSize, 16.0);
    });
  });

  group("the 'compact' theme", () {
    test('is registered and declares its own sizes', () {
      final compact = kNamedThemes['compact'];
      expect(compact, isNotNull);
      expect(compact!.typeScale, isNotNull);
      // Named, not anonymous: the name is what the Java mirror is keyed by, and what lets
      // themes share one measurement.
      expect(compact.typeScale!.name, 'Compact');
    });

    test('keeps the shared palette -- only the type differs', () {
      final compact = kNamedThemes['compact']!;
      final plain = kNamedThemes['nondefault']!;
      expect(compact.lightColorScheme.primary, plain.lightColorScheme.primary);
      expect(compact.lightColorScheme.surface, plain.lightColorScheme.surface);
      expect(compact.darkColorScheme!.primary, plain.darkColorScheme!.primary);
    });

    test('draws every role smaller than the shared scale', () {
      final base = shared();
      final small = kNamedThemes['compact']!.applyTypeSizes(base);
      for (final role in <String>[
        'displayLarge',
        'headlineLarge',
        'titleLarge',
        'titleMedium',
        'titleSmall',
        'labelMedium',
        'labelSmall',
        'bodyLarge',
        'bodyMedium',
        'bodySmall',
      ]) {
        final b = _size(base, role), s = _size(small, role);
        expect(s, lessThan(b), reason: '$role: shared $b, compact $s');
      }
    });

    test('leaves the roles legible', () {
      final small = kNamedThemes['compact']!.applyTypeSizes(shared());
      expect(small.bodyLarge!.fontSize, greaterThanOrEqualTo(9.0));
      expect(small.labelSmall!.fontSize, greaterThanOrEqualTo(8.0));
    });

    test('keeps the body roles inside what a fixed 180px page list leaves', () {
      // JFace pins that tree to 180px; the row's own padding and reserved expander take ~54px,
      // so the label gets ~126px. A tree item draws in bodyLarge.
      const paneWidth = 180.0, rowChrome = 54.0;
      final size = kNamedThemes['compact']!
          .applyTypeSizes(shared())
          .bodyLarge!
          .fontSize!;
      // Measured on the real face: the longest page name of the reported product needs 172.9px
      // at 14px, and scales with the size.
      const widestAt14 = 172.9;
      final needed = widestAt14 * size / 14.0;
      expect(needed, lessThanOrEqualTo(paneWidth - rowChrome),
          reason: 'at ${size}px the label needs ${needed.toStringAsFixed(1)}px '
              'of the ${paneWidth - rowChrome}px the row leaves');
    });
  });
}

double _size(TextTheme t, String role) => switch (role) {
      'displayLarge' => t.displayLarge!.fontSize!,
      'headlineLarge' => t.headlineLarge!.fontSize!,
      'titleLarge' => t.titleLarge!.fontSize!,
      'titleMedium' => t.titleMedium!.fontSize!,
      'titleSmall' => t.titleSmall!.fontSize!,
      'labelMedium' => t.labelMedium!.fontSize!,
      'labelSmall' => t.labelSmall!.fontSize!,
      'bodyLarge' => t.bodyLarge!.fontSize!,
      'bodyMedium' => t.bodyMedium!.fontSize!,
      'bodySmall' => t.bodySmall!.fontSize!,
      _ => throw ArgumentError(role),
    };
