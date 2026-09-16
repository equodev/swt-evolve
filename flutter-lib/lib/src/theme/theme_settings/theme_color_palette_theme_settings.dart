import 'package:flutter/material.dart';

import '../theme_extensions/theme_color_palette_theme_extension.dart';

const String _kDefaultSampleHexCsv =
    '1847CA,F2F22E,22C55E,EF4444,8B5CF6,EC4899,06B6D4,F97316,84CC16,6366F1,14B8A6,78716C';

ThemeColorPaletteThemeExtension getThemeColorPaletteLightTheme({
  required ColorScheme colorScheme,
}) {
  return _buildThemeColorPaletteTheme(colorScheme: colorScheme);
}

ThemeColorPaletteThemeExtension getThemeColorPaletteDarkTheme({
  required ColorScheme colorScheme,
}) {
  return _buildThemeColorPaletteTheme(colorScheme: colorScheme);
}

ThemeColorPaletteThemeExtension _buildThemeColorPaletteTheme({
  required ColorScheme colorScheme,
}) {
  return ThemeColorPaletteThemeExtension(
    swatchSize: 18.0,
    swatchBorderRadius: 4.0,
    swatchSpacing: 2.0,
    trailingGapAfterSwatches: 4.0,
    chevronPadding: const EdgeInsets.symmetric(horizontal: 4, vertical: 2),
    chevronIconSize: 18.0,
    swatchBorderColor: colorScheme.outlineVariant,
    chevronIconColor: colorScheme.onSurfaceVariant,
    expandTooltip: 'Change global theme seed color',
    collapseTooltip: 'Collapse theme seed palette',
    sampleHexCsv: _kDefaultSampleHexCsv,
  );
}

/// A swatch split between a mode's surface and its primary, or a flat [color] when no surface is given.
BoxDecoration getThemePaletteSwatchDecoration(
  ThemeColorPaletteThemeExtension palette, {
  required Color color,
  Color? background,
}) =>
    BoxDecoration(
      color: background == null ? color : null,
      gradient: background == null
          ? null
          : LinearGradient(
              colors: [background, background, color, color],
              stops: const [0, 0.5, 0.5, 1],
            ),
      borderRadius: BorderRadius.circular(palette.swatchBorderRadius),
      border: Border.all(color: palette.swatchBorderColor),
    );
