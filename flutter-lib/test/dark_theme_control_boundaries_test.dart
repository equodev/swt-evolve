// On a dark theme a text field's border and a check box's or radio's outline read on their ground,
// so an empty field and an unselected box stay visible. The boundary is lifted only as far as it
// takes, so it keeps the theme's shade.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/impl/utils/widget_utils.dart';
import 'package:swtflutter/src/theme/named_theme.dart';
import 'package:swtflutter/src/theme/theme_extensions/button_theme_extension.dart';
import 'package:swtflutter/src/theme/theme_extensions/color_scheme_extension.dart';
import 'package:swtflutter/src/theme/theme_extensions/text_theme_extension.dart';

void main() {
  final dark = kStandardTheme.build(null, dark: true);
  final light = kStandardTheme.build(null, dark: false);

  test('a dark text field border reads on the field and on the surface', () {
    final text = dark.extension<TextThemeExtension>()!;
    expect(text.borderColor.a, 1.0);
    expect(contrastRatio(text.borderColor, text.backgroundColor),
        greaterThanOrEqualTo(kMinBoundaryContrast));
    expect(contrastRatio(text.borderColor, dark.colorScheme.surface),
        greaterThanOrEqualTo(kMinBoundaryContrast));
  });

  test('a dark check box and radio outline reads on the surface', () {
    final button = dark.extension<ButtonThemeExtension>()!;
    for (final border in [button.checkboxBorderColor, button.radioButtonBorderColor]) {
      expect(contrastRatio(border, dark.colorScheme.surface),
          greaterThanOrEqualTo(kMinBoundaryContrast));
    }
  });

  test('the light theme keeps its own borders', () {
    expect(light.extension<TextThemeExtension>()!.borderColor,
        light.extension<ColorSchemeExtension>()!.surfaceBorderEnabled);
    expect(light.extension<ButtonThemeExtension>()!.checkboxBorderColor, light.colorScheme.outline);
  });

  group('boundaryOn', () {
    const ground = Color(0xFF1E2023);

    test('keeps a colour that already reads', () {
      const bright = Color(0xFF9AA0A8);
      expect(boundaryOn(bright, ground), bright);
    });

    test('lifts a dim one just to the minimum, in its own hue', () {
      const dim = Color(0xFF3A3F46);
      final lifted = boundaryOn(dim, ground);
      expect(contrastRatio(lifted, ground), inInclusiveRange(kMinBoundaryContrast, 3.1));
      expect(HSLColor.fromColor(lifted).hue, closeTo(HSLColor.fromColor(dim).hue, 3.0));
    });
  });
}
