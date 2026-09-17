import 'package:flutter/material.dart';
import '../theme_extensions/toast_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';

ToastThemeExtension getToastLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getToastTheme(
    isDark: false,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

ToastThemeExtension getToastDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getToastTheme(
    isDark: true,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

ToastThemeExtension _getToastTheme({
  required bool isDark,
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return ToastThemeExtension(
    backgroundColor: colorScheme.surface,
    borderColor: colorScheme.outlineVariant,
    borderWidth: 1.0,
    borderRadius: 6.0,

    titleTextStyle: textTheme.bodyMedium?.copyWith(
      color: colorScheme.onSurface,
      fontSize: 13,
      fontWeight: FontWeight.w600,
    ),
    messageTextStyle: textTheme.bodySmall?.copyWith(
      color: colorScheme.onSurfaceVariant,
      fontSize: 12,
      height: 1.35,
    ),
    messageMaxLines: 4,
    titleMessageSpacing: 4.0,

    infoColor: colorScheme.primary,
    // The palette carries no success colour, so this is the one severity with nowhere to read it
    // from. Replace it the moment a token exists.
    successColor: isDark ? Colors.green.shade300 : Colors.green.shade600,
    warningColor: colorSchemeExtension.warning,
    errorColor: colorScheme.error,
    accentBarWidth: 3.0,

    iconSize: 18.0,
    iconSpacing: 10.0,
    closeIconColor: colorScheme.onSurfaceVariant,
    closeIconSize: 16.0,

    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
    width: 320.0,
    margin: const EdgeInsets.all(16),
    spacing: 8.0,
    maxVisible: 4,

    slideInDuration: const Duration(milliseconds: 220),
    fadeOutDuration: const Duration(milliseconds: 180),
    displayDuration: const Duration(seconds: 4),
    slideOffsetX: 0.25,

    shadowColor: colorScheme.shadow.withOpacity(isDark ? 0.5 : 0.22),
    shadowBlurRadius: 14.0,
    shadowOffsetY: 4.0,
  );
}
