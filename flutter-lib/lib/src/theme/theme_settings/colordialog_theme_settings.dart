import 'package:flutter/material.dart';
import '../theme_extensions/colordialog_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';

ColorDialogThemeExtension getColorDialogLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getColorDialogTheme(
    isDark: false,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

ColorDialogThemeExtension getColorDialogDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getColorDialogTheme(
    isDark: true,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

ColorDialogThemeExtension _getColorDialogTheme({
  required bool isDark,
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return ColorDialogThemeExtension(
    titleStyle: (textTheme.titleMedium ?? const TextStyle()).copyWith(
      color: colorScheme.onSurface,
      fontWeight: FontWeight.w600,
    ),
    labelStyle: (textTheme.bodyMedium ?? const TextStyle()).copyWith(
      color: colorScheme.onSurfaceVariant,
    ),
    backgroundColor: colorScheme.surface,
    borderRadius: 12.0,
    maxWidth: 420.0,
    padding: const EdgeInsets.fromLTRB(24, 24, 24, 20),
    titleContentSpacing: 12.0,
    sectionSpacing: 12.0,
    contentButtonsSpacing: 20.0,
    buttonSpacing: 8.0,
    saturationValueHeight: 160.0,
    thumbRadius: 6.0,
    thumbOutlineRadius: 8.0,
    thumbStrokeWidth: 2.0,
    // The thumbs ride on top of arbitrary user colour, so they stay a fixed
    // white-on-dark pair in both themes rather than following the scheme.
    thumbColor: Colors.white,
    thumbOutlineColor: Colors.black54,
    hueSliderHeight: 18.0,
    hueThumbWidth: 4.0,
    hueThumbStrokeWidth: 2.0,
    hueThumbColor: Colors.white,
    swatchSize: 20.0,
    swatchSpacing: 4.0,
    swatchBorderRadius: 3.0,
    swatchBorderWidth: 1.0,
    swatchBorderColor: colorSchemeExtension.surfaceBorderEnabled,
    selectedSwatchBorderWidth: 2.0,
    selectedSwatchBorderColor: colorScheme.primary,
    previewWidth: 44.0,
    previewHeight: 28.0,
    previewBorderRadius: 3.0,
    previewBorderColor: colorSchemeExtension.surfaceBorderEnabled,
    previewSpacing: 10.0,
    hexFieldWidth: 90.0,
  );
}
