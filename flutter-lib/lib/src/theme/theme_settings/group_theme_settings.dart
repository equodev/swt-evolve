import 'package:flutter/material.dart';
import '../theme_extensions/group_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';

GroupThemeExtension getGroupLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getGroupTheme(
    isDark: false,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

GroupThemeExtension getGroupDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getGroupTheme(
    isDark: true,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

GroupThemeExtension _getGroupTheme({
  required bool isDark,
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  
  return GroupThemeExtension(
    backgroundColor: colorScheme.surface,
    borderColor: colorScheme.outline,
    foregroundColor: colorScheme.onSurface,
    textStyle: textTheme.bodyMedium ?? const TextStyle(),
    padding: const EdgeInsets.all(0.0),
    margin: const EdgeInsets.all(0.0),
    borderWidth: 3.0,
    borderRadius: 16.0,
  );
}

