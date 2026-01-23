import 'package:flutter/material.dart';
import '../theme_extensions/link_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';

LinkThemeExtension getLinkLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getLinkTheme(
    isDark: false,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

LinkThemeExtension getLinkDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getLinkTheme(
    isDark: true,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

LinkThemeExtension _getLinkTheme({
  required bool isDark,
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  final baseTextStyle = textTheme.bodyMedium ?? const TextStyle();

  return LinkThemeExtension(
    // Text colors
    textColor: colorScheme.onSurface,
    linkTextColor: colorScheme.primary,
    linkHoverTextColor: colorSchemeExtension.primaryHovered,
    disabledTextColor: colorSchemeExtension.onSurfaceVariantDisabled,

    // Background colors
    backgroundColor: null,
    hoverBackgroundColor: null,

    // Font styles
    textStyle: baseTextStyle.copyWith(
      color: colorScheme.onSurface,
      height: 1.0,
    ),
    linkTextStyle: baseTextStyle.copyWith(
      color: colorScheme.primary,
      height: 1.0,
    ),
    disabledTextStyle: baseTextStyle.copyWith(
      color: colorSchemeExtension.onSurfaceVariantDisabled,
      height: 1.0,
    ),

    // Spacing and padding
    padding: const EdgeInsets.symmetric(horizontal: 12.0),
    minHeight: 32.0,

    // Interactive properties
    disabledOpacity: 0.6,
    hoverAnimationDuration: const Duration(milliseconds: 150),

    // Decoration properties
    linkDecoration: TextDecoration.none,
    linkHoverDecoration: TextDecoration.underline,

    // Alignment
    textAlign: TextAlign.start,
  );
}
