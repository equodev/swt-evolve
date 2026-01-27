import 'package:flutter/material.dart';
import '../theme_extensions/slider_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';

SliderThemeExtension getSliderLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getSliderTheme(
    isDark: false,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

SliderThemeExtension getSliderDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getSliderTheme(
    isDark: true,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

SliderThemeExtension _getSliderTheme({
  required bool isDark,
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return SliderThemeExtension(
    // Track colors
    activeTrackColor: colorScheme.primary,
    inactiveTrackColor: colorScheme.outline,
    disabledActiveTrackColor: colorSchemeExtension.onSurfaceVariantDisabled,
    disabledInactiveTrackColor: colorScheme.outlineVariant,

    // Thumb colors
    thumbColor: colorScheme.primary,
    disabledThumbColor: colorSchemeExtension.onSurfaceVariantDisabled,

    // Overlay colors
    overlayColor: Colors.transparent,

    // Track sizing
    trackHeight: 4.0,
    trackBorderRadius: 2.0,

    // Thumb sizing
    thumbRadius: 10.0,

    // Default minimum sizes (used when no bounds provided)
    minWidth: 100.0,
    minHeight: 24.0,
    minVerticalWidth: 24.0,
    minVerticalHeight: 100.0,

    // Default slider values
    minimum: 0,
    maximum: 100,
    selection: 0,
    increment: 1,
    pageIncrement: 10,
    thumb: 10,
  );
}
