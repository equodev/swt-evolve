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
    textStyle: baseTextStyle,
    linkTextStyle: baseTextStyle,
    disabledTextStyle: baseTextStyle,

    // Spacing and padding
    // vertical: 2.0 adds 4px total (2 top + 2 bottom) to compensate for
    // font metrics difference between Flutter and Java/SWT
    padding: const EdgeInsets.symmetric(horizontal: 0.0, vertical: 2.0),

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
