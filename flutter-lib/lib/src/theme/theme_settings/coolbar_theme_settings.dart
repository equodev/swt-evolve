import 'package:flutter/material.dart';
import '../theme_extensions/coolbar_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';

CoolBarThemeExtension getCoolBarLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getCoolBarTheme(
    isDark: false,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

CoolBarThemeExtension getCoolBarDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getCoolBarTheme(
    isDark: true,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

CoolBarThemeExtension _getCoolBarTheme({
  required bool isDark,
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return CoolBarThemeExtension(
    backgroundColor: colorScheme.surface,
    borderColor: colorScheme.outline,
    borderWidth: 1.0,
    borderRadius: 0.0,
    padding: const EdgeInsets.all(2),
    animationDuration: const Duration(milliseconds: 150),
  );
}
