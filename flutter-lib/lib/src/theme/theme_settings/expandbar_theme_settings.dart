import 'package:flutter/material.dart';
import '../theme_extensions/expandbar_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';

ExpandBarThemeExtension getExpandBarLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getExpandBarTheme(
    isDark: false,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

ExpandBarThemeExtension getExpandBarDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getExpandBarTheme(
    isDark: true,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

ExpandBarThemeExtension _getExpandBarTheme({
  required bool isDark,
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return ExpandBarThemeExtension(
    // Colors
    backgroundColor: colorScheme.surface,
    borderColor: colorScheme.outline,
    foregroundColor: colorScheme.onSurface,
    foregroundExpandedColor: colorScheme.onPrimary,
    headerBackgroundColor: colorScheme.surfaceContainerHigh,
    headerBackgroundExpandedColor: colorScheme.onPrimary,
    headerBackgroundHoveredColor: colorScheme.surfaceContainerLow,
    contentBackgroundColor: colorScheme.surface,
    iconColor: colorScheme.onSurfaceVariant,
    iconExpandedColor: colorScheme.onSurfaceVariant,
    disabledForegroundColor: colorSchemeExtension.onSurfaceVariantDisabled,
    disabledBackgroundColor: colorScheme.surfaceVariant,

    // Border
    borderWidth: 1.0,
    borderRadius: 4.0,
    itemBorderRadius: 4.0,

    // Padding
    headerPadding: const EdgeInsets.symmetric(horizontal: 12.0, vertical: 8.0),
    contentPadding: const EdgeInsets.symmetric(horizontal: 12.0, vertical: 8.0),
    containerPadding: EdgeInsets.zero,

    // Spacing
    itemSpacing: 2.0,

    // Typography
    headerTextStyle: textTheme.bodyMedium,

    // Animation
    animationDuration: const Duration(milliseconds: 200),
    animationCurve: Curves.easeInOut,

    // Icon
    iconSize: 24.0,
  );
}
