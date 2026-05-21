import 'package:flutter/material.dart';
import '../theme_extensions/tooltip_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';

TooltipThemeExtension getTooltipLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getTooltipTheme(isDark: false, colorScheme: colorScheme, textTheme: textTheme);
}

TooltipThemeExtension getTooltipDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getTooltipTheme(isDark: true, colorScheme: colorScheme, textTheme: textTheme);
}

TooltipThemeExtension _getTooltipTheme({
  required bool isDark,
  required ColorScheme colorScheme,
  required TextTheme textTheme,
}) {
  return TooltipThemeExtension(
    waitDuration: const Duration(milliseconds: 500),
    fadeInDuration: const Duration(milliseconds: 180),
    fadeOutDuration: const Duration(milliseconds: 280),
    slideOffsetY: 0.08,

    backgroundColor: colorScheme.surface,
    informationBackgroundColor: isDark ? const Color(0xFF1E3A5F) : const Color(0xFFE3F2FD),
    warningBackgroundColor: isDark ? const Color(0xFF4A3F1F) : const Color(0xFFFFF3E0),
    errorBackgroundColor: isDark ? const Color(0xFF4A1F1F) : const Color(0xFFFFEBEE),

    borderColor: colorScheme.outlineVariant,
    borderWidth: 1.0,
    borderRadius: 4.0,
    balloonBorderRadius: 12.0,

    textColor: colorScheme.onSurface,
    titleTextStyle: textTheme.bodyMedium?.copyWith(
      color: colorScheme.onSurface,
      fontSize: 14,
      fontWeight: FontWeight.bold,
    ),
    messageTextStyle: textTheme.bodySmall?.copyWith(
      color: colorScheme.onSurface,
      fontSize: 12,
    ),
    messageMaxLines: 10,
    titleMessageSpacing: 4.0,

    informationIconColor: isDark ? Colors.blue.shade300 : Colors.blue,
    warningIconColor: isDark ? Colors.orange.shade300 : Colors.orange,
    errorIconColor: isDark ? Colors.red.shade300 : Colors.red,
    iconSize: 24.0,
    iconSpacing: 12.0,

    padding: const EdgeInsets.all(12),
    minWidth: 100.0,
    maxWidth: 350.0,
    minHeight: 40.0,

    shadowColor: Colors.black.withOpacity(0.3),
    shadowBlurRadius: 10.0,
    shadowOffsetY: 3.0,
  );
}
