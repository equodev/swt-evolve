import 'package:flutter/material.dart';
import '../theme_extensions/styledtext_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';

StyledTextThemeExtension getStyledTextLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getStyledTextTheme(
    isDark: false,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

StyledTextThemeExtension getStyledTextDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getStyledTextTheme(
    isDark: true,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

StyledTextThemeExtension _getStyledTextTheme({
  required bool isDark,
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return StyledTextThemeExtension(
    backgroundColor: isDark ? const Color(0xFF1E1E1E) : const Color(0xFFF2F4F7),
    foregroundColor: isDark ? Colors.white : colorScheme.onSurface,
  );
}
