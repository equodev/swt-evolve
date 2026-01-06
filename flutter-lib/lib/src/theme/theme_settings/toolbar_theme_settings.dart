import 'package:flutter/material.dart';
import '../theme_extensions/toolbar_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';

ToolBarThemeExtension getToolBarLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getToolBarTheme(
    isDark: false,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

ToolBarThemeExtension getToolBarDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getToolBarTheme(
    isDark: true,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

ToolBarThemeExtension _getToolBarTheme({
  required bool isDark,
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return ToolBarThemeExtension(
    backgroundColor: Colors.transparent,
    borderColor: colorScheme.outline,
    borderWidth: 0.5,
    shadowColor: Colors.black,
    shadowOpacity: 0.2,
    shadowBlurRadius: 4.0,
    shadowOffset: const Offset(2, 2),
    itemPadding: const EdgeInsets.symmetric(horizontal: 2.0),
  );
}

